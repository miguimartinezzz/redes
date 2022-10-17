package es.udc.redes.tutorial.tcp.server;
import java.io.IOException;
import java.net.*;

/** Multithread TCP echo server. */

public class TcpServer {
  public static void main(String argv[]) {
    ServerSocket serversocket = null;
    int port = Integer.parseInt(argv[0]);
    if (argv.length != 1) {
      System.err.println("Format: es.udc.redes.tutorial.tcp.server.TcpServer <port>");
      System.exit(-1);
    }
    try {
      // Create a server socket
      serversocket = new ServerSocket(port);
      // Set a timeout of 300 secs
      serversocket.setSoTimeout(300000);
      while (true) {
        // Wait for connections
        Socket server = serversocket.accept();
        // Create a ServerThread object, with the new connection as parameter
        ServerThread ServerThread = new ServerThread(server);
        // Initiate thread using the start() method
        ServerThread.start();
      }
    // Uncomment next catch clause after implementing the logic
     } catch (SocketTimeoutException e) {
      System.err.println("Nothing received in 300 secs");
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
