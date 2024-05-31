
/*
ChatClient Algorithm 
1-Initialize Client
Import necessary networking, I/O, and utility packages.
Define client properties.
Create sockets for communication.

2-Connect to Server
join the multicast group using MulticastSocket to receive broadcast messages.

3-User Input Loop
Prompt the user for input using a Scanner.
Parse the user input to recognize and format commands.

4-Send Messages to Server
For user commands, send the formatted message or command to the server using DatagramSocket.

5-Receive and Display Messages
If joined, listen for messages from the server or the multicast group.
Display any received messages to the user.

6-Execute Commands
Implement logic for joining, messaging, and leaving the chat based on user commands and server responses.

7-Client Exit
Close the sockets (DatagramSocket and MulticastSocket) upon exiting.
Perform cleanup as necessary.
 */


import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final int SERVER_PORT = 6789;
    private static final int GROUP_PORT = 6790;
    private static final String SERVER_IP = "localhost";
    private static DatagramSocket socket;
    private static MulticastSocket multicastSocket;
    private static InetAddress groupAddress;
    private static boolean joined = false;
    private static String nickname; // global nickname variable

    public static void main(String[] args) throws IOException {
        socket = new DatagramSocket();
        groupAddress = InetAddress.getByName("230.0.0.1");
        multicastSocket = new MulticastSocket(GROUP_PORT);
        multicastSocket.joinGroup(groupAddress);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        sendToServer("PASS:" + password);

        new Thread(ChatClient::listenToServer).start();
        new Thread(ChatClient::listenToGroup).start();

        while (!joined) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting to join.");
                return;
            }
        }

        while (joined) {
            if(scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                if ("exit".equalsIgnoreCase(msg)) {
                    leaveChat();
                    break;
                } else if (msg.length() <= 100) {
                    sendToServer(nickname + ": " + msg); // Prepend nickname to message
                } else {
                    System.out.println("Message too long. Maximum 100 characters allowed.");
                }
            }
        }
    }

    private static void sendToServer(String message) throws IOException {
        byte[] buf = message.getBytes();
        InetAddress address = InetAddress.getByName(SERVER_IP);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, SERVER_PORT);
        socket.send(packet);
    }

    private static void listenToServer() {
        try {
            byte[] buf = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());

                if ("PASSWORD_OK".equals(received)) {
                    System.out.print("Enter your nickname: ");
                    nickname = new Scanner(System.in).nextLine(); // Store the nickname
                    sendToServer("NICK:" + nickname);
                } else if ("ERROR:Incorrect Password".equals(received) || "ERROR:Nickname already taken".equals(received)) {
                    System.out.println(received);
                    System.exit(0);
                } else if ("NICKNAME_OK".equals(received)) {
                    joined = true;
                    System.out.println("You have joined the chat. You can now send messages. Type 'exit' to leave the chat.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listenToGroup() {
        try {
            byte[] buf = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if (joined) {
                    System.out.println(received); // print message
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void leaveChat() throws IOException {
        joined = false;
        sendToServer("LEAVE");
        multicastSocket.leaveGroup(groupAddress);
        socket.close();
        multicastSocket.close();
    }
}
