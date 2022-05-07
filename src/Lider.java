import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class Lider extends Agent {

    private ArrayList<InfoPosition> player_position = new ArrayList<InfoPosition>();
    private ArrayList<InfoPosition> enemy_position = new ArrayList<InfoPosition>();

    protected void setup() {
        super.setup();

        Object [] obj = getArguments();
        if(obj[0].equals("EquipaA")){
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("EquipaA");
            sd.setName(getLocalName());
            dfd.addServices(sd);
            try {
                DFService.register(this, dfd);
            }catch (FIPAException e) {
                e.printStackTrace();
            }
        }else{
            DFAgentDescription dfd2 = new DFAgentDescription();
            dfd2.setName(getAID());
            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType("EquipaB");
            sd2.setName(getLocalName());
            dfd2.addServices(sd2);
            try {
                DFService.register(this, dfd2);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        this.addBehaviour(new Register_Equipa());
        this.addBehaviour(new Receiver());
        this.addBehaviour(new update_Equipa(this,10000));

    }

    protected void takeDown() {

        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        super.takeDown();

    }

    private class Register_Equipa extends OneShotBehaviour {
        public void action() {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Arbitro");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                // If Lider is available!
                if (result.length > 0) {

                    InfoEquipa info =  new InfoEquipa(myAgent.getAID(),player_position);

                    ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                    msg.setContentObject(info);

                    for (DFAgentDescription dfAgentDescription : result) {
                        msg.addReceiver(dfAgentDescription.getName());
                    }

                    myAgent.send(msg);
                }
                // No Lider is available - kill the agent!
                else {
                    System.out.println(myAgent.getAID().getLocalName() + ": No Lider available. Agent offline");
                }

            } catch (IOException | FIPAException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class update_Equipa extends TickerBehaviour {

        public update_Equipa(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {


            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Arbitro");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                // If Arbitro is available!
                if (result.length > 0) {
                    Object[] p = getArguments();

                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    InfoEquipa e = new InfoEquipa(myAgent.getAID(),player_position);
                    msg.setContentObject(e);

                    for (DFAgentDescription dfAgentDescription : result) {
                        msg.addReceiver(dfAgentDescription.getName());
                    }

                    myAgent.send(msg);
                }
                // No Lider is available - kill the agent!
                else {
                    System.out.println(myAgent.getAID().getLocalName() + ": No Arbitro available. Agent offline");
                }

            } catch (IOException | FIPAException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class Receiver extends CyclicBehaviour {

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {

                if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

                    try {
                        InfoPosition content = (InfoPosition) msg.getContentObject();
                        player_position.add(content);

                        System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
                                + " registered, " + content.getPosition() + "!");

                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if ((msg.getPerformative() == ACLMessage.INFORM) && (!msg.getSender().getLocalName().equals("Arbitro"))){
                    try {
                        InfoPosition content = (InfoPosition) msg.getContentObject();

                        for(InfoPosition i : player_position){
                            int index = player_position.indexOf(i);
                            if(i.getAgent().toString().equals(content.getAgent().toString()))
                                player_position.set(index,content);
                        }

                        System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
                                + " new position: " + content.getPosition() + "!");

                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if ((msg.getPerformative() == ACLMessage.INFORM) && (msg.getSender().getLocalName().equals("Arbitro"))){
                        try {
                            InfoPosition content = (InfoPosition) msg.getContentObject();
                            //receber mensagem do arbitro com as posiçoes de cada jogador

                        } catch (UnreadableException e){
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    if (msg.getPerformative() == ACLMessage.REQUEST){

                        //msg do arbitro para avisar morte agente
                        try {
                            InfoPosition content = (InfoPosition) msg.getContentObject();
                            int ind = player_position.indexOf(content);
                            player_position.remove(ind);

                            //Assim o jogador tinha de estar nas paginas amarelas e acho estranho
                            //ACLMessage kill = new ACLMessage(ACLMessage.REQUEST);
                            //kill.setContentObject(content.getAgent());

                        } catch (UnreadableException e){
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

//FALTA RECEBER A MENSAGEM DO ARBITRO PARA RETIRAR O AGENTE QUE MORREU DO TIPO REQUEST

