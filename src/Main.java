import static java.lang.Math.pow;

public class Main {
    public static void main(String[] args) {
        String M = "Heeeeeeeeello, I am Sasha";
        //GF field = new GF ((byte) 5);
        Signature s = new Signature();
        boolean b [] =  s.signature(M);
        System.out.println(booleansToInt(b));
        System.out.println(s.i);
        System.out.println(Signature.checkSignature(b, Signature.i, M));

    }

    static long booleansToInt(boolean[] arr) {
        long n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
}
