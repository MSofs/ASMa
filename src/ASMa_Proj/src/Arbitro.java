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

    protected void setup() {
        super.setup();

        Object[] obj = getArguments();

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

        //this.addBehaviour(new Jogador.Register_Player());
        //this.addBehaviour(new Jogador.Request(this,1000));
        this.addBehaviour(new Receiver());
        this.addBehaviour(new Sender());
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
                            if (content.getPos_jogadores().size() == 3) {
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
                        } //ENCONTRAR MELHOR FORMA QUE 2 FOR'S SEGUIDOS
                        else{
                            if (content.getPos_jogadores().size() == 4) {
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

                        System.out.println( "JOGADORES DA EQUIPA A :" + pos_jogadoresA);

                        System.out.println("JOGADORES DA EQUIPA B :" + pos_jogadoresB);
                        System.out.println("\n\n");


                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }






    private class Sender extends CyclicBehaviour{

        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            Script script = new Script();
            
            InfoPosition agenteMorto = null;


            //########  CALCULAR CAMPO DE VISAO DO A  #####################
            for (InfoPosition i : pos_jogadoresA) {
                try{
                    sd.setType("EquipaA");
                    template.addServices(sd);
                    DFAgentDescription[] result = DFService.search(myAgent, template);

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

                    //mandar msg lider das posicoes inimigos
                    InfoInimigo info = new InfoInimigo(i.getAgent(), mapPosInimigos);
                    ACLMessage mens = new ACLMessage(ACLMessage.INFORM);
                    mens.setContentObject(info);

                    for (DFAgentDescription dfAgentDescription : result) {
                        mens.addReceiver(dfAgentDescription.getName());
                        //System.out.println("Esta e a msg enviada do arbitro sobre o jogador: " +i.getAgent().getLocalName() + " e respetivos inimigos: "+ mapPosInimigos + " enviado para: "+ dfAgentDescription.getName().getLocalName());
                    }
                    myAgent.send(mens);


                    //mandar msg lider sobre morte do agente
                    if(morto){
                        ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
                        mensagem.setContentObject(i);
                        mensagem.addReceiver(i.getAgent());

                        if(!i.equals(agenteMorto))
                            pos_jogadoresA.remove(agenteMorto);

                        agenteMorto = i;

                        for (DFAgentDescription dfAgentDescription : result) {
                            mensagem.addReceiver(dfAgentDescription.getName());
                        }
                        myAgent.send(mensagem);
                    }


                    //##################        VERIFICAR SE NAO ANDAM UM POR CIMA DOS OUTROS NA EQUIPA A  ########################3
                    ArrayList<Position> pos_jog = new ArrayList<>();
                    for (InfoPosition p: pos_jogadoresA){
                        pos_jog.add(p.getPosition());
                    }
                    pos_jog.addAll(mapPosInimigos.keySet());
                    ACLMessage msgJogador = new ACLMessage(ACLMessage.INFORM_IF);
                    msgJogador.setContentObject(pos_jog);
                    msgJogador.addReceiver(i.getAgent());

                    //SECALHAR O RECEIVER TEM QUE SER O LIDER E NAO O JOGADOR PORQUE NAO FAZ SENTIDO
                    //O LIDER AO RECEBER ESTAS POSIÇOES COLOCA NA CLASSE ESTRATEGIA ESSA LISTA DAS POSIÇOES (ADICIONAR LISTA DE POSIÇOES CLASSE ESTRATEGIA)
                    //ASSIM O JOGADOR AO RECEBER A ESTRATEGIA ANTES DE SE MOVIMENTAR CONFORME VERIFICA ESSA LISTA DE POSIÇOES

                    myAgent.send(msgJogador);

                    //System.out.println("EQUIPA TOTAL :" + pos_jog);

                    Thread.sleep(100);


                } catch (IOException | FIPAException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            pos_jogadoresA.remove(agenteMorto);
        }
    }






}