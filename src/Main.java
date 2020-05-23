import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.pow;

public class Main {
    private LinearSystem system;

    public static void main(String[] args) {
        byte m;
        int P = 0;
        int s;
        int row;
        int col;
        for (m = 5; m <= 15; m++) {
            System.out.println();
            System.out.println("m = " + m);
            LinearSystem system = new LinearSystem(m);
            for (int t = 1; t < (pow(2, m) - 1) / m; t++) {
                int[] S = new int[2 * t];
                for (int count = 0; count < 10000; count++) {
                    boolean[] H = new boolean[system.field.H.size()];
                    // Sj
                    for (int j = 1; j <= S.length; j++) {
                        //S[0]=S1
                        if ((j) % 2 == 0)
                            S[j - 1] = system.field.powGF(S[j / 2 - 1], 2);
                        else {
                            do
                                row = (int) (Math.random() * (system.field.H.size()));
                            while (H[row]);
                            col = (int) (Math.random() * (system.field.H.get(row).size()));
                            S[j - 1] = system.field.H.get(row).get(col);
                        }
                    }
                    if (system.decode(t, S))
                        P++;
                }
                System.out.println(P);
                P = 0;
            }
        }
        System.out.println();
    }



    static long booleansToInt(boolean[] arr) {
        long n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
}
