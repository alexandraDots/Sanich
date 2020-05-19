public class Signature {
    // hash (M)
    public static boolean[] signature(String M) {
        int i;
        while (true) {
            byte m = 5;
            int t = m / 2;
            byte[] message = M.getBytes();
            int [] h = Sha256.toIntArray(Sha256.hash(message));
            LinearSystem system = new LinearSystem();
            if (system.sign(h, t) != null)
                return system.sign(h, t);
        }
    }
}
