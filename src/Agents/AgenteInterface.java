package Agents;


import Util.Mapa;
import Util.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AgenteInterface extends Agent {
    private Mapa mapa;
    private AID monitor;
    private HashMap<AID,Integer> stationOccupation = new HashMap<>();
    private HashMap<AID, Integer[]> stationPricing = new HashMap<>();

    @Override
    protected void setup() {
        super.setup();
        mapa = new Mapa(100);

        DFAgentDescription df = new DFAgentDescription();
        ServiceDescription s = new ServiceDescription();
        s.setType("monitor");
        df.addServices(s);
        try {
             monitor = DFService.search(this, df)[0].getName();
        }catch (Exception e){e.printStackTrace();}

        ////Stations
        ACLMessage getStas = new ACLMessage(ACLMessage.REQUEST);
        getStas.addReceiver(monitor);
        try {
            getStas.setContentObject(new Object[]{"Stations"});
        } catch (Exception e) {e.printStackTrace();}
        send(getStas);


        ACLMessage recStas = receive();
        while((recStas=receive())==null){}
        try {mapa.setStations((Map<AID, Posicao>) recStas.getContentObject());} catch (UnreadableException e){e.printStackTrace();}
        System.out.println(mapa);
        ////Users
        ACLMessage getUsers = new ACLMessage(ACLMessage.REQUEST);
        getUsers.addReceiver(monitor);
        try {
            getUsers.setContentObject(new Object[]{"Users"});
        } catch (IOException e) {e.printStackTrace();}
        send(getUsers);


        ACLMessage recUsers = receive();
        while((recUsers = receive())==null){}
        try {
                mapa.setUsers((Map<AID, Posicao>) (recUsers.getContentObject()));
        } catch (UnreadableException e) {e.printStackTrace();}

        System.out.println(mapa);
        addBehaviour(new InterfaceBehavior(this));
        addBehaviour(new InterfRecBehavior());
    }


    public class InterfaceBehavior extends TickerBehaviour {

        public InterfaceBehavior(Agent a) {
            super(a, 2000);
        }

        @Override
        protected void onTick() {
            ACLMessage reqAllStats = new ACLMessage(ACLMessage.REQUEST);
            try {
                reqAllStats.setContentObject(new Object[]{"Stats"});
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(AID st : mapa.getStationSet()){
                reqAllStats.addReceiver(st);
            }
            send(reqAllStats);
            //System.out.println ("Occupation"+ stationOccupation);

        }



    }

    private class InterfRecBehavior extends CyclicBehaviour {


        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg!=null){
                AID sender = msg.getSender();
                switch (msg.getPerformative()){
                    case ACLMessage.INFORM:{
                        try {
                            Object[] cnt =(Object[]) msg.getContentObject();

                        switch ((String)cnt[0]){
                            case "Stats":
                                stationOccupation.put(sender,(int)cnt[1]);
                                stationPricing.put(sender,(Integer[]) cnt[2] );
                                break;
                            case "Stations" :
                                break;
                            case "Users" :
                                break;
                        }
                        } catch (UnreadableException e) {e.printStackTrace();}
                        break;
                    }
                }
            }
        }
    }
}
