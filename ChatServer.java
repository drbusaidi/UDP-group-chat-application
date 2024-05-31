/*
 * ChatServer Algorithm Steps
1-Start Server
-Import necessary networking and I/O packages.
-Initialize server properties: server port, multicast group port, and password.
-Create a HashSet to store unique nicknames.
-Initialize a DatagramSocket for communication.

2-Listen for Client Packets
-Bind the DatagramSocket to the server port to receive packets.
-Enter a loop to continuously listen for incoming datagram packets.

3-Process Packets
-Upon receiving a packet, determine its type 
-For join requests, validate nickname uniqueness and send acknowledgment.
-For messages, broadcast them to all clients or to specific clients based on the implementation.
-Handle server-specific commands (e.g., list of active users).

4-Client Management
-Add new clients to the nickname list upon successful joining.
-Remove clients from the list when they leave or disconnect.

5-Shutdown
-Close the DatagramSocket when the server is to be shut down.
-Perform any necessary cleanup actions.

 */


import java.io.*;
import java.net.*;
import java.util.HashSet;

public class ChatServer {
    private static final int PORT = 6789;
    private static final String PASSWORD = "pass"; // set password 
    private static final HashSet<String> nicknames = new HashSet<>();
    private static DatagramSocket socket;
    private static InetAddress groupAddress;
    private static final int GROUP_PORT = 6790;

    public static void main(String[] args) throws IOException {
        socket = new DatagramSocket(PORT);
        groupAddress = InetAddress.getByName("230.0.0.1"); // multicast group address

        System.out.println("Server is running...");

        byte[] buf = new byte[256];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            String received = new String(packet.getData(), 0, packet.getLength());
            if (received.startsWith("PASS:")) {
                handlePassword(received.substring(5), packet.getAddress(), packet.getPort());
            } else if (received.startsWith("NICK:")) {
                handleNickname(received.substring(5), packet.getAddress(), packet.getPort());
            } else {
                broadcastMessage(received);
            }
        }
    }

    private static void handlePassword(String password, InetAddress address, int port) throws IOException {
        if (PASSWORD.equals(password)) {
            sendToClient("PASSWORD_OK", address, port);
        } else {
            sendToClient("ERROR:Incorrect Password", address, port);
        }
    }

    private static void handleNickname(String nickname, InetAddress address, int port) throws IOException {
        if (nicknames.add(nickname)) {
            sendToClient("NICKNAME_OK", address, port);
            broadcastMessage(nickname + " joined the chat");
        } else {
            sendToClient("ERROR:Nickname already taken", address, port);
        }
    }

    private static void sendToClient(String message, InetAddress address, int port) throws IOException {
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
    }

    private static void broadcastMessage(String message) throws IOException {
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, groupAddress, GROUP_PORT);
        socket.send(packet);
    }
}
