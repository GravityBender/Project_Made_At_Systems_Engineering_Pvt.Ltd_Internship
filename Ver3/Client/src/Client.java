import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.scene.control.Button;

public class Client implements Runnable {

    private Socket clientSocket;
    private String ip;
    private int port;
    private DataInputStream din;
    private ConcurrentLinkedQueue<Number> dataQ;
    private Button btn;

    public Client() {

    }

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

    public boolean checkIfClosed() {
        return clientSocket.isClosed();
    }

    public boolean checkIfConnected() {
        return clientSocket.isConnected();
    }

    public ConcurrentLinkedQueue<Number> getDataQ() {
        return dataQ;
    }

}
