# networking-experiment

A collection of Linux networking experiments in Python.

## Experiments

All experiments require root privileges.

### tap-ping-server

A ping server running on a TAP (Layer 2) virtual interface. Demonstrates handling raw Ethernet frames, including ARP resolution and ICMP echo replies.

```bash
# Terminal 1: Start the server
just tap-ping-server

# Terminal 2: Ping any address in the 10.123.0.0/24 subnet
ping 10.123.0.2
# or use the shortcut:
just ping
```

The server responds to pings with a triangle-wave delay pattern (0, 100, 200, 300, 200, 100ms...) so you can see varying latency in the ping output.

### tap-monitor

A packet sniffer for TAP interfaces. Creates a TAP device and displays all incoming Ethernet frames, decoding ARP, ICMP, IGMP, mDNS, and SSDP packets.

```bash
# Terminal 1: Start the monitor
just tap-monitor

# Terminal 2: Generate traffic
ping 10.123.0.2
```

Watch the monitor show ARP requests, ICMP packets, and any multicast traffic.

### tun-ping-server

A ping server running on a TUN (Layer 3) virtual interface. Unlike TAP, TUN operates at the IP layer - no Ethernet frames or ARP handling needed.

```bash
# Terminal 1: Start the server
just tun-ping-server

# Terminal 2: Ping any address in the 10.123.0.0/24 subnet
ping 10.123.0.2
```

Same triangle-wave delay pattern as tap-ping-server, but simpler code since there's no Layer 2 to deal with.

### tun-monitor

A packet sniffer for TUN interfaces. Displays raw IP packets (no Ethernet headers), decoding ICMP, IGMP, mDNS, and SSDP.

```bash
# Terminal 1: Start the monitor
just tun-monitor

# Terminal 2: Generate traffic
ping 10.123.0.2
```

### pyroute2-hello-world

Demonstrates Linux network namespaces using the pyroute2 library. Creates two isolated namespaces connected by a virtual Ethernet (veth) pair, runs an HTTP server in one and a curl client in the other.

```bash
just pyroute2-hello-world
```

This is a self-contained demo - it creates the namespaces, configures networking (10.0.0.1 and 10.0.0.2), starts a server, makes a request, prints the response, and cleans up automatically.

## Requirements

- Python 3.13+
- Linux with TUN/TAP and namespace support
- uv (Python package manager)
- just (task runner)
