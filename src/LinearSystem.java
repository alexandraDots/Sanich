import java.util.ArrayList;

import static java.lang.Math.pow;

public class LinearSystem {
    private  int m;
    GF field;
    private int[][] system;
    //решение системы
    private int[] ai;
    private int[] Syndrome;

    public LinearSystem(byte n){
        this.m = n;
        field =  new GF((byte) m);
    }

    public boolean check(boolean[] e, int[] S, int t) {
        Syndrome = new int[2 * t];
        //S[0] = S1
        for (int i = 0; i < Syndrome.length; i++) {
            Syndrome[i] = generateS(i + 1, e);
            if (Syndrome[i] != S[i])
                return false;
        }
        return true;
    }

    public boolean[] sign(int[] S, int t, int[][]H, int[] check) {

        do {
            generateSystem(t, S);
            try {
                ai = findSolution();
            } catch (LinearlyDependentException e) {
                //System.out.println(e.getMessage());
                t--;
            } catch (TooManySolutionsException e) {
                return null;
            }
        } while (!isLinearlyIndependent() && t != 0);

        //проверка
        boolean[] e = new boolean[(int) Math.pow(2, m) - 1];
        //если t == 0 проверить S = 0
        if (t != 0) {
            for (Integer i : findRoots()) {
                e[i] = true;
            }
            Syndrome = new int[2 * t];
            int[] syn = Signature.multiplyMatrix(H,e);
            for (int i = 0; i < Syndrome.length; i++) {
                Syndrome[i] = generateS(i + 1, e);
                if  (Syndrome[i] != S[i]) { //(syn[i] != check[i] ) {
                    return null;
                }
            }
        } else {
            for (int s : S) {
                if (s != 0) {
                    return null;
                }
            }
        }
        return e;
    }



    private boolean isLinearlyIndependent() {
        for (int i = 0; i < system.length; i++) {
            if (system[i][i] == 0) {
                return false;
            }
        }
        return true;
    }



    private int[] findSolution() throws LinearlyDependentException, TooManySolutionsException {
        int a;
        int[] tmpRow;
        boolean LinearlyDependent = false;
        for (int i = 0; i < system.length; i++) {
            for (int j = system[0].length - 1; j >= 0; j--) {
                // Если і-ый элемент  равен 0 - поменять эту строку с той  где і-ый элемент !=0
                for (int k = i; system[k][i] == 0; k++) {
                    if (k + 1 >= system.length) {
                        //checkSystemOK();
                        LinearlyDependent = true;
                        break;
                    }
                    if (system[k + 1][i] != 0) {
                        tmpRow = system[i];
                        system[k + 1] = system[i];
                        system[i] = tmpRow;
                        break;
                    }
                }
                //Поделить строку на первый ненулевой элемент,
                system[i][j] = field.divide(system[i][j], system[i][i]);
            }
            if (i == system.length - 1)
                break;
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
        checkSystemOK();
        if (LinearlyDependent)
            throw new LinearlyDependentException("System isn't LinearlyIndependent");
        int[] arr = new int[system.length];
        for (int i = 0; i < system.length; i++) {
            arr[i] = system[i][k];
        }
        return arr;
    }

    private void checkSystemOK() throws TooManySolutionsException {
        boolean flag;
        for (int i = 0; i < system.length; i++) {
            flag = true;
            for (int j = 0; j < system[0].length - 1; j++) {
                if (system[i][j] != 0) {
                    flag = false;
                    break;
                }
            }
            if (flag && system[i][system.length]!=0)
                throw new TooManySolutionsException();
        }
    }

    public int generateS(int i, boolean[] e) {
        int s = 0;
        if (i % 2 == 0)
            s = field.powGF(Syndrome[i / 2 - 1], 2);
        else
            for (int j = 0; j < e.length; j++) {
                if (e[j]) {
                    s = s ^ field.powGFByIndex(i, j);
                }
            }
        return s;
    }

    public void generateSystem(int mistakes, int[] S) {
        system = new int[mistakes][mistakes + 1];
        //system[0][0] = 1; //*
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

    static class LinearlyDependentException extends Exception {
        LinearlyDependentException(String s) {
            super(s);
        }
    }

    private class TooManySolutionsException extends Exception {
    }

}

