package server.service;

import org.springframework.stereotype.Service;
import server.dto.*;
import fer.*;
import heuristika.Greedy;
import metaheuristika.GA;
import metaheuristika.ACO;
import utils.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class AlgorithmService {
    
    private final GraphService graphService;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    
    public AlgorithmService(GraphService graphService) {
        this.graphService = graphService;
    }
    
    public SolutionDTO solve(String graphId, String algorithm, Map<String, Object> parameters) {
        Graph graph = graphService.getGraphObject(graphId);
        long startTime = System.currentTimeMillis();
        
        Object result = runAlgorithm(graph, algorithm, parameters);
        
        long duration = System.currentTimeMillis() - startTime;
        
        return createSolutionDTO(algorithm, result, duration);
    }
    
    public ComparisonDTO solveAll(String graphId) {
        Graph graph = graphService.getGraphObject(graphId);
        
        ComparisonDTO comparison = new ComparisonDTO();
        comparison.setGraphId(graphId);
        
        // Paralelno pokretanje svih algoritama
        Map<String, Future<SolutionDTO>> futures = new HashMap<>();
        
        futures.put("HeldKarp", executor.submit(() -> {
            long start = System.currentTimeMillis();
            Result result = ClosedWalkSolver.solve(graph);
            long duration = System.currentTimeMillis() - start;
            return createSolutionDTO("HeldKarp", result, duration);
        }));
        
        futures.put("Greedy", executor.submit(() -> {
            long start = System.currentTimeMillis();
            Result result = Greedy.solve(graph);
            long duration = System.currentTimeMillis() - start;
            return createSolutionDTO("Greedy", result, duration);
        }));
        
        futures.put("GA", executor.submit(() -> {
            long start = System.currentTimeMillis();
            Result result = GA.solve(graph, 50, 20, 0.2, 0);
            long duration = System.currentTimeMillis() - start;
            return createSolutionDTO("GA", result, duration);
        }));
        
        futures.put("ACO", executor.submit(() -> {
            long start = System.currentTimeMillis();
            Result result = ACO.solve(graph, 10, 15, 1.0, 2.0, 0.1, 100.0, 0);
            long duration = System.currentTimeMillis() - start;
            return createSolutionDTO("ACO", result, duration);
        }));
        
        // Prikupljanje rezultata
        Map<String, SolutionDTO> solutions = new HashMap<>();
        for (Map.Entry<String, Future<SolutionDTO>> entry : futures.entrySet()) {
            try {
                solutions.put(entry.getKey(), entry.getValue().get());
            } catch (Exception e) {
                SolutionDTO error = new SolutionDTO();
                error.setAlgorithm(entry.getKey());
                error.setError(e.getMessage());
                solutions.put(entry.getKey(), error);
            }
        }
        
        comparison.setSolutions(solutions);
        
        // Pronaći optimum
        double optimalCost = solutions.values().stream()
            .filter(s -> s.getError() == null)
            .mapToDouble(SolutionDTO::getCost)
            .min()
            .orElse(0.0);
        comparison.setOptimalCost(optimalCost);
        
        return comparison;
    }
    
    public AlgorithmsInfoDTO getAvailableAlgorithms() {
        AlgorithmsInfoDTO info = new AlgorithmsInfoDTO();
        
        List<AlgorithmInfo> algorithms = new ArrayList<>();
        
        algorithms.add(new AlgorithmInfo("HeldKarp", "Held-Karp (Exact)", 
            "Dynamic programming - guaranteed optimal solution", 
            Map.of()));
        
        algorithms.add(new AlgorithmInfo("Greedy", "Greedy Heuristic", 
            "Nearest neighbor construction", 
            Map.of()));
        
        algorithms.add(new AlgorithmInfo("GA", "Genetic Algorithm", 
            "Evolutionary metaheuristic", 
            Map.of(
                "populationSize", 50,
                "generations", 20,
                "crossoverRate", 0.8,
                "mutationRate", 0.2
            )));
        
        algorithms.add(new AlgorithmInfo("ACO", "Ant Colony Optimization", 
            "Swarm intelligence metaheuristic", 
            Map.of(
                "ants", 10,
                "iterations", 15,
                "alpha", 1.0,
                "beta", 2.0,
                "evaporation", 0.1,
                "pheromoneConstant", 100.0
            )));
        
        info.setAlgorithms(algorithms);
        return info;
    }
    
    private Object runAlgorithm(Graph graph, String algorithm, Map<String, Object> parameters) {
        switch (algorithm) {
            case "HeldKarp":
                return ClosedWalkSolver.solve(graph);
            
            case "Greedy":
                return Greedy.solve(graph);
            
            case "GA":
                int popSize = getInt(parameters, "populationSize", 50);
                int generations = getInt(parameters, "generations", 20);
                double mutation = getDouble(parameters, "mutationRate", 0.2);
                return GA.solve(graph, popSize, generations, mutation, 0);
            
            case "ACO":
                int ants = getInt(parameters, "ants", 10);
                int iterations = getInt(parameters, "iterations", 15);
                double alpha = getDouble(parameters, "alpha", 1.0);
                double beta = getDouble(parameters, "beta", 2.0);
                double evaporation = getDouble(parameters, "evaporation", 0.1);
                double pheromone = getDouble(parameters, "pheromoneConstant", 100.0);
                return ACO.solve(graph, ants, iterations, alpha, beta, evaporation, pheromone, 0);
            
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }
    
    private SolutionDTO createSolutionDTO(String algorithm, Object result, long duration) {
        SolutionDTO dto = new SolutionDTO();
        dto.setAlgorithm(algorithm);
        dto.setDurationMs(duration);
        
        if (result instanceof Result) {
            Result r = (Result) result;
            dto.setCost(r.cost);
            dto.setTour(r.tour);
        }
        
        return dto;
    }
    
    private int getInt(Map<String, Object> params, String key, int defaultValue) {
        if (params == null || !params.containsKey(key)) return defaultValue;
        Object value = params.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        return defaultValue;
    }
    
    private double getDouble(Map<String, Object> params, String key, double defaultValue) {
        if (params == null || !params.containsKey(key)) return defaultValue;
        Object value = params.get(key);
        if (value instanceof Number) return ((Number) value).doubleValue();
        return defaultValue;
    }
}
