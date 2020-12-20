package Util;

import jade.core.AID;

import javax.swing.text.Position;
import java.io.Serializable;
import java.rmi.server.UID;
import java.util.List;
import java.util.Set;

public class APE implements Serializable {
    int raio;
    Posicao pos;


    public APE( Posicao pos) {
        this.raio=100;
        this.pos=pos;
    }


    public int getRaio() {return raio;}

    public void setRaio(int raio) {this.raio = raio;}

    @Override
    public String toString() {
        return "APE{" +
                "raio=" + raio +
                ", pos=" + pos +
                '}';
    }

    public boolean isInside(Posicao p){

        return (pos.euclideanDistance(p)<raio);
    }



}
