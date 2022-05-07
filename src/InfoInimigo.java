import jade.core.AID;

import java.util.HashMap;
import java.util.Map;

public class InfoInimigo implements java.io.Serializable {

    private AID agent;
    private Map<Position, Double> mapPosInimigos;


    public InfoInimigo(AID agent,Map<Position, Double> mapPosInimigos) {
        super();
        this.agent = agent;
        this.mapPosInimigos = mapPosInimigos;

    }

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }



    public Map<Position, Double> getMapPosInimigos() {
        Map<Position, Double> mapInimigos = new HashMap<>();
        for (Position p : mapPosInimigos.keySet()){
            Double d = mapPosInimigos.get(p);
            mapInimigos.put(p,d);
        }
        return mapInimigos;
    }

    public void setMapPosInimigos(Map<Position, Double> mapInimigos ) {
        /*
        for (Position p : mapInimigos.keySet()){
            Double d = mapInimigos.get(p);
            this.mapPosInimigos.put(p,d);
        }*/
        this.mapPosInimigos.putAll(mapInimigos);
    }

    @Override
    public String toString() {
        return "InfoInimigo{" +
                "agent=" + agent +
                ", mapPosInimigos=" + mapPosInimigos +
                '}';
    }
}
