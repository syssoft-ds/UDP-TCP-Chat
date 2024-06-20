# UDP-TCP-Chat

Machen Sie sich mit der Implementation der UDP- und TCP-Programme vertraut, die in der ersten Vorlesung vorgestellt wurden (die Klassen nc_udp und nc_tcp, auch zu finden im Repository https://github.com/syssoft-ds/netcat). Nutzen Sie Generische KI, um sich Codeabschnitte, die Ihnen unklar sind, erklären zu lassen. Implementieren Sie die beiden Programme und probieren Sie sie aus. Überlegen Sie sich, wie Sie damit Nachrichten schicken und empfangen können. 
Nutzen Sie die Capture-Funktion von Wireshark, um Ihren Traffic aufzuzeichnen, während die Programme laufen (auch im Rahmen der nächsten beiden Übungen). Vergleichen Sie die Aufzeichnung des TCP-Programms, mit der Aufzeichnung des UDP-Programms. 
Erläutern Sie die Unterschiede und Gemeinsamkeiten der Funktionsweisen der beiden Programme. Nehmen Sie dabei Bezug auf konkrete Pakete in Ihren beiden PCAP-Dateien. 

Nehmen sie nc_udp als Ausgangspunkt, und bauen Sie es zu einem Chatprogramm um. Jede Instanz des Programms soll einen Namen haben und sich bei einer anderen Instanz registrieren können (also so etwas wie „Hallo, hier ist Marvin, meine IP-Adresse ist die 192.168.0.42 und du kannst mich unter Port-Nummer 31337 erreichen.“). Anschließend sollen die Instanzen, die sich kennen, über einen Befehl „send name message“ sich gegenseitig Nachrichten senden können (name für den Ansprechpartner, message für die Nachricht). 

Verändern Sie nc_tcp ebenfalls zu einem Chatprogramm. Allerdings soll die Registrierung der Instanzen hier über den Server ablaufen. Nach der Registrierung soll es den Instanzen jedoch auch hier möglich sein, sich gegenseitig mit „send name message“ Nachrichten zu senden. Beachten Sie hier, dass bei TCP über die gesamte Dauer des Sendens und Empfangens eine Verbindung bestehen muss. 

# Bedienungsanleitung

Bei programstart öffnet sich ein Fenster, hier kann man:
  a) Einen Namen und Port für den Chatteilnemer festlegen
  b) Den Server starten. Hierfür kann das Namensfeld leer gelassen werden.


Jeder Chatclient muss sich über den "Connect" Button im File menu mit einem Server verbinden.
  - Hier wird einmal die IP des Servers abgefragt. (Leerlassen = localhost)
  - Und es wir der Port des Servers abgefragt.
