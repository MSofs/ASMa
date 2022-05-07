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
import java.util.ArrayList;
import java.util.Random;

public class Jogador extends Agent {

    private InfoPosition current_location;
    private ArrayList<Position> enemy_pos;
    private String equipa;
    private String tipo;


    protected void setup() {
        super.setup();
        Object[] o = getArguments();
        this.equipa = (String) o[1];
        this.tipo = (String) o[2];

        this.addBehaviour(new Register_Player());
        this.addBehaviour(new Request(this,10000));
        //this.addBehaviour(new Receiver());
    }

    protected void takeDown() {
        super.takeDown();
    }

    private class Register_Player extends OneShotBehaviour {
        public void action() {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            if (equipa.equals("EquipaA")) sd.setType("EquipaA");
            else sd.setType("EquipaB");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                // If Lider is available!
                if (result.length > 0) {
                    Object[] p = getArguments();
                    current_location = new InfoPosition(myAgent.getAID(), (Position) p[0]);

                    ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                    msg.setContentObject(current_location);

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

    private class Request extends TickerBehaviour { // mandar update posicoes e pedido de ajuda

        public Request(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            if (equipa.equals("EquipaA")) sd.setType("EquipaA");
            else sd.setType("EquipaB");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                if (result.length > 0) {
                    ACLMessage msg = receive(); //se nao houver nenhuma posicao para ir, entao vai para o centro 17*17
                    if (msg == null) {

                        Position nova = current_location.getPosition();
                     // mais a frente vai ser preciso ver se nao existe ninguem no quadrado onde vai tentar ir.int

                       int x = nova.getX();
                       int y = nova.getY();

                        if (x<17) nova.setX(x+1);
                        else if (x>17) nova.setX(x-1);
                        else if (y<17) nova.setY(y+1);
                        else if (y>17) nova.setY(y-1);


                        //System.out.println("Antiga Posição: "+x + ","+ y+" NOVA POSIÇÃO: "+ nova);

                        ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
                        mensagem.setContentObject(current_location);

                        for (DFAgentDescription dfAgentDescription : result) {
                            mensagem.addReceiver(dfAgentDescription.getName());
                        }
                        myAgent.send(mensagem);
                    }else{
                        // e porque ele recebeu mensagem para onde se dirigir

                        Position p = (Position) msg.getContentObject();

                        int x = current_location.getPosition().getX();
                        int y = current_location.getPosition().getY();


                        if(x<p.getX())current_location.setPositionX(x+1);
                        else if(x>p.getX())current_location.setPositionX(x-1);
                        else if(y<p.getY())current_location.setPositionX(y+1);
                        else if(y>p.getY())current_location.setPositionX(y-1);


                        ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
                        mensagem.setContentObject(current_location);

                        for (DFAgentDescription dfAgentDescription : result) {
                            mensagem.addReceiver(dfAgentDescription.getName());
                        }
                        myAgent.send(mensagem);
                    }

                    } else {
                     System.out.println(myAgent.getAID().getLocalName() + ": No Lider available. Agent offline");
                    }

                    //if() // como ver se tem inimigo dentro do campo visao ?
                // por aqui o behaviour para pedir ajuda

                } catch (IOException | FIPAException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
    }


    }
