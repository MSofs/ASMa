import jade.core.AID;

public class InfoPosition implements java.io.Serializable {

    private AID agent;
    private Position position;


    public InfoPosition(AID agent, Position position) {
        super();
        this.agent = agent;
        this.position = position;

    }

    public InfoPosition(AID agent, int x, int y) {
        super();
        this.agent = agent;
        this.position = new Position(x,y);
    }

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setPositionX(int x) {
        this.position.setX(x);
    }

    public void setPositionY(int y) {
        this.position.setY(y);
    }

    @Override
    public String toString() {
        return "InfoPosition [agent=" + agent + ", position=" + position + "]";
    }


}
