package Util;
/** Classe com os parâmetros para iniciar o sistema **/
public class ConfigVars {
    /** Tamanho do mapa **/
    public static int MAP_SIZE = 400;
    /** Quantidade de Utilizadores no inicio **/
    public static int INIT_USERS = 30;
    /** Quntidade de Estações **/
    public static int STATION_NUM = 4;
    /** Capacidade máxima das estações **/
    public static int CAP_INIT = 10;
    /** Multiplicador da velocidade da Simulação **/
    public static float SPEED = 0.1f;

    public static int getMapSize() {
        return MAP_SIZE;
    }

}
