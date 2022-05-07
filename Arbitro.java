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
            int yMin,yMax;
            int xMin,xMax;
            int xa, ya, xb , yb;


//########          DESCOBRIR OS INIMIGOS DA EQUIPA A   #####################
            for (InfoPosition i : pos_jogadoresA) {
                Map<Position, Double> mapPosInimigos = new HashMap<>();
                xa = i.getPosition().getX();
                ya = i.getPosition().getY();
                xMin = i.getPosition().getX() - 4;
                xMax = i.getPosition().getX() + 4;

                yMin = i.getPosition().getY() - 4;
                yMax = i.getPosition().getY() + 4;
                System.out.println("======================================================================");
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

//                || (i.getPosition() == canSupEsq) ||(i.getPosition() == canInfDir) ||(i.getPosition() == canInfEsq
                if (mapPosInimigos.size()>=2){
                    if ((i.getPosition() == canSupDir)){
                       if (mapPosInimigos.containsKey(new Position(1,34)) && mapPosInimigos.containsKey(new Position(2,35))) {
                           pos_jogadoresA.remove(i);
                           ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                           try {
                               msg.setContentObject(i.getAgent());
                           } catch (IOException e) {
                               throw new RuntimeException(e);
                           }
                       }


                    }

                }


                System.out.println("INIMIGO" + mapPosInimigos + "\n\n\n\n\n");
                InfoInimigo info = new InfoInimigo(i.getAgent(), mapPosInimigos);




                //mandar esta mensagem ao lider
            }

            //################# AGENTE MORTO?  ################################









            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }













}
