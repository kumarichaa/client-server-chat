### TCP/UDP CHAT APPLICATION

## Description

This project is a simple client-server chat application that supports both TCP and UDP protocols. It allows multiple clients to connect and exchange messages in real-time.

## Features

- Supports both TCP and UDP communication
- Multiple clients can join the chat
- Real-time message broadcasting
- Simple GUI chat window for enhanced user experience
- Java and Swing 


### Application demo
    ### TCPServer
    $ java -cp ".:/path/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar" TCPServer.java 
![TCPServer](./image/TCPServer_Start.png)

    ###TCPClient
    $ java /path/TCPClientGUI.java
![TCPClient_connected](./image/TCPClient-Connected.png)
![TCPClient](./image/TCPClientGUI.png)

![database_details](./image/database_details.png)
![Disconnected_message](./image/Disconnected_message.png)


    ### UDPServer
    $ java /path/UDPServer.java
![UDPServer](./image/UDPServer_start.png)

    ### UDPClient
    $ java /path/UDPClientGUI.java
![UDPClient](./image/UDPClientGUI.png)

