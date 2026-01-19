#!/usr/bin/env python3
"""Pyroute2 hello world: network namespace demo with HTTP server."""

from pyroute2 import IPRoute, netns, NSPopen
import subprocess
import time
import atexit

# Constants
SERVER_NS = "pyroute2_server_ns"
CLIENT_NS = "pyroute2_client_ns"
VETH_SERVER = "veth_srv"
VETH_CLIENT = "veth_cli"
SERVER_IP = "10.0.0.1"
CLIENT_IP = "10.0.0.2"
PREFIX_LEN = 24
HTTP_PORT = 8080


def cleanup():
    """Remove namespaces, ignoring errors if they don't exist."""
    for ns in [SERVER_NS, CLIENT_NS]:
        try:
            netns.remove(ns)
            print(f"Removed namespace: {ns}")
        except FileNotFoundError:
            pass
        except Exception as e:
            print(f"Warning: Failed to remove {ns}: {e}")


def run_server():
    """Start HTTP server in server namespace, return process."""
    print(f"Starting HTTP server in {SERVER_NS} on {SERVER_IP}:{HTTP_PORT}...")
    server_code = f'''
import json
from http.server import HTTPServer, BaseHTTPRequestHandler

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps({{"message": "hello world"}}).encode())
    def log_message(self, format, *args):
        pass  # Suppress logging

HTTPServer(("{SERVER_IP}", {HTTP_PORT}), Handler).serve_forever()
'''
    proc = NSPopen(
        SERVER_NS,
        ["python3", "-c", server_code],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    return proc


def run_curl():
    """Run curl from client namespace, return response."""
    print(f"Running curl from {CLIENT_NS} to http://{SERVER_IP}:{HTTP_PORT}...")
    proc = NSPopen(
        CLIENT_NS,
        ["curl", "-s", "--max-time", "5", f"http://{SERVER_IP}:{HTTP_PORT}"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    stdout, stderr = proc.communicate()
    proc.wait()
    proc.release()
    return stdout.decode() if stdout else None


def main():
    """Main entry point."""
    atexit.register(cleanup)
    cleanup()  # Clean any leftover state

    try:
        # Create namespaces
        print("Creating namespaces...")
        netns.create(SERVER_NS)
        netns.create(CLIENT_NS)

        # Create veth pair and move to namespaces
        print("Setting up veth pair...")
        with IPRoute() as ipr:
            ipr.link("add", ifname=VETH_SERVER, kind="veth", peer=VETH_CLIENT)
            srv_idx = ipr.link_lookup(ifname=VETH_SERVER)[0]
            ipr.link("set", index=srv_idx, net_ns_fd=SERVER_NS)
            cli_idx = ipr.link_lookup(ifname=VETH_CLIENT)[0]
            ipr.link("set", index=cli_idx, net_ns_fd=CLIENT_NS)

        # Configure interfaces
        print("Configuring interfaces...")
        with IPRoute(netns=SERVER_NS) as ipr:
            idx = ipr.link_lookup(ifname=VETH_SERVER)[0]
            ipr.addr("add", index=idx, address=SERVER_IP, prefixlen=PREFIX_LEN)
            ipr.link("set", index=idx, state="up")
            lo_idx = ipr.link_lookup(ifname="lo")[0]
            ipr.link("set", index=lo_idx, state="up")

        with IPRoute(netns=CLIENT_NS) as ipr:
            idx = ipr.link_lookup(ifname=VETH_CLIENT)[0]
            ipr.addr("add", index=idx, address=CLIENT_IP, prefixlen=PREFIX_LEN)
            ipr.link("set", index=idx, state="up")
            lo_idx = ipr.link_lookup(ifname="lo")[0]
            ipr.link("set", index=lo_idx, state="up")

        # Run server and test with curl
        server_proc = run_server()
        time.sleep(1)  # Let server start

        response = run_curl()

        if response:
            print("=" * 50)
            print("SUCCESS: Received response from server!")
            print("=" * 50)
            print("Response (first 500 chars):")
            print(response[:500])
        else:
            print("FAILED: No response from server")

        server_proc.terminate()
        server_proc.release()

    finally:
        print("\nCleaning up...")
        cleanup()


if __name__ == "__main__":
    main()
