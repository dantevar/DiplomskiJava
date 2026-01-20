package server.controller;

import org.springframework.web.bind.annotation.*;
import server.dto.*;
import server.service.AlgorithmService;

@RestController
@RequestMapping("/api/algorithm")
@CrossOrigin(origins = "*")
public class AlgorithmController {
    
    private final AlgorithmService algorithmService;
    
    public AlgorithmController(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }
    
    @PostMapping("/solve")
    public SolutionDTO solve(@RequestBody SolveRequest request) {
        return algorithmService.solve(
            request.getGraphId(),
            request.getAlgorithm(),
            request.getParameters()
        );
    }
    
    @PostMapping("/solve-all")
    public ComparisonDTO solveAll(@RequestBody SolveAllRequest request) {
        return algorithmService.solveAll(request.getGraphId());
    }
    
    @GetMapping("/algorithms")
    public AlgorithmsInfoDTO getAlgorithmsInfo() {
        return algorithmService.getAvailableAlgorithms();
    }
}
