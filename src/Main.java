import static java.lang.Math.pow;

public class Main {
    public static void main(String[] args) {
        System.out.println(booleansToInt(Signature.signature("Hello, I am Sasha")));
        System.out.println(Signature.i);
    }

    static long booleansToInt(boolean[] arr) {
        long n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
}
