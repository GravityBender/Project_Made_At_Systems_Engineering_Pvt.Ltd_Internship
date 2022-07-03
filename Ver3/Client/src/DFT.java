public class DFT {

    // public Complex[] findFFT(Complex[] x) {
    // int N = x.length;

    // if (N == 1)
    // return new Complex[] { x[0] };

    // if (N % 2 != 0) {
    // throw new RuntimeException("N is not a power of 2");
    // }

    // // fft of even terms
    // Complex[] even = new Complex[N / 2];
    // for (int k = 0; k < N / 2; k++) {
    // even[k] = x[2 * k];
    // }
    // Complex[] q = findFFT(even);

    // // fft of odd terms
    // Complex[] odd = even; // reuse the array
    // for (int k = 0; k < N / 2; k++) {
    // odd[k] = x[2 * k + 1];
    // }
    // Complex[] r = findFFT(odd);

    // // combine
    // Complex[] y = new Complex[N];
    // for (int k = 0; k < N / 2; k++) {
    // double kth = -2 * k * Math.PI / N;
    // Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
    // y[k] = q[k].add(wk.multiply(r[k]));
    // y[k + N / 2] = q[k].subtract(wk.multiply(r[k]));
    // }
    // return y;

    // }

    // public static Complex[] getFFT(Complex[] input, int N) {
    // if ((N / 2) % 2 == 0) {
    // Complex[] even = new Complex[N / 2];// even numbers
    // Complex[] odd = new Complex[N / 2];// Odd number
    // for (int i = 0; i < N / 2; i++) {
    // even[i] = input[2 * i];
    // odd[i] = input[2 * i + 1];
    // }
    // even = getFFT(even, N / 2);
    // odd = getFFT(odd, N / 2);
    // for (int i = 0; i < N / 2; i++) {
    // Complex[] res = Complex.butterfly(even[i], odd[i], Complex.GetW(i, N));
    // input[i] = res[0];
    // input[i + N / 2] = res[1];
    // }
    // return input;
    // } else {// Two point DFT, direct dish operation
    // Complex[] res = Complex.butterfly(input[0], input[1], Complex.GetW(0, N));
    // return res;
    // }
    // }

    public static Complex[] computeDft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];
        double j = 0.00;
        double l = 0.00;
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

                // sumreal += x[t].getImaginary() * Math.cos(angle);
                // sumimag += -x[t].getImaginary() * Math.sin(angle);
                l += 0.01;
            }
            j += 0.01;
            y[k] = new Complex();
            y[k].setReal(sumreal);
            y[k].setImaginary(sumimag);
        }

        return y;
    }

}
