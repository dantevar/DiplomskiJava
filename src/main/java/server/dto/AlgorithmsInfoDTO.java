package server.dto;

import java.util.List;

public class AlgorithmsInfoDTO {
    private List<AlgorithmInfo> algorithms;
    
    public List<AlgorithmInfo> getAlgorithms() { return algorithms; }
    public void setAlgorithms(List<AlgorithmInfo> algorithms) { this.algorithms = algorithms; }
}
