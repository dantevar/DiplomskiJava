package server.dto;

import java.util.Map;

public class ComparisonDTO {
    private String graphId;
    private Map<String, SolutionDTO> solutions;
    private double optimalCost;
    
    public String getGraphId() { return graphId; }
    public void setGraphId(String graphId) { this.graphId = graphId; }
    
    public Map<String, SolutionDTO> getSolutions() { return solutions; }
    public void setSolutions(Map<String, SolutionDTO> solutions) { this.solutions = solutions; }
    
    public double getOptimalCost() { return optimalCost; }
    public void setOptimalCost(double optimalCost) { this.optimalCost = optimalCost; }
}
