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
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AgenteMonitor extends Agent {
    Map<AID, APE> estacoes = new HashMap<>();
    Map<AID,Posicao> userHistory = new HashMap<>();
    Map<AID,Boolean> userCalling = new HashMap<>();
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
                            if(isOld){
                                sendSignal = (isIn != wasIn) || ( isIn && !didCall && isCalling);
                            }else {
                                sendSignal = isIn;
                            }



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






                }


            }

        }
    }


}
