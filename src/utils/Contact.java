package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Contact{

    private static final ArrayList<Contact> contacts = new ArrayList<>();

    private final InetAddress ip;
    private final int port;
    private final String name;

    public Contact(String name, InetAddress ip, int port){
        this.name=name;
        this.ip=ip;
        this.port=port;
    }

    public static Contact addContact(String name, InetAddress ip, int port) throws UserAlreadyExistsException {

        if(userExists(name, ip.toString().replace("/",""), port)) throw new UserAlreadyExistsException("A user with this name or IP and port combination already exists");
        Contact newContact = new Contact(name, ip, port);
        contacts.add(newContact);
        System.out.println("Contact added. Name: " + name + "\tIP: " + ip + "\tPort: " + port);
        return newContact;
    }

    public static Contact addContact(String msg) throws UnknownHostException, NumberFormatException, UserAlreadyExistsException {
        String[] splitData = msg.split(" ",3);

        if(splitData.length != 3) throw new IllegalArgumentException();

        String name = splitData[0];
        String ip = splitData[1];
        int port = Integer.parseInt(splitData[2]);

        if(userExists(name, ip, port)) throw new UserAlreadyExistsException("A user with this name or IP and port combination already exists");

        if (!isValidIP(ip)) throw new UnknownHostException("No valid IP-Address: " + ip);

        InetAddress contactIP = InetAddress.getByName(ip);

        if(port<1 || port>65535) throw new NumberFormatException("Port number must be between 1 and 65535.");

        Contact newContact = new Contact(name, contactIP, port);
        contacts.add(newContact);
        System.out.println("Contact added. Name: " + name + "\tIP: " + contactIP + "\tPort: " + port);

        return newContact;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public static ArrayList<Contact> getContacts(){
        return contacts;
    }

    public static boolean userExists(String name, String ip, int port){
        if(contacts.stream().anyMatch(c -> c.getName().equals(name))) return true;
        if(contacts.stream().anyMatch(c -> c.getIp().toString().equals("/"+ip)
                && contacts.stream().anyMatch(c2 -> c2.getPort() == port))) return true;
        return false;
    }

    public static Contact getContactByName(String name) throws NoSuchElementException{
        return contacts.stream().filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No contact found with name: " + name));
    }

    @Override
    public String toString() {
        return "Name: " + this.name + "\t IP: " + this.ip + "\t Port: " + this.port;
    }

    public static boolean isValidIP(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

}
