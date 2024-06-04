import socket
import sys
import threading

# Dictionary to store registered clients: {name: (ip, port)}
clients = {}

def handleClient(c_sock, c_address):
    with c_sock:
        while True:
            message = c_sock.recv(1024).decode().rstrip()
            if not message:
                break
            print(f"Received message from {c_address}: {message}")
            if message.startswith("register"):
                parts = message.split()
                if len(parts) == 4:
                    name = parts[1]
                    ip = parts[2]
                    port = int(parts[3])
                    clients[name] = (ip, port)
                    print(f"Registered {name} with IP {ip} and port {port}")
                    print(f"Current clients: {clients}")
            elif message.startswith("send"):
                parts = message.split(' ', 2)
                if len(parts) == 3:
                    target_name = parts[1]
                    msg = parts[2]
                    if target_name in clients:
                        target_ip, target_port = clients[target_name]
                        sendMessage(target_ip, target_port, f"{target_name}: {msg}")
                    else:
                        print(f"Client {target_name} not found")
                else:
                    print("Unknown message format:", message)

def sendMessage(ip, port, message):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as c_sock:
        try:
            c_sock.connect((ip, port))
            c_sock.sendall(message.encode())
        except ConnectionRefusedError:
            print(f"Failed to send message to {ip}:{port}. Connection refused.")

def startServer(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s_sock:
        s_sock.bind(('0.0.0.0', port))
        s_sock.listen()
        print(f"Server listening on port {port}")
        while True:
            c_sock, c_address = s_sock.accept()
            threading.Thread(target=handleClient, args=(c_sock, c_address), daemon=True).start()

def registerClient(server_ip, server_port, client_name, client_port):
    message = f"register {client_name} {server_ip} {client_port}"
    sendToServer(server_ip, server_port, message)

    # Start listening on the client port after registration
    threading.Thread(target=startClientListener, args=(client_port,), daemon=True).start()

def startClientListener(client_port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as c_sock:
        c_sock.bind(('0.0.0.0', client_port))
        c_sock.listen()
        print(f"Client listening on port {client_port}")
        while True:
            conn, addr = c_sock.accept()
            threading.Thread(target=handleClientMessage, args=(conn, addr), daemon=True).start()

def handleClientMessage(conn, addr):
    with conn:
        while True:
            message = conn.recv(1024).decode().rstrip()
            if not message:
                break
            print(f"Received message from {addr}: {message}")

def sendToServer(server_ip, server_port, message):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as c_sock:
        try:
            c_sock.connect((server_ip, server_port))
            c_sock.sendall(message.encode())
        except ConnectionRefusedError:
            print(f"Failed to connect to server at {server_ip}:{server_port}. Connection refused.")

def main():
    if len(sys.argv) < 3:
        print("Usage:")
        print("For server: python tcpchat.py server <port>")
        print("For client: python tcpchat.py client <server_ip> <server_port> <client_name> [<client_port>]")
        sys.exit()

    if sys.argv[1] == "server":
        port = int(sys.argv[2])
        startServer(port)
    elif sys.argv[1] == "client":
        if len(sys.argv) < 5:
            print("Usage: python tcpchat.py client <server_ip> <server_port> <client_name> [<client_port>]")
            sys.exit()
        
        server_ip = sys.argv[2]
        server_port = int(sys.argv[3])
        client_name = sys.argv[4]
        client_port = int(sys.argv[5]) if len(sys.argv) > 5 else 0
        
        if client_port == 0:
            print("Using random client port.")
        
        registerClient(server_ip, server_port, client_name, client_port)
        
        while True:
            target_name = input("Enter target name (or 'exit' to quit): ")
            if target_name.lower() == 'exit':
                break
            message = input("Enter message: ")
            sendMessage(server_ip, server_port, f"send {target_name} {message}")

if __name__ == '__main__':
    main()
