import java.util.function.Function;

public class bullshit {
    private static double size = 206;

    public static void main(String[] args) {

        double y = roundUp(x -> Math.floor(x / 100));
        System.out.println(y);

    }

    private static double roundUp(Function<Double, Double> f) {
        double y = f.apply(size);
        return y;
    }
}
