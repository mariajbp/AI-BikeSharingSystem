package Util;

public class Posicao {

    float posX;
    float posY;

    Posicao(float x, float y){
        posX = x;
        posY = y;
    }

    public static int euclideanDistance(Posicao p1, Posicao p2){
        double dist = Math.sqrt(((Math.pow((p1.posX - p2.posX), 2)) + (Math.pow((p1.posY - p2.posY ), 2))));
        return (int) dist ;
    }
}
