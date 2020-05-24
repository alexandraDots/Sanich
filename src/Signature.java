import java.awt.*;
import java.util.Collection;

import static java.lang.Math.pow;
import static java.util.Collections.binarySearch;

public final class Signature {
    static int i = 1;
    static byte m = 5;
    static int t = 2;
    static LinearSystem system = new LinearSystem(m);


    public static boolean[] signature(String M) {
        byte[] message = M.getBytes();
        int[] h = toIntArray(Sha256.hash(message));
        int [] s;
        int [] Syndrome;
        boolean [] b;
        while (true) {
            s = new int [h.length + 1];
            System.arraycopy(h,0,s,0, h.length);
            s[s.length - 1] = i;
            s = toIntArray(Sha256.hash(toByteArray(s)));
            Syndrome = generateSyndrome(s);
            if (Syndrome == null){
                i++;
                continue;
            }
            if ((b = system.sign(Syndrome, t) )!= null)
                return b;
            i++;
        }
    }

    public static boolean checkSignature(boolean [] e, int j, String M){
        byte[] message = M.getBytes();
        int [] h = toIntArray(Sha256.hash(message));
        int [] s = new int [h.length + 1];
        System.arraycopy(h,0,s,0, h.length);
        s[s.length - 1] = j;
        s = toIntArray(Sha256.hash(toByteArray(s)));
        s = generateSyndrome(s);
        return system.check(e,s,t);

    }

    static int[] generateSyndrome(int S[]){
        int s;
        int[] Syndrome = new int[2 * t];
            boolean[] H = new boolean[system.field.H.size()];
            // Sj
            for (int j = 1; j <= Syndrome.length; j++) {
                //S[0]=S1
                if ((j) % 2 == 0)
                    Syndrome[j - 1] = system.field.powGF(Syndrome[j / 2 - 1], 2);
                else {
                    s = S[j/2 + 1];
                    for (int i = 0; i < system.field.H.size(); i++ ){
                        if (H[i] && (binarySearch(system.field.H.get(i),s) != -1))
                            return null;
                    }
                    H[system.field.getRow(s)] = true;
                    Syndrome [j-1] = s;
                }
            }
            return Syndrome;
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

}
