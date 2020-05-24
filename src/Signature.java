import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.util.Collections.binarySearch;

public class Signature {
    static int i = 1;
    static byte m = 5;
    static int t = 2;
    static LinearSystem system = new LinearSystem(m);
    private final int[][] Pt;
    private int[][] Hx;
    private final int[][] invertedX;

    public Signature() {
        int[][] X = generateX();
        int[][] P = generateP();
        Pt = transpose(P);
        int[][] H = arrayListToArr(system.field.H);
        Hx = multiplyMatrix(X, H);
        Hx = multiplyMatrix(Hx, P);
        invertedX = inverteMatrix(X);
        int[][] pr = multiplyMatrix(invertedX, X);
        System.out.println();
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
            s = generateSyndrome(s);
            if (s == null) {
                i++;
                continue;
            }
            s = multiplyMatrix(invertedX,s);
            Syndrome = generateSyndrome(s);
            if (Syndrome == null) {
                i++;
                continue;
            }
            if ((b = system.sign(Syndrome, t)) != null)
                return multiplyMatrix(Pt,b);
            i++;
        }
    }



    public static boolean checkSignature(boolean[] e, int j, String M) {
        byte[] message = M.getBytes();
        int[] h = toIntArray(Sha256.hash(message));
        int[] s = new int[h.length + 1];
        System.arraycopy(h, 0, s, 0, h.length);
        s[s.length - 1] = j;
        s = toIntArray(Sha256.hash(toByteArray(s)));
        s = generateSyndrome(s);
        return system.check(e, s, t);

    }

    int[][] generateX() {
        int[][] X = new int[m * t][m * t];
        do {
            for (int i = 0; i < m * t; i++) {
                for (int j = 0; j < m * t; j++) {
                    X[i][j] = (int) (Math.random() * (pow(2, m) - 1) + 1);
                }
            }
        } while (countDet(X) == 0);
        return X;
    }

    static int[] generateSyndrome(int S[]) {
        int s;
        int[] Syndrome = new int[m * t];
        boolean[] H = new boolean[system.field.H.size()];
        // Sj
        for (int j = 1; j <= Syndrome.length; j++) {
            //S[0]=S1
            if ((j) % 2 == 0)
                Syndrome[j - 1] = system.field.powGF(Syndrome[j / 2 - 1], 2);
            else {
                s = S[j / 2 + 1];
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

    private int[][] arrayListToArr(ArrayList<ArrayList<Integer>> H) {
        int[][] result = new int[m * t][(int) pow(2, m)];
        for (int j = 0; j < result.length; j++) {
            for (int k = 0; k < result[0].length; k++) {
                result[i][k] = H.get(i).get(k % H.get(i).size());
            }
        }
        return result;
    }

    private int[][] transpose(int[][] P) {
        int[][] transposedP = new int[P.length][P[0].length];
        for (int j = 0; j < P.length; j++) {
            for (int k = 0; k < P[0].length; k++) {
                transposedP[j][k] = P[k][j];
            }
        }
        return transposedP;
    }

    private int[][] generateP() {
        int n = (int) pow(2, m);
        int k;
        boolean[] isUsed = new boolean[n];
        int[][] P = new int[n][n];
        for (int j = 0; j < m * t; j++) {
            do
                k = (int) (Math.random() * (pow(2, m)));
            while (isUsed[i]);
            P[j][k] = 1;
        }
        return P;
    }

    //найти обратную матрицу
    int[][] inverteMatrix(int[][] matrix) {
        if (countDet(matrix) == 0) return null;
        int[][] tmpMatrix = new int[matrix.length][matrix.length * 2];
        for (int j = 0; j < tmpMatrix.length; j++) {
            System.arraycopy(matrix[j], 0, tmpMatrix[j], 0, matrix.length);
            tmpMatrix[j][j + matrix.length] = 1;
        }

        int a;
        int[] tmpRow;
        for (int i = 0; i < tmpMatrix.length; i++) {
            for (int j = tmpMatrix[0].length - 1; j >= 0; j--) {
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
                //Поделить строку на первый ненулевой элемент,
                tmpMatrix[i][j] = system.field.divide(tmpMatrix[i][j], tmpMatrix[i][i]);
            }
            if (i == tmpMatrix.length - 1)
                break;
            for (int row = i + 1; row < tmpMatrix.length; row++) {
                a = tmpMatrix[row][i];
                for (int col = 0; col < tmpMatrix[0].length; col++)
                    //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                    tmpMatrix[row][col] = tmpMatrix[row][col] ^ system.field.multiply(tmpMatrix[i][col], a);
            }
        }
        //последний индекс в строке
        int k = tmpMatrix[0].length - 1;
        // в обратную сторону
        for (int i = tmpMatrix.length - 1; i > 0; i--) {
            for (int j = i - 1; j >= 0; j--) {
                //Вычесть iую строку, домноженую на коэфициэнт из всех остальных строк
                tmpMatrix[j][k] = tmpMatrix[j][k] ^ system.field.multiply(tmpMatrix[j][i], tmpMatrix[i][k]);
                tmpMatrix[j][i] = tmpMatrix[j][i] ^ system.field.multiply(tmpMatrix[j][i], tmpMatrix[i][i]);
            }
        }
        for (int j = 0; j < tmpMatrix.length; j++) {
            System.arraycopy(tmpMatrix[j], matrix.length, matrix[j], 0, matrix.length);
        }
        return matrix;
    }

    //принимает квадратную матрицу и возвращает её детерминант
    int countDet(int matrix[][]) {
        int a;
        int[] tmpRow;
        boolean LinearlyDependent = false;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = matrix[0].length - 1; j >= 0; j--) {
                // Если і-ый элемент  равен 0 - поменять эту строку с той  где і-ый элемент !=0
                for (int k = i; matrix[k][i] == 0; k++) {
                    if (k + 1 >= matrix.length) {
                        return 0;
                    }
                    if (matrix[k + 1][i] != 0) {
                        tmpRow = matrix[i];
                        matrix[k + 1] = matrix[i];
                        matrix[i] = tmpRow;
                        break;
                    }
                }
                //Поделить строку на первый ненулевой элемент,
                matrix[i][j] = system.field.divide(matrix[i][j], matrix[i][i]);
            }
            if (i == matrix.length - 1)
                break;
            for (int row = i + 1; row < matrix.length; row++) {
                a = matrix[row][i];
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
    private boolean[] multiplyMatrix(int[][] matrix, boolean[] b) {
        if (matrix[0].length != b.length)
            return null;
        boolean[] result = new boolean[b.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int k = 0; k < b.length; k++) {
                if (matrix[i][k] == 1)
                    result[i] = b[k];
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
            intArr[i] = Math.abs(intArr[i]) % ((int) pow(2, m) - 1) + 1;
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
