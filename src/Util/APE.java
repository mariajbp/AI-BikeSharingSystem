package Util;

import java.io.Serializable;

public class APE implements Serializable {
    int raio;
    Posicao pos;


    public APE( Posicao pos) {
        this.raio=100;
        this.pos=pos;
    }


    @Override
    public String toString() {
        return "APE{" +
                "raio=" + raio +
                ", pos=" + pos +
                '}';
    }

    public boolean isInside(Posicao p){
        return (pos.euclideanDistance(p) < raio);
    }


    public Posicao getPosicao() {
        return pos;
    }
}
