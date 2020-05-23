import static java.lang.Math.pow;

public class Main {
    public static void main(String[] args) {
        byte m = 5;
        int P = 0;
        LinearSystem system = new LinearSystem(m);
        for (int t = 1; t < (pow(2, m) - 1) / m; t++){
            boolean[] c = new boolean[(int) (pow(2, m) - 1)];
            System.out.println();
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < pow(2, m) - 1; j++) {
                    c[j] = (int) (Math.random() * 2) == 0;
                }
                if (system.decode(t,c))
                    P++;
            }
            System.out.print(t + " " + P*1.0/1000);
           P = 0;
        }
    }

    static long booleansToInt(boolean[] arr) {
        long n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
}
