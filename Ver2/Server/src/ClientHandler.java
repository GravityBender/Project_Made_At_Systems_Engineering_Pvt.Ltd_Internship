import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {

    private Socket socket;
    private DataOutputStream dout;
    private int size;
    private ArrayList<Number> serverData;

    public ClientHandler(Socket socket, int size, ArrayList<Number> serverData) {
        try {
            this.socket = socket;
            this.dout = new DataOutputStream(this.socket.getOutputStream());
            this.size = size;
            this.serverData = serverData;
        } catch (IOException e) {
            closeLeaks(this.socket, this.dout);
        }
    }

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
            while (true) {
                double y = (double) serverData.get(size);
                // int j = 0;
                Thread.sleep(TimeUnit.MILLISECONDS.toMillis(100));
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
        }
    }
}
