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
import java.util.HashMap;
import java.util.Map;

public class Lider extends Agent {

    private final ArrayList<InfoPosition> player_position = new ArrayList<InfoPosition>();


    protected void setup() {
        super.setup();

        Object[] obj = getArguments();
        if (obj[0].equals("EquipaA")) {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("EquipaA");
            sd.setName(getLocalName());
            dfd.addServices(sd);
            try {
                DFService.register(this, dfd);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        } else {
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


            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Arbitro");
            template.addServices(sd);

            int nJ =0;
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);

                if (result.length > 0) {
                    if (msg != null) {
                        if (msg.getPerformative() == ACLMessage.SUBSCRIBE) { // regista jogadores

                            try {

                                InfoPosition content = (InfoPosition) msg.getContentObject();
                                player_position.add(content);

                                System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
                                        + " registered, " + content.getPosition() + "!");


                                ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
                                InfoEquipa e = new InfoEquipa(myAgent.getAID(), player_position);
                                msg1.setContentObject(e);

                                for (DFAgentDescription dfAgentDescription : result) {
                                    msg1.addReceiver(dfAgentDescription.getName());
                                }

                                myAgent.send(msg1);

                            } catch (UnreadableException | IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if ((msg.getPerformative() == ACLMessage.INFORM) && (!msg.getSender().getLocalName().equals("Arbitro"))) {
                            //update das posicoes dos jogadores, e envia esse update ao arbitro
                            try {
                                //System.out.println("ENTREI LIDER INFORM -> JOGADOR");
                                InfoPosition content = (InfoPosition) msg.getContentObject();

                                for (InfoPosition i : player_position) {
                                    int index = player_position.indexOf(i);
                                    if (i.getAgent().toString().equals(content.getAgent().toString()))
                                        player_position.set(index, content);
                                }


                                //System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()
                                //        + " new position: " + content.getPosition() + "!");


                                ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                                InfoEquipa e = new InfoEquipa(myAgent.getAID(), player_position);
                                msg2.setContentObject(e);

                                for (DFAgentDescription dfAgentDescription : result) {
                                    msg2.addReceiver(dfAgentDescription.getName());
                                }

                                myAgent.send(msg2);

                                //System.out.println("SAI LIDER INFORM -> JOGADOR");

                            } catch (UnreadableException | IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if ((msg.getPerformative() == ACLMessage.INFORM) && (msg.getSender().getLocalName().equals("Arbitro"))) {
                            //System.out.println("ENTREI LIDER INFORM -> ARBITRO");
                            int max = 0;
                            InfoInimigo melhor = new InfoInimigo();
                            Map<AID, InfoInimigo> info = new HashMap<>();
                            try {
                                InfoInimigo content = (InfoInimigo) msg.getContentObject();
                                info.put(content.getAgent(), content);
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }

                            if (nJ == 0) { //ja temos todos as infos dos jogadores // toma decisoes conforme msgs do arbitro
                                nJ = 0;
                                // System.out.println("ENTREI LIDER INFORM -> ARBITRO RECEBEMOS TODOS OS JGDS");
                                for (AID a : info.keySet()) {
                                    if (info.get(a).getMapPosInimigos().size() >= max) {
                                        max = info.get(a).getMapPosInimigos().size();
                                        melhor = info.get(a);
                                    }
                                }

                                try {

                                    if (max == 0) {
                                        Estrategia stutter = new Estrategia(0, melhor.getAgent(), null);
                                        ACLMessage st = new ACLMessage(ACLMessage.INFORM);
                                        st.addReceiver(melhor.getAgent());
                                        st.setContentObject(stutter);
                                        myAgent.send(st);
                                    } else {//  mandar mensagem com a posicao para onde o jgd se deve mexer, mpInimigos>0
                                        String tipo = new String();
                                        // conforme o jogador vamos ver o seu tipo de jogo(ofensivo,defensivo)
                                        // os inimigos etc e tomar uma decisao
                                        //recebe o agente e o map de inimigos do agente

                                        //temos de esperar pela info dos 5 jgs para ver qual a decisao a tomar
   /*                                         for (InfoPosition i : player_position) {
                                                if (melhor.getAgent().equals(i.getAgent()))
                                                    tipo = i.getTipo();
                                            }

                                            if (tipo.equals("defensivo")) {
                                                //foge?
                                            }
                                            if (tipo.equals("ofensivo")) {
                                                //ataca ?
                                            }*/
                                    }
                                    //System.out.println("SAI LIDER INFORM -> ARBITRO");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            //System.out.println("ENTREI LIDER REQUEST");
                            //msg do arbitro para avisar morte agente
                            try {
                                InfoPosition content = (InfoPosition) msg.getContentObject();
                                player_position.remove(content);
                                //System.out.println("ENTREI LIDER REQUEST");
                            } catch (UnreadableException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }
}


