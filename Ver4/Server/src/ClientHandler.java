import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

//  Runnable Class responsible for sending data to the client socket
public class ClientHandler implements Runnable {

    private Socket socket;
    private DataOutputStream dout;
    private int size;
    private ArrayList<Number> serverData;

    // Initialize the object
    public ClientHandler(Socket socket, int size, ArrayList<Number> serverData) {
        try {
            this.socket = socket;
            this.dout = new DataOutputStream(this.socket.getOutputStream()); // Initialize the outputstream object
            this.size = 0;
            this.serverData = serverData;
        } catch (IOException e) {
            closeLeaks(this.socket, this.dout);
        }
    }

    // Method responsible for closing all connections in case of an error
    private void closeLeaks(Socket socket, DataOutputStream dStream) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (dStream != null) {
                dStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (size < serverData.size()) {
                double y = (double) serverData.get(size);
                // int j = 0;
                Thread.sleep(TimeUnit.MILLISECONDS.toMillis(50)); // Sleep the thread to ensure the main application
                                                                  // thread is able to add data into the series
                // while (j < 1000) {
                // j++;// Do nothing
                // int k = 0;
                // while (k < 1000) {
                // k++;
                // }
                // }
                dout.writeDouble(y);
                dout.flush();
                size++;
            }
        } catch (IOException | InterruptedException e) {
            // TODO: handle exception
        } finally {
            try {
                dout.close();
            } catch (IOException e) {
                // Do nothing
            }

        }
    }
}
