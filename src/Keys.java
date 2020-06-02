import java.io.Serializable;

import static java.lang.Math.pow;

public class Keys implements Serializable {
    byte m;
    int t;
    private  boolean[][] Pt;
    public boolean[][] Hx;


    private  boolean[][] invertedX;

    protected Keys(byte m, int t, boolean[][] Pt,boolean[][] Hx,boolean[][] invertedX){
        this.Hx = Hx;
        this.invertedX = invertedX;
        this.m = m;
        this.t = t;
        this.Pt = Pt;
    }

     boolean[][] getHx() {
        return Hx;
    }
     boolean[][] getInvertedX() {
        return invertedX;
    }
    boolean[][] getPt() {
        return Pt;
    }

}
