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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private BorderPane root; // BorderPane to display all the content
    private double amplitude; // To store the amplitude
    private double frequency; // To store the frequency
    private double samplingFreq; // To store the sampling rate
    private LineChart<Number, Number> sChart; // Object for the chart
    private NumberAxis xAxis; // Object for the x-Axis
    private XYChart.Series<Number, Number> dSeries; // Object to store the data series
    private XYChart.Series<Number, Number> tempSeries; // Object to store the data series
    private ExecutorService executor; // Executor for executing the Server code
    private ConcurrentLinkedQueue<Number> dataQ; // Thread safe Queue in which data is added
    private AddToQueue addToQueue; // Private inner class object for adding data into dataQ object
    private Server server; // Object responsible for the Server
    private double seriesData = 0.00; // Flag variable used as index for adding data
    private Thread dataThread; // Thread responsible for animating the graph plotting action
    private ArrayList<Number> serverData; // Arraylist to store the chart y-Axis data
    // private int qSize = 0;

    // Method responsible for initializing of objects
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

        tempSeries = new XYChart.Series<>();

        sChart = new LineChart<>(xAxis, yAxis);
        sChart.getData().add(dSeries);
        sChart.setTitle("Sine Wave");
        sChart.setCreateSymbols(false);
        sChart.setAnimated(false);

        executor = Executors.newCachedThreadPool(); // Initializing the executor object

        dataQ = new ConcurrentLinkedQueue<>(); // Initializing the thread safe queue
        serverData = new ArrayList<>();
    }

    // Method to start the application GUI window
    @Override
    public void start(Stage window) {
        init(); // call the init method to initialize the objects

        firstScreen(); // Display the first screen of the application

        // Set window title, sceen and show window
        window.setScene(new Scene(root, 500, 500));
        window.setTitle("Server");
        window.show();
    }

    // Method to display the first screen of the application
    private void firstScreen() {

        // Initialize and define the GUI components

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
        Label sampleLabel = new Label("Sampling Frequency: ");
        TextField sampleField = new TextField();
        Label tempLabel1 = new Label("in Hz");
        sampleField.setPromptText("Sampling Frequency");
        fPane3.getChildren().addAll(sampleLabel, sampleField, tempLabel1);

        Button sbmBtn = new Button("Plot Graph");

        VBox vBox1 = new VBox();
        vBox1.setSpacing(10);
        vBox1.setAlignment(Pos.CENTER);
        vBox1.getChildren().addAll(fPane1, fPane2, fPane3, sbmBtn);

        root.setCenter(vBox1);

        // Action event triggered after the sbmBtn is clicked
        // Checks whether the textfields were null or not
        // Assigns value from the textfields to the variables
        // If everything successfull, displays the second screen of the application
        sbmBtn.setOnAction((e) -> {

            if ((ampField.getText().toString() != null) && (freqField.getText().toString() != null)) {
                amplitude = Double.parseDouble(ampField.getText().toString());
                frequency = Double.parseDouble(freqField.getText().toString());
                samplingFreq = Double.parseDouble(sampleField.getText().toString());

                System.out.println(amplitude + " " + frequency);

                secondScreen(); // Calls the method responsible to display the second screen
            } else {
                // Show Alert Box if any of the fields is empty
                Alert alert = new Alert(AlertType.ERROR, "Empty field entered!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
            }

        });
    }

    // Method responsible to display the second screen of the application
    private void secondScreen() {

        root.setCenter(sChart); // Set the line chart object to the center of the borderpane
        addToQueue = new AddToQueue(); // Initialize the private inner class object

        // Execute the private inner class on an external thread different from the
        // javafx
        // application thread
        dataThread = new Thread(addToQueue);
        dataThread.start(); // Start the execution of the thread
        prepareData(true);

        Button spButton = new Button("Stop Plotting");
        spButton.setOnAction((e) -> {
            dataThread.interrupt();
            prepareData(false);
            thirdScreen();
        });

        root.setBottom(spButton);

    }

    // Object responsible for animating the insertion of data into the line chart
    private AnimationTimer animationTimer = new AnimationTimer() {
        @Override
        public void handle(final long now) {
            addDataToSeries();
        }
    };

    // Method responsible for starting/stopping the plotting animation
    private void prepareData(boolean flag) {

        if (flag) {
            animationTimer.start();
        } else {
            animationTimer.stop();
        }
    }

    // Method responsible for displaying the third screen of the application
    // In this method the user can add a sin or cos wave with desired frequency and
    // amplitude by entering the data into the respective text fields
    private void thirdScreen() {

        // Set the different positions to null
        root.setCenter(null);
        root.setBottom(null);

        root.setCenter(sChart); // Display the chart in the center of the BorderPane

        // Declare and configure the various UI elements
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
        ToggleGroup radioGroup = new ToggleGroup(); // Ensures that only one Toggle Button is selected at a time
        fPane3.getChildren().addAll(funcLabel, sinRadioBtn, cosRadioBtn);

        sinRadioBtn.setToggleGroup(radioGroup);
        cosRadioBtn.setToggleGroup(radioGroup);
        sinRadioBtn.setSelected(true);

        Button sbmBtn = new Button("Plot Graph");
        Button shareButton = new Button("Share Data");

        VBox vBox1 = new VBox();
        vBox1.setSpacing(10);
        vBox1.setAlignment(Pos.CENTER);
        vBox1.getChildren().addAll(fPane1, fPane2, fPane3, sbmBtn, shareButton);

        sChart.getData().removeAll(dSeries);
        sChart.getData().add(tempSeries);
        sbmBtn.setOnAction((e1) -> {

            try {
                int amplitude = Integer.parseInt(ampField.getText().toString());
                int frequency = Integer.parseInt(freqField.getText().toString());

                RadioButton tempBtn = (RadioButton) radioGroup.getSelectedToggle();

                // To check which of the radio buttons are selected and perform the respective
                // calculation
                if (tempBtn.getId().equals("sin")) {
                    serverData.clear(); // Clear the serverData array list to allow fresh data to be entered
                    // Iterate throught the tempSeries and calculate the result due to addition
                    tempSeries.getData().forEach(item -> {
                        double value = (double) item.getYValue();
                        value += (Math.sin(((double) item.getXValue() * 2 * Math.PI) * frequency)) * (amplitude);
                        serverData.add(value);
                        item.setYValue(value);
                    });
                } else {
                    serverData.clear(); // Clear the serverData array list to allow fresh data to be entered
                    // Iterate throught the tempSeries and calculate the result due to addition
                    tempSeries.getData().forEach(item -> {
                        double value = (double) item.getYValue();
                        value += (Math.cos(((double) item.getXValue() * 2 * Math.PI) * frequency)) * (amplitude);
                        serverData.add(value);
                        item.setYValue(value);
                    });
                }

            } catch (NumberFormatException exception) {
                // Display an Alert Box if the value entered as input is not a number
                Alert alert = new Alert(AlertType.WARNING, "Invalid Entry!",
                        ButtonType.CLOSE, ButtonType.OK);
                alert.show();
            }

        });

        // When this button is clicked, execute the Server code with the help of the
        // executor object
        shareButton.setOnAction((e) -> {
            server = new Server(serverData.size(), serverData);
            executor.execute(server);
            Alert alert = new Alert(AlertType.INFORMATION, "Server started!",
                    ButtonType.CLOSE, ButtonType.OK);
            alert.show();
        });

        root.setBottom(vBox1);

    }

    // Method responsible for adding data into the dSeries object by removing the
    // first element from the queue
    // The number of entries is controlled by the 'unit' variable. The lower the
    // value, the more entries are added into the series
    private void addDataToSeries() {
        double unit = 1 / samplingFreq;
        for (int i = 0; i < 30; i++) {
            if (dataQ.isEmpty()) {
                break;
            }
            final double dataValue = (double) dataQ.remove();

            System.out.println(seriesData + " " + dataValue);
            dSeries.getData().add(new XYChart.Data<Number, Number>(seriesData,
                    dataValue));
            tempSeries.getData().add(new XYChart.Data<Number, Number>(seriesData,
                    dataValue));
            seriesData += unit;
        }

        // Removes data from the dSeries object so that chart lines do not become
        // clustered
        if (seriesData > frequency * 100) {
            dSeries.getData().remove(0);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO: handle exception
            }
        }

        // Update the x-Axis lower and upper bounds
        xAxis.setLowerBound(seriesData - frequency);
        xAxis.setUpperBound(seriesData - 0.01);

    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    private class AddToQueue implements Runnable {

        private double i = 0.00;
        private double unit = 1 / samplingFreq; // Variable responsible for the number of entries made in a second

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    double y = (Math.sin((i * 2 * Math.PI) * frequency)) * (amplitude);
                    i += unit;
                    dataQ.add(y);
                    serverData.add(y);
                    // qSize = dataQ.size();
                    // System.out.println(y);
                    Thread.sleep(30);
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
