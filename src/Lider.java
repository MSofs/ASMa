import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

import java.util.ArrayList;


public class Lider extends Agent {

    protected void setup() {
        super.setup();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("lider");
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
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

        private ArrayList<InfoPosition> player_position = new ArrayList<InfoPosition>();

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {

                if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

                    try {
                        System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
                                + " registered!");

                        InfoPosition content = (InfoPosition) msg.getContentObject();
                        player_position.add(content);

                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}