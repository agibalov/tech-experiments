from pyroute2 import IPRoute, netns, NSPopen, NetNS
from dataclasses import dataclass
import subprocess
import time
import atexit
import sys


@dataclass
class IspSpec:
    """Specification for an ISP namespace.

    Each ISP has two veth pairs:
    1. Internet connection: veth_internet_br (in internet-ns bridge) <-> veth_isp_internet (in ISP-ns)
    2. Router connection: veth_router (in router-ns) <-> veth_isp_router (in ISP-ns)
    """
    name: str  # ISP name (e.g., "isp-a")
    ns_name: str  # Namespace name (e.g., "isp-a-ns")
    veth_internet_br: str  # Interface in internet namespace bridge
    veth_isp_internet: str  # ISP's internet-facing interface (in ISP namespace)
    veth_router: str  # Interface in router namespace
    veth_isp_router: str  # ISP's router-facing interface (in ISP namespace)
    internet_ip: str  # IP on internet side (veth_isp_internet)
    router_ip: str  # IP on router side (veth_isp_router)
    router_gateway_ip: str  # Router's IP on this ISP's network


# Namespace names
INTERNET_NS = "internet-ns"
ROUTER_NS = "router-ns"

# Interface names
VETH_TESTER_ROUTER = "veth-tst-rtr"
VETH_TESTER_HOST = "veth-tst-hst"
BRIDGE_NAME = "br0"

# IP addresses
INTERNET_IP = "10.0.4.1"
ROUTER_LAN_IP = "10.0.3.1"
HOST_IP = "10.0.3.2"

PREFIX_LEN = 24
HTTP_PORT = 8080

# ISP configurations
ISP_A = IspSpec(
    name="isp-a",
    ns_name="isp-a-ns",
    veth_internet_br="veth-inet-a",
    veth_isp_internet="veth-a-inet",
    veth_router="veth-a-rtr",
    veth_isp_router="veth-a-isp",
    internet_ip="10.0.4.2",
    router_ip="10.0.1.1",
    router_gateway_ip="10.0.1.2"
)

ISP_B = IspSpec(
    name="isp-b",
    ns_name="isp-b-ns",
    veth_internet_br="veth-inet-b",
    veth_isp_internet="veth-b-inet",
    veth_router="veth-b-rtr",
    veth_isp_router="veth-b-isp",
    internet_ip="10.0.4.3",
    router_ip="10.0.2.1",
    router_gateway_ip="10.0.2.2"
)

ISPS = [ISP_A, ISP_B]

server_proc = None


def cleanup():
    """Remove namespaces and kill server process."""
    global server_proc
    if server_proc:
        try:
            server_proc.terminate()
            server_proc.release()
        except:
            pass

    all_namespaces = [INTERNET_NS, ROUTER_NS] + [isp.ns_name for isp in ISPS]
    for ns in all_namespaces:
        try:
            netns.remove(ns)
            print(f"Removed namespace: {ns}")
        except FileNotFoundError:
            pass
        except Exception as e:
            print(f"Warning: Failed to remove {ns}: {e}")


