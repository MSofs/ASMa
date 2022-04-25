import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Jogador extends Agent{

    private InfoPosition current_location;
    private ArrayList<Position> enemy_pos;
    private boolean tipo; //ofensivo = true, senao defensivo
    // e preciso uma cena para identificar a equipa ???

    protected void setup() {
        super.setup();

        this.addBehaviour(new Register_Player());
        //this.addBehaviour(new Receiver());
    }

    protected void takeDown() {
        super.takeDown();
    }

    private class Register_Player extends OneShotBehaviour {
        public void action() {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("lider");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                // If Lider is available!
                if (result.length > 0) {

                    Random rand = new Random();
                    Position p = new Position(rand.nextInt(35), rand.nextInt(35));
                    //talvez ter cuidados nao ter jogadores em cima uns dos outros.

                    current_location = new InfoPosition(myAgent.getAID(), p);

                    ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                    msg.setContentObject(current_location);

                    for (int i = 0; i < result.length; ++i) {
                        msg.addReceiver(result[i].getName());
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

}
