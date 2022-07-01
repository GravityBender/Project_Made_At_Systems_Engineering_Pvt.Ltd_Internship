import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

public class Charts {
    private Axes axes;
    private LineChart<Number, Number> lineChart;
    private XYChart.Series<Number, Number> dataSeries;

    public Charts() {

    }

    public Charts(Axes axes) {
        this.axes = axes;
        this.lineChart = new LineChart<>(axes.getxAxis(), axes.getyAxis());
        this.lineChart.setCreateSymbols(false);
        this.lineChart.setAnimated(false);
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }

    public XYChart.Series<Number, Number> getDataSeries() {
        return dataSeries;
    }

    public void setDataSeries(XYChart.Series<Number, Number> dataSeries) {
        this.dataSeries = dataSeries;
        this.lineChart.getData().add(this.dataSeries);
    }

}
