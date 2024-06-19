# Übungsblatt 3 - David Ritter

## Aufgabe 1: Wireshark
### Vorgehensweise: 
1. Beide Programme als Java Klasse implementieren
2. Server starten mit Argumenten "-l port" (8080 für UDP, 8090 für TCP)
3. Client starten mit Argumenten "127.0.0.1 port"
4. Wireshark-Aufzeichnung starten und mit Display Filter "tcp.port == 8090 || udp.port == 8080" nach dem von den beiden Programmen erzeugten Traffic filtern
5. Nachrichten schreiben, Aufzeichnung beenden, Pakete vergleichen

### Ergebnis:
#### Unterschiede: 
Bei TCP wird zunächst via Handshake eine Verbindung aufgebaut. Dazu werden erst 3 Pakete versendet:
1. Client an Server [SYN] für Synchronisation
2. Server an Client [SYN, ACK] für Synchronisation und Acknowledgement
3. Client an Server [ACK] für Acknowledgement

Damit steht dann die Verbindung und erst anschließend wird die eigentliche Nachricht versendet. 
1. Client an Server [PSH, ACK] für Push and Acknowledgement

Zum Abschluss jeder Nachricht wird noch ein Paket von Server an Client gesendet
1. Sever an Client [ACK] für Acknowledgement

Zum Beenden der Verbindung wird zudem noch ein Paket von Server an Client geschickt
1. Server an Client [RST, ACK] für Reset der Verbindung

Bei UDP findet all das nicht statt. Dort wird lediglich das Paket von Client an Server verwendet.

![image info](./pic/U3A1_TCP_UDP_Wireshark.png)

## Aufgabe2 : UDP

Ich habe es leider nicht geschafft eine den Ansprüchen genügende Variante des Programms zu erzeugen.

### Funktionalität:
Das Programm lässt sich wie gewünscht ausführen, dh.
1. Server erstellen: Name -l Port  ``User1 started as a server on port 5000``
2. Client erstellen: Name IP Port des Servers ``User2 started as a client connecting to 127.0.0.1:5000``
3. Client wird mit eigenem Port in eine Registerliste aufgenommen: ``User User2 registered with IP /127.0.0.1 and port 51408``

### Problem:
Der angesprochene User wird in der Registerliste nicht gefunden. Anstelle dessen wird lediglich beim erstellen des Clients ein UDP Paket von Client an Server geschickt.   
Bsp:``5301	1846.798039	127.0.0.1	127.0.0.1	UDP	43	64823 → 5000 Len=11``

![image info](./pic/U3A2_UDPChat_Wireshark.png)

## Aufgabe 3: TCP

Ich habe zwei Java Klassen erstellt. Eine für den Server und eine für die Clients.

### Bedienung:
1. Server erstellen mit TCPChatServer und dem gewünschten Port ``5000`` als Argument
2. Zwei Clients erstellen mit TCPChatClient und IP und Port ``127.0.0.1 5000``als Argumente
3. Als Client je einen Namen eingeben. Bsp: ``Client 1 = U1``, ``Client 2 = user2``
4. Nachricht unter den Clients verschicken mit ``send <Client> <Nachricht>``

### Beobachtung:
Mit laufender Aufzeichnung von Wireshark und Filter ``tcp.port == 5000`` ergeben sich folgende Beobachtungen.

#### 1. Verbindungsaufbau:
1. Wie in Aufgabe 1 wird wieder ein Handshake zwischen Server und Client gemacht. 

#### 2. Registrierung:
1. Server sendet [PSH] an Client mit der Aufforderung sich mit Namen zu registrieren.
2. Client sendet als Antwort darauf [ACK] Paket an Server
3. Client sendet [PSH, ACK] den registrierten Namen an Server
4. Server [ACK] + [PSH, ACK] mit Bestätigung dass Client jetzt registriert ist
5. Client [ACK]

#### 3. Nachrichten zwischen Clients:
1. Sender-Client sendet an Server, Server bestätigt
2. Server sendet an Empfänger-Client, Empfänger-Client bestätigt

#### 4. Beenden
1. Client sendet [RST, ACK] an Server und wird beendet

![image info](./pic/U3A3_TCPChat_Wireshark.png)

## Aufgabe 4: Manchester-Code
![image info](./pic/U3A4_Manchester_Code.png)





