package pl.edu.icm.board.geography.graph;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class GraphStoreItem {
    private int id;
    private int baseNode;
    private int adjNode;
    private double distance;
    private double lon;
    private double lat;
    private boolean node;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBaseNode() {
        return baseNode;
    }

    public void setBaseNode(int baseNode) {
        this.baseNode = baseNode;
    }

    public int getAdjNode() {
        return adjNode;
    }

    public void setAdjNode(int adjNode) {
        this.adjNode = adjNode;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public boolean getNode() {
        return node;
    }

    public void setNode(boolean node) {
        this.node = node;
    }
}
