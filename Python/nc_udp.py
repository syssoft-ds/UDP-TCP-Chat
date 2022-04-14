import socket
import sys

def receiveLines(port):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s_sock:
        s_sock.bind(('0.0.0.0',port))
        while True:
            line, c_address = s_sock.recvfrom(4096)
            line = line.decode().rstrip()
            print('Message <{0}> received from client {1}'.format(repr(line),c_address))
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
        print("Usage: \"{0} -l <port>\" or \"{0} <ip> <port>\"".format(sys.argv[0]))
        sys.exit()
    port = int(sys.argv[2])
    if sys.argv[1].lower() == '-l':
        receiveLines(port)
    else:
        sendLines(sys.argv[1],port)

if __name__ == '__main__':
    main()
