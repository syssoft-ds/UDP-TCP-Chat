import socket
import sys
import threading

def server(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s_sock:
        s_sock.bind(('0.0.0.0',port))
        s_sock.listen()
        while True:
            c_sock, c_address = s_sock.accept()
            t = threading.Thread(target=serveClient,args=(c_sock,c_address))
            t.start()

def serveClient(c_sock,c_address):
    with c_sock:
        while True:
            line = c_sock.recv(1024).decode().rstrip()
            print(f'Message <{repr(line)}> received from client {c_address}')
            if line.lower() == 'stop':
                break

def client(host,port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as c_sock:
        c_sock.connect((host,port))
        for line in sys.stdin:
            line = line.rstrip()
            c_sock.sendall(line.encode())
            if line.lower() == 'stop':
                break

def main():
    if len(sys.argv) != 3:
        name = sys.argv[0]
        print(f"Usage: \"{name} -l <port>\" or \"{name} <ip> <port>\"")
        sys.exit()
    port = int(sys.argv[2])
    if sys.argv[1].lower() == '-l':
        server(port)
    else:
        client(sys.argv[1],port)

if __name__ == '__main__':
    main()
