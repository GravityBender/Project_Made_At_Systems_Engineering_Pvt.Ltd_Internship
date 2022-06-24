import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

public class Charts {
    private LineChart<Number, Number> mainLineChart;
    private XYChart.Series<Number, Number> mainDataSeries;
    private int i;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private double amplitude;
    private double frequency;
    private double phase;

    public Charts() {
        mainDataSeries = new XYChart.Series<>();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        mainLineChart = new LineChart<>(xAxis, yAxis);
        mainLineChart.getData().add(mainDataSeries);
        mainLineChart.setCreateSymbols(false);
        i = -1;
    }

    public XYChart.Series<Number, Number> getMainDataSeries() {
        return mainDataSeries;
    }

    public void setMainDataSeries(XYChart.Series<Number, Number> mainDataSeries) {
        this.mainDataSeries = mainDataSeries;
    }

    public LineChart<Number, Number> getMainLineChart() {
        return mainLineChart;
    }

    public void setMainLineChart(LineChart<Number, Number> mainLineChart) {
        this.mainLineChart = mainLineChart;
    }

    public NumberAxis getxAxis() {
        return xAxis;
    }

    public void setxAxis(NumberAxis xAxis) {
        this.xAxis = xAxis;
    }

    public NumberAxis getyAxis() {
        return yAxis;
    }

    public void setyAxis(NumberAxis yAxis) {
        this.yAxis = yAxis;
    }

    public void animateChart(ObservableList<XYChart.Data<Number, Number>> list) {

        Timeline chartUpdater = new Timeline();

        KeyFrame kf = new KeyFrame(Duration.millis(80), event -> {

            if (geti() >= list.size() - 1) {
                chartUpdater.stop();
            } else {
                seti();
                mainDataSeries.getData()
                        .add(new XYChart.Data<Number, Number>(list.get(geti()).getXValue(),
                                list.get(geti()).getYValue()));

                System.out.println(list.get(geti()).getXValue() + "   " + list.get(geti()).getYValue());
                if (geti() > 500) {
                    mainDataSeries.getData().remove(0);
                }
                // System.out.println(xAxis.getLowerBound());
                xAxis.setLowerBound(i - 500);
                xAxis.setUpperBound(i + 1);
            }
        });

        mainLineChart.setAnimated(false);
        chartUpdater.getKeyFrames().addAll(kf);
        chartUpdater.setCycleCount(Timeline.INDEFINITE);
        chartUpdater.play();
    }

    private void seti() {
        i++;
    }

    private int geti() {
        return i;
    }

}
