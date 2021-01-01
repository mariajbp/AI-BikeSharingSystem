package Util;

import java.io.Serializable;
/** Estrutura representativa de uma posição no mapa **/
public class Posicao implements Serializable {

   public float posX;
   public float posY;

   public Posicao(float x, float y){
        posX = x;
        posY = y;
    }

    @Override
    public String toString() {
        return "Posicao{" +
                "posX=" + posX +
                ", posY=" + posY +
                '}';
    }
    /** Calculo da distancia euclidiana desta posição a uma outra **/
    public double euclideanDistance(Posicao p2){
        double dist = Math.sqrt(((Math.pow((posX - p2.posX), 2)) + (Math.pow((posY - p2.posY ), 2))));
        return  dist ;
    }

    public boolean equals(Posicao o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Posicao posicao = (Posicao) o;
        return Float.compare(posicao.posX, posX) == 0 &&
                Float.compare(posicao.posY, posY) == 0;
    }
    /** Caclulo da proxima posição após dar um passo em direção a um destino **/
    public Posicao nextStep(Posicao to){
       if(this.equals(to)) {return to;}
       double dist = euclideanDistance(to);
        if (Math.abs(dist)<1) return to;
       double vx = (to.posX-posX)/dist;
       double vy = (to.posY-posY)/dist;

       return  new Posicao((float) vx+posX,(float) vy+posY);
    }


}
