import static java.lang.Math.pow;

public final class Signature {
    static int i = 1;
    static byte m = 5;
    static int t = 2;
    static LinearSystem system = new LinearSystem(m);


    public static boolean[] signature(String M) {
        byte[] message = M.getBytes();
        int[] h = toIntArray(Sha256.hash(message));
        int [] s = new int [h.length + 1];
        boolean [] b;
        while (true) {
            s = new int [h.length + 1];
            System.arraycopy(h,0,s,0, h.length);
            s[s.length - 1] = i;
            s = toIntArray(Sha256.hash(toByteArray(s)));
            if ((b = system.sign(s, t) )!= null)
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
        return system.check(e,s,t);

    }

    // 4 bytes become 1 int  modulo 2^m
    private static int[] toIntArray(byte[] byteArr) {
        int[] intArr = Sha256.toIntArray(byteArr);
        for (int i = 0; i < intArr.length; i++) {
            intArr[i] = Math.abs(intArr[i]) % (int) pow(2, m);
        }
        return intArr;
    }

    // 1 int becomes 4 bytes
    private static byte[] toByteArray(int[] intArr) {
        byte[] byteArr = Sha256.toByteArray(intArr);
        return byteArr;
    }
}
