# Aufgabe 3

## Idee

Meine Implementierung soll mit der Musterlösung aus ÜB03 kommunizieren

## Bericht

### TCP

Ich starte die Musterlösung als Server, meine Implementierung aus ÜB3 als Client.

![wireshark_screenshot_IPv4_paket](.\TCP1.png)
Server und Client bauen eine TCP-Verbindung auf, wie in Wireshark zu erkennen ist.
Jedoch registriert der Server den Client nicht. 

Dies liegt daran, dass meine Implementierung anders aufgebaut ist und sich nicht wie vom Server erwartet verbindet.
So fehlt bei den Args der name.

### UDP

Auch bei UDP unterscheiden sich die Implementierungen zu stark, um eine erfolgreiche Registrierung zu bewerkstelligen

![wireshark_screenshot_IPv4_paket](.\UDP1.png)
Der Client schickt lediglich ein Paket an den Server

### Inter-Client-Kommunikation

Hier wäre es wichtig sich auf gemeinsame Standards zu einigen bzgl. Aufbau und Interpretation von Nachrichten.




