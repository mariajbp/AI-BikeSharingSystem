public class ConfigVars {

    static int MAP_SIZE = 200;
    static int MAX_USERS = 1;
    static int BIKE_NUM = 5;
    static int STATION_NUM = 3;

    static void changeMap(int size, int nStations)
    {
        MAP_SIZE = size;
        STATION_NUM = nStations;
    }
}
