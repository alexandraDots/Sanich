import java.util.ArrayList;

import static java.lang.Math.pow;

public class LinearSystem {
    private static GF field = new GF((byte) 5);
    private int[][] system;
    //решение системы
    private int[] ai;
    private int[] S;

    public static void setP(int p) {
        P = p;
    }

    private static int P = 0;


    public LinearSystem(int t, boolean[] c) {
        S = new int[2 * t];
        //S[0] = S1
        for (int i = 0; i < S.length; i++)
            S[i] = generateS(i + 1, c);
        do {
            generateSystem(t, c);
            ai = findSolution();
            if (!isLinearlyIndependent())
                t--;
        } while (!isLinearlyIndependent() && t != 0);

        boolean flag = true;
        //если t == 0 проверить S = 0
        if (t != 0) {
            boolean[] e = new boolean[c.length];
            for (Integer i : findRoots()) {
                e[i] = true;
            }
            for (int i = 0; i < 2 * t; i++) {
                if (generateS(i + 1, e) != S[i]) {
                    flag = false;
                    break;
                }
            }
        } else {
            for (int s : S) {
                if (s != 0) {
                    flag = false;
                    break;
                }
            }
        }
        if (flag)
            P++;
    }

    private boolean isLinearlyIndependent() {
        for (int i = 0; i < system.length; i++) {
            if (system[i][i] == 0) {
                return false;
            }
        }
        return true;
    }

    public static int getP() {
        return P;
    }

    private int[] findSolution() {
        for (int i = 0; i < system.length; i++) {
            for (int j = system[0].length - 1; j >= 0; j--) {
                //Поделить строку на первый ненулевой элемент
                system[i][j] = field.divide(system[i][j], system[i][i]);
            }
            if (i == system.length - 1)
                break;
            int a;
            for (int row = i + 1; row < system.length; row++) {
                a = system[row][i];
                for (int col = 0; col < system[0].length; col++)
                    //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                    system[row][col] = system[row][col] ^ field.multiply(system[i][col], a);
            }
        }
        //последний индекс в строке
        int k = system[0].length - 1;
        // в обратную сторону
        for (int i = system.length - 1; i > 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                system[j][k] = system[j][k] ^ field.multiply(system[j][i], system[i][k]);
                system[j][i] = system[j][i] ^ field.multiply(system[j][i], system[i][i]);
            }
        }
        int[] a = new int[system.length];
        for (int i = 0; i < system.length; i++) {
            a[i] = system[i][k];
        }
        return a;
    }

    public int generateS(int i, boolean[] e) {
        int s = 0;
        if (i % 2 == 0)
            s = field.powGF(S[i / 2 - 1], 2);
        else
            for (int j = 0; j < e.length; j++) {
                if (e[j]) {
                    s = s ^ field.powGFByIndex(i, j);
                }
            }
        return s;
    }

    public void generateSystem(int mistakes, boolean[] e) {
        system = new int[mistakes][mistakes + 1];
        for (int i = 0; i < mistakes; i++) {
            System.arraycopy(S, i, system[i], 0, mistakes + 1);
        }
    }

    public void printSystem() {
        for (int i = 0; i < system.length; i++) {
            for (int j = 0; j < system[0].length; j++) {
                System.out.print(field.index_of[system[i][j]] + " ");
            }
            System.out.println();
        }
    }

    private ArrayList<Integer> findRoots() {
        ArrayList roots = new ArrayList(ai.length);
        for (int root = 0; root < pow(2, field.getM()) - 1; root++) {
           int res = hornerMethod(field.arr[root+1]);
            /*int res = field.powGFByIndex(root, ai.length);
            for (int i = 0; i < ai.length; i++) {
                res = res ^ field.multiply(ai[i], field.powGFByIndex(root, i));
            }*/
            if (res == 0)
                roots.add(root);
        }
        return roots;
    }

    public int hornerMethod(int x) {
        int res = 1;
        for (int i = ai.length - 1; i >= 0; i--) {
            res = field.multiply(res, x);
            res = res ^ ai[i];
        }
        return (res);
    }
}

