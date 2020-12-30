package Util;

public class ConfigVars {

    public static int MAP_SIZE = 200;
    public static int INIT_USERS = 5;
    public static int STATION_NUM = 3;
    public static int CAP_INIT = 30;

    public static float SPEED = 0.1f;

    static void changeMap(int size, int nStations)
    {
        MAP_SIZE = size;
        STATION_NUM = nStations;
    }

    public static float getSPEED() {
        return SPEED;
    }

    public static int getMapSize() {
        return MAP_SIZE;
    }

    public static int getInitUsers() {
        return INIT_USERS;
    }

    public static int getStationNum() {
        return STATION_NUM;
    }

    public static int getCapInit() {
        return CAP_INIT;
    }
}
