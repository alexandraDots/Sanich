import static java.lang.Math.pow;

public final class Signature {
    // hash (M)
    static int i = 0;
    static byte m = 6;
    static int t = m / 2;

    public static boolean[] signature(String M) {
        byte[] message = M.getBytes();
        int[] h = toIntArray(Sha256.hash(message));
        LinearSystem system = new LinearSystem(m);
        boolean [] b;
        while (true) {
            if ((b = system.sign(h, t) )!= null)
                return b;
            h = toIntArray(Sha256.hash(toByteArray(h)));
            i++;
            if (i == (13484))
                System.out.println();

        }
    }

    private static int[] toIntArray(byte[] byteArr) {
        int[] intArr = new int[byteArr.length];
        for (int i = 0; i < byteArr.length; i++) {
            intArr[i] = Math.abs(byteArr[i]) % (int) pow(2, m);
        }
        return intArr;
    }

    private static byte[] toByteArray(int[] intArr) {
        byte[] byteArr = new byte[intArr.length];
        for (int i = 0; i < byteArr.length; i++) {
            byteArr[i] = (byte) intArr[i];
        }
        return byteArr;
    }
}
