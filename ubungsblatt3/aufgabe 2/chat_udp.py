import socket
import sys
import threading
import time

import keyboard


dictNameChats = {}
name = ""
port = 0
ipaddress = "127.0.0.1"
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

def getHostAndPort(name):
    return dictNameChats[name]

def addChat(name, host, port):
    dictNameChats[name] = (host, port) 

def getChatPartner():
    return dictNameChats.keys()
def receiveLines(port):
    with sock:
        sock.bind(('0.0.0.0',port))
        while True:
            line, c_address = sock.recvfrom(4096)
            line = line.decode().rstrip()
            print(f'Message <{repr(line)}> received from client {c_address}')
            if line.lower() == 'stop':
                break
            time.sleep(0.1)
def sendLines(host,port,text):
        print("send")
        sock.sendto(text.encode(), (host, port))

def chat():
    for line in sys.stdin:
        line = line.rstrip()
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
                    sendLines(reciver[1], int(reciver[0]), message)
            except Exception as e:
                print("for connect: <name> <port> <host> , for send: <name> <port> <message>")
        time.sleep(0.1)
def main():
    if len(sys.argv) != 3:
        name = sys.argv[0]
        print(f"Usage: \"{name} <name> <port>\"")
        sys.exit()
    name = sys.argv[1]
    port = int(sys.argv[2])
    print(name + " " + str(port) + " " + str(ipaddress))
    listener = threading.Thread(target=receiveLines, args=(port,))
    sender = threading.Thread(target=chat)
    sender.start()
    listener.start()

if __name__ == '__main__':
    main()
