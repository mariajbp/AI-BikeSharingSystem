package Agents;


import Util.Personalidade;
import Util.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;

public class AgenteUtilizador extends Agent {
    private Posicao posInicial;
    private Posicao posAtual;
    private Posicao dest;
    private double dist2dest;
    private AID monitor;
    private AID deliveryStation = null;
    private boolean stay = false;
    private boolean arriving = false;
    final Personalidade persona = new Personalidade();

    public void setup(){
        Object[] args = getArguments();
        posAtual= posInicial =(Posicao) args[0];
        dest = (Posicao) args[1];
        System.out.println(getAID().getLocalName()+"From:"+posAtual+": Goal:"+ dest+persona);
        dist2dest = dest.euclideanDistance(posAtual);

        DFAgentDescription dfd= new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("monitor");
        dfd.addServices(sd);
        try {
           monitor = DFService.search(this, dfd)[0].getName();
        } catch (FIPAException e) {
            e.printStackTrace();
        }


        addBehaviour(new UserTicker(this,1000));
        addBehaviour(new UserReceiver(this));
    }


    public class UserTicker extends TickerBehaviour {
        public UserTicker(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            Posicao pa = posAtual;
            //System.out.println(getAID().getLocalName()+" : "+posAtual);
            if(!stay && (posAtual = posAtual.nextStep(dest)).equals(pa)){
                if(deliveryStation!=null) {
                    System.out.println(getAID().getLocalName() + ": FinalDest");
                    stay = true;
                    addBehaviour(new OneShotDeliver());
                }
                else {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    Object[] cont = new Object[]{"UserLost", posAtual};
                    try {
                        msg.setContentObject(cont);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    msg.addReceiver(monitor);
                    send(msg);
                }
            }
            else {
                ACLMessage msg;
                if (!arriving&&posAtual.euclideanDistance(dest) < (dist2dest * 1 / 4))
                    msg = new ACLMessage(ACLMessage.CFP);
                else
                    msg = new ACLMessage(ACLMessage.INFORM);

                msg.addReceiver(monitor);
                try {
                    msg.setContentObject(posAtual);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                send(msg);
            }
        }
    }



    public class UserReceiver extends CyclicBehaviour {
        public UserReceiver(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                switch (msg.getPerformative()) {
                    case ACLMessage.PROPOSE:
                        System.out.println(getAID().getLocalName() + " : PROPOSAL REC");
                        Object[] cont = new Object[0];
                        try {
                            cont = (Object[]) msg.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        String s = (String) cont[0];
                        AID st = msg.getSender();
                        msg = msg.createReply();

                        switch (s) {
                            case "proposeDelivery":
                                if (!posInicial.equals(cont[1]) && persona.ponder(posAtual.euclideanDistance((Posicao) cont[1]), (int) cont[2])) {
                                    ACLMessage msg2 = new ACLMessage(ACLMessage.CONFIRM);
                                    msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                    try {
                                        msg.setContentObject(new Object[]{cont[2]});
                                        msg2.setContentObject(st);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    deliveryStation = st;
                                    dest = (Posicao) cont[1];
                                    arriving = true;
                                    msg2.addReceiver(monitor);
                                    send(msg2);
                                } else
                                    msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                send(msg);
                                break;

                            case "backupStation":
                                ACLMessage msg2 = new ACLMessage(ACLMessage.CONFIRM);
                                msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                try {
                                    msg.setContentObject(new Object[]{cont[2]});
                                    msg2.setContentObject(st);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                deliveryStation = st;
                                dest = (Posicao) cont[1];
                                arriving = true;

                                msg2.addReceiver(monitor);
                                send(msg2);
                                send(msg);
                                break;
                        }
                        break;

                }
            }


        }
    }

    private class OneShotDeliver extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(deliveryStation);
            Object[] cnt = new Object[]{"depositBike"};

            try {
                msg.setContentObject(cnt);
            } catch (IOException e) {e.printStackTrace();}
            if(msg.getAllReceiver().hasNext())
                send(msg);
            System.out.println(getAID().getLocalName()+": RIP");

            doDelete();

        }
    }
}
