package Agents;

import Util.APE;
import Util.ConfigVars;
import Util.Posicao;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class AgenteEstacao extends Agent {
    /** Preço base da Estação**/
    private int pBase;
    private APE ape;
    private AID monitor;
    private Posicao pos;
    private int capAtual;
    private int capLim;
    /** Contador de Utilizadores criados **/
    private int createdUsers;
    /** Utilizadores na APE **/
    private Map<AID,Boolean> users = new HashMap<>();
    /** Utilizadores em espera pra deixar a bicicleta **/
    private Queue<AID> usersWaiting = new LinkedList<>();
    /** Histórico de transações
     * 0 : Desconto , 1 : Normal, 2 : Inflacionado **/
    private Integer[] dealHistory= new Integer[]{0,0,0};


    public void setup(){
        Object[] args = getArguments();
        pBase = 100;
        this.pos = (Posicao) args[1];
        ape = (APE) args[0];
        capLim = (int) args[2];
        capAtual =(int) (2*(float)capLim/3.f);
        createdUsers = 0;
        monitor = null;
        /** Registar Estação no DF **/
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
        addBehaviour(new CreateUsers(this));
    }
    /** Calculo do preço para depositar a bicicleta **/
    public int calcProposal(){
        double currentOccup= ((double)capAtual)/((double)capLim);
        if(currentOccup>0.8){
            return (int) (pBase*1.8);}
        else
            if(currentOccup<0.2) return (int) (pBase*0.2);
                else return pBase;
    }

    /** Behavior base para receber mensagens **/
    public class RecieveStation extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg!=null){
                switch (msg.getPerformative()){
                    /** Informar o Monitor da APE e posição desta estação **/
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
                            /** Assinalar entrada ou saida na APE de um Utilizador **/
                            case "SignalInOutAPE":
                                AID userSignal =(AID) msgCont[1];
                                if(users.containsKey(userSignal)){
                                    users.remove(userSignal);
                                }else {users.put(userSignal,false);}
                                break;
                            /** Assinalar entrada ou saida na APE de um Utilizador disposto a receber proposals **/
                            case "callingForProposal":
                                AID user =(AID) msgCont[1];

                                if(users.containsKey(user)){
                                    if(users.get(user))
                                        users.remove(user);
                                    else
                                        users.replace(user,true);
                                }else {users.put(user,true);}
                                break;
                            /** Pedido do Utilizador para entregar a bicicleta **/
                            case "depositBike":
                                AID usr = msg.getSender();
                                ACLMessage msg2;
                                if(capAtual < capLim) {
                                    capAtual++;
                                    System.out.println(usr.getLocalName() + " Depositou na " + getAID().getLocalName());
                                    msg = new ACLMessage(ACLMessage.INFORM);
                                    try {
                                        msg.setContentObject(new Object[]{"done"});
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    msg.addReceiver(usr);
                                } else {
                                    usersWaiting.offer(usr);
                                    msg = new ACLMessage(ACLMessage.INFORM);
                                    try {
                                        msg.setContentObject(new Object[]{"wait"});
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    msg.addReceiver(usr);
                                }
                                msg2 = new ACLMessage(ACLMessage.CONFIRM);
                                try {
                                    msg2.setContentObject(new Object[]{"received", usr});
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if(monitor == null) {
                                    DFAgentDescription dfd= new DFAgentDescription();
                                    ServiceDescription sd = new ServiceDescription();
                                    sd.setType("monitor");
                                    dfd.addServices(sd);
                                    try {
                                        monitor = DFService.search(myAgent, dfd)[0].getName();
                                    } catch (FIPAException e) {
                                        e.printStackTrace();
                                    }
                                }
                                msg2.addReceiver(monitor);
                                send(msg2);
                                send(msg);

                                break;
                        }
                    break;
                    }
                    /** Utilizador aceita a proposta da estação **/
                    case ACLMessage.ACCEPT_PROPOSAL:{
                        AID usr = msg.getSender();
                        users.put(usr, false);
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
                        break;
                    }
                    /** Utilizador rejeita a proposta **/
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
                            /** Requisita estatisticas da Estação **/
                            case "Stats":
                                msg = msg.createReply();
                                msg.setPerformative(ACLMessage.INFORM);
                                Object[] sendObj = new Object[]{
                                        "Station",
                                        (int)(((double)capAtual/(double)capLim)*100),
                                        dealHistory
                                };
                                try {
                                    msg.setContentObject(sendObj);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                send(msg);
                                break;
                            /** Requisita entrega da bicicleta de um Utilizador em emergência **/
                            case "UserLost":
                                AID usr =(AID) cont[1];
                                System.out.println("USERLOST: "+ usr.getLocalName() +" REDIRECTED TO STATION :"+getAID().getLocalName() );
                                int i = calcProposal();

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
                                break;
                        }
                        break;
                    }
                    /** Utilizador já não está disposto a aceitar propostas **/
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
    /** Criar utilizadores  **/
    public class CreateUsers extends TickerBehaviour {
        public CreateUsers(Agent a) {
            super(a,  (long)(((float)3000) * ConfigVars.SPEED)  );
        }

        @Override
        protected void onTick() {
            //Random
            Double[] ratio = new Double[3];
            for(int i=0; i<3;i++)
                ratio[i]= dealHistory[i]/(double) Arrays.stream(dealHistory).reduce(0,(acc,x)-> acc+=x);

            Random r = new Random();
            int y =r.nextInt(11-(int)(10*ratio[0])+(int)(10*ratio[2]) );
               if(y>8) y=2;
               else if(y>6) y=1;
                        else y=0;
            //Create users
            for(int i = 0; i < y; i++) {
                if(capAtual > 0) {
                    Random x = new Random();
                    try {
                        getContainerController().createNewAgent("User" + getAID().getLocalName() + createdUsers, "Agents.AgenteUtilizador", new Object[]{
                                pos,
                                new Posicao(x.nextInt(ConfigVars.getMapSize()), x.nextInt(ConfigVars.getMapSize()))}).start();
                        createdUsers++;
                        capAtual--;
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    /** Envia propostas aos utilizadores na APE da estação **/
    public class PursuitUsers extends TickerBehaviour {
        public PursuitUsers(Agent a) {
            super(a,  (long)(((float)1000) * ConfigVars.SPEED)  );
        }
        @Override
        protected void onTick() {
            int x = capLim - capAtual;
            AID id = null;
            if (!usersWaiting.isEmpty()) {
                if (x > 0) {
                    for (int i = 0; i < x; i++) {
                        id = usersWaiting.poll();
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(id);
                        send(msg);
                    }
                }
            } else {
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                users.forEach((usr, isRdy) -> {
                    if (isRdy) {
                        int i = calcProposal();
                        Object[] cont = new Object[]{
                                "proposeDelivery",
                                pos,
                                i
                        };
                        try {
                            msg.setContentObject(cont);
                        } catch (IOException e) {e.printStackTrace();}
                        msg.addReceiver(usr);

                    }
                });
                if (msg.getAllReceiver().hasNext())
                    send(msg);
            }
        }

    }
}
