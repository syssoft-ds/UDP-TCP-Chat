import socket
import sys

def receiveLines(port):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s_sock:
        s_sock.bind(('0.0.0.0',port))
        while True:
            line, c_address = s_sock.recvfrom(4096)
            line = line.decode().rstrip()
            print(f'Message <{repr(line)}> received from client {c_address}')
            if line.lower() == 'stop':
                break

def sendLines(host,port):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as c_sock:
        for line in sys.stdin:
            line = line.rstrip()
            c_sock.sendto(line.encode(),(host,port))
            if line.lower() == 'stop':
                break

def main():
    if len(sys.argv) != 3:
        name = sys.argv[0]
        print(f"Usage: \"{name} -l <port>\" or \"{name} <ip> <port>\"")
        sys.exit()
    port = int(sys.argv[2])
    if sys.argv[1].lower() == '-l':
        receiveLines(port)
    else:
        sendLines(sys.argv[1],port)

if __name__ == '__main__':
    main()
