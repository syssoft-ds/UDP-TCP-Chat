import socket
import sys
import threading

# Dictionary to store known clients: {name: (ip, port)}
clients = {}

def receiveMessages(port):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s_sock:
        s_sock.bind(('0.0.0.0', port))
        print(f"Listening for messages on port {port}")
        while True:
            message, c_address = s_sock.recvfrom(4096)
            message = message.decode().rstrip()
            handleMessage(message, c_address, port)

def handleMessage(message, c_address, my_port):
    global clients
    if message.startswith("register"):
        parts = message.split()
        if len(parts) == 4:
            name = parts[1]
            ip = parts[2]
            port = int(parts[3])
            if name not in clients:
                clients[name] = (ip, port)
                print(f"Registered {name} with IP {ip} and port {port}")
                print(f"Current clients: {clients}")
                # Register back with the client
                sendMessage(ip, port, f"register {sys.argv[1]} {c_address[0]} {my_port}")
    elif message.startswith("send"):
        parts = message.split(' ', 2)
        if len(parts) >= 3:
            target_name = parts[1]
            msg = parts[2]
            if target_name in clients:
                target_ip, target_port = clients[target_name]
                sendMessage(target_ip, target_port, f"message {msg}")
                print(f"Received message: '{msg}' from {target_name}")
            else:
                print(f"Client {target_name} not found")

def sendMessage(ip, port, message):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as c_sock:
        c_sock.sendto(message.encode(), (ip, port))

def client(name, host, port):
    while True:
        line = sys.stdin.readline().rstrip()
        if line.startswith("register"):
            parts = line.split()
            if len(parts) == 4:
                target_ip = parts[2]
                target_port = int(parts[3])
                sendMessage(target_ip, target_port, f"register {name} {host} {port}")
        elif line.startswith("send"):
            parts = line.split(' ', 2)
            if len(parts) >= 3:
                target_name = parts[1]
                msg = parts[2]
                if target_name in clients:
                    target_ip, target_port = clients[target_name]
                    sendMessage(target_ip, target_port, f"send {name} {msg}")
                else:
                    print(f"Client {target_name} not found")
        elif line == "exit":
            break

def main():
    if len(sys.argv) != 4:
        name = sys.argv[0]
        print(f"Usage: \"{name} <name> -l <port>\" or \"{name} <name> <ip> <port>\"")
        sys.exit()
    my_name = sys.argv[1]
    port = int(sys.argv[3])
    threading.Thread(target=receiveMessages, args=(port,), daemon=True).start()
    client(my_name, '127.0.0.1', port)  # Assuming '127.0.0.1' for local runs

if __name__ == '__main__':
    main()
