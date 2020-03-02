import java.util.ArrayList;

import static java.lang.Math.*;

public class GF {
    private byte m;
    private int g;
    int[] arr;
    int[] index_of;
    private final int[] gSet = {3, 7, 11, 19, 37, 67, 137, 285, 529, 1033, 2053, 4179, 8219, 17475, 32771, 69643,
            131081, 262273, 524327, 1048585, 2097157, 4194307, 8388641, 16777351, 33554441};
    private ArrayList<Integer> conjugateEl;
    private int[] H;

    public GF(byte degree) {
        m = degree;
        g = generatePolynomial(m);
        generateConj();
        arr = new int[(int) pow(2, m)];
        index_of = new int[(int) pow(2, m)];
        arr[0] = 0;
        arr[1] = 1;
        index_of[0] = 0;
        index_of[1] = 1;
        for (int i = 2; i < pow(2, m); i++) {
            arr[i] = multiplyByX(arr[i - 1]);
            index_of[arr[i]] = i;
        }
    }


    private int generatePolynomial(byte m) {
        return gSet[m - 1];
    }

    public int add(int a, int b) {
        return a ^ b;
    }

    // 0 = нулевая степень
    public int multiplyByIndex(int a, int b) {
        int sum;
        if (a == -1 || b == -1) return 0;
        sum = a + b;
        if (sum >= (int)(pow(2,m)) - 1)
            sum %= (int)(pow(2,m)) - 1;
        return sum ;
    }

    public int multiply(int a, int b) {
        int result = 0;
        int x = a;
        if (b % 2 != 0)
            result = a;
        b = b / 2;
        while (b > 0) {
            x = multiplyByX(x);
            if (b % 2 == 1)
                result = result ^ x;
            b = b / 2;
        }
        return result;
    }

    private int multiplyByX(int a) {
        if (a >> (m - 1) != 0)
            return a << 1 ^ g;
        return a << 1;
    }


    public void binaryPrint(int a) {
        StringBuffer str;
        str = new StringBuffer();
        for (int j = 0; j < m; j++) {
            str.append(a % 2);
            a = a / 2;
        }
        System.out.println(str.reverse());
    }


    public int powGF(int a, int n) {
        // Быстрое возведение в степень.
        if (n == 0) {
            return 1;
        } else if (n % 2 == 0) {
            return powGF(multiply(a, a), n / 2); // (a*a)^(n/2)
        } else {
            int square = multiply(a, a);
            return multiply(powGF(square, n / 2), a); // a * (a*a)^[n/2]
        }
    }
    // 0 = нулевая степень
    public int powGFByIndex(int a , int b) {
        int res;
        if (a == 0 || b == 0) return 1;
        res = a * b;
        if (res >= arr.length - 1)
            res = res % (arr.length - 1);
        while (res < 0)
            res += arr.length - 1;
        return arr[res + 1];
    }

    int divideByIndex(int a, int b) {
        int diff;
        if (a == 0) return 0;
        if (b == 0) return -1;
        diff = a - b;
        while (diff < 0)
            diff += (int)(pow(2,m)) - 1;
        return diff;
    }

    int divide(int a, int b) {
        int diff;
        if (a == 0) return 0;
        if (b == 0) return -1;
        return multiply(a, inverse(b));
    }

    int inverse(int a) {
        return powGF(a, (int) pow(2, m) - 2);
    }

    public void generateConj() {
        ArrayList<Integer> allElements = new ArrayList<Integer>();
        // минимум классов - (2^m-2)/m
        conjugateEl = new ArrayList<Integer>((int) ((pow(2, m) - 2) / m));
        int a = 1;
        int j;
        while (allElements.size() != pow(2, m) - 2) {
            // минимум елементов в одном классе - наименьший делитель m
            ArrayList<Integer> elements = new ArrayList<Integer>(leastDivisor(m));
            j = a;
            do {
                elements.add(j);
                j = (int) (j * 2 % (pow(2, m) - 1));
            } while (j != a);
            //можно убрать сортировку (она для наглядности)
            elements.sort(null);
            conjugateEl.add(elements.get(0));
            allElements.addAll(elements);
            allElements.sort(null);
            for (int i = 1; i < allElements.size(); i++) {
                if (allElements.get(i) != allElements.get(i - 1) + 1) {
                    a = allElements.get(i - 1) + 1;
                    break;
                }
            }
        }
    }

    public void createParityCheckMatrix(int numberOfClasses) {
        H = new int[numberOfClasses];
        for (int i = 0; i < numberOfClasses; i++) {
            H[i] = conjugateEl.get(i);
        }
    }

    private int leastDivisor(int a) {
        for (int i = 2; i <= sqrt(a); i++) {
            if (a % i == 0)
                return i;
        }
        return a;
    }

    public void printConj() {
        for (int j : conjugateEl) {
            int a = j;
            do {
                System.out.print(j + " ");
                j = (int) (j * 2 % (pow(2, m) - 1));
            } while (j != a);
            System.out.println();
        }
    }

    public void printH() {
        int a;
        for (int i = 0; i < H.length; i++) {
            a = H[i];
            for (int j = 0; j < pow(2, m) - 1; j++) {
                System.out.print((int) ((a * j) % (pow(2, m) - 1) ) + " ");
            }
            System.out.println();
        }
    }

    public int count2T() {
        ArrayList<Integer> elements =
                new ArrayList<Integer>(leastDivisor(m) * H.length);
        int a, j;
        for (int i = 0; i < H.length; i++) {
            j = H[i];
            do {
                elements.add(j);
                j = (int) (j * 2 % (pow(2,m) - 1));
            } while (j != H[i]);
        }
        elements.sort(null);
        System.out.println(elements);
        int maxSeqCnt = 0;
        for (int i = 1, currentSeqCnt = 1; i < elements.size()  ; i++) {
            if ((elements.get(i) != elements.get(i - 1) + 1) ) {
                maxSeqCnt = max(currentSeqCnt,maxSeqCnt);
                currentSeqCnt = 0;
                continue;
            }
            currentSeqCnt++;
        }
        return maxSeqCnt;
    }

    public int addByIndex(int a, int b) {
        return index_of[arr[a+1]^arr[b+1]] - 1;
    }

    public int getM() {
        return m;
    }
}