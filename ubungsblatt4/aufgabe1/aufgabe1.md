## Aufgabe 1

### IPv4

![wireshark_screenshot_IPv4_paket](./Screenshot%20(9).png)

Im Header von einem IPv4 Paket sind die folgenden Elemente enthalten:  
- Version (erste Hälfte des ersten Byte) : hat hier den Dezimalwert 4, da es ein IPv4 Paket ist
- Header Length (zweite Hälfte des ersten Byte) : hat hier den Binärwert 0101, was für eine Länge von 20 Byte steht
- Type of Service (das zweite Byte) : hat hier den Hexadezimalwert 0x00 (wird nicht verwendet)
- Total Length (das dritte und vierte Byte) : hat hier den Dezimalwert 52
- Identification (das fünfte und sechste Byte) : hat hier den Hexadezimalwert 0x348b
- Fragmentaion (das siebte und achte Byte) : ersten 3 Bit sind Flags, die restlichen Bit geben das offset, bei Fragmentierung, an. Die Flags sind wie folgt gesetzt:
    - erst ein reserved bit, hier 0
    - dann das Bit für "don't fragment", hier 1
    - dann das Bit für "more fragments", hier 0  
    - die restlichen Bit sind 0
- Time to Live (das neunte Byte) : hat hier den Dezimalwert 60
- Protocol (das zehnte Byte) : hat hier den Dezimalwert 6, was für TCP steht
- Header Checksum (das elfte und zwölfte Byte) : hat hier den Hexadezimalwert 0x3ec6
- Source Adress (das 13. bis 16. Byte) 
- Destination Adress (das 17. bis 20. Byte)

### TCP

![wireshark_screenshot_TCP_paket](./Screenshot%20(10).png)

Im Header von einem TCP Paket sind folgende Elemente enthalten:  
- Source Port (die ersten zwei Byte) : hat hier den Dezimalwert 49959
- Destination Port (das dritte und vierte Byte) : hat hier den Dezimalwert 443
- Sequence Number (das fünfte bis achte Byte) : der Wert hier ist einmal relativ zu den anderen Paketen in der Folge 1 und "raw" Hexadezimal 0x 9f177c71 
- Acknowlegement Number (das neunte bis zwölfte Byte) : der Wert ist auch hier einmal relativ 1 und "raw" Hexadezimal 0xa8286ff4
- Header Length (die ersten 4 Bit des 13. Byte) : hat hier den Binärwert 0101
- Flags (das 14. Byte) : habe hier den Binärwert 0001000 (Damit ist nur die Acknowlegement Flag gesetzt)
- Window (das 15. und 16. Byte) : hat hier den Dezimalwert 514
- Checksum (das 17. und 18. Byte) : hat hier den Hexadezimalwert 0x373d
- Urgent Pointer (das 19. und 20. Byte) : hat hier den Hexadezimalwert 0x0000

### UDP

![wireshark_screenshot_UDP_paket](./Screenshot%20(11).png)

Im Header von einem UDP Paket sind folgende Elemente enthalten:
- Source Port (die ersten zwei Byte) : hat hier den Dezimalwert 57621
- Destination Port (das dritte und vierte Byte) : hat hier den Dezimalwert 57621
- Length (das fünfte und sechte Byte) : hat hier den Dezimalwert 48
- Checksum (das siebte und achte Byte) : hat hier den Hexadezimalwert 0x1868