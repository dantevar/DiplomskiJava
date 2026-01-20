package server.dto;

import java.util.Map;

public class AlgorithmInfo {
    private String id;
    private String name;
    private String description;
    private Map<String, Object> defaultParameters;
    
    public AlgorithmInfo() {}
    
    public AlgorithmInfo(String id, String name, String description, Map<String, Object> defaultParameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultParameters = defaultParameters;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Map<String, Object> getDefaultParameters() { return defaultParameters; }
    public void setDefaultParameters(Map<String, Object> defaultParameters) { this.defaultParameters = defaultParameters; }
}
