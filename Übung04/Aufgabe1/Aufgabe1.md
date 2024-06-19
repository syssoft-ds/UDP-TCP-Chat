# Aufgabe 1

## IPv4

![wireshark_screenshot_IPv4_paket](.\IP4packet1.png)

Header besteht aus:

1. Byte: '45'
    Version: '4' steht für IPv4
    Header length: '5' steht für Headerlänge von 20 bytes
2. Byte '00'
    Type of Service: '00' für Default
3. - 4. Byte '0028' (entspricht 40 in Dezimal)
    Total Length: 40 Bytes
5. - 6. Byte 'baf8'
    Identification: 47864
7. - 8. Byte '40'
    Fragmentation
9. Byte '37' (55)
    Time To Live: 55
10. Byte '06'
    Protocol: '06' steht für TCP
11. - 12. Byte '10c4'
    Header Checksum '10c4'
13. - 16. Byte
    Source address
17. - 20. Byte
    Destination address

## TCP

![wireshark_screenshot_IPv4_paket](.\TCP1.png)

Header besteht aus:

Source Port: 55822
Destination Port: 443
Sequence Number: 4916
Acknowledgment Number: 1188
Header Length: 20 bytes (5)
Flags: 0x010 (ACK), hier nur ACK Flag gesetzt:
    .... ...1 .... = Acknowledgment: Set
Window: 1028
Checksum: 0x1418
Urgent Pointer: 0

## UDP

![wireshark_screenshot_IPv4_paket](.\UDP1.png)

Version: 4 (IPv4)
Header Length: 20 bytes (5)
Differentiated Services Field: 0x00 (DSCP: CS0, ECN: Not-ECT)
Total Length: 353 (0x0161)
Identification: 0xf4fb (62715)
Flags: 0x0, keine Flags gesetzt
Time to Live: 64
Protocol: UDP (17)
Header Checksum: 0x9f28 [validation disabled]
Source Address: 192.168.178.1
Destination Address: 192.168.178.21







