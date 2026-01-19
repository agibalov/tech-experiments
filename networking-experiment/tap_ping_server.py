import os
import fcntl
import struct
import subprocess
import signal
import sys
import time

TUNSETIFF = 0x400454ca
IFF_TAP = 0x0002
IFF_NO_PI = 0x1000
INTERFACE_IP = "10.123.0.1/24"
FAKE_MAC = b"\x02\x00\x00\x12\x34\x56"


def format_mac(mac_bytes):
    return ":".join(f"{b:02x}" for b in mac_bytes)


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


def make_arp_reply(frame):
    """Create ARP reply for an ARP request."""
    ethertype = struct.unpack("!H", frame[12:14])[0]
    if ethertype != 0x0806:
        return None

    arp = frame[14:]
    if len(arp) < 28:
        return None

    operation = struct.unpack("!H", arp[6:8])[0]
    if operation != 1:  # Not a request
        return None

    # Extract request details
    sender_mac = arp[8:14]
    sender_ip = arp[14:18]
    target_ip = arp[24:28]

    # Build ARP reply
    eth_header = sender_mac + FAKE_MAC + b"\x08\x06"
    arp_header = b"\x00\x01\x08\x00\x06\x04"
    arp_op = b"\x00\x02"
    arp_sender = FAKE_MAC + target_ip
    arp_target = sender_mac + sender_ip

    return eth_header + arp_header + arp_op + arp_sender + arp_target


def make_icmp_reply(frame):
    """Create ICMP echo-reply for an echo-request."""
    ethertype = struct.unpack("!H", frame[12:14])[0]
    if ethertype != 0x0800:
        return None

    ip_header = frame[14:]
    if len(ip_header) < 20:
        return None

    ihl = (ip_header[0] & 0x0F) * 4
    protocol = ip_header[9]

    if protocol != 1:  # Not ICMP
        return None

    icmp_data = frame[14 + ihl:]
    if len(icmp_data) < 8:
        return None

    icmp_type = icmp_data[0]
    if icmp_type != 8:  # Not echo-request
        return None

    # Build reply
    dst_mac = frame[0:6]
    src_mac = frame[6:12]
    eth_header = src_mac + dst_mac + b"\x08\x00"

    # IP: swap src/dst IPs
    new_ip = bytearray(ip_header[:ihl])
    new_ip[12:16], new_ip[16:20] = ip_header[16:20], ip_header[12:16]
    new_ip[10:12] = b"\x00\x00"
    ip_cksum = checksum(bytes(new_ip))
    new_ip[10:12] = struct.pack("!H", ip_cksum)

    # ICMP: change type to 0 (echo-reply), recalculate checksum
    new_icmp = bytearray(icmp_data)
    new_icmp[0] = 0
    new_icmp[2:4] = b"\x00\x00"
    icmp_cksum = checksum(bytes(new_icmp))
    new_icmp[2:4] = struct.pack("!H", icmp_cksum)

    return eth_header + bytes(new_ip) + bytes(new_icmp)


def get_icmp_seq(frame):
    """Extract ICMP sequence number from frame."""
    ihl = (frame[14] & 0x0F) * 4
    return struct.unpack("!H", frame[14 + ihl + 6 : 14 + ihl + 8])[0]


def setup_interface(ifname):
    subprocess.run(["ip", "addr", "add", INTERFACE_IP, "dev", ifname], check=True)
    subprocess.run(["ip", "link", "set", "dev", ifname, "up"], check=True)


def teardown_interface(ifname):
    subprocess.run(["ip", "tuntap", "del", "dev", ifname, "mode", "tap"], check=False)


def main():
    # Open TUN/TAP device
    fd = os.open("/dev/net/tun", os.O_RDWR)

    # Create TAP interface
    ifr = struct.pack("16sH", b"tap%d", IFF_TAP | IFF_NO_PI)
    result = fcntl.ioctl(fd, TUNSETIFF, ifr)

    ifname = result[:16].strip(b"\x00").decode("ascii")
    print(f"Created TAP device: {ifname}")

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
        frame = os.read(fd, 2048)
        if len(frame) < 14:
            continue

        # Handle ARP
        arp_reply = make_arp_reply(frame)
        if arp_reply:
            sender_ip = frame[14 + 14 : 14 + 18]
            target_ip = frame[14 + 24 : 14 + 28]
            print(f"ARP who-has {format_ip(sender_ip)} -> {format_ip(target_ip)}")
            os.write(fd, arp_reply)
            print(f"  -> sent ARP reply with MAC {format_mac(FAKE_MAC)}")
            continue

        # Handle ICMP
        icmp_reply = make_icmp_reply(frame)
        if icmp_reply:
            seq = get_icmp_seq(frame)
            # Triangle wave delay: 0,1,2,3,2,1,0,1,2,3...
            cycle_pos = seq % 6
            delay_ms = cycle_pos if cycle_pos <= 3 else 6 - cycle_pos
            delay_ms *= 100

            ihl = (frame[14] & 0x0F) * 4
            src_ip = frame[14 + 12 : 14 + 16]
            dst_ip = frame[14 + 16 : 14 + 20]
            print(f"ICMP echo-request {format_ip(src_ip)} -> {format_ip(dst_ip)} seq={seq}")

            time.sleep(delay_ms / 1000)
            os.write(fd, icmp_reply)
            print(f"  -> sent ICMP echo-reply (delay {delay_ms}ms)")


if __name__ == "__main__":
    main()
