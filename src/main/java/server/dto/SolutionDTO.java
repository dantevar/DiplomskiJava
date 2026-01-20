package server.dto;

import java.util.List;

public class SolutionDTO {
    private String algorithm;
    private double cost;
    private List<Integer> tour;
    private long durationMs;
    private String error;
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    
    public List<Integer> getTour() { return tour; }
    public void setTour(List<Integer> tour) { this.tour = tour; }
    
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
