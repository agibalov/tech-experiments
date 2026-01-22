#!/usr/bin/env python3
from http.server import HTTPServer, BaseHTTPRequestHandler


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/':
            source_ip = self.client_address[0]
            response = f"Request from: {source_ip}\n"
            print(f"REQUEST from {source_ip}")
            self.send_response(200)
            self.send_header('Content-Type', 'text/plain')
            self.end_headers()
            self.wfile.write(response.encode())
        else:
            self.send_response(404)
            self.end_headers()


def get_all_interface_ips():
    import socket
    import fcntl
    import struct
    ips = []
    for iface in socket.if_nameindex():
        iface_name = iface[1]
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            ip = socket.inet_ntoa(fcntl.ioctl(
                sock.fileno(),
                0x8915,  # SIOCGIFADDR
                struct.pack('256s', iface_name.encode('utf-8')[:15])
            )[20:24])
            ips.append(ip)
        except OSError:
            pass  # Interface has no IPv4 address
    return sorted(ips)


if __name__ == '__main__':
    server = HTTPServer(('', 8080), Handler)
    ips = get_all_interface_ips()
    print(f'Server running on port 8080, listening on: {", ".join(ips)}')
    server.serve_forever()
