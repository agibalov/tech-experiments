import os
import fcntl
import struct
import subprocess
import signal
import sys

TUNSETIFF = 0x400454ca
IFF_TAP = 0x0002
IFF_NO_PI = 0x1000
INTERFACE_IP = "10.123.0.1/24"


def format_mac(mac_bytes):
    return ":".join(f"{b:02x}" for b in mac_bytes)


def format_ip(ip_bytes):
    return ".".join(str(b) for b in ip_bytes)


def decode_arp(frame):
    """Decode ARP packet."""
    ethertype = struct.unpack("!H", frame[12:14])[0]
    if ethertype != 0x0806:
        return None

    arp = frame[14:]
    if len(arp) < 28:
        return None

    operation = struct.unpack("!H", arp[6:8])[0]
    sender_ip = arp[14:18]
    target_ip = arp[24:28]

    op_name = {1: "who-has", 2: "is-at"}.get(operation, f"op={operation}")
    return f"ARP {op_name} {format_ip(sender_ip)} -> {format_ip(target_ip)}"


def decode_icmp(frame):
    """Decode IPv4 ICMP packet."""
    ethertype = struct.unpack("!H", frame[12:14])[0]
    if ethertype != 0x0800:
        return None

    ip_header = frame[14:]
    if len(ip_header) < 20:
        return None

    version_ihl = ip_header[0]
    ihl = (version_ihl & 0x0F) * 4
    protocol = ip_header[9]

    if protocol != 1:
        return None

    src_ip = ip_header[12:16]
    dst_ip = ip_header[16:20]

    icmp_header = frame[14 + ihl:]
    if len(icmp_header) < 8:
        return None

    icmp_type = icmp_header[0]
    seq = struct.unpack("!H", icmp_header[6:8])[0]

    type_names = {0: "echo-reply", 8: "echo-request"}
    type_name = type_names.get(icmp_type, f"type={icmp_type}")

    return f"ICMP {type_name} {format_ip(src_ip)} -> {format_ip(dst_ip)} seq={seq}"


def decode_igmp(frame):
    """Decode IGMP packet."""
    ethertype = struct.unpack("!H", frame[12:14])[0]
    if ethertype != 0x0800:
        return None

    ip_header = frame[14:]
    if len(ip_header) < 20:
        return None

    protocol = ip_header[9]
    if protocol != 2:  # IGMP
        return None

    ihl = (ip_header[0] & 0x0F) * 4
    igmp = frame[14 + ihl:]
    if len(igmp) < 8:
        return None

    igmp_type = igmp[0]
    group_ip = igmp[4:8]

    type_names = {0x11: "query", 0x16: "v2-report", 0x17: "leave", 0x22: "v3-report"}
    type_name = type_names.get(igmp_type, f"type=0x{igmp_type:02x}")

    return f"IGMP {type_name} group={format_ip(group_ip)}"


def decode_udp_service(frame):
    """Decode mDNS and SSDP (UDP-based services)."""
    ethertype = struct.unpack("!H", frame[12:14])[0]
    if ethertype != 0x0800:
        return None

    ip_header = frame[14:]
    if len(ip_header) < 20:
        return None

    protocol = ip_header[9]
    if protocol != 17:  # UDP
        return None

    ihl = (ip_header[0] & 0x0F) * 4
    src_ip = ip_header[12:16]
    udp = frame[14 + ihl:]
    if len(udp) < 8:
        return None

    dst_port = struct.unpack("!H", udp[2:4])[0]

    if dst_port == 5353:  # mDNS
        # Try to extract query name from DNS payload
        dns = udp[8:]
        if len(dns) > 12:
            # Skip header (12 bytes), try to read first query name
            name_parts = []
            pos = 12
            while pos < len(dns) and dns[pos] != 0:
                length = dns[pos]
                if pos + 1 + length > len(dns):
                    break
                name_parts.append(dns[pos + 1 : pos + 1 + length].decode("ascii", errors="replace"))
                pos += 1 + length
            if name_parts:
                return f"mDNS {format_ip(src_ip)} query {'.'.join(name_parts)}"
        return f"mDNS {format_ip(src_ip)}"

    if dst_port == 1900:  # SSDP
        payload = udp[8:]
        # Extract first line of SSDP message
        try:
            first_line = payload.split(b"\r\n")[0].decode("ascii", errors="replace")
            return f"SSDP {format_ip(src_ip)} {first_line[:50]}"
        except:
            return f"SSDP {format_ip(src_ip)}"

    return None


def decode_frame(frame):
    """Try to decode frame, return description string."""
    # Try specific decoders in order
    for decoder in [decode_arp, decode_icmp, decode_igmp, decode_udp_service]:
        result = decoder(frame)
        if result:
            return result

    # Fallback: show ethertype and MACs
    dst_mac = frame[0:6]
    src_mac = frame[6:12]
    ethertype = struct.unpack("!H", frame[12:14])[0]

    if dst_mac[0] == 0x33 and dst_mac[1] == 0x33:
        return f"IPv6-multicast src={format_mac(src_mac)} ethertype=0x{ethertype:04x}"

    return f"src={format_mac(src_mac)} -> dst={format_mac(dst_mac)} ethertype=0x{ethertype:04x}"


def setup_interface(ifname):
    subprocess.run(["ip", "addr", "add", INTERFACE_IP, "dev", ifname], check=True)
    subprocess.run(["ip", "link", "set", "dev", ifname, "up"], check=True)


def teardown_interface(ifname):
    subprocess.run(["ip", "tuntap", "del", "dev", ifname, "mode", "tap"], check=False)


def main():
    # Open TUN/TAP device
    fd = os.open("/dev/net/tun", os.O_RDWR)

    # Create TAP interface - use "tap%d" to let kernel assign name
    ifr = struct.pack("16sH", b"tap%d", IFF_TAP | IFF_NO_PI)
    result = fcntl.ioctl(fd, TUNSETIFF, ifr)

    # Extract assigned interface name from result
    ifname = result[:16].strip(b"\x00").decode("ascii")
    print(f"Created TAP device: {ifname}")

    # Configure interface
    setup_interface(ifname)
    print(f"Interface configured: {INTERFACE_IP}")

    # Setup cleanup on exit
    def cleanup(signum, frame):
        print("\nCleaning up...")
        os.close(fd)
        teardown_interface(ifname)
        sys.exit(0)

    signal.signal(signal.SIGINT, cleanup)
    signal.signal(signal.SIGTERM, cleanup)

    print("Listening for frames... (Ctrl+C to stop)")

    # Read frames
    while True:
        frame = os.read(fd, 2048)
        if len(frame) < 14:
            continue

        print(decode_frame(frame))


if __name__ == "__main__":
    main()
