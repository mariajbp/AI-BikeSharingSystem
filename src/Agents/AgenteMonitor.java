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

import java.util.HashMap;
import java.util.Map;

public class AgenteMonitor extends Agent {
    Map<AID, APE> estacoes = new HashMap<>();
    Map<AID,Posicao> userHistory = new HashMap<>();
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
            for (DFAgentDescription d : DFService.search(this, df)){
                ACLMessage getAPE = new ACLMessage(ACLMessage.SUBSCRIBE);
                getAPE.addReceiver(d.getName());
                send(getAPE);
                ACLMessage rec;
                while((rec = receive())==null){
                }
                if (rec.getPerformative()==ACLMessage.INFORM)
                 estacoes.put(d.getName(),(APE)rec.getContentObject());
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
                try {
                   p=(Posicao) msg.getContentObject();
                }catch (Exception e){e.printStackTrace();}
                if(userHistory.containsKey(userId)){

                    for(Map.Entry<AID,APE> e : estacoes.entrySet() ){
                        if(e.getValue().isInside(p)!=e.getValue().isInside(userHistory.get(userId))) {
                            ACLMessage retmsg = new ACLMessage(ACLMessage.INFORM);
                            try{
                                Object[] sendCont = new Object[]{"SignalInOutAPE",userId};
                                retmsg.setContentObject(sendCont);
                                retmsg.addReceiver(e.getKey());
                            }catch (Exception ee){ee.printStackTrace();}
                            send(retmsg);
                        }
                    }
                    userHistory.replace(userId,p);
                }
                else {
                    for(Map.Entry<AID,APE> e : estacoes.entrySet() ){
                    if(e.getValue().isInside(p)) {
                        ACLMessage retmsg = new ACLMessage(ACLMessage.INFORM);
                        try{
                            retmsg.setContentObject(userId);
                            retmsg.addReceiver(e.getKey());
                        }catch (Exception ee){ee.printStackTrace();}
                        send(retmsg);
                        }
                    }
                    userHistory.put(userId,p);
                    }

            }

        }
    }


}
