import socket
import sys
import threading
import time

dictNameChats = {}
name = ""
port = 0
ipaddress = "127.0.0.1"

def getHostAndPort(name):
    return dictNameChats[name]

def addChat(name, host, port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind(host,port)
    dictNameChats[name] = sock
    
def getChatPartner():
    return dictNameChats.keys()
def server(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s_sock:
        s_sock.bind(('0.0.0.0', port))
        s_sock.listen()
        while True:
            c_sock, c_address = s_sock.accept()
            t = threading.Thread(target=serveClient,args=(c_sock,c_address))
            t.start()

def serveClient(c_sock,c_address):
    with c_sock:
        while True:
            line = c_sock.recv(1024).decode().rstrip()
            #print(f'Message <{repr(line)}> received from client {c_address}')
            tocken = line.split()
            if len(tocken) > 0:
                try:
                    if tocken[0] == "chat":
                        print(list(getChatPartner()))
                    if tocken[0] == "connect":
                        addChat(tocken[1], tocken[2], tocken[3])
                    if tocken[0] == "send":
                        reciver = dictNameChats[tocken[1]]
                        message = " ".join(tocken[2:])
                        reciver.sendall(message.encode())
                except Exception as e:
                    print("for connect: <name> <port> <host>, for send: <name> <port> <message>, for participants: chat")
            if line.lower() == 'stop':
                break

def client(host,port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as c_sock:
        c_sock.connect((host,port))
        message = "connect "+name+" "+str(port)+" "+ipaddress
        c_sock.sendall(message.encode())
        send = threading.Thread(target=sendLines,args=(c_sock,))
        listen = threading.Thread(target=listenLines,args=(c_sock,))
        listen.start()
        send.start()


def listenLines(socket):
    while True:
        line = socket.recv(1024).decode().rstrip()
        print(f'Message <{repr(line)}> received from server')
        if line.lower() == 'stop':
            break
        time.sleep(0.1)
def sendLines(socket):
    for line in sys.stdin:
        line = line.rstrip()
        socket.sendall(line.encode())
        if line.lower() == 'stop':
            break
        time.sleep(0.1)
def main():
    if len(sys.argv) < 3:
        name = sys.argv[0]
        print(f"Usage: \"{name} -s <port>\" or \"{name} <name> <port> <ip>\"")
        sys.exit()
    port = int(sys.argv[2])
    if sys.argv[1].lower() == '-s':
        server(port)
    else:
        name = sys.argv[1]
        print(name + " " + str(port) + " " + str(ipaddress))
        client(ipaddress,port)

if __name__ == '__main__':
    main()
