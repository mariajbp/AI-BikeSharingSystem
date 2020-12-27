package Util;

import java.io.Serializable;
import java.util.Random;

public class Personalidade implements Serializable {
    private int fitLevel;
    private int wealthLevel;



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
        maxDistance= r.nextInt(100);
        maxPrice= r.nextInt(300);
        done= false;

    }

    public boolean ponder(double dist, int price){
        System.out.println("PERSONA PONDERINg");
        if(done) return  false;
        if(price>maxPrice || dist>maxDistance) return false;
        done = true;
        return true;
    }
}
