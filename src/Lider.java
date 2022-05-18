import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.util.*;


import java.util.stream.Stream;


public class Lider extends Agent {

    private final ArrayList<InfoPosition> player_position = new ArrayList<InfoPosition>();
    private int nrJogadores;
    Map<AID, InfoInimigo> info = new HashMap<>();
    private int i;
    private int jogadas;
    private boolean ocupado;
    int max = 0;
    int j;
    int k;


    protected void setup() {
        super.setup();

        ocupado = false;
        i = 0;
        j = 0;
        k = 0;
        jogadas = 0;
        Object[] obj = getArguments();
        if (obj[0].equals("EquipaA")) {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("EquipaA");
            sd.setName(getLocalName());
            dfd.addServices(sd);
            nrJogadores = 5;
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
            nrJogadores = 5;
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
            Script script = new Script();


            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Arbitro");
            template.addServices(sd);
            //Map<AID, InfoInimigo> info = new HashMap<>();


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


                                if (player_position.size() == nrJogadores) {
                                    ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
                                    InfoEquipa e = new InfoEquipa(myAgent.getAID(), player_position);
                                    msg1.setContentObject(e);

                                    for (DFAgentDescription dfAgentDescription : result) {
                                        msg1.addReceiver(dfAgentDescription.getName());
                                    }

                                    myAgent.send(msg1);
                                    System.out.println("\n\n");
                                }

                            } catch (UnreadableException | IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if ((msg.getPerformative() == ACLMessage.INFORM) && (!msg.getSender().getLocalName().equals("Arbitro"))) {
                            //update das posicoes dos jogadores, e envia esse update ao arbitro

                            try {
                                InfoPosition content = (InfoPosition) msg.getContentObject();

                                for (InfoPosition i : player_position) {
                                    int index = player_position.indexOf(i);
                                    if (i.getAgent().toString().equals(content.getAgent().toString()))
                                        player_position.set(index, content);
                                }

                                ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                                InfoEquipa e = new InfoEquipa(myAgent.getAID(), player_position);
                                msg2.setContentObject(e);

                                for (DFAgentDescription dfAgentDescription : result) {
                                    msg2.addReceiver(dfAgentDescription.getName());
                                }

                                myAgent.send(msg2);


                            } catch (UnreadableException | IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if ((msg.getPerformative() == ACLMessage.INFORM) && (msg.getSender().getLocalName().equals("Arbitro")) && myAgent.getLocalName().equals("LiderA") && nrJogadores > 0) {
                            //DEPOIS TIRAR O && LIDERA

                            InfoInimigo melhor = new InfoInimigo();
                            ArrayList<Position> pos_Inimigos = new ArrayList<>();

                            try {
                                ArrayList<InfoInimigo> content = (ArrayList<InfoInimigo>) msg.getContentObject();
                                for (InfoInimigo infoI : content) {
                                    for (InfoPosition pos : player_position) {
                                        if (pos.getAgent().equals(infoI.getAgent())) {
                                            info.put(infoI.getAgent(), infoI);
                                            for (Position p : infoI.getMapPosInimigos().keySet()) {
                                                if (!pos_Inimigos.contains(p)) pos_Inimigos.add(p);
                                            }
                                        }
                                    }
                                }
                                System.out.println("InfoValues: " + info);


                                // for (InfoInimigo p : info.values()) System.out.println(p.getAgent().getLocalName() + " " +p.getMapPosInimigos());
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }


                            //   System.out.println("ENTREI1");
                            //       System.out.println("TAMANHO DO INFO: " + info.size());
                            if (info.size() == nrJogadores && nrJogadores > 0) { //ja temos todos as infos dos jogadores // toma decisoes conforme msgs do arbitro

                                //         System.out.println("ENTREI2");

                                for (AID a : info.keySet()) {
                                    if (info.get(a).getMapPosInimigos().size() >= max) {
                                        // System.out.println("AQUI " + a.getLocalName() + ' ' + info.get(a).getMapPosInimigos());
                                        max = info.get(a).getMapPosInimigos().size();
                                        melhor = info.get(a);
                                    }
                                }
                                System.out.println(melhor);


                                try {
                                    if (max == 0) {

                                        // depois temos de ver isto quando for a vez dos turnos
                                        // System.out.println("Melhor agente: " + melhor.getAgent().getLocalName() + " com " + melhor.getMapPosInimigos().size() + "inimigos !" );

                                        for (InfoPosition p : player_position) {
                                            Estrategia stutter = new Estrategia(0, p.getAgent(), null, pos_Inimigos);
                                            System.out.println("Estrategia1 para " + p.getAgent().getLocalName() + " " + stutter.toString());
                                            ACLMessage st = new ACLMessage(ACLMessage.INFORM);
                                            st.addReceiver(p.getAgent());
                                            st.setContentObject(stutter);
                                            myAgent.send(st);
                                        }
                                    } else { // TIVER 1 OU MAIS INIMIGOS NO SEU CAMPO DE VISAO
                                        // depois temos de ver isto por causa dos turnos ocupado=true;

                                        //     System.out.println("ENTREI3");
                                        //     System.out.println("INFO :" + info.values());


                                        for (InfoPosition p : player_position) pos_Inimigos.add(p.getPosition());


                                        Position inimigo;

                                        //prcura o inimigo com menor distancia e torna-o o alvo
                                        //System.out.println( melhor.getAgent().getLocalName() + " TEM INIMIGOS :" + melhor.getMapPosInimigos());

                                        System.out.println("MELHOR :" + melhor.getAgent().getLocalName() + " :");

                                        Double menorDist = Collections.min(melhor.getMapPosInimigos().values());
                                        inimigo = script.getKey(melhor.getMapPosInimigos(), menorDist);
                                        System.out.println("INIMIGO: " + inimigo);


                                        System.out.println(melhor.getAgent().getLocalName() + " ESCOLHEU O INIMIGO :" + inimigo);


                                        Position adjCima = new Position(inimigo.getX() - 1, inimigo.getY());
                                        //System.out.println("POSIÇAO CIMA :" + adjCima);

                                        Position adjBaixo = new Position(inimigo.getX() + 1, inimigo.getY());
                                        //System.out.println("POSIÇAO DE BAIXO: " + adjBaixo);

                                        Position adjEsq = new Position(inimigo.getX(), inimigo.getY() - 1);


                                        Position adjDir = new Position(inimigo.getX(), inimigo.getY() + 1);
                                        //System.out.println("POSIÇÃO DA DIREITA:" + adjDir + "\n");


                                        double distC, distB, distE, distD;

                                        AID jogadorC, jogadorB = null, jogadorE, jogadorD;


                                        Map<AID, Double> listaDistC = new HashMap<>();
                                        Map<AID, Double> listaDistB = new HashMap<>();
                                        Map<AID, Double> listaDistE = new HashMap<>();
                                        Map<AID, Double> listaDistD = new HashMap<>();


                                        //0-cima//1-dir//2-baix//3-esq
                                        Map<Position, Double> listaJog1 = new HashMap<>();
                                        Map<Position, Double> listaJog2 = new HashMap<>();
                                        Map<Position, Double> listaJog3 = new HashMap<>();
                                        Map<Position, Double> listaJog4 = new HashMap<>();
                                        Map<Position, Double> listaJog5 = new HashMap<>();


                                        ACLMessage mJC = new ACLMessage(ACLMessage.INFORM);
                                        ACLMessage mJB = new ACLMessage(ACLMessage.INFORM);
                                        ACLMessage mJD = new ACLMessage(ACLMessage.INFORM);
                                        ACLMessage mJE = new ACLMessage(ACLMessage.INFORM);
                                        ACLMessage mJF = new ACLMessage(ACLMessage.INFORM);

                                        for (InfoPosition p : player_position) {
                                            //System.out.println("IMPRIMIR GET AGENT:" + p.getAgent());

                                            distC = script.calculaDistancia(adjCima, p.getPosition());
                                            listaDistC.put(p.getAgent(), distC);

                                            distB = script.calculaDistancia(adjBaixo, p.getPosition());
                                            listaDistB.put(p.getAgent(), distB);

                                            distE = script.calculaDistancia(adjEsq, p.getPosition());
                                            listaDistE.put(p.getAgent(), distE);

                                            distD = script.calculaDistancia(adjDir, p.getPosition());
                                            listaDistD.put(p.getAgent(), distD);
                                        }


                                        for (AID agente : listaDistC.keySet()) {
                                            if (agente.getLocalName().equals("Jogador1"))
                                                listaJog1.put(adjCima , listaDistC.get(agente));
                                            if (agente.getLocalName().equals("Jogador2"))
                                                listaJog2.put(adjCima , listaDistC.get(agente));
                                            if (agente.getLocalName().equals("Jogador3"))
                                                listaJog3.put(adjCima , listaDistC.get(agente));
                                            if (agente.getLocalName().equals("Jogador4"))
                                                listaJog4.put(adjCima , listaDistC.get(agente));
                                            if (agente.getLocalName().equals("Jogador5"))
                                                listaJog5.put(adjCima , listaDistC.get(agente));
                                        }

                                        for (AID agente : listaDistB.keySet()) {
                                            if (agente.getLocalName().equals("Jogador1"))
                                                listaJog1.put(adjBaixo , listaDistB.get(agente));
                                            if (agente.getLocalName().equals("Jogador2"))
                                                listaJog2.put(adjBaixo, listaDistB.get(agente));
                                            if (agente.getLocalName().equals("Jogador3"))
                                                listaJog3.put(adjBaixo, listaDistB.get(agente));
                                            if (agente.getLocalName().equals("Jogador4"))
                                                listaJog4.put(adjBaixo, listaDistB.get(agente));
                                            if (agente.getLocalName().equals("Jogador5"))
                                                listaJog5.put(adjBaixo, listaDistB.get(agente));
                                        }

                                        for (AID agente : listaDistE.keySet()) {
                                            if (agente.getLocalName().equals("Jogador1"))
                                                listaJog1.put(adjEsq, listaDistE.get(agente));
                                            if (agente.getLocalName().equals("Jogador2"))
                                                listaJog2.put(adjEsq, listaDistE.get(agente));
                                            if (agente.getLocalName().equals("Jogador3"))
                                                listaJog3.put(adjEsq, listaDistE.get(agente));
                                            if (agente.getLocalName().equals("Jogador4"))
                                                listaJog4.put(adjEsq, listaDistE.get(agente));
                                            if (agente.getLocalName().equals("Jogador5"))
                                                listaJog5.put(adjEsq, listaDistE.get(agente));
                                        }


                                        for (AID agente : listaDistD.keySet()) {
                                            if (agente.getLocalName().equals("Jogador1"))
                                                listaJog1.put(adjDir, listaDistD.get(agente));
                                            if (agente.getLocalName().equals("Jogador2"))
                                                listaJog2.put(adjDir, listaDistD.get(agente));
                                            if (agente.getLocalName().equals("Jogador3"))
                                                listaJog3.put(adjDir, listaDistD.get(agente));
                                            if (agente.getLocalName().equals("Jogador4"))
                                                listaJog4.put(adjDir, listaDistD.get(agente));
                                            if (agente.getLocalName().equals("Jogador5"))
                                                listaJog5.put(adjDir, listaDistD.get(agente));
                                        }


                                        InfoPosition best = null;
                                        String type = null;
                                        for (InfoPosition i : player_position) {
                                            if (i.getAgent().equals(melhor.getAgent())) {
                                                type = i.getTipo();
                                                best = i;
                                                System.out.println("best : " + best);
                                            }
                                        }

                                        //caso tenha apenas 1 inimigo e for defensivo ele foge

                                        if (best != null) {
                                            Map<Position, Double> ajuda = new HashMap<>();
                                            ajuda = script.campoVisao(best, player_position);

                                            System.out.println("Ajuda " + ajuda);
                                            if (max == 1 && type != null && type.equals("defensivo") && ajuda.size() == 1) {
                                                listaDistD.remove(melhor.getAgent());
                                                listaDistC.remove(melhor.getAgent());
                                                listaDistB.remove(melhor.getAgent());
                                                listaDistE.remove(melhor.getAgent());
                                                try {
                                                    mJF.addReceiver(melhor.getAgent());
                                                    Estrategia f = new Estrategia(0, melhor.getAgent(), null, pos_Inimigos);
                                                    mJF.setContentObject(f);
                                                    myAgent.send(mJF);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            if ((max == 2) && (ajuda.size() == 1)) {
                                                listaDistD.remove(melhor.getAgent());
                                                listaDistC.remove(melhor.getAgent());
                                                listaDistB.remove(melhor.getAgent());
                                                listaDistE.remove(melhor.getAgent());
                                                try {
                                                    mJF.addReceiver(melhor.getAgent());
                                                    Estrategia f = new Estrategia(0, melhor.getAgent(), null, pos_Inimigos);
                                                    mJF.setContentObject(f);
                                                    myAgent.send(mJF);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

/*
                                        if ((inimigo.getX() == 1) && (inimigo.getY() == 1)) {

                                            Map<AID, Double> aux = listaDistB;

                                            boolean exit = false;

                                            while ( aux.size() > 0) {

                                                double minDistB = Collections.min(aux.values());
                                                jogadorB = script.getKey(listaDistB, minDistB);

                                                Map<Position, Double> distsJogAux = new HashMap<>();


                                                switch (jogadorB.getLocalName()) {
                                                    case "Jogador1":
                                                        distsJogAux = listaJog1;
                                                        break;
                                                    case "Jogador2":
                                                        distsJogAux = listaJog2;
                                                        break;
                                                    case "Jogador3":
                                                        distsJogAux = listaJog3;
                                                        break;
                                                    case "Jogador4":
                                                        distsJogAux = listaJog4;
                                                        break;
                                                    case "Jogador5":
                                                        distsJogAux = listaJog5;
                                                        break;
                                                }

                                                if (Collections.min(distsJogAux.values()) != minDistB) {
                                                    aux.remove(jogadorB);

                                                } else {
                                                    listaDistD.remove(jogadorB);
                                                    exit = true;
                                                    break;
                                                }
                                            }

                                            if (!exit) {
                                                distB = Collections.min(listaDistB.values());
                                                jogadorB = script.getKey(listaDistB, distB);
                                            }

                                            // aqui o jogadorB ]e o nome do agente certo
                                            mJB.addReceiver(jogadorB);
                                            Estrategia b = new Estrategia(1, jogadorB, adjBaixo, pos_Inimigos);
                                            System.out.println("EstrategiaA1 para " + jogadorB.getLocalName() + " " + b.toString());
                                            mJB.setContentObject(b);
                                            myAgent.send(mJB);

                                            if (listaDistD.size() > 0) {
                                                distD = Collections.min(listaDistD.values());
                                                jogadorD = script.getKey(listaDistD, distD);

                                                mJD.addReceiver(jogadorD);
                                                Estrategia d = new Estrategia(1, jogadorD, adjDir, pos_Inimigos);
                                                System.out.println("EstrategiaA2 para " + jogadorD.getLocalName() + " " + d.toString());
                                                mJD.setContentObject(d);
                                                myAgent.send(mJD);
                                                listaDistD.remove(jogadorD);
                                            }

                                            for (AID j : listaDistD.keySet()) {
                                                Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                System.out.println("EstrategiaA3 para " + j.getLocalName() + " " + r.toString());
                                                ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                m.addReceiver(j);
                                                m.setContentObject(r);
                                                myAgent.send(m);
                                            }
                                        }*/





                                        //se for encurrlado no canto superior esq
                                        if((inimigo.getX()==1) && (inimigo.getY()==1)){

                                            distB = Collections.min(listaDistB.values());
                                            jogadorB = script.getKey(listaDistB,distB);
                                            mJB.addReceiver(jogadorB);
                                            Estrategia b = new Estrategia(1,jogadorB,adjBaixo,pos_Inimigos);
                                            //       System.out.println("Estrategia1 para " + jogadorB.getLocalName() + " " + b.toString());
                                            mJB.setContentObject(b);
                                            myAgent.send(mJB);

                                            listaDistD.remove(jogadorB);

                                            if(listaDistD.size()>0){
                                                distD = Collections.min(listaDistD.values());
                                                jogadorD = script.getKey(listaDistD,distD);

                                                mJD.addReceiver(jogadorD);
                                                Estrategia d = new Estrategia(1,jogadorD,adjDir,pos_Inimigos);
                                                //            System.out.println("Estrategia2 para " + jogadorD.getLocalName() + " " + d.toString());
                                                mJD.setContentObject(d);
                                                myAgent.send(mJD);
                                                listaDistD.remove(jogadorD);

                                                for(AID j: listaDistD.keySet()) {
                                                    Estrategia r = new Estrategia(0,j, null,pos_Inimigos);
                                                    //               System.out.println("Estrategia3 para " + j.getLocalName() + " " + r.toString());
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }
                                        }

                                        //se for encurrulado no canto superior dir
                                        if ((inimigo.getX() == 1) && (inimigo.getY() == 35)) {
                                            distB = Collections.min(listaDistB.values());
                                            jogadorB = script.getKey(listaDistB, distB);

                                            mJB.addReceiver(jogadorB);
                                            Estrategia b = new Estrategia(1, jogadorB, adjBaixo, pos_Inimigos);
                                            mJB.setContentObject(b);
                                            myAgent.send(mJB);

                                            listaDistE.remove(jogadorB);

                                            if (listaDistE.size() > 0) {
                                                distE = Collections.min(listaDistE.values());
                                                jogadorE = script.getKey(listaDistE, distE);

                                                mJE.addReceiver(jogadorE);
                                                Estrategia e = new Estrategia(1, jogadorE, adjEsq, pos_Inimigos);
                                                mJE.setContentObject(e);
                                                myAgent.send(mJE);
                                                listaDistE.remove(jogadorE);

                                                for (AID j : listaDistE.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);

                                                }
                                            }

                                        }

                                        //se for encurrulado no canto inferior esq
                                        if ((inimigo.getX() == 35) && (inimigo.getY() == 1)) {
                                            distC = Collections.min(listaDistC.values());
                                            jogadorC = script.getKey(listaDistC, distC);

                                            mJC.addReceiver(jogadorC);
                                            Estrategia c = new Estrategia(1, jogadorC, adjCima, pos_Inimigos);
                                            mJC.setContentObject(c);
                                            myAgent.send(mJC);

                                            listaDistD.remove(jogadorC);

                                            if (listaDistD.size() > 0) {
                                                distD = Collections.min(listaDistD.values());
                                                jogadorD = script.getKey(listaDistD, distD);

                                                mJD.addReceiver(jogadorD);
                                                Estrategia d = new Estrategia(1, jogadorD, adjDir, pos_Inimigos);
                                                mJD.setContentObject(d);
                                                myAgent.send(mJD);
                                                listaDistD.remove(jogadorD);

                                                for (AID j : listaDistD.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }
                                        }

                                        //se for encurralado no canto inf dir
                                        if ((inimigo.getX() == 35) && (inimigo.getY() == 35)) {
                                            distC = Collections.min(listaDistC.values());
                                            jogadorC = script.getKey(listaDistC, distC);


                                            mJC.addReceiver(jogadorC);
                                            Estrategia c = new Estrategia(1, jogadorC, adjCima, pos_Inimigos);
                                            mJC.setContentObject(c);
                                            myAgent.send(mJC);

                                            listaDistE.remove(jogadorC);

                                            if (listaDistE.size() > 0) {
                                                distE = Collections.min(listaDistE.values());
                                                jogadorE = script.getKey(listaDistE, distE);

                                                mJE.addReceiver(jogadorE);
                                                Estrategia e = new Estrategia(1, jogadorE, adjEsq, pos_Inimigos);
                                                mJE.setContentObject(e);
                                                myAgent.send(mJE);
                                                listaDistE.remove(jogadorE);

                                                for (AID j : listaDistE.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }
                                        }

                                        //encurralado na parede de cima
                                        if ((inimigo.getX() == 1) && (inimigo.getY() != 1) && (inimigo.getY() != 35)) {
                                            //System.out.println("ENCURRLADO NA PAREDE");
                                            distB = Collections.min(listaDistB.values());
                                            jogadorB = script.getKey(listaDistB, distB);
                                            mJB.addReceiver(jogadorB);
                                            Estrategia b = new Estrategia(1, jogadorB, adjBaixo, pos_Inimigos);
                                            //      System.out.println("Estrategia1 para " + jogadorB.getLocalName() + " " + b.toString());
                                            mJB.setContentObject(b);
                                            myAgent.send(mJB);


                                            listaDistE.remove(jogadorB);
                                            if (listaDistE.size() > 0) {
                                                distE = Collections.min(listaDistE.values());
                                                jogadorE = script.getKey(listaDistE, distE);
                                                mJE.addReceiver(jogadorE);
                                                Estrategia e = new Estrategia(1, jogadorE, adjEsq, pos_Inimigos);
                                                //               System.out.println("Estrategia2 para " + jogadorE.getLocalName() + " " + e.toString());
                                                mJE.setContentObject(e);
                                                myAgent.send(mJE);
                                                listaDistD.remove(jogadorE);
                                            }

                                            listaDistD.remove(jogadorB);
                                            if (listaDistD.size() > 0) {

                                                distD = Collections.min(listaDistD.values());
                                                jogadorD = script.getKey(listaDistD, distD);

                                                mJD.addReceiver(jogadorD);
                                                Estrategia d = new Estrategia(1, jogadorD, adjDir, pos_Inimigos);
                                                //                 System.out.println("Estrategia3 para " + jogadorD.getLocalName() + " " + d.toString());
                                                mJD.setContentObject(d);
                                                myAgent.send(mJD);
                                                listaDistD.remove(jogadorD);
                                            }

                                            if (listaDistD.size() > 0) {
                                                for (AID j : listaDistD.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }
                                        }


                                        if ((inimigo.getX() == 35) && (inimigo.getY() != 1) && (inimigo.getY() != 35)) {
                                            distC = Collections.min(listaDistC.values());
                                            jogadorC = script.getKey(listaDistC, distC);

                                            mJC.addReceiver(jogadorC);
                                            Estrategia c = new Estrategia(1, jogadorC, adjCima, pos_Inimigos);
                                            //       System.out.println("Estrategia1 para " + jogadorC.getLocalName() + " " + c.toString());
                                            mJC.setContentObject(c);
                                            myAgent.send(mJC);

                                            listaDistE.remove(jogadorC);

                                            if (listaDistE.size() > 0) {
                                                distE = Collections.min(listaDistE.values());
                                                jogadorE = script.getKey(listaDistE, distE);
                                                mJE.addReceiver(jogadorE);
                                                Estrategia e = new Estrategia(1, jogadorE, adjEsq, pos_Inimigos);
                                                //                  System.out.println("Estrategia2 para " + jogadorE.getLocalName() + " " + e.toString());
                                                mJE.setContentObject(e);
                                                myAgent.send(mJE);
                                                listaDistD.remove(jogadorE);
                                            }

                                            listaDistD.remove(jogadorC);
                                            if (listaDistD.size() > 0) {
                                                distD = Collections.min(listaDistD.values());
                                                jogadorD = script.getKey(listaDistD, distD);
                                                mJD.addReceiver(jogadorD);
                                                Estrategia d = new Estrategia(1, jogadorD, adjDir, pos_Inimigos);
                                                //       System.out.println("Estrategia3 para " + jogadorD.getLocalName() + " " + d.toString());
                                                mJD.setContentObject(d);
                                                myAgent.send(mJD);
                                                listaDistD.remove(jogadorD);
                                            }
                                            if (listaDistD.size() > 0) {
                                                for (AID j : listaDistD.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }
                                        }

                                        //#$###################################
                                        // AQUI DENTRO#######
                                        // ##
                                        // #######################

                                        if ((inimigo.getY() == 1) && (inimigo.getX() != 1) && (inimigo.getX() != 35)) {

                                            //      System.out.println("ENTREI AQUI DENTRO ");
                                            distB = Collections.min(listaDistB.values());
                                            jogadorB = script.getKey(listaDistB, distB);

                                            //   System.out.println("LISTA DE POSIÇOES B: " + listaDistB);
                                            //   System.out.println("LISTA DE POSIÇOES C: " + listaDistC);
                                            //    System.out.println("LISTA DE POSIÇOES D: " + listaDistD);


                                            mJB.addReceiver(jogadorB);
                                            Estrategia b = new Estrategia(1, jogadorB, adjBaixo, pos_Inimigos);
                                            //        System.out.println("Estrategia1 para " + jogadorB.getLocalName() + " " + b.toString());
                                            mJB.setContentObject(b);
                                            myAgent.send(mJB);

                                            listaDistC.remove(jogadorB);

                                            if (listaDistC.size() > 0) {
                                                distC = Collections.min(listaDistC.values());
                                                jogadorC = script.getKey(listaDistC, distC);
                                                mJC.addReceiver(jogadorC);
                                                Estrategia c = new Estrategia(1, jogadorC, adjCima, pos_Inimigos);
                                                //                System.out.println("Estrategia2 para " + jogadorC.getLocalName() + " " + c.toString());
                                                mJC.setContentObject(c);
                                                myAgent.send(mJC);
                                                listaDistD.remove(jogadorC);
                                            }

                                            listaDistD.remove(jogadorB);
                                            //  System.out.println("LISTA DE POSIÇOES PARA A DIREITA :" + listaDistD);
                                            if (listaDistD.size() > 0) {
                                                //    System.out.println("LISTA DE POSIÇOES PARA A DIREITA :" + listaDistD);

                                                distD = Collections.min(listaDistD.values());
                                                jogadorD = script.getKey(listaDistD, distD);

                                                mJD.addReceiver(jogadorD);
                                                Estrategia d = new Estrategia(1, jogadorD, adjDir, pos_Inimigos);
                                                //               System.out.println("Estrategia3 para " + jogadorD.getLocalName() + " " + d.toString());
                                                mJD.setContentObject(d);

                                                myAgent.send(mJD);
                                                listaDistD.remove(jogadorD);
                                            }

                                            if (listaDistD.size() > 0) {
                                                for (AID j : listaDistD.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }

                                        }


                                        if ((inimigo.getY() == 35) && (inimigo.getX() != 1) && (inimigo.getX() != 35)) {
                                            distB = Collections.min(listaDistB.values());
                                            jogadorB = script.getKey(listaDistB, distB);

                                            mJB.addReceiver(jogadorB);
                                            Estrategia b = new Estrategia(1, jogadorB, adjBaixo, pos_Inimigos);
                                            mJB.setContentObject(b);
                                            myAgent.send(mJB);

                                            listaDistC.remove(jogadorB);

                                            if (listaDistC.size() > 0) {
                                                distC = Collections.min(listaDistC.values());
                                                jogadorC = script.getKey(listaDistC, distC);

                                                mJC.addReceiver(jogadorC);
                                                Estrategia c = new Estrategia(1, jogadorC, adjCima, pos_Inimigos);
                                                mJC.setContentObject(c);
                                                myAgent.send(mJC);

                                                listaDistE.remove(jogadorC);
                                            }


                                            listaDistE.remove(jogadorB);

                                            if (listaDistE.size() > 0) {
                                                distE = Collections.min(listaDistE.values());
                                                jogadorE = script.getKey(listaDistE, distE);

                                                mJE.addReceiver(jogadorE);
                                                Estrategia e = new Estrategia(1, jogadorE, adjEsq, pos_Inimigos);
                                                mJE.setContentObject(e);
                                                myAgent.send(mJE);
                                                listaDistE.remove(jogadorE);
                                            }

                                            if (listaDistE.size() > 0) {
                                                for (AID j : listaDistE.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }
                                        }
                                        if ((inimigo.getY() != 1) && (inimigo.getY() != 35) && (inimigo.getX() != 1) && (inimigo.getX() != 35)) { /////////////////////////////PARA QUANDO ESTIVER RODEADO
                                            //             System.out.println("ELE ESTA RODEADO ");

                                            distC = Collections.min(listaDistC.values());
                                            jogadorC = script.getKey(listaDistC, distC);

                                            mJC.addReceiver(jogadorC);
                                            Estrategia c = new Estrategia(1, jogadorC, adjCima, pos_Inimigos);
                                            //    System.out.println("LISTA POS CIMA: " +listaDistC);
                                            //    System.out.println(jogadorC.getLocalName()  + "VAI PARA A POSIÇÃO" + adjCima + "\n");

                                            mJC.setContentObject(c);
                                            myAgent.send(mJC);

                                            listaDistB.remove(jogadorC);
                                            listaDistD.remove(jogadorC);
                                            listaDistE.remove(jogadorC);

                                            if (listaDistD.size() > 0) {
                                                distD = Collections.min(listaDistD.values());
                                                jogadorD = script.getKey(listaDistD, distD);

                                                mJD.addReceiver(jogadorD);
                                                Estrategia d = new Estrategia(1, jogadorD, adjDir, pos_Inimigos);
                                                //    System.out.println("LISTA POS Direita: " +listaDistD);
                                                //    System.out.println(jogadorD.getLocalName()  + "VAI PARA A POSIÇÃO" + adjDir + "\n");

                                                mJD.setContentObject(d);
                                                myAgent.send(mJD);
                                                listaDistB.remove(jogadorD);
                                                listaDistE.remove(jogadorD);
                                            }


                                            if (listaDistB.size() > 0) {
                                                distB = Collections.min(listaDistB.values());
                                                jogadorB = script.getKey(listaDistB, distB);

                                                mJB.addReceiver(jogadorB);
                                                Estrategia b = new Estrategia(1, jogadorB, adjBaixo, pos_Inimigos);
                                                //     System.out.println("LISTA POS BAIXO: " + listaDistB);
                                                //     System.out.println(jogadorB.getLocalName()  + "VAI PARA A POSIÇÃO" + adjBaixo + "\n");

                                                mJB.setContentObject(b);
                                                myAgent.send(mJB);
                                                listaDistE.remove(jogadorB);

                                            }


                                            if (listaDistE.size() > 0) {
                                                distE = Collections.min(listaDistE.values());
                                                jogadorE = script.getKey(listaDistE, distE);

                                                mJE.addReceiver(jogadorE);
                                                Estrategia e = new Estrategia(1, jogadorE, adjEsq, pos_Inimigos);
                                                //   System.out.println("LISTA POS Esquerda: " + listaDistE );
                                                //   System.out.println(jogadorE.getLocalName()  + "VAI PARA A POSIÇÃO" + adjEsq + "\n");

                                                mJE.setContentObject(e);
                                                myAgent.send(mJE);
                                                listaDistE.remove(jogadorE);
                                            }



                                            if (listaDistE.size() > 0) {
                                                //                   System.out.println("NAO PODE ENTRAR AQUI");
                                                for (AID j : listaDistE.keySet()) {
                                                    Estrategia r = new Estrategia(0, j, null, pos_Inimigos);
                                                    ACLMessage m = new ACLMessage(ACLMessage.INFORM);
                                                    m.addReceiver(j);
                                                    m.setContentObject(r);
                                                    myAgent.send(m);
                                                }
                                            }
                                        }


                                        //                 System.out.println("MAPA INIMIGOS AGENTE ESCOLHIDO:" + melhor.getMapPosInimigos());
                                        max = 0;

                                        // System.out.println("VALOR J DEPOIS " + j);
                                    }

                                    //System.out.println("SAI LIDER INFORM -> ARBITRO");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if ((msg.getPerformative() == ACLMessage.INFORM) && (msg.getSender().getLocalName().equals("Arbitro")) && myAgent.getLocalName().equals("LiderB")) {

                            try {
                                Thread.sleep(100);
                                System.out.println("EQUIPA B JOGOU");
                                ACLMessage m = new ACLMessage(ACLMessage.CONFIRM);
                                m.setContentObject(myAgent.getAID().getLocalName());


                                for (DFAgentDescription dfAgentDescription : result) {
                                    m.addReceiver(dfAgentDescription.getName());
                                }
                                myAgent.send(m);

                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (msg.getPerformative() == ACLMessage.CONFIRM) {
                            i++;
                            if (i == player_position.size()) {
                                try {
                                    ACLMessage m = new ACLMessage(ACLMessage.CONFIRM);
                                    m.setContentObject(myAgent.getAID().getLocalName());


                                    for (DFAgentDescription dfAgentDescription : result) {
                                        m.addReceiver(dfAgentDescription.getName());
                                    }
                                    myAgent.send(m);
                                    i = 0;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // System.out.println("OCUPADO = " + ocupado);}
                            }
                        }
                        if (msg.getPerformative() == ACLMessage.AGREE) {

                            try {
                                InfoPosition po = (InfoPosition) msg.getContentObject();
                                Position posMorto = po.getPosition();
                                System.out.println("ENTROU AGREE para retirar o " + po.getAgent().getLocalName());
                                //Position posMorto = new Position(1,1);

                                for (InfoInimigo ini : info.values()) {
                                    //        System.out.println("INFO VALUES :" + ini );
                                    if (ini.getMapPosInimigos().containsKey(posMorto)) {
                                        //            System.out.println("ELIMINOU O INIMIGO");
                                        ini.setMapPosInimigos(ini.removerInimigos(posMorto));
                                    }
                                    //      System.out.println("POSIÇÕES DE INIMIGOS RESTANTES NO JOGADOR :" + ini + "\n\n");

                                }
                            } catch (UnreadableException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            //msg do arbitro para avisar morte agente
                            try {

                                InfoPosition content = (InfoPosition) msg.getContentObject();
                                for (InfoPosition p : player_position) {
                                    if (p.equals(content)) {
                                        player_position.remove(p);
                                        break;
                                    }
                                }

                            } catch (UnreadableException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (FIPAException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


