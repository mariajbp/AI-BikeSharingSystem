package Util;

import jade.core.AID;

import java.util.*;
/** Mapa de posiçoes dos agentes **/
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
    /** Quantidade de Utilizadores ativos **/
    public int getNumUsers() {
        return users.size();
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
