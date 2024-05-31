# UDP-group-chat-application
simple UDP group chat application by java

This repository contains a simple group chat application developed using Java Sockets. The application includes the following features:

## Client Authentication:

Any client can join the chat if it provides the correct password.
The password is known by the server and may be hardcoded by the server administrator.
## Unique Nicknames:

Every client must have a unique nickname to join the chat.
## Message Constraints:

Clients can send messages up to 100 characters long.
## Message Transmission:

Clients send their messages to the server.
The server broadcasts the messages to all other clients.
## Client Exit:

A client can leave the group by sending a request to the server.
Once a client leaves, it will no longer receive chat messages.
## Networking:

The application uses UDP and Multicast sockets for communication.
