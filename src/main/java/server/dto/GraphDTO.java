package server.dto;

public class GraphDTO {
    private String id;
    private int n;
    private double[][] distances;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public int getN() { return n; }
    public void setN(int n) { this.n = n; }
    
    public double[][] getDistances() { return distances; }
    public void setDistances(double[][] distances) { this.distances = distances; }
}
