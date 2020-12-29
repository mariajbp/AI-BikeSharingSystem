package Agents;

import Util.APE;
import Util.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AgenteMonitor extends Agent {
    private Map<AID, APE> estacoes = new HashMap<>();
    private HashMap<AID,Posicao> userHistory = new HashMap<>();
    private Map<AID,Boolean> userCalling = new HashMap<>();

    public void setup(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("monitor");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        DFAgentDescription df = new DFAgentDescription();
        ServiceDescription s = new ServiceDescription();
        s.setType("station");
        df.addServices(s);
        try{
            DFAgentDescription[] sts = DFService.search(this, df);
            for (DFAgentDescription d : DFService.search(this, df)){
                ACLMessage getAPE = new ACLMessage(ACLMessage.SUBSCRIBE);
                getAPE.addReceiver(d.getName());
                send(getAPE);
                ACLMessage rec;
                while((rec = receive())==null){
                }
                if (rec.getPerformative()==ACLMessage.INFORM)
                 estacoes.put(d.getName(),(APE) rec.getContentObject());
            }
        }catch (Exception e){e.printStackTrace();}


        addBehaviour(new MonitorBehavior());
    }


    public class MonitorBehavior extends CyclicBehaviour{

        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg!=null){
                Posicao p = new Posicao(0,0);
                AID userId = msg.getSender();
                switch (msg.getPerformative()){
                    case ACLMessage.CFP:
                    case ACLMessage.INFORM:
                            try {
                                p=(Posicao) msg.getContentObject();
                            }catch (Exception e){e.printStackTrace();}

                            Posicao last= userHistory.getOrDefault(userId, p);
                            boolean isOld = userHistory.containsKey(userId);
                            boolean isCalling =msg.getPerformative()==ACLMessage.CFP;
                            boolean didCall = userCalling.getOrDefault(userId,false);
                            boolean sendSignal;

                            String s = isCalling? "callingForProposal": "SignalInOutAPE";

                            for(Map.Entry<AID,APE> e : estacoes.entrySet() ){
                                boolean isIn = e.getValue().isInside(p);
                                boolean wasIn =e.getValue().isInside(last);
                                if(isOld)
                                    sendSignal = (isIn != wasIn) || ( isIn && !didCall && isCalling);
                                else
                                    sendSignal = isIn;

                                if(sendSignal) {
                                    ACLMessage retmsg = new ACLMessage(ACLMessage.INFORM);
                                    try{
                                        Object[] sendCont = new Object[]{s,userId};
                                        retmsg.setContentObject(sendCont);
                                        retmsg.addReceiver(e.getKey());
                                    }catch (Exception ee){ee.printStackTrace();}
                                    send(retmsg);
                                }
                            }
                            if(isOld){
                                userHistory.replace(userId,p);
                                userCalling.replace(userId,isCalling);
                            }
                            else {
                                userHistory.put(userId, p);
                                userCalling.put(userId,isCalling);
                            }
                            break;
                    case ACLMessage.REQUEST:
                            Object[] cont = null;

                            try {
                                cont = (Object[]) msg.getContentObject();
                            } catch (UnreadableException e) {e.printStackTrace();}
                            String ss =(String) cont[0];
                            AID usr = msg.getSender();
                            msg = msg.createReply();
                            msg.setPerformative(ACLMessage.INFORM);

                            switch (ss){
                                case "Stations":
                                        HashMap<AID,Posicao> sendThis= new HashMap<>();
                                        estacoes.forEach((aid,ape) -> {
                                            sendThis.put(aid,ape.getPosicao());
                                        });

                                        try {
                                            msg.setContentObject(sendThis);
                                        }catch (Exception e){e.printStackTrace();}
                                        break;
                                case "Users":
                                        try {
                                            msg.setContentObject(userHistory);
                                        }catch (Exception e){e.printStackTrace();}
                                    break;
                                case "UserLost":
                                    Object[] cont2 = new Object[]{"UserLost", usr};
                                    Posicao usrPos  = (Posicao) cont[1];
                                    msg = new ACLMessage(ACLMessage.REQUEST);
                                    try {
                                        msg.setContentObject(cont2);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    AID st = null;
                                    double dist = 100000;
                                    for(Map.Entry<AID,APE> id : estacoes.entrySet()){
                                        if((id.getValue().getPosicao().euclideanDistance(usrPos)) < dist) {
                                            dist = id.getValue().getPosicao().euclideanDistance(usrPos);
                                            st = id.getKey();
                                        }
                                    }
                                    msg.addReceiver(st);
                                    break;
                            }
                            send(msg);
                        break;
                    case ACLMessage.CONFIRM:
                        cont = null;
                        try {
                            cont = (Object[]) (msg.getContentObject());
                        } catch (UnreadableException e) {e.printStackTrace();}
                        s = (String) cont[0];

                        msg = new ACLMessage(ACLMessage.CONFIRM);
                        if(s.equals("haveStation")) {
                            for (AID id : estacoes.keySet()) {
                                try {
                                    msg.setContentObject(userId);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                msg.addReceiver(id);
                            }
                            msg.removeReceiver((AID) cont[1]);
                            send(msg);
                        } else {
                            userHistory.remove(cont[1]);
                            userCalling.remove(cont[1]);
                        }
                        break;
                }
            }

        }
    }


}
