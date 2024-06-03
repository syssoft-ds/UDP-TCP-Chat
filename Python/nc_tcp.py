import socket
import sys
import threading
import getpass
import regex as re

#Server 
userlist = {} #list of users with their ip and port
CMDREGEX = re.compile(r'^[A-Z_]{3,}=')

#Client
USERNAME = None
NETINFO = None


def server(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s_sock:
        s_sock.bind(('0.0.0.0',port))
        s_sock.listen()
        while True:
            c_sock, c_address = s_sock.accept()
            port = c_sock.getsockname()[1]
            print(f'Client {c_address} connected on port {port}')
            t = threading.Thread(target=serveClient,args=(c_sock,c_address))
            t.start()

def serveClient(c_sock, c_address):
    with c_sock:
        while True:
            line = c_sock.recv(1024).decode().rstrip()
            if not line:
                print(f'Client {c_address} disconnected')
                #remove user from userlist
                for user in list(userlist.keys()):
                    if userlist[user][0] == c_address[0] and userlist[user][1] == c_address[1]:
                        userlist.pop(user)
                break

            #Process incoming server requests
            if CMDREGEX.match(line):
                cmd = line.split("=")
                if cmd[0] == 'ADDUSR':
                    #ADDUSR=<username>;<ip>;<port> -> Adds user to userlist of server
                    user = cmd[1].split(";")
                    userlist[user[0]] = (user[1],int(user[2]), c_sock)
                elif cmd[0] == 'LIST':
                    #LIST= -> Sends list of users to requesting client
                    users = [user for user in userlist]
                    c_sock.sendall(str(users).encode())
                elif cmd[0] == 'MSG':
                    #MSG=<sender>;<reciever>;<msg> -> Sends message to reciever
                    sender, reciever = cmd[1].split(";")[0], cmd[1].split(";")[1]
                    msg = cmd[1].split(";")[2]
                    
                    if reciever in userlist:
                        try:
                            reciever_sock = userlist[reciever][2]
                            reciever_sock.sendall(f'{sender}: {msg}'.encode())
                        except Exception:
                            print(f'Error sending message to {reciever}\n{Exception}')
                    print(f'Message <{repr(msg)}> received from client {sender}, sending to {reciever}')         
            else:
                print(f'Unknown interaction <{repr(line)}> received from client {c_address}')
            if line.lower() == 'stop':
                break

def client(host,port):
    global USERNAME, NETINFO
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as c_sock:
        c_sock.connect((host,port))
        NETINFO = (c_sock.getsockname()[0],c_sock.getsockname()[1])

        connectmsg = f'ADDUSR={USERNAME};{NETINFO[0]};{NETINFO[1]}'
        c_sock.sendall(connectmsg.encode())
        print(f'Hello {USERNAME}! You are connected to {host}:{port}')

        #start new thread to accept incoming messages
        threading.Thread(target=display_msg, args=(c_sock,)).start()
        #client interface
        while True:
            try:
                cmd = input("< ").split(" ")
                if cmd[0] == 'send':
                    if len(cmd) < 3:
                        print("Usage: send <name> <message>")
                        continue
                    msg = ' '.join(cmd[2:])
                    c_sock.sendall(f'MSG={USERNAME};{cmd[1]};{msg}'.encode())
                elif cmd[0] == 'list':
                    c_sock.sendall('LIST='.encode())
                elif cmd[0] == 'help':
                    print("Available commands: list, send, help, exit")
                elif cmd[0] == 'exit':
                    sys.exit()
                else:
                    print("Invalid command")
            except Exception:
                print("Something went wrong")

def display_msg(c_sock):
    while True:
        msg = c_sock.recv(1024).decode()
        print(f'{msg}')

def main():
    global USERNAME
    if len(sys.argv) != 3:
        name = sys.argv[0]
        print(f"Usage: \"{name} -l <port>\" or \"{name} <ip> <port>\"")
        sys.exit()
    port = int(sys.argv[2])
    if sys.argv[1].lower() == '-l':
        server(port)
    else:
        USERNAME = input("Enter your username: ").replace(" ", "_") or getpass.getuser()
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        client(sys.argv[1],port)

if __name__ == '__main__':
    main()
