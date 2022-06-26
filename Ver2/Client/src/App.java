import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private BorderPane root;
    private ExecutorService executor;
    private Client client;
    private String ip;
    private int port;
    private Charts chart1;
    private Socket clientSocket;
    private ConcurrentLinkedQueue<Number> dataQ;
    private int seriesData = 0;

    public void init() {
        root = new BorderPane();
        executor = Executors.newCachedThreadPool();
        dataQ = new ConcurrentLinkedQueue<>();
        chart1 = new Charts();

        chart1.getxAxis().setLabel("Time");
        chart1.getyAxis().setLabel("Amplitude");
        chart1.getMainDataSeries().setName("y(t)");
        chart1.getMainLineChart().setTitle("Sine Wave");

    }

    @Override
    public void start(Stage window) {
        init();

        firstScreen();

        window.setScene(new Scene(root, 500, 600));
        window.setTitle("Application");
        window.show();
    }

    private void firstScreen() {
        FlowPane fPane1 = new FlowPane();

        Label ipLabel = new Label("I.P. Address of Server:");
        TextField ipField = new TextField();

        fPane1.getChildren().addAll(ipLabel, ipField);
        fPane1.setAlignment(Pos.CENTER);

        FlowPane fPane2 = new FlowPane();

        Label portLabel = new Label("Port of Server:");
        TextField portField = new TextField();

        fPane2.getChildren().addAll(portLabel, portField);
        fPane2.setAlignment(Pos.CENTER);

        VBox vBox1 = new VBox();

        Button checkButton = new Button("Connect to Server");

        vBox1.getChildren().addAll(fPane1, fPane2, checkButton);
        vBox1.setAlignment(Pos.CENTER);
        vBox1.setSpacing(10.0);

        root.setCenter(vBox1);
        root.setBottom(null);

        checkButton.setOnAction((event) -> {

            boolean ipFlag = true, portFlag = true;

            if (ipField.getText().toString() != "") {
                ip = ipField.getText().toString();
                ipFlag = true;
            } else {
                Alert alert = new Alert(AlertType.ERROR, "IP Field left blank!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
                ipFlag = false;
            }
            try {
                port = Integer.parseInt(portField.getText().toString());
                portFlag = true;
            } catch (NumberFormatException e) {
                Alert alert = new Alert(AlertType.ERROR, "Not a valid port number!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
                portFlag = false;
            }

            if (ipFlag && portFlag) {
                if (verifyServer()) {
                    // Button proceedBtn = new Button("Proceed");
                    // vBox1.getChildren().add(proceedBtn);
                    // proceedBtn.setOnAction((e) -> {
                    // secondscreen();
                    // });
                    secondscreen();
                }
            }

            // try {
            // if (portFlag && ipFlag) {
            // try {
            // clientSocket = new Socket(ip, port);

            // if (!clientSocket.isConnected()) {
            // Alert alert = new Alert(AlertType.WARNING, "Server Not Reachable!",
            // ButtonType.CLOSE, ButtonType.OK);
            // alert.show();
            // } else {
            // client = new Client();
            // executor.execute(client);
            // Button proceedBtn = new Button("Proceed");

            // }
            // } catch (SocketException e) {
            // Alert alert = new Alert(AlertType.WARNING, "Network Not Reachable!",
            // ButtonType.CLOSE, ButtonType.OK);
            // alert.show();
            // }
            // }

            // } catch (IOException e1) {
            // // TODO Auto-generated catch block
            // e1.printStackTrace();
            // }

        });

    }

    private boolean verifyServer() {
        try {

            clientSocket = new Socket(ip, port);

            if (!clientSocket.isConnected()) {
                Alert alert = new Alert(AlertType.WARNING, "Server Not Reachable!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
            } else {
                client = new Client();
                executor.execute(client);
                return true;
            }

        } catch (SocketException e) {
            Alert alert = new Alert(AlertType.WARNING, "Network Not Reachable!",
                    ButtonType.CLOSE, ButtonType.OK);
            alert.show();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    private void secondscreen() {
        root.setCenter(null);

        root.setCenter(chart1.getMainLineChart());
        chart1.getMainDataSeries().getData().clear();
        seriesData = 0;
        prepareData();

        HBox hBox1 = new HBox();
        hBox1.setSpacing(10.0);
        hBox1.setAlignment(Pos.CENTER);

        Button proceedBtn = new Button("Proceed");
        Button backBtn = new Button("Back");

        hBox1.getChildren().addAll(proceedBtn, backBtn);

        proceedBtn.setOnAction((e) -> {
            thirdScreen();
        });

        backBtn.setOnAction((e) -> {
            firstScreen();
        });

        root.setBottom(hBox1);
    }

    private void thirdScreen() {
        VBox vBox1 = new VBox();
        vBox1.setSpacing(10.0);
        vBox1.setAlignment(Pos.CENTER);

        Charts chart2 = new Charts();

        // chart2.getMainDataSeries().setData(chart1.getMainDataSeries().getData());
        chart2.animateChart(chart1.getMainDataSeries().getData());
        chart2.getxAxis().setLabel("Time");
        chart2.getxAxis().setLowerBound(0);
        chart2.getxAxis().setAutoRanging(false);
        chart2.getyAxis().setLabel("Amplitude");
        chart2.getyAxis().setAutoRanging(true);
        chart2.getyAxis().setLowerBound(0);
        chart2.getMainDataSeries().setName("y(t)");
        chart2.getMainLineChart().setTitle("Sine Wave");

        vBox1.getChildren().addAll(chart1.getMainLineChart(), chart2.getMainLineChart());

        root.setCenter(vBox1);
    }

    private void prepareData() {
        new AnimationTimer() {
            @Override
            public void handle(final long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) {
            if (dataQ.isEmpty()) {
                break;
            }
            final double dataValue = (double) dataQ.remove();

            System.out.println(seriesData + " " + dataValue);
            chart1.getMainDataSeries().getData().add(new XYChart.Data<Number, Number>(seriesData, dataValue));
            seriesData += 1;
        }

    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    private class Client implements Runnable {

        // private Socket clientSocket;
        private DataInputStream din;

        // @Override
        // public void run() {
        // try {
        // int i = 0;
        // din = new DataInputStream(clientSocket.getInputStream());

        // while (true) {
        // try {
        // // System.out.println(din.readDouble());
        // chart1.getMainDataSeries().getData().add(new XYChart.Data<Number, Number>(i,
        // din.readDouble()));
        // i++;
        // } catch (EOFException e) {
        // System.out.println("End of File reached!");
        // break;
        // }
        // }
        // } catch (IOException e) {

        // }
        // }

        @Override
        public void run() {
            try {
                int i = 0;
                din = new DataInputStream(clientSocket.getInputStream());

                while (true) {
                    try {

                        dataQ.add(din.readDouble());
                    } catch (EOFException e) {
                        System.out.println("End of File reached!");
                        break;
                    }
                }
            } catch (IOException e) {

            }
        }

    }

}