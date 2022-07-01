import javafx.scene.chart.NumberAxis;

public class Axes {
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    public Axes() {
        double x = Math.round(296 + 50 / 100) * 100;
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
    }

    public NumberAxis getxAxis() {
        return xAxis;
    }

    public NumberAxis getyAxis() {
        return yAxis;
    }

}
