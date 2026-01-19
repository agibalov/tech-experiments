import os
import fcntl
import struct
import subprocess
import signal
import sys
import time

TUNSETIFF = 0x400454ca
IFF_TUN = 0x0001
IFF_NO_PI = 0x1000
INTERFACE_IP = "10.123.0.1/24"


def format_ip(ip_bytes):
    return ".".join(str(b) for b in ip_bytes)


def checksum(data):
    """Calculate ones' complement checksum for ICMP/IP."""
    if len(data) % 2:
        data += b"\x00"
    s = sum(struct.unpack("!%dH" % (len(data) // 2), data))
    while s >> 16:
        s = (s & 0xFFFF) + (s >> 16)
    return ~s & 0xFFFF


def make_icmp_reply(packet):
    """Create ICMP echo-reply for an echo-request."""
    if len(packet) < 20:
        return None

    version_ihl = packet[0]
    if (version_ihl >> 4) != 4:  # Not IPv4
        return None

    ihl = (version_ihl & 0x0F) * 4
    protocol = packet[9]

    if protocol != 1:  # Not ICMP
        return None

    icmp_data = packet[ihl:]
    if len(icmp_data) < 8:
        return None

    icmp_type = icmp_data[0]
    if icmp_type != 8:  # Not echo-request
        return None

    # Build reply IP header: swap src/dst IPs
    new_ip = bytearray(packet[:ihl])
    new_ip[12:16], new_ip[16:20] = packet[16:20], packet[12:16]
    new_ip[10:12] = b"\x00\x00"
    ip_cksum = checksum(bytes(new_ip))
    new_ip[10:12] = struct.pack("!H", ip_cksum)

    # ICMP: change type to 0 (echo-reply), recalculate checksum
    new_icmp = bytearray(icmp_data)
    new_icmp[0] = 0
    new_icmp[2:4] = b"\x00\x00"
    icmp_cksum = checksum(bytes(new_icmp))
    new_icmp[2:4] = struct.pack("!H", icmp_cksum)

    return bytes(new_ip) + bytes(new_icmp)


def get_icmp_seq(packet):
    """Extract ICMP sequence number from packet."""
    ihl = (packet[0] & 0x0F) * 4
    return struct.unpack("!H", packet[ihl + 6 : ihl + 8])[0]


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

    print("Ping server running... (Ctrl+C to stop)")

    while True:
        packet = os.read(fd, 2048)
        if len(packet) < 20:
            continue

        # Handle ICMP
        icmp_reply = make_icmp_reply(packet)
        if icmp_reply:
            seq = get_icmp_seq(packet)
            # Triangle wave delay: 0,1,2,3,2,1,0,1,2,3...
            cycle_pos = seq % 6
            delay_ms = cycle_pos if cycle_pos <= 3 else 6 - cycle_pos
            delay_ms *= 100

            ihl = (packet[0] & 0x0F) * 4
            src_ip = packet[12:16]
            dst_ip = packet[16:20]
            print(f"ICMP echo-request {format_ip(src_ip)} -> {format_ip(dst_ip)} seq={seq}")

            time.sleep(delay_ms / 1000)
            os.write(fd, icmp_reply)
            print(f"  -> sent ICMP echo-reply (delay {delay_ms}ms)")


if __name__ == "__main__":
    main()
