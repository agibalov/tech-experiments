#!/usr/bin/env bash
set -euo pipefail
set -x

ISP_A_NS="isp-a-ns"
VETH_ISP_A_SELF="veth-a-self"
VETH_ISP_A_ROUTER="veth-a-rtr"

ISP_B_NS="isp-b-ns"
VETH_ISP_B_SELF="veth-b-self"
VETH_ISP_B_ROUTER="veth-b-rtr"

ROUTER_NS="router-ns"
VETH_TESTER_ROUTER="veth-tst-rtr"
VETH_TESTER_HOST="veth-tst-hst"

cleanup() {
    echo "Cleaning up..."
    
    pkill -f 'TAG=isp-a' || true
    sudo ip netns delete $ISP_A_NS || true

    pkill -f 'TAG=isp-b' || true
    sudo ip netns delete $ISP_B_NS || true

    sudo ip netns delete $ROUTER_NS || true

    echo "Clean up completed!"
}

run() {
    trap cleanup EXIT

    UV=$(which uv)

    sudo ip link add $VETH_ISP_A_SELF type veth peer name $VETH_ISP_A_ROUTER
    sudo ip netns add $ISP_A_NS
    sudo ip link set $VETH_ISP_A_SELF netns $ISP_A_NS
    sudo ip netns exec $ISP_A_NS ip link set $VETH_ISP_A_SELF up
    sudo ip netns exec $ISP_A_NS ip addr add 10.1.0.1/16 dev $VETH_ISP_A_SELF
    sudo ip netns exec $ISP_A_NS ip route add 10.123.0.0/16 via 10.1.0.2 dev $VETH_ISP_A_SELF
    sudo ip netns exec $ISP_A_NS env TAG=isp-a MESSAGE="ISP A" $UV run python dummy_web_server.py &

    sudo ip link add $VETH_ISP_B_SELF type veth peer name $VETH_ISP_B_ROUTER
    sudo ip netns add $ISP_B_NS
    sudo ip link set $VETH_ISP_B_SELF netns $ISP_B_NS
    sudo ip netns exec $ISP_B_NS ip link set $VETH_ISP_B_SELF up
    sudo ip netns exec $ISP_B_NS ip addr add 10.1.0.1/16 dev $VETH_ISP_B_SELF
    sudo ip netns exec $ISP_B_NS ip route add 10.123.0.0/16 via 10.1.0.2 dev $VETH_ISP_B_SELF
    sudo ip netns exec $ISP_B_NS env TAG=isp-b MESSAGE="ISP B" $UV run python dummy_web_server.py &

    sudo ip link add $VETH_TESTER_ROUTER type veth peer name $VETH_TESTER_HOST
    sudo ip netns add $ROUTER_NS
    sudo ip netns exec $ROUTER_NS sysctl -w net.ipv4.ip_forward=1
    sudo ip link set $VETH_TESTER_ROUTER netns $ROUTER_NS
    sudo ip netns exec $ROUTER_NS ip link set $VETH_TESTER_ROUTER up
    sudo ip link set $VETH_ISP_A_ROUTER netns $ROUTER_NS
    sudo ip netns exec $ROUTER_NS ip link set $VETH_ISP_A_ROUTER up
    sudo ip link set $VETH_ISP_B_ROUTER netns $ROUTER_NS
    sudo ip netns exec $ROUTER_NS ip link set $VETH_ISP_B_ROUTER up
    sudo ip link set $VETH_TESTER_HOST up
    sudo ip netns exec $ROUTER_NS ip addr add 10.1.0.2/16 dev $VETH_ISP_A_ROUTER
    sudo ip netns exec $ROUTER_NS ip addr add 10.123.0.1/16 dev $VETH_TESTER_ROUTER

    sudo ip addr add 10.123.0.2/16 dev $VETH_TESTER_HOST
    sudo ip route add 10.1.0.0/16 via 10.123.0.1 dev $VETH_TESTER_HOST

    echo "$ISP_A_NS ***********************************"
    sudo ip netns exec $ISP_A_NS ip link show
    echo ""
    echo ip netns exec $ISP_A_NS ip route show
    echo ""

    echo "$ISP_B_NS ***********************************"
    sudo ip netns exec $ISP_B_NS ip link show
    echo ""
    echo ip netns exec $ISP_B_NS ip route show
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
    sudo ip netns exec $ROUTER_NS ip addr flush dev $VETH_ISP_B_ROUTER
    sudo ip netns exec $ROUTER_NS ip addr replace 10.1.0.2/16 dev $VETH_ISP_A_ROUTER
elif [[ "${1:-}" == "isp-b" ]]; then
    sudo ip netns exec $ROUTER_NS ip addr flush dev $VETH_ISP_A_ROUTER
    sudo ip netns exec $ROUTER_NS ip addr replace 10.1.0.2/16 dev $VETH_ISP_B_ROUTER
else
    echo "Usage: $0 run"
    exit 1
fi
