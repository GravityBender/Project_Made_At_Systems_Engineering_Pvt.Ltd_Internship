import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

//  Runnable Class responsible for executing the server code
public class Server implements Runnable {

    private ServerSocket serverSocket;
    private ArrayList<Number> serverData;
    private int size;

    // Initialize the server object
    public Server(int size, ArrayList<Number> serverData) {
        try {
            serverSocket = new ServerSocket(); // Initialize the server socket
            SocketAddress address = new InetSocketAddress("localhost", 6666); // Assign a valid address to the server
                                                                              // socket
            serverSocket.bind(address); // Bind the address to the socket
            this.serverData = serverData;
            this.size = size - 1;
        } catch (IOException e) {

        }
    }

    // Execute the server code
    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept(); // Accept incoming request from the client
                ClientHandler ClientHandler = new ClientHandler(clientSocket, size, serverData);

                Thread clientThread = new Thread(ClientHandler); // Run the clientHandler object responsible for sending
                                                                 // the data on another thread
                clientThread.run();
            }
        } catch (IOException e) {
            closeServer();
        }
    }

    // Method responsible for closing the server connection
    private void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
