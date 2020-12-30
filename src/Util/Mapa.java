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
        Set<String> usrs = new TreeSet<>();
        Set<String> stas = new TreeSet<>();
        for(Map.Entry<AID,Posicao> e : users.entrySet()){
            usrs.add("["+e.getKey().getLocalName()+ ": "+ e.getValue() +"]");
        }
        for(Map.Entry<AID,Posicao> e : stations.entrySet()){
            stas.add("["+e.getKey().getLocalName()+ ": "+ e.getValue() +"]");
        }
        return "Mapa{" +
                "size=" + size +
                ", users=" + usrs + '\n'+
                ", stations=" + stas +
                '}';
    }

    public int getNumUsers() {
        return users.size();
    }

    public void setUsers(Map<AID, Posicao> users) {
        this.users = users;
    }
    public void updateUser(AID usr, Posicao pos){
        if(this.users.containsKey(usr)) this.users.replace(usr, pos);
            else this.users.put(usr, pos);
    }
    public void setStations(Map<AID, Posicao> stations) {
        this.stations = stations;
    }
    public Set<AID> getStationSet() {
        return stations.keySet();
    }
}
