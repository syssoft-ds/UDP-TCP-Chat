# Rechnernetze 2024-SS - Übungsblatt 03
`#19D9DA-s4joregn`

## Aufgabe 1 - Wireshark
Die Hauptunterschiede zwischen UDP und TCP liegen in der Art und Weise, wie sie Daten übertragen:

- UDP: Sendet Daten ohne Bestätigung, was bedeutet, dass es keine Garantie dafür gibt, dass alle Daten korrekt und vollständig ankommen. Dies macht es ideal für Echtzeitanwendungen, wo die Latenz wichtig ist.

- TCP: Stellt eine zuverlässige Verbindung her, indem es Bestätigungen für jeden gesendeten Datensatz erfordert. Dies kann zu höherer Latenz führen, aber es garantiert, dass alle Daten korrekt und vollständig übertragen werden.

Durch die Analyse Ihrer PCAP-Dateien mit Wireshark können Sie spezifische Beispiele für diese Unterschiede sehen, wie z.B. die Anzahl der benötigten Pakete für die Übertragung, die Reihenfolge der Pakete und ob Fehlermeldungen auftreten.


## Aufgabe 2: UDP
Nehmen sie nc_udp als Ausgangspunkt, und bauen Sie es zu einem Chatprogramm um. Jede Instanz des
Programms soll einen Namen haben und sich bei einer anderen Instanz registrieren können (also so etwas wie
„Hallo, hier ist Marvin, meine IP-Adresse ist die 192.168.0.42 und du kannst mich unter Port-Nummer 31337
erreichen.“). Anschließend sollen die Instanzen, die sich kennen, über einen Befehl „send name message“ sich
gegenseitig Nachrichten senden können (name für den Ansprechpartner, message für die Nachricht).


## Aufgabe 3: TCP
Verändern Sie nc_tcp ebenfalls zu einem Chatprogramm. Allerdings soll die Registrierung der Instanzen hier
über den Server ablaufen. Nach der Registrierung soll es den Instanzen jedoch auch hier möglich sein, sich
gegenseitig mit „send name message“ Nachrichten zu senden. Beachten Sie hier, dass bei TCP über die
gesamte Dauer des Sendens und Empfangens eine Verbindung bestehen muss.

## Aufgabe 4: Manchester-Code

     During the first half of the bit-symbol, the encoded signal is the logical complement of the bit value being encoded. During the second half of the bit-symbol, the encoded signal is the uncomplemented value of the bit being encoded.
https://einstein.informatik.uni-oldenburg.de/rechnernetze/manchester.htm

### Grafik: `A4-Manchester-Code-1`: 
![A4-Manchester-Code-1.svg](./media/A4-Manchester-Code-1.svg)




## Abgabe
Die Abgabe in Form eines Pull-Requests in das Repository https://github.com/syssoft-ds/UDP-TCP-Chat muß bis zum
5.6.2024 geschehen.