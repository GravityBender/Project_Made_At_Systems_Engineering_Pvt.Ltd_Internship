import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class Server implements Runnable {

    private ServerSocket serverSocket;
    private ArrayList<Number> serverData;
    private int size;

    public Server(int size, ArrayList<Number> serverData) {
        try {
            serverSocket = new ServerSocket();
            SocketAddress address = new InetSocketAddress("192.168.43.57", 80);
            serverSocket.bind(address);
            this.serverData = serverData;
            this.size = size - 1;
        } catch (IOException e) {

        }
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler ClientHandler = new ClientHandler(clientSocket, size, serverData);

                Thread clientThread = new Thread(ClientHandler);
                clientThread.run();
            }
        } catch (IOException e) {
            closeServer();
        }
    }

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
