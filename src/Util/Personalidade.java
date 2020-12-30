package Util;

import java.io.Serializable;
import java.util.Random;

public class Personalidade implements Serializable {
    private int maxDistance;
    private int maxPrice;
    private boolean done;

    @Override
    public String toString() {
        return "Personalidade{" +
                "maxDistance=" + maxDistance +
                ", maxPrice=" + maxPrice +
                ", done=" + done +
                '}';
    }

    public Personalidade() {
        Random r = new Random();
        maxDistance= r.ints(50,100).findAny().getAsInt();
        maxPrice= r.ints(20,180).findAny().getAsInt();
        done= false;

    }

    public boolean ponder(double dist, int price){

        if(done) return  false;
        if(price>maxPrice || dist>maxDistance) return false;
        done = true;
        return true;
    }
}
