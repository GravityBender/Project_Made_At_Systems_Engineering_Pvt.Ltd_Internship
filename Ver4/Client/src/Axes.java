package com.systemsengineering;

import javafx.scene.chart.NumberAxis;

//  Class to define the axes of the line chart
public class Axes {
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    public Axes() {
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
