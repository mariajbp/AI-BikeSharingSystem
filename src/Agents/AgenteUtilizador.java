package Agents;


import Util.ConfigVars;
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
    /** Distancia inicial para o destino **/
    private double dist2dest;
    private AID monitor;
    private AID deliveryStation;
    /** Flag que indica que o utilizador chegou ao seu destino **/
    private boolean stay;
    /** Flag que indica que o utilizador encontrou estação **/
    private boolean arriving;
    private Personalidade persona;

    public void setup(){
        Object[] args = getArguments();
        posAtual= posInicial =(Posicao) args[0];
        dest = (Posicao) args[1];
        dist2dest = dest.euclideanDistance(posAtual);
        deliveryStation = null;
        stay = false;
        arriving = false;
        persona = new Personalidade();
        /** Registar o Utilizador no DF **/
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("utilizador");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        /**Procurar o Monitor**/
        dfd= new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType("monitor");
        dfd.addServices(sd);
        try {
           monitor = DFService.search(this, dfd)[0].getName();
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new UserTicker(this));
        addBehaviour(new UserReceiver(this));
    }

    /**Behavior base que define cada passo do Utilizador**/
    public class UserTicker extends TickerBehaviour {
        public UserTicker(Agent a) {
            super(a, (long)(((float)1000) * ConfigVars.SPEED));
        }

        @Override
        protected void onTick() {
            Posicao pa = posAtual;
             if(!stay && (posAtual = posAtual.nextStep(dest)).equals(pa)){
                if(deliveryStation!=null) {
                    stay = true;
                    addBehaviour(new OneShotDeliver());
                }
                else {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    Object[] cont = new Object[]{"UserLost", posAtual};
                    try {
                        msg.setContentObject(cont);
                    } catch (IOException e) {e.printStackTrace();}
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
                } catch (IOException e) {e.printStackTrace();}
                send(msg);
            }
        }
    }


    /**Behavior base para receber mensagens**/
    public class UserReceiver extends CyclicBehaviour {
        public UserReceiver(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                switch (msg.getPerformative()) {
                    /** Receber propostas das estações **/
                    case ACLMessage.PROPOSE:
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
                            /** Receber propostas das estações em situaçao normal **/
                            case "proposeDelivery":
                                if (!posInicial.equals(cont[1]) && persona.ponder(posAtual.euclideanDistance((Posicao) cont[1]), (int) cont[2])) {
                                    ACLMessage msg2 = new ACLMessage(ACLMessage.CONFIRM);
                                    msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                    try {
                                        msg.setContentObject(new Object[]{cont[2]});
                                        msg2.setContentObject(new Object[]{"haveStation",st});
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
                            /** Receber propostas das estações quando em estado de emergência**/
                            case "backupStation":
                                ACLMessage msg2 = new ACLMessage(ACLMessage.CONFIRM);
                                msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                try {
                                    msg.setContentObject(new Object[]{cont[2]});
                                    msg2.setContentObject(new Object[]{"haveStation",st});
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
    /** Behavior para depositar a bicicleta **/
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

            msg = blockingReceive();
            String s;
            try {
                cnt = (Object[]) msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            s = (String) cnt[0];
            if(s.equals("wait")){
                blockingReceive();
            }

            doDelete();

        }
    }
}
