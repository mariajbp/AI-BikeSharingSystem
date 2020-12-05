package Util;

import java.io.Serializable;

public class Posicao implements Serializable {

    float posX;
    float posY;

   public Posicao(float x, float y){
        posX = x;
        posY = y;
    }

    public static int euclideanDistance(Posicao p1, Posicao p2){
        double dist = Math.sqrt(((Math.pow((p1.posX - p2.posX), 2)) + (Math.pow((p1.posY - p2.posY ), 2))));
        return (int) dist ;
    }

    @Override
    public String toString() {
        return "Posicao{" +
                "posX=" + posX +
                ", posY=" + posY +
                '}';
    }

    public int euclideanDistance(Posicao p2){
        double dist = Math.sqrt(((Math.pow((posX - p2.posX), 2)) + (Math.pow((posY - p2.posY ), 2))));
        return (int) dist ;
    }
    public Posicao nextStep(Posicao to){
        float vx = to.posX-posX;
        float vy = to.posY-posY;
        System.out.println("[Posicao-NextStep: From:"+this+"\nTo :"+to+"]\n");
        return  new Posicao(posX + (vx>0? 1 :(vx<0? -1 : 0)),posY + (vy>0? 1 : (vy<0? -1 : 0)));
    }
}
