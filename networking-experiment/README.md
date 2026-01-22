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

### isp-failover

Simulates a multi-homed network with ISP failover using network namespaces, NAT, and routing. Demonstrates how a network with multiple ISP connections can switch between them transparently.

**Setup:**
```bash
# Terminal 1: Start the infrastructure
just isp-failover
# This sets up all namespaces, routing, NAT, and starts a web server
# Leave this running - it will display instructions
```

**Usage:**
```bash
# Terminal 2: Test connectivity (default: ISP-A)
curl http://10.0.4.1:8080/
# Response shows which ISP IP made the request (10.0.4.2 for ISP-A)

# Switch to ISP-B
just isp-b

# Test again - should now show ISP-B's IP (10.0.4.3)
curl http://10.0.4.1:8080/

# Switch back to ISP-A
just isp-a

# Test again - should show ISP-A's IP (10.0.4.2)
curl http://10.0.4.1:8080/
```

**Testing:**
```bash
# Run automated tests
just test-isp-failover
```

**Architecture:**
```
Host (10.0.3.2)
  |
  | LAN: 10.0.3.0/24
  |
Router (10.0.3.1, 10.0.1.2, 10.0.2.2)
  |
  +--- ISP-A (10.0.1.1) --- NAT ---> Internet side (10.0.4.2)
  |                                        |
  |                                    [Bridge]
  |                                        |
  +--- ISP-B (10.0.2.1) --- NAT ---> Internet side (10.0.4.3)
                                             |
                                        Internet (10.0.4.1)
```

**Key networking concepts demonstrated:**
- **Network namespaces**: Separate isolated network stacks for internet, ISPs, and router
- **NAT/MASQUERADE**: ISPs translate internal IPs (10.0.3.2) to their public IPs (10.0.4.2/10.0.4.3)
- **Bridge**: Connects both ISP uplinks to the same internet namespace, solving ARP conflicts
- **IP forwarding**: Router and ISPs forward packets between networks
- **Atomic route replacement**: Router switches ISPs by replacing its default route (no downtime)
- **Connection tracking**: NAT automatically handles bidirectional traffic translation

This simulates a realistic multi-homed network where you control only your router, not the ISPs or internet. The web server in the internet namespace sees requests from different ISP IPs depending on which ISP is active, demonstrating transparent failover.

## Requirements

- Python 3.13+
- Linux with TUN/TAP and namespace support
- uv (Python package manager)
- just (task runner)
