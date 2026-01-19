import os
import fcntl
import struct
import subprocess
import signal
import sys

TUNSETIFF = 0x400454ca
IFF_TUN = 0x0001
IFF_NO_PI = 0x1000
INTERFACE_IP = "10.123.0.1/24"


def format_ip(ip_bytes):
    return ".".join(str(b) for b in ip_bytes)


def decode_icmp(packet):
    """Decode ICMP packet."""
    if len(packet) < 20:
        return None

    version_ihl = packet[0]
    if (version_ihl >> 4) != 4:  # Not IPv4
        return None

    ihl = (version_ihl & 0x0F) * 4
    protocol = packet[9]

    if protocol != 1:  # Not ICMP
        return None

    src_ip = packet[12:16]
    dst_ip = packet[16:20]

    icmp_header = packet[ihl:]
    if len(icmp_header) < 8:
        return None

    icmp_type = icmp_header[0]
    seq = struct.unpack("!H", icmp_header[6:8])[0]

    type_names = {0: "echo-reply", 8: "echo-request"}
    type_name = type_names.get(icmp_type, f"type={icmp_type}")

    return f"ICMP {type_name} {format_ip(src_ip)} -> {format_ip(dst_ip)} seq={seq}"


def decode_igmp(packet):
    """Decode IGMP packet."""
    if len(packet) < 20:
        return None

    version_ihl = packet[0]
    if (version_ihl >> 4) != 4:
        return None

    protocol = packet[9]
    if protocol != 2:  # IGMP
        return None

    ihl = (version_ihl & 0x0F) * 4
    igmp = packet[ihl:]
    if len(igmp) < 8:
        return None

    igmp_type = igmp[0]
    group_ip = igmp[4:8]

    type_names = {0x11: "query", 0x16: "v2-report", 0x17: "leave", 0x22: "v3-report"}
    type_name = type_names.get(igmp_type, f"type=0x{igmp_type:02x}")

    return f"IGMP {type_name} group={format_ip(group_ip)}"


def decode_udp_service(packet):
    """Decode mDNS and SSDP (UDP-based services)."""
    if len(packet) < 20:
        return None

    version_ihl = packet[0]
    if (version_ihl >> 4) != 4:
        return None

    protocol = packet[9]
    if protocol != 17:  # UDP
        return None

    ihl = (version_ihl & 0x0F) * 4
    src_ip = packet[12:16]
    udp = packet[ihl:]
    if len(udp) < 8:
        return None

    dst_port = struct.unpack("!H", udp[2:4])[0]

    if dst_port == 5353:  # mDNS
        dns = udp[8:]
        if len(dns) > 12:
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
        try:
            first_line = payload.split(b"\r\n")[0].decode("ascii", errors="replace")
            return f"SSDP {format_ip(src_ip)} {first_line[:50]}"
        except:
            return f"SSDP {format_ip(src_ip)}"

    return None


def decode_packet(packet):
    """Try to decode packet, return description string."""
    for decoder in [decode_icmp, decode_igmp, decode_udp_service]:
        result = decoder(packet)
        if result:
            return result

    # Fallback: show protocol and IPs
    if len(packet) >= 20:
        version_ihl = packet[0]
        if (version_ihl >> 4) == 4:
            protocol = packet[9]
            src_ip = packet[12:16]
            dst_ip = packet[16:20]
            return f"IPv4 proto={protocol} {format_ip(src_ip)} -> {format_ip(dst_ip)}"
        elif (version_ihl >> 4) == 6:
            return f"IPv6 packet ({len(packet)} bytes)"

    return f"Unknown packet ({len(packet)} bytes)"


def setup_interface(ifname):
    subprocess.run(["ip", "addr", "add", INTERFACE_IP, "dev", ifname], check=True)
    subprocess.run(["ip", "link", "set", "dev", ifname, "up"], check=True)


def teardown_interface(ifname):
    subprocess.run(["ip", "tuntap", "del", "dev", ifname, "mode", "tun"], check=False)


def main():
    # Open TUN/TAP device
    fd = os.open("/dev/net/tun", os.O_RDWR)

    # Create TUN interface
    ifr = struct.pack("16sH", b"tun%d", IFF_TUN | IFF_NO_PI)
    result = fcntl.ioctl(fd, TUNSETIFF, ifr)

    ifname = result[:16].strip(b"\x00").decode("ascii")
    print(f"Created TUN device: {ifname}")

    setup_interface(ifname)
    print(f"Interface configured: {INTERFACE_IP}")

    def cleanup(signum, frame):
        print("\nCleaning up...")
        os.close(fd)
        teardown_interface(ifname)
        sys.exit(0)

    signal.signal(signal.SIGINT, cleanup)
    signal.signal(signal.SIGTERM, cleanup)

    print("Listening for packets... (Ctrl+C to stop)")

    while True:
        packet = os.read(fd, 2048)
        if len(packet) < 1:
            continue

        print(decode_packet(packet))


if __name__ == "__main__":
    main()
