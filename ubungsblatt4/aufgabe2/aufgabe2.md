## Aufgabe 2

Bei 103.161.122.83/18 steht der 103.161.122.83 Teil für die vier Byte, die die IP Adresse ausmachen. 
Davon sind laut dem /18 Teil die ersten 18 Bit der Netzanteil der Adresse und die übrigen 14 Bit der Hostanteil.

Die Subnetzmaske ist die Bitfolge 11111111111111111100000000000000 die man in die Dezimaldarstellung der ip Adresse umschreiben kann.
Also: 255.255.192.0. Sie ergibt sich aus der /18. 

Bei der Broadcastadresse ist der Hostanteil kompett 1. Also ergibt sich die Adresse 103.161.127.255.

Die Netzwerkadresse, mit dem Hostanteil auf 0 sieht wie folgt aus: 103.161.64.0. Ermittelt wird sie  durch eine konjunktive Verknüpfung mit der Subnetzmaske.

103.161.122.83/18 und 103.161.193.83/18 sind nicht im selben Netz, da die ersten zwei Bit im dritten Byte, die wegen der /18 noch zur Netzadresse gehöhren, nicht identisch sind: bei 122 ergibt das ein 01 und bei 193 ein 11.
122: 01111010
193: 11000001

