import socket
import sys
import threading

clients = {}

class ChatClient: #Diese Klasse ist so konzipiert, dass sie einen Chatclient in einem Netzwerkkommunikationssystem darstellt.
    def __init__(self, name: object, host: object, port: object) -> object:
        self.name = name #Stellt den Namen des Chatclients dar.
        self.host = host #Gibt die Host-IP-Adresse an, mit der der Client eine Verbindung herstellt.
        self.port = port #Bestimmt die Portnummer, mit der der Client mit dem Server kommunizieren soll.
        self.clients = {} #Ein Wörterbuch, in dem die registrierten Clients gespeichert werden.

    def receive_messages(self):
        pass

    def send_messages(self):
        pass

def serveClient(c_sock, c_address, name):
    with c_sock:
        register_client(c_sock, c_address, name)
        while True:
            line = c_sock.recv(1024).decode().rstrip()
            print(f'Message <{repr(line)}> received from client {c_address}')
            if line.lower() == 'top':
                break
            elif line.startswith('send '):
                parts = line.split(' ', 2)
                if len(parts) == 3:
                    recipient, message = parts[1], parts[2]
                    if recipient in clients:
                        clients[recipient].sendall(f'{name}: {message}'.encode())
                    else:
                        c_sock.sendall(f'Error: Client {recipient} not found'.encode())

def client(host, port): #Die Methode stellt die Clientfunktion dar, die für den Aufbau einer TCP-Verbindung zu einem Server und das Senden von Nachrichten an ihn verantwortlich ist.
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as c_sock: #erstellt den Socket
        c_sock.connect((host, port)) #stellt Verbindung her
        name = input("Enter your name: ")
        c_sock.sendall(name.encode()) #Der Client sendet den eingegebenen Namen an den Server.
        for line in sys.stdin: #Jede Zeile wird von nachgestellten Leerzeichen befreit.
            line = line.rstrip()
            c_sock.sendall(line.encode())
            if line.lower() == 'top':
                break

def register_client(c_sock, c_address, name): #Die Methode ist Teil der Funktion register_client in der nc_tcp.py Datei. Diese Funktion ist dafür verantwortlich, neue Clients mit ihren jeweiligen Namen zu registrieren, wenn sie sich mit dem Server verbinden.
    with c_sock:
        name = c_sock.recv(1024).decode().rstrip() #Der Name des Clients wird vom Socket empfangen. Der empfangene Name wird dann von Bytes in eine Zeichenfolge decodiert und von allen nachfolgenden Leerzeichen befreit.
        print(f'Client {c_address} registered as {name}')
        clients[name] = c_sock #Der Name und der Socket des Clients werden dem Clientwörterbuch hinzugefügt.

def server(port): #Die Methode ist Teil der Serverfunktion in der nc_tcp.py Datei. Diese Funktion richtet einen TCP-Server ein, der auf eingehenden Verbindungen an einem bestimmten Port lauscht.
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s_sock: #Ein TCP-Socket wird erstellt
        s_sock.bind(('0.0.0.0',port)) #Der Server bindet den Socket an den angegebenen Port. Die IP-Adresse "0.0.0.0" bedeutet, dass der Server auf allen Netzwerkschnittstellen lauscht.
        s_sock.listen() #Der Server beginnt mit dem Lauschen auf eingehende Verbindungen.
        while True: #Innerhalb einer Endlosschleife akzeptiert der Server eine neue Verbindung. Dadurch wird das Programm blockiert, bis ein Client eine Verbindung herstellt.
            c_sock, c_address = s_sock.accept()
            name = c_sock.recv(1024).decode().rstrip()  # Receive name from client
            t = threading.Thread(target=serveClient, args=(c_sock, c_address, name))
            t.start()

def main():
    if len(sys.argv) != 3:
        name = sys.argv[0]
        print(f"Usage: \"{name} -l <port>\" or \"{name} <ip> <port>\"")
        sys.exit()
    port = int(sys.argv[2])
    name = sys.argv[3]
    if sys.argv[1].lower() == '-l':
        client = ChatClient(name, '0.0.0.0', port)
        client.receive_messages()
    else:
        host = sys.argv[1]
        client = ChatClient(name, host, port)
        client.send_messages()

if __name__ == '__main__':
    main()
