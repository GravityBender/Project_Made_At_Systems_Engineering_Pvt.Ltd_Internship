import java.util.ArrayList;
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
    private LineChart<Number, Number> sChart;
    private NumberAxis xAxis;
    private XYChart.Series<Number, Number> dSeries;
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> dataQ;
    private AddToQueue addToQueue;
    private Server server;
    private int seriesData = 0;
    private Thread dataThread;
    private ArrayList<Number> serverData;
    // private int qSize = 0;

    public void init() {
        root = new BorderPane();

        xAxis = new NumberAxis();
        xAxis.setLowerBound(0);
        xAxis.autoRangingProperty().set(false);
        xAxis.setForceZeroInRange(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLowerBound(0);
        yAxis.autoRangingProperty().set(true);

        dSeries = new XYChart.Series<>();
        dSeries.setName("y(t)");

        sChart = new LineChart<>(xAxis, yAxis);
        sChart.getData().add(dSeries);
        sChart.setTitle("Sine Wave");
        sChart.setCreateSymbols(false);
        sChart.setAnimated(false);

        executor = Executors.newCachedThreadPool();

        dataQ = new ConcurrentLinkedQueue<>();
        serverData = new ArrayList<>();
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

        Button sbmBtn = new Button("Plot Graph");

        VBox vBox1 = new VBox();
        vBox1.setSpacing(10);
        vBox1.setAlignment(Pos.CENTER);
        vBox1.getChildren().addAll(fPane1, fPane2, sbmBtn);

        root.setCenter(vBox1);

        sbmBtn.setOnAction((e) -> {

            if ((ampField.getText().toString() != null) && (freqField.getText().toString() != null)) {
                amplitude = Double.parseDouble(ampField.getText().toString());
                frequency = Double.parseDouble(freqField.getText().toString());

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
        addToQueue = new AddToQueue();

        dataThread = new Thread(addToQueue);
        dataThread.start();
        prepareData(true);
        server = new Server(serverData.size(), serverData);
        executor.execute(server);

        Button spButton = new Button("Stop Plotting");
        spButton.setOnAction((e) -> {
            dataThread.interrupt();
            prepareData(false);
        });

        root.setBottom(spButton);

    }

    private AnimationTimer animationTimer = new AnimationTimer() {
        @Override
        public void handle(final long now) {
            addDataToSeries();
        }
    };

    private void prepareData(boolean flag) {

        if (flag) {
            animationTimer.start();
        } else {
            animationTimer.stop();
        }
    }

    private void addDataToSeries() {
        for (int i = 0; i < 30; i++) {
            if (dataQ.isEmpty()) {
                break;
            }
            final double dataValue = (double) dataQ.remove();
            seriesData += 1;
            System.out.println(seriesData + " " + dataValue);
            dSeries.getData().add(new XYChart.Data<Number, Number>(seriesData,
                    dataValue));
        }

        if (seriesData > frequency * 3) {
            dSeries.getData().remove(0);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO: handle exception
            }
        }

        xAxis.setLowerBound(seriesData - frequency);
        xAxis.setUpperBound(seriesData - 1);

    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    private class AddToQueue implements Runnable {

        private int i = 0;

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    double y = (Math.sin((i++ * 2 * Math.PI) / frequency)) * (amplitude);
                    dataQ.add(y);
                    serverData.add(y);
                    // qSize = dataQ.size();
                    // System.out.println(y);
                    Thread.sleep(50);
                }
                // executor.execute(this);
                // Thread.currentThread().start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Do nothing
            }
        }
    }
}
