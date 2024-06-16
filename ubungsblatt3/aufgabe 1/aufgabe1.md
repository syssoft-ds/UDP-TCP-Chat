## Aufgabe 1

Bei der UDP implementierung von Netcat wird nur ein Paket versendet. Das Paket enthält die Nachricht direkt. Erkennbar auch daran, dass ein Wert für Len vorliegt.
![Bild_UDP_Capture](./Screenshot%20(6).png)

Bei der TCP implementierung von Netcat wird erst mithilfe von drei Paketen ohne "data" sichergestellt, dass die Verbindung steht. Anschließend wird ein Paket mit den Daten gesendet. Der Empfänger bestätigt noch das erhalten der Daten, erst dann ist die Interaktion zuende. Bei den nachfolgenden Paketen werden nichtmehr die drei ersten Pakete gesendet, sondern nur noch das Paket mit den Daten und die Bestätigung des Empfängers.  
![Bild_TCP_Capture](./Screenshot%20(5).png)