import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.util.Collections.binarySearch;

public class Signature {
     int[][] H;
    int i = 1;
    static byte m = 5;
    static int t = 4;
    static int n = (int) pow(2, m) - 1;
    static LinearSystem system = new LinearSystem(m);
    private final int[][] Pt;
    private int[][] Hx;
    private final int[][] invertedX;
    int[][] X ;
    int [][] P;

    public Signature() {
        X = generateX();
        P = generateP();
        H = generateH();
        Pt = transpose(P);
        Hx = multiplyMatrix(X, H);
        Hx = multiplyMatrix(Hx, P);
        invertedX = invertMatrix(X);
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
            s = multiplyMatrix((invertedX), bits);
            Syndrome = generateSyndrome(bitsToIntArray(s));
            if (Syndrome == null) {
                i++;
                continue;
            }
            if ((b = system.sign(Syndrome, t, H, s)) != null) {
                int[] result = new int[b.length];
                for (int i = 0; i < Pt.length; i++) {
                    for (int k = 0; k < b.length; k++) {
                        if (Pt[i][k] == 1)
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

    static int[] bitsToIntArray(int[] s) {
        int[] res = new int[s.length / m];
        for (int i = 0; i < res.length; i++) {
            res[i] = 0;
            for (int j = m - 1; j >= 0; j--)
                res[i] = (res[i] << 1) | (s[i * m + j]);
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
        boolean[] b2 = toBoolean(multiplyMatrix(Hx, e));
        boolean flag = true;
        for (int l = 0; l < b2.length; l++)
            if (b1[l] != b2[l])
                flag = false;
        return flag;

    }

    int[][] generateX() {
        int[][] X;
        do {
            X = new int[m * t][m * t];
            for (int i = 0; i < m * t; i++) {
                for (int j = 0; j < m * t; j++) {
                    if (i != j)
                        X[i][j] = (int) (Math.random() * 2);
                    else
                        X[i][j] = 1;
                }
            }
        } while (countDet(X) == 0);
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

    private int[][] generateH() {
        int alpha, a, b;
        int[][] H = new int[m * t][n];
        for (int j = 0; j < t; j++) {
            a = 1;
            alpha = system.field.arr[system.field.H.get(j).get(0) + 1];//system.field.arr[j + 2];
            for (int k = 0; k < n; k++) {
                b = a;
                for (int l = 0; l < m; l++) {
                    H[j * m + l][k] = b % 2;
                    b /= 2;
                }
                a = system.field.multiply(a, alpha);
            }
        }
        return H;
    }


    private int[][] transpose(int[][] P) {
        int[][] transposedP = new int[P[0].length][P.length];
        for (int j = 0; j < P.length; j++) {
            for (int k = 0; k < P[0].length; k++) {
                transposedP[j][k] = P[k][j];
            }
        }
        return transposedP;
    }

    private int[][] generateP() {
        int k;
        ArrayList<Integer> arr = new ArrayList<Integer>(n);
        for (int i = 0; i < n; i++)
            arr.add(i);
        int[][] P = new int[n][n];
        for (int j = 0; j < n; j++) {
            k = (int) (Math.random() * (arr.size()));
            P[j][arr.get(k)] = 1;
            arr.remove(k);
        }
        return P;
    }

    //найти обратную матрицу
    int[][] invertMatrix(int[][] X) {
        int[][] matrix = new int[X.length][X[0].length];
        for (int j = 0; j < X.length; j++) {
            System.arraycopy(X[j], 0, matrix[j], 0, matrix[j].length);
        }
        int[][] tmpMatrix = new int[matrix.length][matrix.length * 2];
        for (int j = 0; j < tmpMatrix.length; j++) {
            System.arraycopy(matrix[j], 0, tmpMatrix[j], 0, matrix.length);
            tmpMatrix[j][j + matrix.length] = 1;
        }
        int a;
        int[] tmpRow;
        for (int i = 0; i < tmpMatrix.length; i++) {
            // Если і-ый элемент  равен 0 - поменять эту строку с той  где і-ый элемент !=0
            for (int k = i; tmpMatrix[k][i] == 0; k++) {
                if (k + 1 >= tmpMatrix.length) {
                    return null;
                }
                if (tmpMatrix[k + 1][i] != 0) {
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
                if (a != 0)
                    for (int col = 0; col < tmpMatrix[0].length; col++)
                        tmpMatrix[row][col] = tmpMatrix[row][col] ^ tmpMatrix[i][col];

            }
        }
        // в обратную сторону
        for (int i = tmpMatrix.length - 1; i > 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                if (tmpMatrix[j][i] != 0) {
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

    void printMatrix(int[][] matrix) {
        for (int j = 0; j < matrix.length; j++) {
            for (int number : matrix[j])
                System.out.print(number + " ");
            System.out.println();
        }
    }

    //принимает квадратную матрицу c единичной диагональю и возвращает её детерминант
    int countDet(int X[][]) {
        int a;
        int[][] matrix = new int[X.length][X[0].length];
        for (int j = 0; j < X.length; j++) {
            System.arraycopy(X[j], 0, matrix[j], 0, matrix[j].length);
        }

        int[] tmpRow;
        boolean LinearlyDependent = false;
        for (int i = 0; i < matrix.length - 1; i++) {
            for (int row = i + 1; row < matrix.length; row++) {
                a = matrix[row][i];
                if (a != 0)
                    for (int col = 0; col < matrix[0].length; col++)
                        //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                        matrix[row][col] = matrix[row][col] ^ system.field.multiply(matrix[i][col], a);
            }
        }
        int det = matrix[0][0];
        for (int i = 1; i < matrix.length; i++) {
            det = system.field.multiply(det, matrix[i][i]);
        }
        return det;
    }

    //умножение матрицы на вектор vector^T
    public static int[] multiplyMatrix(int[][] matrix, int[] vector) {
        if (matrix[0].length != vector.length)
            return null;
        int[] result = new int[matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int k = 0; k < vector.length; k++) {
                result[i] ^= system.field.multiply(matrix[i][k], vector[k]);
            }
        }
        return result;
    }

    //Умнлжение на массив бит, возвращает массив бит
    static int[] multiplyMatrix(int[][] matrix, boolean[] b) {
        if (matrix[0].length != b.length)
            return null;
        int[] result = new int[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int k = 0; k < b.length; k++) {
                if (b[k])
                    result[i] ^= matrix[i][k];
            }
        }
        return result;
    }

    public static int[][] multiplyMatrix(int[][] matrix1, int[][] matrix2) {

        if (matrix1[0].length != matrix2.length)
            return null;

        int[][] result = new int[matrix1.length][matrix2[0].length];

        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix1[0].length; k++) {
                    result[i][j] ^= system.field.multiply(matrix1[i][k], matrix2[k][j]);
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

    public int[][] getHx() {
        return Hx;
    }
}