def run_server():
    """Start HTTP server in internet namespace."""
    print(f"Starting HTTP server in {INTERNET_NS} on {INTERNET_IP}:{HTTP_PORT}...")
    server_code = f'''
from http.server import HTTPServer, BaseHTTPRequestHandler

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/":
            source_ip = self.client_address[0]
            response = f"Request from: {{source_ip}}\\n"
            print(f"REQUEST from {{source_ip}}")
            self.send_response(200)
            self.send_header("Content-Type", "text/plain")
            self.end_headers()
            self.wfile.write(response.encode())
        else:
            self.send_response(404)
            self.end_headers()
    def log_message(self, format, *args):
        pass

HTTPServer(("{INTERNET_IP}", {HTTP_PORT}), Handler).serve_forever()
'''
    proc = NSPopen(
        INTERNET_NS,
        ["python3", "-c", server_code],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    return proc


def create_namespaces():
    """Create all network namespaces."""
    print("Creating namespaces...")
    all_namespaces = [INTERNET_NS, ROUTER_NS] + [isp.ns_name for isp in ISPS]
    for ns in all_namespaces:
        netns.create(ns)
        print(f"  Created: {ns}")


def setup_internet_ns():
    """Setup internet namespace with bridge connecting to all ISPs."""
    print("Setting up internet namespace...")

    # Create veth pairs - one end goes to internet-ns bridge, other to ISP-ns
    with IPRoute() as ipr:
        # Create veth pairs: bridge side <-> ISP internet side
        for isp in ISPS:
            ipr.link("add", ifname=isp.veth_internet_br, kind="veth", peer=isp.veth_isp_internet)

        # Move bridge sides to internet namespace
        for isp in ISPS:
            inet_idx = ipr.link_lookup(ifname=isp.veth_internet_br)[0]
            ipr.link("set", index=inet_idx, net_ns_fd=INTERNET_NS)

    # Configure internet namespace
    with IPRoute(netns=INTERNET_NS) as ipr:
        # Bring up loopback
        lo_idx = ipr.link_lookup(ifname="lo")[0]
        ipr.link("set", index=lo_idx, state="up")

        # Create bridge
        ipr.link("add", ifname=BRIDGE_NAME, kind="bridge")
        br_idx = ipr.link_lookup(ifname=BRIDGE_NAME)[0]

        # Add all ISP veth interfaces to bridge and bring them up
        for isp in ISPS:
            inet_idx = ipr.link_lookup(ifname=isp.veth_internet_br)[0]
            ipr.link("set", index=inet_idx, master=br_idx)
            ipr.link("set", index=inet_idx, state="up")

        # Bring up bridge
        ipr.link("set", index=br_idx, state="up")

        # Add IP to bridge
        ipr.addr("add", index=br_idx, address=INTERNET_IP, prefixlen=PREFIX_LEN)


def setup_isp_ns(isp: IspSpec):
    """Setup ISP namespace with forwarding and NAT."""
    print(f"Setting up {isp.ns_name}...")

    # Create veth pair for router connection
    with IPRoute() as ipr:
        ipr.link("add", ifname=isp.veth_router, kind="veth", peer=isp.veth_isp_router)

        # Move interfaces to ISP namespace
        isp_internet_idx = ipr.link_lookup(ifname=isp.veth_isp_internet)[0]
        ipr.link("set", index=isp_internet_idx, net_ns_fd=isp.ns_name)
        isp_router_idx = ipr.link_lookup(ifname=isp.veth_isp_router)[0]
        ipr.link("set", index=isp_router_idx, net_ns_fd=isp.ns_name)

    # Configure ISP namespace
    with IPRoute(netns=isp.ns_name) as ipr:
        # Bring up loopback
        lo_idx = ipr.link_lookup(ifname="lo")[0]
        ipr.link("set", index=lo_idx, state="up")

        # Configure interfaces
        internet_idx = ipr.link_lookup(ifname=isp.veth_isp_internet)[0]
        router_idx = ipr.link_lookup(ifname=isp.veth_isp_router)[0]

        ipr.link("set", index=internet_idx, state="up")
        ipr.link("set", index=router_idx, state="up")

        # Add IP addresses
        ipr.addr("add", index=internet_idx, address=isp.internet_ip, prefixlen=PREFIX_LEN)
        ipr.addr("add", index=router_idx, address=isp.router_ip, prefixlen=PREFIX_LEN)

        # Add routes
        ipr.route("add", dst="10.0.3.0/24", gateway=isp.router_gateway_ip)
        ipr.route("add", dst="default", gateway=INTERNET_IP)

    # Enable IP forwarding
    with NetNS(isp.ns_name):
        with open('/proc/sys/net/ipv4/ip_forward', 'w') as f:
            f.write('1\n')

    # Setup NAT with nftables
    nft_commands = [
        ["nft", "add", "table", "ip", "nat"],
        ["nft", "add", "chain", "ip", "nat", "postrouting", "{", "type", "nat", "hook", "postrouting", "priority", "100", ";", "}"],
        ["nft", "add", "rule", "ip", "nat", "postrouting", "oifname", isp.veth_isp_internet, "masquerade"],
    ]
    for cmd in nft_commands:
        NSPopen(isp.ns_name, cmd, stdout=subprocess.PIPE).wait()


def setup_router_ns(isps: list[IspSpec]):
    """Setup router namespace with forwarding, connecting to multiple ISPs."""
    print("Setting up router namespace...")

    # Create veth pair for host connection
    with IPRoute() as ipr:
        ipr.link("add", ifname=VETH_TESTER_ROUTER, kind="veth", peer=VETH_TESTER_HOST)

        # Move router interface to router namespace
        router_idx = ipr.link_lookup(ifname=VETH_TESTER_ROUTER)[0]
        ipr.link("set", index=router_idx, net_ns_fd=ROUTER_NS)

        # Move all ISP router interfaces to router namespace
        for isp in isps:
            isp_router_idx = ipr.link_lookup(ifname=isp.veth_router)[0]
            ipr.link("set", index=isp_router_idx, net_ns_fd=ROUTER_NS)

    # Configure router namespace
    with IPRoute(netns=ROUTER_NS) as ipr:
        # Bring up loopback
        lo_idx = ipr.link_lookup(ifname="lo")[0]
        ipr.link("set", index=lo_idx, state="up")

        # Bring up and configure LAN interface
        tester_idx = ipr.link_lookup(ifname=VETH_TESTER_ROUTER)[0]
        ipr.link("set", index=tester_idx, state="up")
        ipr.addr("add", index=tester_idx, address=ROUTER_LAN_IP, prefixlen=PREFIX_LEN)

        # Bring up and configure all ISP interfaces
        for isp in isps:
            isp_idx = ipr.link_lookup(ifname=isp.veth_router)[0]
            ipr.link("set", index=isp_idx, state="up")
            ipr.addr("add", index=isp_idx, address=isp.router_gateway_ip, prefixlen=PREFIX_LEN)

        # Add default route via first ISP
        ipr.route("add", dst="default", gateway=isps[0].router_ip)

    # Enable IP forwarding
    with NetNS(ROUTER_NS):
        with open('/proc/sys/net/ipv4/ip_forward', 'w') as f:
            f.write('1\n')


def setup_host():
    """Setup host interface in root namespace."""
    print("Setting up host interface...")

    with IPRoute() as ipr:
        # Bring up host interface
        host_idx = ipr.link_lookup(ifname=VETH_TESTER_HOST)[0]
        ipr.link("set", index=host_idx, state="up")

        # Add IP address
        try:
            ipr.addr("add", index=host_idx, address=HOST_IP, prefixlen=PREFIX_LEN)
        except Exception as e:
            print(f"Warning: Could not add address {HOST_IP}: {e}")

        # Add route to 10.0.0.0/16
        try:
            ipr.route("add", dst="10.0.0.0/16", gateway=ROUTER_LAN_IP)
        except Exception as e:
            print(f"Warning: Could not add route: {e}")


def switch_to_isp(isp_name):
    """Switch default route to specified ISP."""
    # Find the ISP by name
    isp = next((i for i in ISPS if i.name == isp_name), None)
    if not isp:
        print(f"Unknown ISP: {isp_name}")
        return False

    print(f"\nSwitching to {isp_name.upper()}...")
    with IPRoute(netns=ROUTER_NS) as ipr:
        # Replace default route atomically
        ipr.route("replace", dst="default", gateway=isp.router_ip)

    return True


def setup():
    """Setup ISP failover environment."""
    global server_proc

    print("Setting up ISP failover environment...")
    print("=" * 60)

    create_namespaces()
    setup_internet_ns()
    for isp in ISPS:
        setup_isp_ns(isp)
    setup_router_ns(ISPS)
    setup_host()

    # Start web server
    server_proc = run_server()
    time.sleep(1)  # Let server start

    print("\n" + "=" * 60)
    print("Setup complete!")
    print("=" * 60)
    print(f"\nYou can now:")
    print(f"  - Test connectivity: curl http://{INTERNET_IP}:{HTTP_PORT}/")
    print(f"  - Switch ISPs: python isp_failover.py isp-a")
    print(f"                 python isp_failover.py isp-b")
    print("\nPress Ctrl+C to exit and cleanup.")

    # Keep running
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        pass


def switch_isp_command(isp_name):
    """Switch ISP without running full demo (requires namespaces to exist)."""
    try:
        if switch_to_isp(isp_name):
            print(f"Successfully switched to {isp_name.upper()}")
            return 0
        else:
            return 1
    except Exception as e:
        print(f"Error switching to {isp_name}: {e}")
        return 1


def main():
    """Main entry point."""
    # Parse command line arguments
    if len(sys.argv) > 1:
        command = sys.argv[1]

        # Check if it's a valid ISP name
        if command in [isp.name for isp in ISPS]:
            sys.exit(switch_isp_command(command))
        else:
            isp_names = "|".join(isp.name for isp in ISPS)
            print(f"Usage: {sys.argv[0]} [{isp_names}]")
            for isp in ISPS:
                print(f"  {isp.name}  - Switch to {isp.name.upper()} (requires setup to be running)")
            sys.exit(1)

    # Setup the environment
    atexit.register(cleanup)
    cleanup()  # Clean any leftover state

    try:
        setup()
    except KeyboardInterrupt:
        print("\n\nInterrupted by user")
    except Exception as e:
        print(f"\nError: {e}")
        import traceback
        traceback.print_exc()
    finally:
        print("\nCleaning up...")
        cleanup()


if __name__ == "__main__":
    main()
