package server.service;

import org.springframework.stereotype.Service;
import server.dto.GraphDTO;
import utils.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GraphService {
    
    private final Map<String, Graph> graphs = new ConcurrentHashMap<>();
    
    public GraphDTO generateGraph(int n, int maxWeight) {
        double[][] distances = GraphGenerator.generateRandomGraph(n);
        Graph graph = new Graph(distances);
        String id = UUID.randomUUID().toString();
        graphs.put(id, graph);
        
        return toDTO(id, graph);
    }
    
    public GraphDTO saveGraph(GraphDTO graphDTO) {
        String id = graphDTO.getId() != null ? graphDTO.getId() : UUID.randomUUID().toString();
        Graph graph = fromDTO(graphDTO);
        graphs.put(id, graph);
        
        return toDTO(id, graph);
    }
    
    public GraphDTO getGraph(String id) {
        Graph graph = graphs.get(id);
        if (graph == null) {
            throw new RuntimeException("Graph not found: " + id);
        }
        return toDTO(id, graph);
    }
    
    public List<GraphDTO> getAllGraphs() {
        List<GraphDTO> result = new ArrayList<>();
        for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
            result.add(toDTO(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    public Graph getGraphObject(String id) {
        Graph graph = graphs.get(id);
        if (graph == null) {
            throw new RuntimeException("Graph not found: " + id);
        }
        return graph;
    }
    
    private GraphDTO toDTO(String id, Graph graph) {
        GraphDTO dto = new GraphDTO();
        dto.setId(id);
        dto.setN(graph.n);
        dto.setDistances(graph.distance_matrix);
        return dto;
    }
    
    private Graph fromDTO(GraphDTO dto) {
        return new Graph( dto.getDistances());
    }
}
