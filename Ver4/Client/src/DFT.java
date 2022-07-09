package com.systemsengineering;

//  Class responsible for calculating the DFT of the line chart
public class DFT {

    public static Complex[] computeDft(Complex[] x, double samplingFreq) {
        int n = x.length;
        Complex[] y = new Complex[n];
        double j = 0.00; // Index of output ie time
        double l = 0.00; // Frequency of input
        double unit = 1 / samplingFreq;
        double u = unit * 10;
        for (int k = 0; k < n; k++) { // For each output element
            double sumreal = 0;
            double sumimag = 0;
            l = 0.00;
            for (int t = 0; t < n; t++) { // For each input element
                double angle = 2 * Math.PI * l * j;
                sumreal += x[t].getReal() * Math.cos(angle) + x[t].getImaginary() *
                        Math.sin(angle);
                sumimag += -x[t].getReal() * Math.sin(angle) + x[t].getImaginary() *
                        Math.cos(angle);

                l += unit;
            }
            j += u;
            y[k] = new Complex();
            y[k].setReal(sumreal);
            y[k].setImaginary(sumimag);
        }

        return y;
    }

}
