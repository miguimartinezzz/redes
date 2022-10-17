package es.udc.redes.tutorial.udp.server;

import java.io.IOException;
import java.net.*;

/**
 * Implements a UDP echo server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }
        int portNumber = Integer.parseInt(argv[0]);
        DatagramSocket datagramSocket = null;
        try {
            // Create a server socket
            datagramSocket = new DatagramSocket(portNumber);

            // Set maximum timeout to 300 secs
            datagramSocket.setSoTimeout(300000);
            while (true) {
                // Prepare datagram for reception
                byte[] buffer = new byte[1024];
                DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);
                // Receive the message
                datagramSocket.receive(rPacket);
                System.out.println("SERVER: Received "
                        + new String(rPacket.getData(),0,rPacket.getLength()) + " to "
                        + rPacket.getAddress().toString() + ":"
                        + rPacket.getPort());

                // Prepare datagram to send response
                String received = new String(rPacket.getData(),0,rPacket.getLength());
                DatagramPacket response = new DatagramPacket(received.getBytes(), received.length(), rPacket.getAddress(), rPacket.getPort());
                // Send response
                datagramSocket.send(response);
                System.out.println("CLIENT: Sending "
                        + new String(response.getData(),0,response.getLength()) + " to "
                        + response.getAddress().toString() + ":"
                        + response.getPort());
            }

            // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }  finally {
            //Close the socket
                    datagramSocket.close();

        }
    }
}
