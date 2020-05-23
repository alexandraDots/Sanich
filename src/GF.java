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
    ArrayList<ArrayList<Integer>> H;

    public GF(byte degree) {
        m = degree;
        g = generatePolynomial(m);
        createParityCheckMatrix();
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
        boolean pp = countParity(a) == countParity(b);
        if (countParity(a ^ b) == pp)
            return a ^ b;
        else
            try {
                throw new Exception("The error has occurred. Predicted parity of sum doesn't match the real one");
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }

    }


    // 0 = нулевая степень
    public int multiplyByIndex(int index1, int index2) {
        int sum;
        if (index1 == -1 || index2 == -1) return 0;
        sum = index1 + index2 - 1;
        if (sum >= (int) (pow(2, m)) - 1)
            sum %= (int) (pow(2, m)) - 1;
        return sum;
    }

    public int multiply(int a, int b) {
        boolean pA, pX = countParity(a);
        boolean pp = true;
        int B = b;
        int result = 0;
        int x = a;
        while (b > 0) {
            pp = pp == (countParity(b%2) | pX);
            if (b % 2 == 1)
                result = add(result, x);
            b = b >> 1;
            pX = pX  ==  countParity(x >> (m - 1));
            x = multiplyByX(x);
            if (pX != countParity(x))
                try {
                    throw new Exception ("Predicted parity of X doesn't match the actual parity of X");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        //System.out.println(pp==countParity(result));
        return result;
    }

    private int multiplyByAlpha(int a) {
        boolean pp = countParity(a) == countParity(a >> (m - 1));
        int result;
        if (a >> (m - 1) != 0)
            result = a << 1 ^ g;
        else
            result = a << 1;
        if (pp == countParity(result))
            return result;
        else
            try {
                throw new Exception("The error has occurred. Predicted parity of alpha module doesn't match the real one");
            } catch (Exception e) {
                e.printStackTrace();
                //System.out.println(e.getMessage());
                return result;
            }
    }

    private int multiplyByX(int a) {
        boolean pp = countParity(a) == countParity(a >> (m - 1));
        int result;
        if (a >> (m - 1) != 0)
            result = a << 1 ^ g;
        else
            result = a << 1;
        if (pp == countParity(result))
            return result;
        else
            try {
                throw new Exception("The error has occurred. Predicted parity of alpha module doesn't match the real one");
            } catch (Exception e) {
                e.printStackTrace();
                //System.out.println(e.getMessage());
                return result;
            }
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
    public int powGFByIndex(int indexOfA, int degree) {
        indexOfA--;
        int res;
        if (indexOfA   == 0 || degree == 0) return 1;
        res = (indexOfA) * degree;
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
            diff += (int) (pow(2, m)) - 1;
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

    public void createParityCheckMatrix() {
        // минимум классов - (2^m-2)/m
        H = new ArrayList<ArrayList<Integer>> ((int) ((pow(2, m) - 2)) / m);
        ArrayList<Integer> allElements = new ArrayList<Integer>();
        int a = 1;
        int j;
        int i = 0;
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
            H.add(i,elements);
            allElements.addAll(elements);
            allElements.sort(null);
            for (int k = 1; k < allElements.size(); k++) {
                if (allElements.get(k) != allElements.get(k - 1) + 1) {
                    a = allElements.get(k - 1) + 1;
                    break;
                }
            }
        }

    }

     int leastDivisor(int a) {
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
        ArrayList<Integer> a;
        for (int i = 0; i < H.size(); i++) {
            a = H.get(i);
            for (int j = 0; j < a.size(); j++) {
                System.out.print(a.get(j) + " ");
            }
            System.out.println();
        }
    }



    // true - even, false - odd
    public boolean countParity(int n) {
        int p = 0;
        while (n > 0) {
            p = p ^ (n % 2);
            n = n >> 1;
        }
        return p == 0;
    }

    public int countEvenParity(int n) {
        int p = 0;
        while (n > 0) {
            p = p + (n % 2);
            n = n >> 1;
        }
        return p;
    }

    public int countOddParity(int n) {
        int p = 0;
        while (n > 0) {
            p = p + (n % 2 == 0 ? 1 : 0);
            n = n >> 1;
        }
        return p;
    }

    public int addByIndex(int a, int b) {
        return index_of[add(arr[a + 1], arr[b + 1])] - 1;
    }

    public int getM() {
        return m;
    }

    public int getHSize() {
        return H.size();
    }
}