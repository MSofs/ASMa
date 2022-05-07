import java.io.IOException;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class GetHelp implements java.io.Serializable{

    private InfoPosition p;
    private ArrayList<Position> enemy_pos;
    private String type ;

    public GetHelp(InfoPosition i,ArrayList<Position> enemy_p, String t) {
        super();
        this.p = i;
        this.enemy_pos = enemy_p;
        this.type=t;
    }

    public double calcularDis(Position p1, Position p2) {
        return Math.sqrt((p2.getY() - p1.getY()) * (p2.getY() - p1.getY()) + (p2.getX() - p1.getX()) * (p2.getX() - p1.getX()));
    }

    public ArrayList<Double> getEnemyDis(){
        ArrayList<Double> lista = new ArrayList<>();
        for(Position po : enemy_pos){
            double dist = calcularDis(this.p.getPosition(),po);
            lista.add(dist);
        }
    return lista;
    }

    public AID getAgent() {
        return p.getAgent();
    }

    public String getType() { return type; }

    public Position getPos() {
        return p.getPosition();
    }

    public void setPos(Position init) {
        this.p.setPosition(init);
    }

    public ArrayList<Position> getEnemyP() {
        ArrayList<Position> lista = new ArrayList<>();
        for (Position p : enemy_pos){
            lista.add(p);
        }
        return lista;
    }


    public int getNrEnemys() {
        return this.enemy_pos.size();
    }

    @Override
    public String toString() {
        return "HelpRequest [agent=" + getAgent()  + ", pos=" + getPos() + ", nr_enemys=" + getNrEnemys() + ", pos_inimigos="+ getEnemyP() +"]";
    }



}
