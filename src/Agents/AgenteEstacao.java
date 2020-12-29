package Agents;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.MessageTemplate;
import org.apache.commons.codec.binary.Base64;

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
    private int pBase = 100;
    APE ape;
    Posicao pos;
    int capAtual;
    int capLim;
    boolean isFull;
    volatile Map<AID,Boolean> users = new HashMap<>();
    Set<AID> usersComing = new HashSet<>();
    // 0 : Desconto , 1 : Normal, 2 : Expensive
    private Integer[] dealHistory= new Integer[]{0,0,0};


    public void setup(){
        Object[] args = getArguments();
        this.pos=(Posicao) args[1];
        ape =(APE) args[0];
        capLim=(int) args[2];
        capAtual=0;
        isFull=false;


        System.out.println(getAID().getLocalName()+": "+ pos);
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
        addBehaviour(new PursuitUsers(this));
    }

    public int calcProposal(AID user){
        double currentOccup=(double)capAtual/(double)capLim;
        if(currentOccup>0.8){
            dealHistory[2]++;
            return pBase*=1.8;}
        else
            if(currentOccup<0.2) return pBase*=0.2;
                else return pBase;
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
                        msg.setContentObject(ape);
                        }catch (Exception e){e.printStackTrace();}
                        send(msg);
                        break;
                    }
                    case ACLMessage.INFORM:{
                        Object[] msgCont = new Object[0];
                        try {
                            msgCont = (Object[]) msg.getContentObject();
                        } catch (UnreadableException e) {e.printStackTrace();}
                        String subject = (String) msgCont[0];
                        switch (subject){
                            case "SignalInOutAPE":
                                AID userSignal =(AID) msgCont[1];
                                System.out.println(getAID().getLocalName()+": "+ getAID().getHap()+" InOut");
                                if(users.containsKey(userSignal)){
                                    users.remove(userSignal);
                                }else {users.put(userSignal,false);}
                                break;
                            case "callingForProposal":
                                AID user =(AID) msgCont[1];
                                System.out.println(getAID().getLocalName()+": "+ getAID().getHap()+" InOutCALLING");

                                if(users.containsKey(user)){
                                    if(users.get(user))
                                        users.remove(user);
                                    else
                                        users.replace(user,true);
                                }else {users.put(user,true);}
                                break;
                            case "depositBike":
                                AID usr = msg.getSender();
                                usersComing.remove(usr);
                                capAtual++;
                                System.out.println(usr.getLocalName()+" Depositou na "+getAID().getLocalName());
                                break;
                        }
                    break;
                    }

                    case ACLMessage.ACCEPT_PROPOSAL:{
                        AID usr = msg.getSender();
                        try {
                            switch ((int)((Object[]) msg.getContentObject())[0] ){
                                case 20 :
                                    dealHistory[0]++;
                                    break;
                                case 100 :
                                    dealHistory[1]++;
                                    break;
                                case 180 :
                                    dealHistory[2]++;
                                    break;
                            };
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        usersComing.add(usr);
                        break;
                    }
                    case ACLMessage.REJECT_PROPOSAL:{

                        break;
                    }
                    case ACLMessage.REQUEST:{
                        Object[] cont = null;
                        try {
                            cont = (Object[]) msg.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        String sss= (String) cont[0];
                        switch (sss){
                            case "Stats":
                                msg = msg.createReply();
                                msg.setPerformative(ACLMessage.INFORM);
                                Object[] sendObj = new Object[]{
                                        "Stats",
                                        capAtual/capLim,
                                        dealHistory
                                };
                                try {
                                    msg.setContentObject(sendObj);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                send(msg);
                                break;
                            case "Users":
                                break;
                            case "UserLost":
                                AID usr =(AID) cont[1];
                                System.out.println("USERLOST: "+ usr +" STATION");
                                users.put(usr, false);  // para parar de o perseguir e só mandar este inform?
                                int i = calcProposal(usr);

                                msg = new ACLMessage(ACLMessage.PROPOSE);

                                sendObj = new Object[]{
                                        "backupStation",
                                        pos,
                                        i
                                };
                                try {
                                    msg.setContentObject(sendObj);
                                } catch (IOException e){e.printStackTrace();}
                                msg.addReceiver(usr);
                                send(msg);
                                System.out.println("SENT");
                                break;
                        }
                        break;
                    }
                    case ACLMessage.CONFIRM:
                        AID id = new AID();
                        try {
                            id =(AID) (msg.getContentObject());
                        } catch (UnreadableException e) {e.printStackTrace();}

                        if(users.containsKey(id))
                            users.remove(id);
                        break;
                }
            }


        }
    }

    public class PursuitUsers extends TickerBehaviour {


        public PursuitUsers(Agent a) {
            super(a, 1000);
        }

        @Override
        protected void onTick() {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            users.forEach((usr,isRdy)->{
                if(isRdy){
                    int i = calcProposal(usr);
                    Object[] cont = new Object[]{
                            "proposeDelivery",
                            pos,
                            i
                    };
                    try {
                        msg.setContentObject(cont);
                    } catch (IOException e){e.printStackTrace();}
                    msg.addReceiver(usr);

                }
            });
            if(msg.getAllReceiver().hasNext())
                send(msg);
        }

    }
}
