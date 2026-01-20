package server.dto;

import java.util.Map;

public class SolveRequest {
    private String graphId;
    private String algorithm;
    private Map<String, Object> parameters;
    
    public String getGraphId() { return graphId; }
    public void setGraphId(String graphId) { this.graphId = graphId; }
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
}
