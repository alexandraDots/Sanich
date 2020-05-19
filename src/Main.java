import static java.lang.Math.pow;

public class Main {
    public static void main(String[] args) {
        byte m = 5;
               System.out.println(booleansToInt( Signature.signature("Hello, I am Sasha")));
    }
    static int booleansToInt(boolean[] arr){
        int n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
}
