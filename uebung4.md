Übungsblatt 4 - David Ritter

## Aufgabe 1: Protokoll-Header
### IPv4:
![image info](./pic/U3A1_TCP_UDP_Wireshark.png)

### UDP:

![image info](./pic/U3A1_TCP_UDP_Wireshark.png)
### TCP:

![image info](./pic/U3A1_TCP_UDP_Wireshark.png)


## Aufgabe 2: CIDR

Beschreiben Sie 103.161.122.83/18. Was bedeuten jeweils die Werte 103.161.122.83 und die 18?

Kurz: IP-Adresse/Bits Netzanteil

Lang: **103.161.122.83** ist eine IPv4 Adresse. Sie besteht aus insgesamt 32 Bits, also 4 Bytes à 8 Bit. Die Bytes sind durch einen Punkt voneinander getrennt und können Dezimal Werte von 0 bis 255 bzw binär von 00000000 bis 11111111 annehmen.
Dh. 103.161.122.83 kann auch so dargestellt werden: 01100111.10100001.01111010.01010011

Die **/18** besagt wie viele der Bits der IP von links gezählt der Netzanteil sind. Daraus ergibt sich, dass der Rest dann der Hostanteil sind, in dem Fall also 18 Bits Netzanteil, 32-18=14 Bits Hostanteil.

Netzanteil = Grün, Hostanteil = rot:
<span style="color:green">01100111.10100001.01</span><span style="color:red">111010.01010011</span>

### Subnetzmaske ermitteln:

Wegen der /18 folgt, dass die ersten 18 Bits der Subnetzmaske auf 1 gesetzt sind und die restlichen Bits auf 0. Dh:

11111111.11111111.11000000.00000000 und umgewandelt in dezimal 255.255.192.0

### Netzwerkadresse ermitteln:

Die Netzwerkadresse ergibt sich aus der UND-Verknüofung von IP-Adresse und Subnetzmaske:

![image info](./pic/U3A1_TCP_UDP_Wireshark.png)

Daraus ergibt sich dezimal:  103.161.64.0

### Broadcastadresse ermitteln:

Daraus lässt sich jetzt die Broadcastadresse ermitteln, indem alle Bits des Hostanteils der Netzwerkadresse auf 1 setzt, also bei /18 die letzten 14.

01100111.10100001.01000000.00000000 -> 01100111.10100001.01111111.11111111

Dies ergibt die Broadcastadresse: 103.161.127.255

### Überprüfung ob die beiden CIDR Adressen im selben Netz liegen:
Dazu muss die Netzwerkadresse der 2. IP erzeugt werden, also die 2. IP mit derselben Subnetzmaske UND-verknüpft werden:

103.161.193.83 = 01100111.10100001.11000001.01010011

#### Verknüpfung:

![image info](./pic/U3A1_TCP_UDP_Wireshark.png)

#### Netzwerkadresse für 103.161.193.83/18: 

103.161.192.0

#### Ergebnis: 

Die Netzwerkadressen unterscheiden sich in Byte 3 und 4, also die IPs nicht im selben Netz.

## Aufgabe 3: Kommunikation zwischen Implementationen 

### UDP:

#### Vorhaben:
UDP_Chat(Musterimplementation - Abk.: MI) und UDPChat(eigene Implementation - Abk.: EI) miteinerander kommunizieren lassen. 

#### Versuch 1: 
1. MI starten mit Name user1 und Port 8000.
2. EI starten mit Name user2, IP 127.0.0.1 und Port 8001
#### Beobachtung: 
Destination unreachable. EI bekommt Port 62651 versucht 8001 zu erreichen was fehlt schlägt.

#### Versuch 2:
1. MI starten mit Name user1 und Port 8000.
2. EI starten mit Name user2, IP 127.0.0.1 und Port 8000
#### Beobachtung:
Kommunikation einmalig erfolgreich. EI erhält Port 63722 und sendet Nachricht an Port 8000

#### Versuch 3:
1. Aufbau wie Versuch 2
2. in MI "register 127.0.0.1 63722" ausführen 
3. in MI "send user2 message"
#### Beobachtung:
1. register: MI erhält neuen Port 51770 und sendet an Port 63722, also EI
2. send: funktioniert nicht. Unknown client. Sowohl an sich selbst, als auch an andere Implementatrion. Kein Paket wird gesendet

#### Versuch 4:
1. Aufbau wie Versuch 2
2. register 127.0.0.1 8000
3. send an user1 (also MI an sich selbst)

#### Beobachtung:
1. Selbstregistrierung funktioniert, aber MI wird neuer zusätzlicher Port zugeordnet und ein Paket an Port 8000 verschickt.
2. send funtkioniert, MI erhält neuen Port und sendet an Port 8000

#### Versuch 5:
1. 2x MI starten und untereinander kommnunizieren lassen

#### Beobachtung:

Funktioniert.

#### Fazit:

Der Versuch einer Kommunikation zwischen unterschiedlichen Implementationen stattfinden zu lassen schlägt fehl. Außerdem ist zu beobachten, dass jede neue Nachricht via "send" als Source anscheinend einen neuen Port erhält und lediglich die Destination identisch bleibt. Außerdem ist zu beobachten, dass bei den Nachrichten nicht mehr die Loopback Adresse sondern die eigene IP als Source und Destination verwendet wird.


### TCP:

#### Versuch 1:
EI Server mit 2x MI Client.

#### Beobachtung:
Clients verbinden sich, Nachrichten untereinander kommen nicht an. Allerdings versucht TCP mit [TCP Keep-Alive] Tag weiter die Nachricht zwischen den Clients zu versenden.  

#### Versuch 2:
MI Server mit 2x EI Client.

#### Beobachtung: 
Verbindung wird aufgebaut, aber Implementationen anscheiennd nicht kompatibel.

#### Versuch 3+4:
MI Server mit 1x EI und 1x MI CLient und umgekehrt.

#### Beobachtung:
Wie erwartet nicht kompatibel.

#### Fazit: 

Obwohl ich davon ausging, dass die Übung mit TCP funktionieren sollte, scheinen die Implementationen zu unterschieldich zu sein, um sie miteinander kommunizieren zu lassen. Ein Problem ist, dass die MI anscheinend mit der eigenen IP arbeitet und die EI mit der Loopback IP. Da diese Voraussetzung hart in den Argumenten der Main-Methoden implementiert ist, sehe ich ohne Modifikationen keine Möglichkeit die beiden Versionen kompatibel zu bekommen.


## Übung 4: 

### Vorgehensweise:
1. Schrittweise Implementation der neuen Funktionen mit Hilfe von GAI. 
2. Nach jedem Schritt testen die neuen Funktionen funktionieren.

### Dokumentation:
1. a) erfolgreich implementiert, "broadcast message" sendet wie gewünscht von Client an Server und von dort an alle Clients. [ACK] und [PSH] fidnet wie gewohnt statt.
2. b) erfolgreich implementiert, "list" sendet von Client eine Anfrage an den Server und erhält daraufhin die Liste als Antwort
3. c) hier habe ich mich schwer getan und bin leider zu keiner funktionierenden Version gekommen. 

### Fazit: 

Die gewünschten Funktionen von a) und b) sind funktionsfähig implementiert, die von c) leider nicht. 