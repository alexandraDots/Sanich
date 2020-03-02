import static java.lang.Math.pow;

public class Main {
    public static void main(String[] args) {
        byte m = 5;
        LinearSystem system;
        for (int t = 1; t < (pow(2, m) - 1) / m; t++) {
            boolean[] c = new boolean[(int) (pow(2, m) - 1)];
            System.out.println();
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < pow(2, m) - 1; j++) {
                    c[j] = (int) (Math.random() * 2) == 0;
                }
                system = new LinearSystem(t, c);
            }
            System.out.print(LinearSystem.getP() * 1.0);
            LinearSystem.setP(0);
        }
    }
}
