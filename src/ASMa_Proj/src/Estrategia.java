import jade.core.AID;

import java.io.Serializable;

public class Estrategia implements  java.io.Serializable {
    private int tipo; //0 -> stutter; 1-> estrategia
    private AID agente;
    private Position pos;


    public Estrategia(int t, AID agent, Position position) {
        super();
        this.tipo = t;
        this.agente = agent;
        this.pos = position;

    }

    public AID getAgent() {
        return agente;
    }

    public void setAgent(AID agent) {
        this.agente = agent;
    }

    public Position getPosition() {
        return pos;
    }

    public void setPosition(Position position) {
        this.pos = position;
    }

    public int getTipo() { return tipo; }

    public void setTipo(int tipo) { this.tipo = tipo; }

}