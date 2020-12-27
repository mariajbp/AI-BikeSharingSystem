package Util;

import jade.core.AID;

import java.util.*;

public class Mapa {
    private int size;
    private Map<AID,Posicao> users;
    private Map<AID,Posicao> stations;

    public Mapa(int size) {
        this.size = size;
        this.users = new HashMap<>();
        this.stations = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Mapa{" +
                "size=" + size +
                ", users=" + users +
                ", stations=" + stations +
                '}';
    }

    public void setUsers(Map<AID, Posicao> users) {
        this.users = users;
    }

    public void setStations(Map<AID, Posicao> stations) {
        this.stations = stations;
    }
    public Set<AID> getStationSet() {
        return stations.keySet();
    }
}
