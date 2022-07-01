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

/**
 * App
 */
public class App extends Application {

    private BorderPane root;
    private Client client;
    private ConcurrentLinkedQueue<Number> dataQ;
    private ExecutorService executor;
    private double xValue = 0.00;
    XYChart.Series<Number, Number> dataSeries;
    private Button proceedBtn;

    public void init() {
        root = new BorderPane();
        dataQ = new ConcurrentLinkedQueue<>();
        executor = Executors.newCachedThreadPool();
        dataSeries = new XYChart.Series<>();
        proceedBtn = new Button("Proceed");
        proceedBtn.setDisable(true);
    }

    @Override
    public void start(Stage window) {

        init();

        firstScreen();

        Scene scene = new Scene(root, 500, 400);
        window.setScene(scene);
        window.setTitle("Plot");
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
            String ip = "";
            int port = 0;

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
                if (verifyServer(ip, port)) {
                    secondscreen();
                }
            }
        });

    }

    private boolean verifyServer(String ip, int port) {
        try {

            client = new Client(ip, port, dataQ, proceedBtn);
            client.openSocket();

            if (!client.checkIfConnected()) {
                Alert alert = new Alert(AlertType.WARNING, "Server Not Reachable!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
            } else {
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

        Axes axes = new Axes();
        Charts chart1 = new Charts(axes);
        chart1.setDataSeries(dataSeries);
        root.setCenter(chart1.getLineChart());
        prepareData();

        HBox hBox1 = new HBox();
        hBox1.getChildren().add(proceedBtn);
        hBox1.setAlignment(Pos.CENTER);
        root.setBottom(hBox1);

        proceedBtn.setOnAction((e) -> {
            thirdScreen();
        });
    }

    private void thirdScreen() {
        root.setCenter(null);
        root.setBottom(null);

        Charts chart2 = new Charts(new Axes());
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        dataSeries.getData().forEach(item -> {
            series.getData().add(item);
        });

        series.getData().forEach(item -> {
            double xValue = (double) item.getXValue();
            double yValue = (double) item.getYValue();
            item.setXValue(yValue * Math.cos(-2 * Math.PI * 100 * xValue));
            item.setYValue(yValue * Math.sin(-2 * Math.PI * 100 * xValue));
        });

        chart2.setDataSeries(series);
        root.setCenter(chart2.getLineChart());
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
            // System.out.println(dataValue);
            dataSeries.getData()
                    .add(new XYChart.Data<Number, Number>(xValue, dataValue));
            xValue += 0.01;
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}