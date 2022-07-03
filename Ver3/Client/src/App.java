import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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

        chart2.setDataSeries(series);

        Charts chart3 = new Charts(new Axes());
        XYChart.Series<Number, Number> dftSeries = new XYChart.Series<>();
        chart3.setDataSeries(dftSeries);
        populateDataSeriesForChart3(chart2.getDataSeries(), chart3.getDataSeries());

        VBox vBox1 = new VBox();
        vBox1.setSpacing(10);
        vBox1.getChildren().addAll(chart2.getLineChart(), chart3.getLineChart());
        root.setCenter(vBox1);

        Button btn1 = new Button("View Original graph");
        Button btn2 = new Button("View DFT graph");
        Button btn3 = new Button("Proceed");

        HBox hBox1 = new HBox();
        hBox1.setSpacing(10);
        hBox1.setAlignment(Pos.CENTER);
        hBox1.getChildren().addAll(btn1, btn2, btn3);
        root.setBottom(hBox1);

        btn1.setOnAction((e1) -> {
            root.setCenter(null);
            root.setCenter(chart2.getLineChart());
            Button btn4 = new Button("Back");
            hBox1.getChildren().clear();
            hBox1.getChildren().addAll(btn2, btn4);

            btn4.setOnAction((e2) -> {
                thirdScreen();
            });

        });

        btn2.setOnAction((e1) -> {
            root.setCenter(null);
            root.setCenter(chart3.getLineChart());
            Button btn4 = new Button("Back");
            hBox1.getChildren().clear();
            hBox1.getChildren().addAll(btn1, btn4);

            btn4.setOnAction((e2) -> {
                thirdScreen();
            });

        });

        btn3.setOnAction((e) -> {
            fourthScreen();
        });

    }

    private void fourthScreen() {
        root.setCenter(null);
        root.setBottom(null);

        Charts chart4 = new Charts(new Axes());
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        dataSeries.getData().forEach(item -> {
            series.getData().add(new XYChart.Data<Number, Number>(item.getXValue(), item.getYValue()));
        });

        chart4.setDataSeries(series);
        root.setCenter(chart4.getLineChart());

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
        Label funcLabel = new Label("Choose Function: ");
        RadioButton sinRadioBtn = new RadioButton("sin");
        sinRadioBtn.setId("sin");
        RadioButton cosRadioBtn = new RadioButton("cos");
        cosRadioBtn.setId("cos");
        ToggleGroup radioGroup = new ToggleGroup();
        fPane3.getChildren().addAll(funcLabel, sinRadioBtn, cosRadioBtn);

        sinRadioBtn.setToggleGroup(radioGroup);
        cosRadioBtn.setToggleGroup(radioGroup);
        sinRadioBtn.setSelected(true);

        Button sbmBtn = new Button("Plot Graph");

        VBox vBox1 = new VBox();
        vBox1.setSpacing(10);
        vBox1.setAlignment(Pos.CENTER);
        vBox1.getChildren().addAll(fPane1, fPane2, fPane3, sbmBtn);

        Button proceedBtn = new Button("Proceed");

        sbmBtn.setOnAction((e1) -> {

            try {
                int amplitude = Integer.parseInt(ampField.getText().toString());
                int frequency = Integer.parseInt(freqField.getText().toString());

                RadioButton tempBtn = (RadioButton) radioGroup.getSelectedToggle();

                if (tempBtn.getId().equals("sin")) {
                    chart4.getDataSeries().getData().forEach(item -> {
                        double value = (double) item.getYValue();
                        value += (Math.sin(((double) item.getXValue() * 2 * Math.PI) * frequency)) * (amplitude);
                        item.setYValue(value);
                    });
                } else {
                    chart4.getDataSeries().getData().forEach(item -> {
                        double value = (double) item.getYValue();
                        value += (Math.cos(((double) item.getXValue() * 2 * Math.PI) * frequency)) * (amplitude);
                        item.setYValue(value);
                    });
                }

            } catch (NumberFormatException exception) {
                Alert alert = new Alert(AlertType.WARNING, "Invalid Entry!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
            }

            if (!vBox1.getChildren().contains(proceedBtn)) {
                vBox1.getChildren().add(proceedBtn);
            }

            proceedBtn.setOnAction((e2) -> {
                fifthScreen(chart4);
            });
        });

        root.setBottom(vBox1);

    }

    private void fifthScreen(Charts modifiedChart) {
        root.setCenter(null);
        root.setBottom(null);

        Charts chart5 = new Charts(new Axes());
        XYChart.Series<Number, Number> dftSeries = new XYChart.Series<>();
        chart5.setDataSeries(dftSeries);
        populateDataSeriesForChart3(modifiedChart.getDataSeries(), chart5.getDataSeries());

        VBox vBox1 = new VBox();
        vBox1.setSpacing(10);
        vBox1.getChildren().addAll(modifiedChart.getLineChart(), chart5.getLineChart());
        root.setCenter(vBox1);

        Button btn1 = new Button("View Original graph");
        Button btn2 = new Button("View DFT graph");
        Button btn3 = new Button("Back to Main Graph");

        HBox hBox1 = new HBox();
        hBox1.setSpacing(10);
        hBox1.setAlignment(Pos.CENTER);
        hBox1.getChildren().addAll(btn1, btn2, btn3);
        root.setBottom(hBox1);

        btn1.setOnAction((e1) -> {
            root.setCenter(null);
            root.setCenter(modifiedChart.getLineChart());
            hBox1.getChildren().clear();
            hBox1.getChildren().addAll(btn2, btn3);

        });

        btn2.setOnAction((e1) -> {
            root.setCenter(null);
            root.setCenter(chart5.getLineChart());
            hBox1.getChildren().clear();
            hBox1.getChildren().addAll(btn1, btn3);

        });

        btn3.setOnAction((e) -> {
            thirdScreen();
        });
    }

    private void populateDataSeriesForChart3(XYChart.Series<Number, Number> dataSeries1,
            XYChart.Series<Number, Number> dataSeries2) {

        List<Complex> cArrayList = new ArrayList<>();

        dataSeries1.getData().forEach(item -> {
            cArrayList.add(new Complex((double) item.getXValue(),
                    (double) item.getYValue()));
        });

        // int size =(int) convertToNearestPowerOfTwo(dataSeries1.getData().size());
        // cArrayList.subList((int) Math.pow(2, size), cArrayList.size()).clear();

        Complex[] temp = new Complex[cArrayList.size()];
        temp = cArrayList.toArray(temp);

        Complex[] result = DFT.computeDft(temp);

        double j = 0.00;
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i]);
            dataSeries2.getData().add(new XYChart.Data<Number, Number>(j,
                    result[i].getReal()));
            j += 0.01;
        }

    }

    // double convertToNearestPowerOfTwo(int size) {
    // return (Math.floor(Math.log(size) / Math.log(2)));
    // }

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