Die Adresse `103.161.122.83/18` ist eine IP-Adresse im CIDR-Format (Classless Inter-Domain Routing).

- `103.161.122.83` ist die IP-Adresse.
- `18` ist die Subnetzmaske, die angibt, wie viele Bits der IP-Adresse das Netzwerk identifizieren und wie viele das Host innerhalb dieses Netzwerks identifizieren.

Um die Subnetzmaske, die Broadcastadresse und die Netzwerkadresse zu ermitteln, können wir folgende Berechnungen durchführen:

1. Subnetzmaske: Die Subnetzmaske kann durch Umwandlung der CIDR-Notation in eine binäre Darstellung ermittelt werden. Eine CIDR-Notation von 18 bedeutet, dass die ersten 18 Bits der IP-Adresse das Netzwerk identifizieren. Daher wäre die Subnetzmaske `255.255.192.0`.

2. Netzwerkadresse: Die Netzwerkadresse ist die erste Adresse in einem Netzwerk und kann ermittelt werden, indem die Bits, die den Host identifizieren, in der IP-Adresse auf 0 gesetzt werden. In diesem Fall wäre die Netzwerkadresse `103.161.64.0`.

3. Broadcastadresse: Die Broadcastadresse ist die letzte Adresse in einem Netzwerk und kann ermittelt werden, indem die Bits, die den Host identifizieren, in der IP-Adresse auf 1 gesetzt werden. In diesem Fall wäre die Broadcastadresse `103.161.127.255`.

Um zu überprüfen, ob `103.161.122.83/18` und `103.161.193.83/18` im selben Netzwerk liegen, vergleichen wir ihre Netzwerkadressen. Da beide die gleiche CIDR-Notation (18) haben, aber unterschiedliche IP-Adressen, liegen sie nicht im selben Netzwerk.

Ergebnisse:

- Subnetzmaske: `255.255.192.0`
- Netzwerkadresse: `103.161.64.0`
- Broadcastadresse: `103.161.127.255`
- `103.161.122.83/18` und `103.161.193.83/18` liegen nicht im selben Netzwerk.