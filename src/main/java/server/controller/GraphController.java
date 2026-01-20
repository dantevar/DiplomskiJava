package server.controller;

import org.springframework.web.bind.annotation.*;
import server.dto.*;
import server.service.GraphService;
import java.util.List;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*") // Za React development
public class GraphController {
    
    private final GraphService graphService;
    
    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }
    
    @PostMapping("/generate")
    public GraphDTO generateGraph(@RequestBody GenerateGraphRequest request) {
        return graphService.generateGraph(request.getN(), request.getMaxWeight());
    }
    
    @PostMapping("/upload")
    public GraphDTO uploadGraph(@RequestBody GraphDTO graphDTO) {
        return graphService.saveGraph(graphDTO);
    }
    
    @GetMapping("/{id}")
    public GraphDTO getGraph(@PathVariable String id) {
        return graphService.getGraph(id);
    }
    
    @GetMapping("/list")
    public List<GraphDTO> listGraphs() {
        return graphService.getAllGraphs();
    }
}
