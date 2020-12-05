package Agents;

import java.util.*;

import Util.APE;
import Util.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import javax.swing.text.Position;

public class AgenteEstacao extends Agent {
    APE ape;
    Posicao pos;
    int capAtual;
    int capLim;
    boolean isFull;
    Set<AID> users = new TreeSet<>();

    public AgenteEstacao(APE ap, Posicao pos, int maxCap){
        this.pos=pos;
        ape = ap;
        capLim=maxCap;
        capAtual=0;
        isFull=false;
    }
    public AgenteEstacao(){
        PrimitiveIterator.OfInt rd = new Random().ints(0,100).iterator();
        this.pos=new Posicao(rd.nextInt(),rd.nextInt());
        //DEBUG ONLY
        this.pos=new Posicao(50,50);

        ape = new APE(100,pos);
        capLim=100;
        capAtual=0;
        isFull=false;
    }

    public void userEnter(AID aid){
        users.add(aid);
    }
    public void userExit(AID aid) {
        users.remove(aid);
    }

    public void setup(){

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("station");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new RecieveStation());
    }


    public class RecieveStation extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg!=null){
                switch (msg.getPerformative()){
                    case ACLMessage.SUBSCRIBE:{
                        msg =msg.createReply();
                        msg.setPerformative(ACLMessage.INFORM);
                        try{
                        msg.setContentObject(ape);}catch (Exception e){e.printStackTrace();}
                        send(msg);
                        break;
                    }
                    case ACLMessage.INFORM:{
                        try {
                            users.add((AID) msg.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }


        }
    }
}
