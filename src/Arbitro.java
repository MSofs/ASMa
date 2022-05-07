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
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;



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

                if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {

                    try {

                        InfoEquipa content = (InfoEquipa) msg.getContentObject();

                        pos_jogadoresA.addAll(content.getPos_jogadores());


                    } catch (UnreadableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (msg.getPerformative() == ACLMessage.INFORM) {

                    try {
                        InfoEquipa content = (InfoEquipa) msg.getContentObject();

                        if(content.getAgent().getLocalName().equals("LiderA")){
                            if (pos_jogadoresA.size()==0) pos_jogadoresA.addAll(content.getPos_jogadores());
                            else {
                                for(InfoPosition pa: pos_jogadoresA){
                                    for (InfoPosition pb: content.getPos_jogadores()){
                                        int indice = pos_jogadoresA.indexOf(pa);
                                        if(pa.getAgent().toString().equals(pb.getAgent().toString())  ) pos_jogadoresA.set(indice, pb);
                                    }
                                }
                            }
                        } else{
                            if (pos_jogadoresB.size()==0) pos_jogadoresB.addAll(content.getPos_jogadores());
                            else {
                                for(InfoPosition pa: pos_jogadoresB){
                                    for (InfoPosition pb: content.getPos_jogadores()){
                                        if(pa.getAgent().toString().equals(pb.getAgent().toString())) pos_jogadoresB.set(pos_jogadoresB.indexOf(pa),pb);
                                    }
                                }
                            }
                        }


                        //System.out.println( "JOGADORES DA EQUIPA A" + pos_jogadoresA);


                        //System.out.println("JOGADORES DA EQUIPA B" + pos_jogadoresB);
                        //  if(content.getAgent())
                        System.out.println("\n\n");

                       /* for (InfoPosition e: content.getPos_jogadores()) {
                            System.out.println("AQUI");
                            System.out.println(e.getAgent().toString() + e.getPosition().toString());
                        } */


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

            int yMin,yMax;
            int xMin,xMax;
            int xa, ya, xb , yb;


            //########          DESCOBRIR OS INIMIGOS DA EQUIPA A   #####################
            for (InfoPosition i : pos_jogadoresA) {
                sd.setType("LiderA");
                template.addServices(sd);

                try{
                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    Map<Position, Double> mapPosInimigos = new HashMap<>();
                    xa = i.getPosition().getX();
                    ya = i.getPosition().getY();
                    xMin = i.getPosition().getX() - 4;
                    xMax = i.getPosition().getX() + 4;

                    yMin = i.getPosition().getY() - 4;
                    yMax = i.getPosition().getY() + 4;
                   // System.out.println("======================================================================");
                    System.out.println(" JOGADOR " + i.getAgent().getLocalName() +":" + i.getPosition() );

                    for (InfoPosition j: pos_jogadoresB){

                        if ((xMin < j.getPosition().getX()) && ( j.getPosition().getX() < xMax)
                                && (yMin < j.getPosition().getY()) && ( j.getPosition().getY() < yMax)){

                            xb = j.getPosition().getX();
                            yb = j.getPosition().getY();

                            Double dist  = Math.sqrt((yb - ya) * (yb - ya) + (xb - xa) * (xb - xa));
                            mapPosInimigos.put(j.getPosition(),dist);
                        }
                    }

                    Position canSupEsq = new Position(1,1);
                    Position canSupDir = new Position(1,35);
                    Position canInfEsq = new Position(35,1);
                    Position canInfDir = new Position(35,35);
                    boolean morto = false;

                    if (mapPosInimigos.size()>=2){
                        if ((i.getPosition().equals(canSupDir)) && mapPosInimigos.containsKey(new Position(1,34)) && mapPosInimigos.containsKey(new Position(2,35))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }

                        if ((i.getPosition().equals(canSupEsq)) && mapPosInimigos.containsKey(new Position(1,2)) && mapPosInimigos.containsKey(new Position(2,1))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }

                        if ((i.getPosition().equals(canInfEsq)) && mapPosInimigos.containsKey(new Position(34,1)) && mapPosInimigos.containsKey(new Position(35,2))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }

                        if ((i.getPosition().equals(canInfDir)) && mapPosInimigos.containsKey(new Position(34,35)) && mapPosInimigos.containsKey(new Position(35,34))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }
                        //PAREDE DE CIMA
                        if ((i.getPosition().getX()==1) && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1))
                        && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1))
                        && mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY()))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }
                        // PAREDE BAIXO
                        if ((i.getPosition().getX()==1) && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY()))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }

                        // PAREDE LADO DIR
                        if ((i.getPosition().getX()==1) && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY()))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY()))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }

                        // PAREDE LADO ESQ
                        if ((i.getPosition().getX()==1) && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY()))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY()))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }
                        // RODEADO
                        if ((i.getPosition().getX()==1) && mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY()))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY()))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1))
                                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1))){
                            morto=true;
                            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
                        }


                    }

                    System.out.println("INIMIGO" + mapPosInimigos + "\n\n\n\n\n");

                    //mandar msg lider das posicoes inimigos
                    InfoInimigo info = new InfoInimigo(i.getAgent(), mapPosInimigos);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContentObject(info);
                        for (DFAgentDescription dfAgentDescription : result) {
                            msg.addReceiver(dfAgentDescription.getName());
                        }
                        myAgent.send(msg);

                    //mandar msg lider sobre morte do agente
                    if(morto){
                        ACLMessage msg1 = new ACLMessage(ACLMessage.REQUEST);
                        msg1.setContentObject(i);
                        pos_jogadoresA.remove(i);
                        for (DFAgentDescription dfAgentDescription : result) {
                            msg1.addReceiver(dfAgentDescription.getName());
                        }
                        myAgent.send(msg1);
                    }

                    Thread.sleep(100);

                } catch (IOException | FIPAException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}