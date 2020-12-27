package Util;

public class ConfigVars {

    public static int MAP_SIZE = 200;
    public static int MAX_USERS = 10;
    public static int BIKE_NUM = 5;
    public static int STATION_NUM = 3;

    static void changeMap(int size, int nStations)
    {
        MAP_SIZE = size;
        STATION_NUM = nStations;
    }
}
