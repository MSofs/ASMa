import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Script {
    Position canSupEsq;
    Position canSupDir;
    Position canInfEsq;
    Position canInfDir;



    public Script(){
        canSupEsq = new Position(1,1);
        canSupDir = new Position(1,35);
        canInfEsq = new Position(35,1);
        canInfDir = new Position(35,35);

    }



    public boolean morteCanto(InfoPosition i, Map<Position, Double> mapPosInimigos){
        boolean morto = false;
        if ((i.getPosition().equals(canSupEsq)) && mapPosInimigos.containsKey(new Position(1,2)) && mapPosInimigos.containsKey(new Position(2,1))){
            morto=true;
            System.out.println("ENCURRALADO NO CANTO SUP ESQ");
            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
        }

        if ((i.getPosition().equals(canSupDir)) && mapPosInimigos.containsKey(new Position(1,34)) && mapPosInimigos.containsKey(new Position(2,35))){
            morto=true;

            System.out.println("ENCURRALADO NO CANTO SUP DIR");
            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
        }
        if ((i.getPosition().equals(canInfEsq)) && mapPosInimigos.containsKey(new Position(34, 1)) && mapPosInimigos.containsKey(new Position(35, 2))) {
            morto = true;
            System.out.println("ENCURRALADO NO CANTO INF ESQ");
            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
        }
        if ((i.getPosition().equals(canInfDir)) && mapPosInimigos.containsKey(new Position(34, 35)) && mapPosInimigos.containsKey(new Position(35, 34))) {
            morto = true;
            System.out.println("ENCURRLADO NO CANTO INF DIR");
            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
        }
        return morto;
    }

    //PAREDE DE CIMA
    public  boolean morteParedes(InfoPosition i, Map<Position, Double> mapPosInimigos){
        boolean morto = false;
        if (
                //PARECE DE CIMA
                ((i.getPosition().getX()==1) && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY())))

                //PAREDE DE BAIXO
                || ((i.getPosition().getX()==35) && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY())))

                //PAREDE DA ESQUERDA
                || ((i.getPosition().getY()==1) && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY()))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY()))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1)))

                //PAREDE DA DIREITA
                || ((i.getPosition().getY()==35) && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY()))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY()))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1)))

        ){
            morto=true;
            System.out.println("ENCURRALADO NA PAREDE");
            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
        }
        return morto;
    }

    public  boolean morterodeado(InfoPosition i, Map<Position, Double> mapPosInimigos){
        boolean morto = false;
        if ((mapPosInimigos.containsKey(new Position(i.getPosition().getX()+1 , i.getPosition().getY()))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX()-1 , i.getPosition().getY()))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()+1))
                && mapPosInimigos.containsKey(new Position(i.getPosition().getX() , i.getPosition().getY()-1)))){
            System.out.println("ENRABADO POR TODOS OS LADOS");
            morto=true;
            System.out.println("Mandei matar o agente:" + i.getAgent().getLocalName());
        }
        return morto;
    }



    //CALCULA O CAMPO DE VISAO PARA CADA JOGADOR
    public Map<Position,Double> campoVisao( InfoPosition i, ArrayList<InfoPosition> pos_jogadoresInimigos){
        int xa = i.getPosition().getX();
        int ya = i.getPosition().getY();
        int xMin = i.getPosition().getX() - 4;
        int xMax = i.getPosition().getX() + 4;

        int yMin = i.getPosition().getY() - 4;
        int yMax = i.getPosition().getY() + 4;


        Map<Position, Double> mapPosInimigos = new HashMap<>();

        // System.out.println("======================================================================");
        System.out.println(" JOGADOR " + i.getAgent().getLocalName() +":" + i.getPosition() );

        for (InfoPosition j: pos_jogadoresInimigos){

            if ((xMin < j.getPosition().getX()) && ( j.getPosition().getX() < xMax)
                    && (yMin < j.getPosition().getY()) && ( j.getPosition().getY() < yMax)){

                int xb = j.getPosition().getX();
                int yb = j.getPosition().getY();

                Double dist  = Math.sqrt((yb - ya) * (yb - ya) + (xb - xa) * (xb - xa));
                mapPosInimigos.put(j.getPosition(),dist);
            }
        }
        System.out.println("JOGADOR NA POSIÇAO :" + i.getPosition()  +"TEM INIMIGOS EM :" + mapPosInimigos );



        return mapPosInimigos;
    }

























/*    public  ArrayList<InfoInimigo> campoVisao ( ArrayList<InfoPosition> pos_jogadoresA , ArrayList<InfoPosition> pos_jogadoresB ) {

        ArrayList<InfoInimigo> lista = new ArrayList<>();
        int yMin,yMax;
        int xMin,xMax;
        int xa, ya, xb , yb;

        for (InfoPosition i : pos_jogadoresA) {

            xa = i.getPosition().getX();
            ya = i.getPosition().getY();

            xMin = i.getPosition().getX() - 4;
            xMax = i.getPosition().getX() + 4;

            yMin = i.getPosition().getY() - 4;
            yMax = i.getPosition().getY() + 4;

            for (InfoPosition j : pos_jogadoresB) {
                Map<Position, Double> mapPosInimigos = new HashMap<>();

                if ((xMin < j.getPosition().getX()) && ( j.getPosition().getX() < xMax)
                        && (yMin < j.getPosition().getY()) && ( j.getPosition().getY() < yMax)){

                    xb = j.getPosition().getX();
                    yb = j.getPosition().getY();

                    Double dist  = Math.sqrt((yb - ya) * (yb - ya) + (xb - xa) * (xb - xa));
                    mapPosInimigos.put(j.getPosition(),dist);
                    InfoInimigo inimigo = new InfoInimigo(i.getAgent(),mapPosInimigos);
                    lista.add(inimigo);
                }
            }
        }



    return lista;

    }
    public  ArrayList<InfoInimigo> campoVisao ( ArrayList<InfoPosition> pos_jogadoresA , ArrayList<InfoPosition> pos_jogadoresB ) {

        ArrayList<InfoInimigo> lista = new ArrayList<>();
        int yMin,yMax;
        int xMin,xMax;
        int xa, ya, xb , yb;

        for (InfoPosition i : pos_jogadoresA) {

            xa = i.getPosition().getX();
            ya = i.getPosition().getY();

            xMin = i.getPosition().getX() - 4;
            xMax = i.getPosition().getX() + 4;

            yMin = i.getPosition().getY() - 4;
            yMax = i.getPosition().getY() + 4;

            for (InfoPosition j : pos_jogadoresB) {
                Map<Position, Double> mapPosInimigos = new HashMap<>();

                if ((xMin < j.getPosition().getX()) && ( j.getPosition().getX() < xMax)
                        && (yMin < j.getPosition().getY()) && ( j.getPosition().getY() < yMax)){

                    xb = j.getPosition().getX();
                    yb = j.getPosition().getY();

                    Double dist  = Math.sqrt((yb - ya) * (yb - ya) + (xb - xa) * (xb - xa));
                    mapPosInimigos.put(j.getPosition(),dist);
                    InfoInimigo inimigo = new InfoInimigo(i.getAgent(),mapPosInimigos);
                    lista.add(inimigo);
                }
            }
        }



    return lista;

    }*/
}
