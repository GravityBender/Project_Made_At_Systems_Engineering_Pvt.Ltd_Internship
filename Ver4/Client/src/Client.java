import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.scene.control.Button;

//  Class respnsible for connecting to the server and recieve data
public class Client implements Runnable {

    private Socket clientSocket;
    private String ip;
    private int port;
    private DataInputStream din;
    private ConcurrentLinkedQueue<Number> dataQ; // Queue resposible for storing data recieved from the server
    private Button btn;

    public Client() {

    }

    // Initialise the data members
    public Client(String ip, int port, ConcurrentLinkedQueue<Number> dataQ) {
        this.dataQ = dataQ;
        this.ip = ip;
        this.port = port;
    }

    public Client(String ip, int port, ConcurrentLinkedQueue<Number> dataQ, Button proceedBtn) {
        this.dataQ = dataQ;
        this.ip = ip;
        this.port = port;
        this.btn = proceedBtn;
        btn.setDisable(true);
    }

    // Run the client and read data from the server
    @Override
    public void run() {
        try {
            din = new DataInputStream(clientSocket.getInputStream());
            while (true) {
                double data = din.readDouble();
                dataQ.add(data);
            }
        } catch (IOException e) {
            System.out.println("End of File reached!");
        } finally {
            try {
                din.close();
                clientSocket.close();
                btn.setDisable(false);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void openSocket() throws UnknownHostException, IOException {
        clientSocket = new Socket(ip, port);
    }

    // Method to check if client socket has been closed or not
    public boolean checkIfClosed() {
        return clientSocket.isClosed();
    }

    // Method to check if client socket is still connected to the server or not
    public boolean checkIfConnected() {
        return clientSocket.isConnected();
    }

    public ConcurrentLinkedQueue<Number> getDataQ() {
        return dataQ;
    }

}
