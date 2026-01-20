package server.dto;

public class GenerateGraphRequest {
    private int n;
    private int maxWeight;
    
    public int getN() { return n; }
    public void setN(int n) { this.n = n; }
    
    public int getMaxWeight() { return maxWeight; }
    public void setMaxWeight(int maxWeight) { this.maxWeight = maxWeight; }
}
