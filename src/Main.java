import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.lang.Math.pow;

public class Main {
    public static void main(String[] args) {
        String M = "Hello, I am Sasha";
        Keys keys;
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("keys.dat")))
        {
            keys=(Keys)ois.readObject();
            long time = System.currentTimeMillis();
            Signature s = new Signature(keys);
            boolean b [] =  s.signature(M);
            System.out.println(booleansToInt(b));
            System.out.println(s.i);
            System.out.println(s.checkSignature(b, s.i, M));
            System.out.println("Time in millis" + (System.currentTimeMillis()-time));
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }


    }

    static long booleansToInt(boolean[] arr) {
        long n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
}
