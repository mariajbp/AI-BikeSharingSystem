package Agents;


import Util.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentDescriptor;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.IntStream;

public class AgenteUtilizador extends Agent {
    private Posicao posAtual;
    private Posicao dest;
    private AID monitor;

    public void setup(){

        Object[] args = getArguments();
        posAtual=(Posicao) args[0];
        dest = (Posicao) args[1];
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
    }


    public class UserTicker extends TickerBehaviour {
        public UserTicker(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            posAtual = posAtual.nextStep(dest);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
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
