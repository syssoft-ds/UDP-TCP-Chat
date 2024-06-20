[# Exercise 1 - Wireshark
Die Unterschiede zwischen UDP und TCP sind beim Bearbeiten dieser Übung deutlich geworden.
Bei Betrachtung der verschiedenen aufgenommen Pakete in Wireshark, sieht man wie der 3-Way-Hanshake bei TCP abläuft und 
wie die Datenpakete übertragen werden. So sind die ersten drei Pakete in [TCP Protokoll mit einem Client](netcat_tcp_protocol_one_client.pcapng)
die zum 3-Wege-Handshake zunächst SYN, SYN-ACK und ACK. Danach folgen erst die Datenpakete. Beim Beenden der TCP-Verbindung
sieht man wie FIN und ACK gesendet werden, dies sind Pakete 10-13. Bei UDP hingegen gibt es keinen 3-Wege-Handshake, so wird die Verbindung 
einfach geöffnet und genauso geschlossen.
Zusätzlich kommt es bei TCP dazu, dass nach jedem gesendeten Paket ein ACK-Paket zurückgesendet wird, um zu bestätigen, dass das Paket
angekommen ist. Bei UDP hingegen gibt es keine Bestätigung, dass das Paket angekommen ist.

#Exercise 4 - Manchester Encoding
[Solution](exercise4_manchester_coding.png)