# Aufgabe 2

## CIDR Notation

103.161.122.83/18

IP: 103.161.122.83

Prafixlänge: 18, d.h. die ersten 18 Bits bilden die Netzadresse, die übrigen Bits die Hostadresse
Wir setzen die ersten 18 Bits auf 1 und erhalten so die subnet mask.

subnet mask:
11111111.11111111.11000000.00000000 entspricht
255.255.192.0 in dezimal
Durch Anwendung des AND-Operators zwischen IP und subnetmask erhalten wir die Netzwerkadresse.

Netzwerkadresse:
01100111.10100001.01111010.01010011 AND
11111111.11111111.11000000.00000000 =
01100111.10100001.01000000.00000000 =
103.161.64.0 (dezimal)

Setzt man alle Host-Bits der Netzwerkadresse auf '1', erhält man die Broadcast-Adresse

Broadcastadresse:
01100111.10100001.01111111.11111111 =
103.161.127.255 (dezimal)

Gehört 103.161.193.83/18 demselben Netz an?

103.161.122.83/18 und 103.161.193.83/18
Berechnen wir die Netzwerkadresse für 103.161.193.83/18:

IP-Adresse in Binärform:
103.161.193.83 -> 01100111.10100001.11000001.01010011

Subnetzmaske in Binärform:
255.255.192.0 -> 11111111.11111111.11000000.00000000

Ergebnis des AND-Operators:
01100111.10100001.11000000.00000000

Die Netzwerkadresse in dezimaler Form ist:
103.161.192.0

Ergebnis
Die Netzwerkadresse für 103.161.122.83/18 ist 103.161.64.0.
Die Netzwerkadresse für 103.161.193.83/18 ist 103.161.192.0.
Da die Netzwerkadressen unterschiedlich sind, gehören 103.161.122.83/18 und 103.161.193.83/18 nicht zum selben Netz.