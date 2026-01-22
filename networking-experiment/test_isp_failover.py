#!/usr/bin/env python3
"""Tests for ISP failover demo."""

import subprocess
import time
import pytest
from isp_failover import (
    cleanup,
    create_namespaces,
    setup_internet_ns,
    setup_isp_ns,
    setup_router_ns,
    setup_host,
    run_server,
    switch_to_isp,
    ISP_A,
    ISP_B,
    ISPS,
    INTERNET_IP,
    HTTP_PORT,
)


def curl_internet(timeout=5):
    """Helper function to make a curl request to the internet server.

    Returns:
        tuple: (returncode, stdout, stderr)
    """
    result = subprocess.run(
        ["curl", "-s", f"http://{INTERNET_IP}:{HTTP_PORT}/"],
        capture_output=True,
        text=True,
        timeout=timeout
    )
    return result.returncode, result.stdout, result.stderr


@pytest.fixture(scope="module")
def isp_failover_setup():
    """Setup ISP failover environment for testing."""
    print("\nSetting up ISP failover test environment...")

    # Clean any leftover state
    cleanup()

    # Setup all namespaces and networking
    create_namespaces()
    setup_internet_ns()
    for isp in ISPS:
        setup_isp_ns(isp)
    setup_router_ns(ISPS)
    setup_host()

    # Start web server
    server_proc = run_server()
    time.sleep(1)  # Let server start

    yield server_proc

    # Cleanup after tests
    print("\nCleaning up test environment...")
    try:
        server_proc.terminate()
        server_proc.release()
    except:
        pass
    cleanup()


def test_connectivity_with_first_isp(isp_failover_setup):
    """Test that connectivity works with ISP-A (default)."""
    returncode, stdout, stderr = curl_internet()

    assert returncode == 0, f"curl failed: {stderr}"
    assert "Request from:" in stdout, f"Unexpected response: {stdout}"

    # Should see traffic from ISP-A's internet IP
    assert ISP_A.internet_ip in stdout, \
        f"Expected traffic from {ISP_A.internet_ip}, got: {stdout}"


def test_switch_to_each_isp(isp_failover_setup):
    """Test switching between all ISPs."""
    for isp in ISPS:
        print(f"\nTesting switch to {isp.name}...")

        # Switch to this ISP
        success = switch_to_isp(isp.name)
        assert success, f"Failed to switch to {isp.name}"

        time.sleep(0.5)  # Allow routing to settle

        # Test connectivity
        returncode, stdout, stderr = curl_internet()

        assert returncode == 0, \
            f"curl failed after switching to {isp.name}: {stderr}"
        assert "Request from:" in stdout, \
            f"Unexpected response from {isp.name}: {stdout}"
        assert isp.internet_ip in stdout, \
            f"Expected traffic from {isp.internet_ip} ({isp.name}), got: {stdout}"


def test_switch_back_and_forth(isp_failover_setup):
    """Test switching back and forth between ISP-A and ISP-B multiple times."""
    for iteration in range(3):
        print(f"\nIteration {iteration + 1}")

        # Switch to ISP-A
        assert switch_to_isp(ISP_A.name), f"Failed to switch to {ISP_A.name}"
        time.sleep(0.3)

        returncode, stdout, _ = curl_internet()
        assert returncode == 0
        assert ISP_A.internet_ip in stdout, \
            f"Iteration {iteration + 1}: Expected {ISP_A.internet_ip}, got {stdout}"

        # Switch to ISP-B
        assert switch_to_isp(ISP_B.name), f"Failed to switch to {ISP_B.name}"
        time.sleep(0.3)

        returncode, stdout, _ = curl_internet()
        assert returncode == 0
        assert ISP_B.internet_ip in stdout, \
            f"Iteration {iteration + 1}: Expected {ISP_B.internet_ip}, got {stdout}"
