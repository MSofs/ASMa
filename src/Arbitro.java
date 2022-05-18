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

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.function.UnaryOperator;


public class Arbitro extends Agent {
    private final ArrayList<InfoPosition> pos_jogadoresA = new ArrayList<>();
    private final ArrayList<InfoPosition> pos_jogadoresB =  new ArrayList<>();
    private Mapa mapa;
    private int nrJogadoresA = 5;
    private int nrJogadoresB = 5;
    private int j=0;

    protected void setup() {
        super.setup();

        Object[] obj = getArguments();
        this.mapa = new Mapa(pos_jogadoresA, pos_jogadoresB);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Arbitro");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        this.addBehaviour(new Receiver());
        // this.addBehaviour(new Sender());
    }

    protected void takeDown() {super.takeDown();}


    private class Receiver extends CyclicBehaviour {

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {

                if (msg.getPerformative() == ACLMessage.INFORM) {

                    try {
                        InfoEquipa content = (InfoEquipa) msg.getContentObject();
                        if(content.getAgent().getLocalName().equals("LiderA")) {
                            if (content.getPos_jogadores().size() == nrJogadoresA) {
                                for (InfoPosition io : content.getPos_jogadores()) {
                                    if (pos_jogadoresA.size()==0) {
                                        pos_jogadoresA.addAll(content.getPos_jogadores());
                                    } else {
                                        for (InfoPosition pa : pos_jogadoresA) {
                                            if (io.getAgent().getLocalName().equals(pa.getAgent().getLocalName())) {
                                                pos_jogadoresA.set(pos_jogadoresA.indexOf(pa), io);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            if (content.getPos_jogadores().size() == nrJogadoresB) {
                                for (InfoPosition io : content.getPos_jogadores()) {
                                    if (pos_jogadoresB.size()==0) {
                                        pos_jogadoresB.addAll(content.getPos_jogadores());
                                    } else {
                                        for (InfoPosition pb : pos_jogadoresB) {
                                            if (io.getAgent().getLocalName().equals(pb.getAgent().getLocalName())) {
                                                pos_jogadoresB.set(pos_jogadoresB.indexOf(pb), io);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        mapa.update(pos_jogadoresA, pos_jogadoresB);
                        mapa.updateGUI(pos_jogadoresA.size(), pos_jogadoresB.size());

                        if(pos_jogadoresA.size()==nrJogadoresA && pos_jogadoresB.size()== nrJogadoresB) j++;


                        //System.out.println("EquipaA : " + pos_jogadoresA);

                        //System.out.println("EquipaB : " + pos_jogadoresB);

                        //    System.out.println("\n");


                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                DFAgentDescription template1 = new DFAgentDescription();
                ServiceDescription sd1 = new ServiceDescription();
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd  = new ServiceDescription();
                sd1.setType("EquipaA");
                template1.addServices(sd1);
                Script script = new Script();

                sd.setType("EquipaB");
                template.addServices(sd);

                InfoPosition agenteMorto = null;
                ArrayList<InfoInimigo> mapasIni= new ArrayList<>();

                if ( (msg.getPerformative() == ACLMessage.CONFIRM && msg.getSender().getLocalName().equals("LiderB") ) || j==1 ) {

                    try{

                        DFAgentDescription[] result1 = DFService.search(myAgent, template1);
                    //########  CALCULAR CAMPO DE VISAO DO A E SE ALGUEM MORRE DA EQUIPA A #####################

                        for (InfoPosition i : pos_jogadoresA) {

                            Map<Position, Double> mapPosInimigos;

                            mapPosInimigos = script.campoVisao(i,pos_jogadoresB);

                            //========= verificar se esta encurralado  ===========
                            boolean morto = false;

                            if (mapPosInimigos.size()>=2){
                                morto = script.morteCanto(i,mapPosInimigos);
                            }

                            if(mapPosInimigos.size()>=3){
                                morto = script.morteParedes(i,mapPosInimigos);
                            }

                            if(mapPosInimigos.size()>=4){
                                morto = script.morterodeado(i,mapPosInimigos);
                            }


                            if(morto){
                                ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
                                mensagem.setContentObject(i);
                                mensagem.addReceiver(i.getAgent());

                                if(!i.equals(agenteMorto))
                                    pos_jogadoresA.remove(agenteMorto);


                                nrJogadoresA--;
                                agenteMorto = i;

                                for (DFAgentDescription dfAgentDescription : result1) {
                                    mensagem.addReceiver(dfAgentDescription.getName());
                                    //  System.out.println(i.getAgent().getLocalName() + "morreu arbitro" + dfAgentDescription.getName().getLocalName() );
                                }
                                myAgent.send(mensagem);

                                ACLMessage m1 = new ACLMessage(ACLMessage.AGREE);
                                m1.setContentObject(i);

                                DFAgentDescription[] result = DFService.search(myAgent, template);

                                for (DFAgentDescription dfAgentDescription : result) {
                                    m1.addReceiver(dfAgentDescription.getName());
                                    System.out.println("msg para" + dfAgentDescription.getName().getLocalName() + " tirar inimigo : " + i .getAgent().getLocalName());
                                }

                                myAgent.send(m1);


                            }
                            InfoInimigo ini = new InfoInimigo(i.getAgent(),mapPosInimigos);
                            mapasIni.add(ini);


                        }
                        pos_jogadoresA.remove(agenteMorto);
                        mapa.update(pos_jogadoresA, pos_jogadoresB);
                        mapa.updateGUI(pos_jogadoresA.size(), pos_jogadoresB.size());
                        //mandar msg lider das posicoes inimigos
                        ACLMessage mens = new ACLMessage(ACLMessage.INFORM);
                        mens.setContentObject(mapasIni);
                        for (DFAgentDescription dfAgentDescription : result1) {
                            mens.addReceiver(dfAgentDescription.getName());
                            //System.out.println("Esta e a msg enviada do arbitro sobre o jogador: " +i.getAgent().getLocalName() + " e respetivos inimigos: "+ mapPosInimigos + " enviado para: "+ dfAgentDescription.getName().getLocalName());
                        }
                          Thread.sleep(200);
                        myAgent.send(mens);


                    }catch (IOException | FIPAException | InterruptedException  e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }


                if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getSender().getLocalName().equals("LiderA")) {
                    //################### calcular campo de visao de B e matar jogadores B ################

                    try{

                        DFAgentDescription[] result = DFService.search(myAgent, template);

                        for (InfoPosition i : pos_jogadoresB) {

                            Map<Position, Double> mapPosInimigos;

                            mapPosInimigos = script.campoVisao(i,pos_jogadoresA);

                            //========= verificar se esta encurralado  ===========
                            boolean morto = false;

                            if (mapPosInimigos.size()>=2){
                                morto = script.morteCanto(i,mapPosInimigos);
                            }

                            if(mapPosInimigos.size()>=3){
                                morto = script.morteParedes(i,mapPosInimigos);
                            }

                            if(mapPosInimigos.size()>=4){
                                morto = script.morterodeado(i,mapPosInimigos);
                            }

                            //mandar msg lider sobre morte do agente
                            if(morto){
                                ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
                                mensagem.setContentObject(i);
                                mensagem.addReceiver(i.getAgent());


                                if(!i.equals(agenteMorto))
                                    pos_jogadoresB.remove(agenteMorto);

                                nrJogadoresB--;
                                agenteMorto = i;

                                for (DFAgentDescription dfAgentDescription : result) {
                                    mensagem.addReceiver(dfAgentDescription.getName());
                                }
                                myAgent.send(mensagem);

                                ACLMessage m = new ACLMessage(ACLMessage.AGREE);
                                m.setContentObject(i);

                                DFAgentDescription[] result1 = DFService.search(myAgent, template1);

                                for (DFAgentDescription dfAgentDescription : result1) {
                                    m.addReceiver(dfAgentDescription.getName());
                                 //   System.out.println("msg para" + dfAgentDescription.getName().getLocalName() + " tirar inimigo : " + i .getAgent().getLocalName() );
                                }

                                myAgent.send(m);


                            }
                            InfoInimigo ini = new InfoInimigo(i.getAgent(),mapPosInimigos);
                            mapasIni.add(ini);


                            //Thread.sleep(100);

                        }

                        pos_jogadoresB.remove(agenteMorto);
                        mapa.update(pos_jogadoresA, pos_jogadoresB);
                        mapa.updateGUI(pos_jogadoresA.size(), pos_jogadoresB.size());

                        ACLMessage mens = new ACLMessage(ACLMessage.INFORM);
                        mens.setContentObject(mapasIni);

                        for (DFAgentDescription dfAgentDescription : result) {
                            mens.addReceiver(dfAgentDescription.getName());

                            //System.out.println("Esta e a msg enviada do arbitro sobre o jogador: " +i.getAgent().getLocalName() + " e respetivos inimigos: "+ mapPosInimigos + " enviado para: "+ dfAgentDescription.getName().getLocalName());
                        }

                        Thread.sleep(200);
                        myAgent.send(mens);



                    } catch (FIPAException | IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }



                }
            }
        }
    }

}