import ipaddress
import socket
import sys

class ChatClient: #Diese Klasse ist so konzipiert, dass sie einen Chatclient in einem Netzwerkkommunikationssystem darstellt.
    def __init__(self, name, host, port, ipaddress):
        self.name = name #Stellt den Namen des Chatclients dar.
        self.host = host #Gibt die Host-IP-Adresse an, mit der der Client eine Verbindung herstellt.
        self.port = port #Bestimmt die Portnummer, mit der der Client mit dem Server kommunizieren soll.
        self.ip = ipaddress
        self.clients = {} #Ein Wörterbuch, in dem die registrierten Clients gespeichert werden.

    def receive_messages(self):
        pass

    def send_messages(self):
        pass


def receive_messages(self): #Diese Methode ist für das Abhören eingehender Nachrichten von anderen Clients im Netzwerk verantwortlich.
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s_sock: #Ein UDP-Socket wird erstellt. Das Argument AF_INET gibt die Adressfamilie (IPv4) an, und SOCK_DGRAM gibt an, dass es sich um einen UDP-Socket handelt.
        s_sock.bind(('0.0.0.0', self.port)) #Der Socket ist an den angegebenen Host und Port gebunden. In diesem Fall wird der Host auf '0.0.0.0' gesetzt, um auf allen Netzwerkschnittstellen zu lauschen.
        while True:
            message, c_address = s_sock.recvfrom(4096) #Innerhalb der Schleife wird eine Nachricht von einem Client empfangen.
            message = message.decode().rstrip() #Die Nachricht wird decodiert, um alle nachfolgenden Leerzeichen zu entfernen.
            print(f'<{repr(message)}>')
            if message.lower() == 'top': #Wenn gegeben, wird die Schleife unterbrochen und die Methode wird zurückgegeben.
                break
            elif message.startswith('register '): #Wenn die Nachricht mit "register" beginnt, wird die register_client Methode aufgerufen, um den Client mit seiner IP-Adresse und Portnummer zu registrieren.
                self.register_client(message, c_address)
            elif message.startswith('send '): #Wenn die Nachricht mit 'send' beginnt, wird die send_message Methode aufgerufen, um die Nachricht an einen anderen Client zu senden.
                self.send_message(message)

def send_message(self, message):
    parts = message.split(' ', 2)
    if len(parts) == 3:
        recipient_name, message_text = parts[1:]
        if recipient_name in self.clients:
            recipient_ip, recipient_port = self.clients[recipient_name]
            with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as c_sock:
                c_sock.sendto(f'Hallo, hier ist {self.name},  meine IP-Adresse ist die {self.ip} und du kannst mich unter Port-Nummer {self.port}'.encode(), (recipient_ip, recipient_port))
                c_sock.sendto({message_text}.encode(), (recipient_ip, recipient_port))
                print(f'gesendet an {recipient_name}: {message_text}')
        else:
            print(f'Client {recipient_name} not found')

def send_messages(self): #Die Methode ist für das Senden von Nachrichten an den Server oder einen anderen Client im Netzwerk verantwortlich.
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as c_sock: #ertellen Socket
        for line in sys.stdin:
            line = line.rstrip() #Jede Zeile wird von nachgestellten Leerzeichen befreit.
            c_sock.sendto(line.encode(), (self.host, self.port)) #Die codierte Leitung wird an den Server oder einen anderen Client gesendet.
            if line.lower() == 'top':
                break

def register_client(self, message, c_address): #Die Methode ist für die Registrierung eines neuen Clients mit seiner IP-Adresse und Portnummer verantwortlich.
    parts = message.split(' ', 2)
    if len(parts) == 3:
        client_name, client_ip, client_port = parts[1:]
        self.clients[client_name] = (client_ip, int(client_port))
        print(f'Client {client_name} registered with IP {client_ip} and port {client_port}')

def main():
    if len(sys.argv) != 3:
        name = sys.argv[0]
        print(f"Usage: \"{name} -l <port>\" or \"{name} <ip> <port>\"")
        sys.exit()
    port = int(sys.argv[2])
    name = sys.argv[3]
    if sys.argv[1].lower() == '-l':
        client = ChatClient(name, '0.0.0.0', port, ipaddress)
        client.receive_messages()
    else:
        host = sys.argv[1]
        client = ChatClient(name, host, port, ipaddress)
        client.send_messages()

if __name__ == '__main__':
    main()
