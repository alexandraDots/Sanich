import static java.lang.Math.pow;

public class Main {
    public static void main(String[] args) {
        String M = "Hello, I am Sasha";
        boolean [] signature = Signature.signature(M);
        System.out.println(booleansToInt(signature));
        System.out.println(Signature.i);
        System.out.println(Signature.checkSignature(signature,Signature.i,M));
    }

    static long booleansToInt(boolean[] arr) {
        long n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
}
