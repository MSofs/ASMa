import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class Lider extends Agent {

    private ArrayList<InfoPosition> player_position = new ArrayList<InfoPosition>();
    private ArrayList<InfoPosition> enemy_position = new ArrayList<InfoPosition>();

    protected void setup() {
        super.setup();

        Object [] obj = getArguments();
        if(obj[1].equals("EquipaA")){
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

        this.addBehaviour(new Receiver());
    }

    protected void takeDown() {

        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        super.takeDown();

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

                if (msg.getPerformative() == ACLMessage.INFORM){
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
                }
            }
        }
    }
}