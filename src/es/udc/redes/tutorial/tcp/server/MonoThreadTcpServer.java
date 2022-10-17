package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * MonoThread TCP echo server.
 */
public class MonoThreadTcpServer {


    public static void main(String argv[]) throws IOException {
        ServerSocket serversocket = null;
        int portNumber = Integer.parseInt(argv[0]);
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.MonoThreadTcpServer <port>");
            System.exit(-1);
        }
        try {
            // Create a server socket
            serversocket = new ServerSocket(portNumber);
            // Set a timeout of 300 secs
            serversocket.setSoTimeout(300000);
            
            while (true) {
                // Wait for connections
                Socket server = serversocket.accept();
                // Set the input channel
                BufferedReader input =new BufferedReader(new InputStreamReader(server.getInputStream()));
                // Set the output channel
                PrintWriter output = new PrintWriter(new OutputStreamWriter(server.getOutputStream()),true);
                // Receive the client message
                String message = input.readLine();
                System.out.println("SERVER: Received "+message+" from: "+server.getLocalAddress()+":"+server.getPort());
                // Send response to the client
                output.println(message);
                System.out.println("SERVER: Sending " + message + " to " + server.getInetAddress().toString() + ":" + server.getPort());
                // Close the streams
                input.close();
                output.close();
            }
        // Uncomment next catch clause after implementing the logic            
        } catch (SocketTimeoutException e) {
           System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }  finally {
            //Close the socket
            if(serversocket!=null){
                try {
                    serversocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
