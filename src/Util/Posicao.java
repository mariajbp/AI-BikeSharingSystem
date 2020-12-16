package Util;

import java.io.Serializable;
import java.util.Objects;

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

    public boolean equals(Posicao o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Posicao posicao = (Posicao) o;
        return Float.compare(posicao.posX, posX) == 0 &&
                Float.compare(posicao.posY, posY) == 0;
    }

    public Posicao nextStep(Posicao to){
       if(this.equals(to)) {return to;}

        float vx = to.posX-posX;
        float vy = to.posY-posY;
        return  new Posicao(posX + (vx>0? 1 :(vx<0? -1 : 0)),posY + (vy>0? 1 : (vy<0? -1 : 0)));
    }

    public boolean is34ths(Posicao to){
        if(this.equals(to)) {return true;}

        float vx = to.posX-posX;
        float vy = to.posY-posY;
        return  (vx!=0& vy!=0);
    }
}
