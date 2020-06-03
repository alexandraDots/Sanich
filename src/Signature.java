import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.util.Collections.binarySearch;

public class Signature {
    int i = 1;
    static byte m;
    static int t;
    static int n;
    static LinearSystem system ;
    private final boolean[][] Pt;
    private boolean[][] Hx;
    private final boolean[][] invertedX;
    private Keys keys;

    public Signature(byte m, int t) {
        this.m = m;
        this.t = t;
        n =   (int) pow(2, m) - 1;
        system = new LinearSystem(m);
        boolean[][] X = generateX();
        boolean[][] P = generateP();
        boolean[][] H = generateH();
        Pt = transpose(P);
        Hx = multiplyMatrix(X, H);
        Hx = multiplyMatrix(Hx, P);
        invertedX = invertMatrix(X);
        keys = new Keys(m,t,Pt,Hx,invertedX);
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("keys.dat")))
        {
            oos.writeObject(keys);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Signature(Keys keySet) {
        keys = keySet;
        Hx = keys.getHx();
        invertedX = keys.getInvertedX();
        Pt = keys.getPt();
        m = keys.m;
        t = keySet.t;
        n =   (int) pow(2, m) - 1;
        system = new LinearSystem(m);
    }

    public boolean[] signature(String M) {
        byte[] message = M.getBytes();
        int[] h = toIntArray(Sha256.hash(message));
        int[] s;
        int[] Syndrome;
        boolean[] b;
        while (true) {
            s = new int[h.length + 1];
            System.arraycopy(h, 0, s, 0, h.length);
            s[s.length - 1] = i;
            s = toIntArray(Sha256.hash(toByteArray(s)));
            boolean[] bits = Sha256.intToBitArray(s, m * t);
            bits = multiplyMatrix((invertedX), bits);
            Syndrome = generateSyndrome(bitsToIntArray(bits));
            if (Syndrome == null) {
                i++;
                continue;
            }
            if ((b = system.sign(Syndrome, t)) != null) {
                int[] result = new int[b.length];
                for (int i = 0; i < Pt.length; i++) {
                    for (int k = 0; k < b.length; k++) {
                        if (Pt[i][k])
                            result[i] = (b[k] ? 1 : 0);
                    }
                }
                for (int k = 0; k < b.length; k++) {
                    b[k] = (result[k] == 1);
                }

                return b;
            }
            i++;
        }
    }

    static boolean[] toBoolean(int[] s) {
        boolean[] b = new boolean[s.length];
        for (int k = 0; k < b.length; k++) {
            b[k] = (s[k] == 1);
        }
        return b;
    }

    static int[] bitsToIntArray(boolean[] s) {
        int[] res = new int[s.length / m];
        for (int i = 0; i < res.length; i++) {
            res[i] = 0;
            for (int j = m - 1; j >= 0; j--)
                res[i] = (res[i] << 1) | ((s[i * m + j] ? 1 : 0));
        }
        return res;
    }


    public boolean checkSignature(boolean[] e, int j, String M) {
        byte[] message = M.getBytes();
        int[] h = toIntArray(Sha256.hash(message));
        int[] s = new int[h.length + 1];
        System.arraycopy(h, 0, s, 0, h.length);
        s[s.length - 1] = j;
        s = toIntArray(Sha256.hash(toByteArray(s)));
        boolean[] b1 = Sha256.intToBitArray(s, n);
        boolean[] b2 = multiplyMatrix(Hx, e);
        boolean flag = true;
        for (int l = 0; l < b2.length; l++)
            if (b1[l] != b2[l])
                flag = false;
        return flag;

    }

    boolean[][] generateX() {
        boolean[][] X;
        do {
            X = new boolean[m * t][m * t];
            for (int i = 0; i < m * t; i++) {
                for (int j = i; j < m * t; j++) {
                    if (i != j)
                        X[i][j] = (int) (Math.random() * 2) == 1;
                    else
                        X[i][j] = true;
                }
            }
        } while (!countDet(X));
        //ToDo Добавить перестановку строк
        return X;
    }

    static int[] generateSyndrome(int S[]) {
        int s;
        int[] Syndrome = new int[2 * t];
        boolean[] H = new boolean[system.field.H.size()];
        // Sj
        for (int j = 1; j <= Syndrome.length; j++) {
            //S[0]=S1
            if ((j) % 2 == 0)
                Syndrome[j - 1] = system.field.powGF(Syndrome[j / 2 - 1], 2);
            else {
                s = S[j / 2];
                if (s == 0) return null;
                for (int i = 0; i < system.field.H.size(); i++) {
                    if (H[i] && (binarySearch(system.field.H.get(i), s) >= 0))
                        return null;
                }
                H[system.field.getRow(s)] = true;
                Syndrome[j - 1] = s;
            }
        }
        return Syndrome;
    }

    private boolean[][] generateH() {
        int alpha, a, b;
        boolean[][] H = new boolean[m * t][n];
        for (int j = 0; j < t; j++) {
            a = 1;
            alpha = system.field.arr[system.field.H.get(j).get(0) + 1];//system.field.arr[j + 2];
            for (int k = 0; k < n; k++) {
                b = a;
                for (int l = 0; l < m; l++) {
                    H[j * m + l][k] = b % 2==1;
                    b /= 2;
                }
                a = system.field.multiply(a, alpha);
            }
        }
        return H;
    }


    private boolean[][] transpose(boolean[][] P) {
        boolean[][] transposedP = new boolean[P[0].length][P.length];
        for (int j = 0; j < P.length; j++) {
            for (int k = 0; k < P[0].length; k++) {
                transposedP[j][k] = P[k][j];
            }
        }
        return transposedP;
    }

    private boolean[][] generateP() {
        int k;
        ArrayList<Integer> arr = new ArrayList<Integer>(n);
        for (int i = 0; i < n; i++)
            arr.add(i);
        boolean[][] P = new boolean[n][n];
        for (int j = 0; j < n; j++) {
            k = (int) (Math.random() * (arr.size()));
            P[j][arr.get(k)] = true;
            arr.remove(k);
        }
        return P;
    }

    //найти обратную матрицу
    boolean[][] invertMatrix(boolean[][] X) {
        boolean[][] matrix = new boolean[X.length][X[0].length];
        for (int j = 0; j < X.length; j++) {
            System.arraycopy(X[j], 0, matrix[j], 0, matrix[j].length);
        }
        boolean[][] tmpMatrix = new boolean[matrix.length][matrix.length * 2];
        for (int j = 0; j < tmpMatrix.length; j++) {
            System.arraycopy(matrix[j], 0, tmpMatrix[j], 0, matrix.length);
            tmpMatrix[j][j + matrix.length] = true;
        }
        boolean a;
        boolean[] tmpRow;
        for (int i = 0; i < tmpMatrix.length; i++) {
            // Если і-ый элемент  равен 0 - поменять эту строку с той  где і-ый элемент !=0
            for (int k = i; !tmpMatrix[k][i]; k++) {
                if (k + 1 >= tmpMatrix.length) {
                    return null;
                }
                if (tmpMatrix[k + 1][i]) {
                    tmpRow = tmpMatrix[i];
                    tmpMatrix[k + 1] = tmpMatrix[i];
                    tmpMatrix[i] = tmpRow;
                    break;
                }
            }
            if (i == tmpMatrix.length - 1)
                break;
            for (int row = i + 1; row < tmpMatrix.length; row++) {
                a = tmpMatrix[row][i];
                //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                if (a)
                    for (int col = 0; col < tmpMatrix[0].length; col++)
                        tmpMatrix[row][col] = tmpMatrix[row][col] ^ tmpMatrix[i][col];

            }
        }
        // в обратную сторону
        for (int i = tmpMatrix.length - 1; i > 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                if (tmpMatrix[j][i]) {
                    tmpMatrix[j][i] ^= tmpMatrix[i][i];
                    for (int l = matrix.length; l < tmpMatrix[0].length; l++) {
                        tmpMatrix[j][l] ^= tmpMatrix[i][l];
                    }
                }
            }
        }
        for (int j = 0; j < tmpMatrix.length; j++) {
            System.arraycopy(tmpMatrix[j], matrix.length, matrix[j], 0, matrix.length);
        }
        return matrix;
    }

    void printMatrix(boolean[][] matrix) {
        for (int j = 0; j < matrix.length; j++) {
            for (boolean number : matrix[j])
                System.out.print(number?1:0 + " ");
            System.out.println();
        }
    }

    //принимает квадратную матрицу c единичной диагональю и возвращает её детерминант
    boolean countDet(boolean X[][]) {
        boolean a;
        boolean[][] matrix = new boolean[X.length][X[0].length];
        for (int j = 0; j < X.length; j++) {
            System.arraycopy(X[j], 0, matrix[j], 0, matrix[j].length);
        }

        boolean[] tmpRow;
        boolean LinearlyDependent = false;
        for (int i = 0; i < matrix.length - 1; i++) {
            for (int row = i + 1; row < matrix.length; row++) {
                a = matrix[row][i];
                if (a)
                    for (int col = 0; col < matrix[0].length; col++)
                        //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                        matrix[row][col] = matrix[row][col] ^ (matrix[i][col] & a);
            }
        }
        boolean det = matrix[0][0];
        for (int i = 1; i < matrix.length; i++) {
            det = det & matrix[i][i];
        }
        return det;
    }

    //умножение матрицы на вектор vector^T
    public static boolean[] multiplyMatrix(boolean[][] matrix, boolean[] vector) {
        if (matrix[0].length != vector.length)
            return null;
        boolean[] result = new boolean[matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int k = 0; k < vector.length; k++) {
                result[i] ^= (matrix[i][k] & vector[k]);
            }
        }
        return result;
    }




    public static boolean[][] multiplyMatrix(boolean[][] matrix1, boolean[][] matrix2) {

        if (matrix1[0].length != matrix2.length)
            return null;

        boolean[][] result = new boolean[matrix1.length][matrix2[0].length];

        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix1[0].length; k++) {
                    result[i][j] ^= (matrix1[i][k]&matrix2[k][j]);
                }
            }
        }
        return result;
    }

    // 4 bytes become 1 int  modulo 2^m
    private static int[] toIntArray(byte[] byteArr) {
        int[] intArr = Sha256.toIntArray(byteArr);
        for (int i = 0; i < intArr.length; i++) {
            intArr[i] = Math.abs(intArr[i]) % n + 1;
        }
        return intArr;
    }

    // 1 int becomes 4 bytes
    private static byte[] toByteArray(int[] intArr) {
        byte[] byteArr = Sha256.toByteArray(intArr);
        return byteArr;
    }

}
