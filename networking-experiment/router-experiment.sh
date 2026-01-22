#!/usr/bin/env bash
set -euo pipefail
set -x

INTERNET_NS="internet-ns"
VETH_INTERNET_A="veth-inet-a"
VETH_INTERNET_B="veth-inet-b"

ISP_A_NS="isp-a-ns"
VETH_ISP_A_INTERNET="veth-a-inet"
VETH_ISP_A_ROUTER="veth-a-rtr"
VETH_ISP_A_ISP="veth-a-isp"

ISP_B_NS="isp-b-ns"
VETH_ISP_B_INTERNET="veth-b-inet"
VETH_ISP_B_ROUTER="veth-b-rtr"
VETH_ISP_B_ISP="veth-b-isp"

ROUTER_NS="router-ns"
VETH_TESTER_ROUTER="veth-tst-rtr"
VETH_TESTER_HOST="veth-tst-hst"

cleanup() {
    echo "Cleaning up..."

    pkill -f 'TAG=internet' || true
    sudo ip netns delete $INTERNET_NS || true
    sudo ip netns delete $ISP_A_NS || true
    sudo ip netns delete $ISP_B_NS || true
    sudo ip netns delete $ROUTER_NS || true

    echo "Clean up completed!"
}

run() {
    trap cleanup EXIT
    UV=$(which uv)

    # Create internet namespace with web server
    sudo ip netns add $INTERNET_NS
    sudo ip link add $VETH_INTERNET_A type veth peer name $VETH_ISP_A_INTERNET
    sudo ip link add $VETH_INTERNET_B type veth peer name $VETH_ISP_B_INTERNET
    sudo ip link set $VETH_INTERNET_A netns $INTERNET_NS
    sudo ip link set $VETH_INTERNET_B netns $INTERNET_NS
    sudo ip netns exec $INTERNET_NS ip link set lo up
    sudo ip netns exec $INTERNET_NS ip link add br0 type bridge
    sudo ip netns exec $INTERNET_NS ip link set $VETH_INTERNET_A master br0
    sudo ip netns exec $INTERNET_NS ip link set $VETH_INTERNET_B master br0
    sudo ip netns exec $INTERNET_NS ip link set $VETH_INTERNET_A up
    sudo ip netns exec $INTERNET_NS ip link set $VETH_INTERNET_B up
    sudo ip netns exec $INTERNET_NS ip link set br0 up
    sudo ip netns exec $INTERNET_NS ip addr add 10.0.4.1/24 dev br0
    sudo ip netns exec $INTERNET_NS env TAG=internet $UV run python dummy_web_server.py &

    # Create ISP-A namespace with forwarding
    sudo ip netns add $ISP_A_NS
    sudo ip link add $VETH_ISP_A_ROUTER type veth peer name $VETH_ISP_A_ISP
    sudo ip link set $VETH_ISP_A_INTERNET netns $ISP_A_NS
    sudo ip link set $VETH_ISP_A_ISP netns $ISP_A_NS
    sudo ip netns exec $ISP_A_NS ip link set lo up
    sudo ip netns exec $ISP_A_NS ip link set $VETH_ISP_A_INTERNET up
    sudo ip netns exec $ISP_A_NS ip link set $VETH_ISP_A_ISP up
    sudo ip netns exec $ISP_A_NS ip addr add 10.0.4.2/24 dev $VETH_ISP_A_INTERNET
    sudo ip netns exec $ISP_A_NS ip addr add 10.0.1.1/24 dev $VETH_ISP_A_ISP
    sudo ip netns exec $ISP_A_NS sysctl -w net.ipv4.ip_forward=1
    sudo ip netns exec $ISP_A_NS ip route add 10.0.3.0/24 via 10.0.1.2 dev $VETH_ISP_A_ISP
    sudo ip netns exec $ISP_A_NS ip route add default via 10.0.4.1 dev $VETH_ISP_A_INTERNET
    sudo ip netns exec $ISP_A_NS nft add table ip nat
    sudo ip netns exec $ISP_A_NS nft add chain ip nat postrouting { type nat hook postrouting priority 100 \; }
    sudo ip netns exec $ISP_A_NS nft add rule ip nat postrouting oifname \"$VETH_ISP_A_INTERNET\" masquerade

    # Create ISP-B namespace with forwarding
    sudo ip netns add $ISP_B_NS
    sudo ip link add $VETH_ISP_B_ROUTER type veth peer name $VETH_ISP_B_ISP
    sudo ip link set $VETH_ISP_B_INTERNET netns $ISP_B_NS
    sudo ip link set $VETH_ISP_B_ISP netns $ISP_B_NS
    sudo ip netns exec $ISP_B_NS ip link set lo up
    sudo ip netns exec $ISP_B_NS ip link set $VETH_ISP_B_INTERNET up
    sudo ip netns exec $ISP_B_NS ip link set $VETH_ISP_B_ISP up
    sudo ip netns exec $ISP_B_NS ip addr add 10.0.4.3/24 dev $VETH_ISP_B_INTERNET
    sudo ip netns exec $ISP_B_NS ip addr add 10.0.2.1/24 dev $VETH_ISP_B_ISP
    sudo ip netns exec $ISP_B_NS sysctl -w net.ipv4.ip_forward=1
    sudo ip netns exec $ISP_B_NS ip route add 10.0.3.0/24 via 10.0.2.2 dev $VETH_ISP_B_ISP
    sudo ip netns exec $ISP_B_NS ip route add default via 10.0.4.1 dev $VETH_ISP_B_INTERNET
    sudo ip netns exec $ISP_B_NS nft add table ip nat
    sudo ip netns exec $ISP_B_NS nft add chain ip nat postrouting { type nat hook postrouting priority 100 \; }
    sudo ip netns exec $ISP_B_NS nft add rule ip nat postrouting oifname \"$VETH_ISP_B_INTERNET\" masquerade

    # Create router namespace
    sudo ip netns add $ROUTER_NS
    sudo ip link add $VETH_TESTER_ROUTER type veth peer name $VETH_TESTER_HOST
    sudo ip link set $VETH_TESTER_ROUTER netns $ROUTER_NS
    sudo ip link set $VETH_ISP_A_ROUTER netns $ROUTER_NS
    sudo ip link set $VETH_ISP_B_ROUTER netns $ROUTER_NS
    sudo ip netns exec $ROUTER_NS ip link set lo up
    sudo ip netns exec $ROUTER_NS ip link set $VETH_TESTER_ROUTER up
    sudo ip netns exec $ROUTER_NS ip link set $VETH_ISP_A_ROUTER up
    sudo ip netns exec $ROUTER_NS ip link set $VETH_ISP_B_ROUTER up
    sudo ip netns exec $ROUTER_NS sysctl -w net.ipv4.ip_forward=1
    sudo ip netns exec $ROUTER_NS ip addr add 10.0.3.1/24 dev $VETH_TESTER_ROUTER
    sudo ip netns exec $ROUTER_NS ip addr add 10.0.1.2/24 dev $VETH_ISP_A_ROUTER
    sudo ip netns exec $ROUTER_NS ip addr add 10.0.2.2/24 dev $VETH_ISP_B_ROUTER
    sudo ip netns exec $ROUTER_NS ip route add default via 10.0.1.1 dev $VETH_ISP_A_ROUTER

    # Setup host
    sudo ip link set $VETH_TESTER_HOST up
    sudo ip addr add 10.0.3.2/24 dev $VETH_TESTER_HOST
    sudo ip route add 10.0.0.0/16 via 10.0.3.1 dev $VETH_TESTER_HOST

    echo "$INTERNET_NS ***********************************"
    sudo ip netns exec $INTERNET_NS ip link show
    echo ""
    sudo ip netns exec $INTERNET_NS ip route show
    echo ""

    echo "$ISP_A_NS ***********************************"
    sudo ip netns exec $ISP_A_NS ip link show
    echo ""
    sudo ip netns exec $ISP_A_NS ip route show
    echo ""

    echo "$ISP_B_NS ***********************************"
    sudo ip netns exec $ISP_B_NS ip link show
    echo ""
    sudo ip netns exec $ISP_B_NS ip route show
    echo ""

    echo "$ROUTER_NS ***********************************"
    sudo ip netns exec $ROUTER_NS ip link show
    echo ""
    sudo ip netns exec $ROUTER_NS ip route show
    echo ""

    echo "Host ***********************************"
    ip link show
    echo ""
    ip route show
    echo ""

    echo "Running. Press Ctrl+C to stop."
    wait
}

if [[ "${1:-}" == "run" ]]; then
    run
elif [[ "${1:-}" == "isp-a" ]]; then
    sudo ip netns exec $ROUTER_NS ip route replace default via 10.0.1.1 dev $VETH_ISP_A_ROUTER
elif [[ "${1:-}" == "isp-b" ]]; then
    sudo ip netns exec $ROUTER_NS ip route replace default via 10.0.2.1 dev $VETH_ISP_B_ROUTER
else
    echo "Usage: $0 run|isp-a|isp-b"
    exit 1
fi
