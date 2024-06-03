import socket
import sys
import getpass
import threading
import regex as re

#Client information
contacts = {}
USERNAME = None
NETINFO = None
CMDREGEX = re.compile(r'^[A-Z_]{3,}=')


def receiveLines(port):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s_sock:
        s_sock.bind(('0.0.0.0',port))
        while True:
            line, c_address = s_sock.recvfrom(4096)
            line = line.decode().rstrip()
            
            #Process incoming requests
            if CMDREGEX.match(line):
                cmd = line.split("=")
                if cmd[0] == 'FRIEND_ADD':
                    #FRIEND_ADD=<username>;<ip>;<port> -> Adds user to contacts
                    contact = cmd[1].split(";")
                    contacts[contact[0]] = (contact[1],int(contact[2]))     #name -> (host,port)
                    print(f'User {contact[0]} added you as a friend')
                elif cmd[0] == 'MSG_RECIEVE':
                    #MSG_RECIEVE=<sender>;<msg> -> Recieve message from a sender
                    msg = cmd[1].split(";")
                    print(f'> Message <{repr(msg[1])}> received from client {msg[0]}')    
            else:
                print(f'Unknown interaction: <{repr(line)}> received from client {c_address}')
            if line.lower() == 'stop':
                break

#Added aditional parameter msg to be called from chat_interface
def sendLines(host, port, msg: str=None):
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as c_sock:
        if not msg:
            for line in sys.stdin:
                line = line.rstrip()
                c_sock.sendto(line.encode(),(host,port))
                if line.lower() == 'stop':
                    break
        else:
            c_sock.sendto(msg.encode(),(host,port))

def chat_interface():
    print(f'Welcome {USERNAME}!')
    while True:
        try:
            cmd = input("< ").split(" ")
            if cmd[0] == 'add':
                if len(cmd) < 3:
                    print("Usage: add <name> <host> <port>")
                    continue
                sendLines(cmd[1],int(cmd[2]),f'FRIEND_ADD={USERNAME};{NETINFO[0]};{NETINFO[1]}' )
            elif cmd[0] == 'send':
                if len(cmd) < 3:
                    print("Usage: send <name> <message>")
                    continue
                msg = ' '.join(cmd[2:])
                sendLines(contacts[cmd[1]][0],contacts[cmd[1]][1],f'MSG_RECIEVE={USERNAME};{msg}')
            elif cmd[0] == 'contacts':
                print(contacts)
            elif  cmd[0] == 'whoami':
                print(f'{USERNAME}: {NETINFO[0]}:{NETINFO[1]}')
            elif cmd[0] == 'help':
                print("Available commands: add, send, contacts, whoami, help, exit")
            elif cmd[0] == 'exit':
                sys.exit()
            else:
                print("Invalid command")
        except Exception:
            print("Something went wrong")

def main():
    global USERNAME, NETINFO
    if len(sys.argv) != 2:
        name = sys.argv[0]
        print(f"Usage: \"{name} <port>\"")
        sys.exit()
    port = int(sys.argv[1])
    USERNAME = input("Enter your username: ").replace(" ", "_") or getpass.getuser()
    threading.Thread(target=receiveLines, args=(port,)).start()

    # Get the host name and port number
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
        s.connect(("8.8.8.8", 80))
        NETINFO = (s.getsockname()[0],port)
    chat_interface()

if __name__ == '__main__':
    main()
