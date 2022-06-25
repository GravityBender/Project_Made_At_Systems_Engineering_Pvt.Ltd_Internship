import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private BorderPane root;
    private double amplitude;
    private double frequency;
    private double size;
    private LineChart<Number, Number> sChart;
    private NumberAxis xAxis;
    private XYChart.Series<Number, Number> dSeries;
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> dataQ;
    private Server server;
    private int seriesData = 0;

    public void init() {
        root = new BorderPane();

        xAxis = new NumberAxis();
        xAxis.setLowerBound(0);
        // xAxis.autoRangingProperty().set(false);
        // xAxis.setForceZeroInRange(false);
        xAxis.autoRangingProperty().set(true);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLowerBound(0);
        yAxis.autoRangingProperty().set(true);

        dSeries = new XYChart.Series<>();
        dSeries.setName("y(t)");

        sChart = new LineChart<>(xAxis, yAxis);
        sChart.getData().add(dSeries);
        sChart.setTitle("Sine Wave");
        sChart.setCreateSymbols(false);

        executor = Executors.newCachedThreadPool();

        dataQ = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void start(Stage window) {
        init();

        firstScreen();

        window.setScene(new Scene(root, 500, 500));
        window.setTitle("Server");
        window.show();
    }

    private void firstScreen() {
        FlowPane fPane1 = new FlowPane();
        fPane1.setAlignment(Pos.CENTER);
        Label ampLabel = new Label("Amplitude: ");
        TextField ampField = new TextField();
        ampField.setPromptText("Amplitude");
        fPane1.getChildren().addAll(ampLabel, ampField);

        FlowPane fPane2 = new FlowPane();
        fPane2.setAlignment(Pos.CENTER);
        Label freqLabel = new Label("Frequency: ");
        TextField freqField = new TextField();
        freqField.setPromptText("Frequency");
        fPane2.getChildren().addAll(freqLabel, freqField);

        FlowPane fPane3 = new FlowPane();
        fPane3.setAlignment(Pos.CENTER);
        Label sizeLabel = new Label("Sample Size: ");
        TextField sizeField = new TextField();
        freqField.setPromptText("Sample Size");
        fPane2.getChildren().addAll(sizeLabel, sizeField);

        Button sbmBtn = new Button("Plot Graph");

        VBox vBox1 = new VBox();
        vBox1.setSpacing(10);
        vBox1.setAlignment(Pos.CENTER);
        vBox1.getChildren().addAll(fPane1, fPane2, fPane3, sbmBtn);

        root.setCenter(vBox1);

        sbmBtn.setOnAction((e) -> {
            if ((ampField.getText().toString() != null) && (freqField.getText().toString() != null) && (sizeField
                    .getText().toString() != null)) {
                amplitude = Double.parseDouble(ampField.getText().toString());
                frequency = Double.parseDouble(freqField.getText().toString());
                size = Double.parseDouble(sizeField.getText().toString());

                System.out.println(amplitude + " " + frequency);

                secondScreen();
            } else {
                Alert alert = new Alert(AlertType.ERROR, "Empty field entered!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
            }
        });
    }

    private void secondScreen() {

        root.setCenter(sChart);

        Button cButton = new Button("Share Chart Data");

        cButton.setOnAction((e) -> {
            seriesData = 0;
            dSeries.getData().clear();
            server = new Server();
            executor.execute(server);
            // addDataToSeries();

            prepareData();
        });

        root.setBottom(cButton);
    }

    private void prepareData() {
        new AnimationTimer() {
            @Override
            public void handle(final long now) {
                addDataToSeries();
            }
        }.start();
    }

    // private void addDataToSeries() {
    // double y;
    // dSeries.getData().clear();
    // for (int i = 0; i < size; i++) {
    // y = ((Math.sin((i * 2 * Math.PI) / frequency) * (amplitude)));
    // System.out.println(i + " " + y);
    // dSeries.getData().add(new XYChart.Data<Number, Number>(i, y));
    // }
    // }

    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) {
            if (dataQ.isEmpty()) {
                break;
            }
            final double dataValue = (double) dataQ.remove();
            seriesData += 1;
            System.out.println(seriesData + " " + dataValue);
            dSeries.getData().add(new XYChart.Data<Number, Number>(seriesData, dataValue));
        }

        // if (dSeries.getData().size() > 1000) {
        // dSeries.getData().remove(0);
        // }

        // xAxis.setLowerBound(seriesData - 1000);
        // xAxis.setUpperBound(seriesData + 1);

    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    private class Server implements Runnable {
        private ServerSocket serverSocket;
        private Socket clientSocket;
        private DataOutputStream dout;
        private int i = 0;

        @Override
        public void run() {
            i = 0;
            startServer();
        }

        // public void startServer() {
        // try {
        // serverSocket = new ServerSocket(6666);
        // clientSocket = serverSocket.accept();

        // dout = new DataOutputStream(clientSocket.getOutputStream());

        // addDataToSeries();

        // int i = 0;
        // while (i < dSeries.getData().size()) {
        // dout.writeDouble((double) (dSeries.getData().get(i).getYValue()));
        // dout.flush();
        // i++;
        // }
        // } catch (IOException e) {

        // } finally {
        // try {
        // dout.close();
        // clientSocket.close();
        // serverSocket.close();
        // } catch (IOException e) {
        // // Do nothing
        // }

        // }
        // } dataQ.add((Math.sin((i * 2 * Math.PI) / frequency) * (amplitude)));

        // public void startServer() {
        // try {
        // serverSocket = new ServerSocket(6666);
        // clientSocket = serverSocket.accept();

        // dout = new DataOutputStream(clientSocket.getOutputStream());

        // addDataToSeries();

        // transferData();

        // } catch (IOException e) {

        // } finally {
        // try {
        // dout.close();
        // clientSocket.close();
        // serverSocket.close();
        // } catch (IOException e) {
        // // Do nothing
        // }

        // }
        // }

        public void startServer() {
            try {
                serverSocket = new ServerSocket();
                SocketAddress address = new InetSocketAddress("192.168.43.105", 6666);
                serverSocket.bind(address);
                clientSocket = serverSocket.accept();

                dout = new DataOutputStream(clientSocket.getOutputStream());

                while (i < size) {
                    double y = (Math.sin((i * 2 * Math.PI) / frequency) * (amplitude));
                    dataQ.add(y);
                    dout.writeDouble(y);
                    dout.flush();
                    i++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    dout.close();
                    clientSocket.close();
                    serverSocket.close();
                } catch (IOException e) {
                    // Do nothing
                }

            }
        }

        public void transferData() {
            System.out.println("Server Here!");
            try {
                int i = 0;
                while (i < dSeries.getData().size()) {
                    dout.writeDouble((double) (dSeries.getData().get(i).getYValue()));
                    dout.flush();
                    i++;
                }
            } catch (IOException e) {

            } finally {
                try {
                    dout.close();
                    clientSocket.close();
                    serverSocket.close();
                } catch (IOException e) {
                    // Do nothing
                }

            }

        }
    }

}
