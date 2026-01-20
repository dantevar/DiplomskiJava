package fer;

import java.util.*;
import utils.*;
import metaheuristika.NSGAII_MCW;

/**
 * Test primjer za višekriterijsku optimizaciju MCW problema
 */
public class MultiObjectiveMCWExample {
    
    public static void main(String[] args) {
        // Učitaj graf
        Graph graph = loadGraph("data/n10/instance_0.txt");
        
        System.out.println("=== Višekriterijska optimizacija MCW problema ===");
        System.out.println("Broj vrhova: " + graph.n);
        System.out.println();
        
        // Pokreni NSGA-II algoritam
        NSGAII_MCW nsgaii = new NSGAII_MCW(
            graph,
            100,    // Veličina populacije
            100,    // Broj generacija
            0.1,    // Stopa mutacije
            0.8     // Stopa crossover-a
        );
        
        long startTime = System.currentTimeMillis();
        List<MultiObjectiveResult> paretoFront = nsgaii.optimize();
        long endTime = System.currentTimeMillis();
        
        // Ispis rezultata
        System.out.println("\n=== PARETO FRONTA ===");
        System.out.println("Broj rješenja u Pareto fronti: " + paretoFront.size());
        System.out.println("Vrijeme izvršavanja: " + (endTime - startTime) + " ms");
        System.out.println();
        
        // Sortiraj po ukupnoj cijeni za pregledniji prikaz
        paretoFront.sort(Comparator.comparingDouble(r -> r.totalCost));
        
        System.out.println("Top 10 rješenja po ukupnoj cijeni:");
        System.out.println("-------------------------------------------");
        for (int i = 0; i < Math.min(10, paretoFront.size()); i++) {
            MultiObjectiveResult result = paretoFront.get(i);
            System.out.printf("%2d. %s%n", i + 1, result);
            if (i < 3) {
                System.out.println("    Tura: " + formatTour(result.tour));
            }
        }
        
        // Analiza trade-off-ova
        System.out.println("\n=== ANALIZA TRADE-OFF-OVA ===");
        analyzeTradeoffs(paretoFront);
        
        // Usporedba sa single-objective rješenjem
        System.out.println("\n=== USPOREDBA SA HELD-KARP ===");
        compareWithHeldKarp(graph, paretoFront);
    }
    
    /**
     * Učitava graf iz datoteke
     */
    private static Graph loadGraph(String filename) {
        try {
            return InstanceLoader.loadInstance(filename);
        } catch (Exception e) {
            System.err.println("Greška pri učitavanju grafa: " + e.getMessage());
            // Kreiraj mali test graf
            double[][] matrix = {
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}
            };
            return new Graph(matrix);
        }
    }
    
    /**
     * Formatira turu za ispis
     */
    private static String formatTour(List<Integer> tour) {
        if (tour.size() <= 15) {
            return tour.toString();
        }
        return tour.subList(0, 10) + " ... " + tour.subList(tour.size() - 3, tour.size());
    }
    
    /**
     * Analizira trade-off-ove između kriterija
     */
    private static void analyzeTradeoffs(List<MultiObjectiveResult> paretoFront) {
        // Pronađi ekstreme za svaki kriterij
        MultiObjectiveResult minCost = Collections.min(paretoFront, 
            Comparator.comparingDouble(r -> r.totalCost));
        MultiObjectiveResult minEdges = Collections.min(paretoFront, 
            Comparator.comparingInt(r -> r.edgeCount));
        MultiObjectiveResult minMaxWeight = Collections.min(paretoFront, 
            Comparator.comparingDouble(r -> r.maxEdgeWeight));
        MultiObjectiveResult minVariance = Collections.min(paretoFront, 
            Comparator.comparingDouble(r -> r.variance));
        MultiObjectiveResult minReps = Collections.min(paretoFront, 
            Comparator.comparingInt(r -> r.vertexRepetitions));
        
        System.out.println("Najbolje rješenje za svaki kriterij:");
        System.out.println("  Min ukupna cijena:       " + minCost);
        System.out.println("  Min broj bridova:        " + minEdges);
        System.out.println("  Min maks težina brida:   " + minMaxWeight);
        System.out.println("  Min varijanca:           " + minVariance);
        System.out.println("  Min ponavljanja vrhova:  " + minReps);
        
        // Korelacija između kriterija
        double corrCostEdges = calculateCorrelation(paretoFront, 
            r -> r.totalCost, r -> (double) r.edgeCount);
        double corrCostMaxWeight = calculateCorrelation(paretoFront, 
            r -> r.totalCost, r -> r.maxEdgeWeight);
        
        System.out.println("\nKorelacije između kriterija:");
        System.out.printf("  Cijena vs Broj bridova:    %.3f%n", corrCostEdges);
        System.out.printf("  Cijena vs Maks težina:     %.3f%n", corrCostMaxWeight);
    }
    
    /**
     * Računa Pearsonov koeficijent korelacije
     */
    private static double calculateCorrelation(List<MultiObjectiveResult> results,
            java.util.function.ToDoubleFunction<MultiObjectiveResult> f1,
            java.util.function.ToDoubleFunction<MultiObjectiveResult> f2) {
        
        double[] x = results.stream().mapToDouble(f1).toArray();
        double[] y = results.stream().mapToDouble(f2).toArray();
        
        double meanX = Arrays.stream(x).average().orElse(0.0);
        double meanY = Arrays.stream(y).average().orElse(0.0);
        
        double numerator = 0.0;
        double denomX = 0.0;
        double denomY = 0.0;
        
        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            numerator += dx * dy;
            denomX += dx * dx;
            denomY += dy * dy;
        }
        
        return numerator / Math.sqrt(denomX * denomY);
    }
    
    /**
     * Uspoređuje sa single-objective Held-Karp rješenjem
     */
    private static void compareWithHeldKarp(Graph graph, List<MultiObjectiveResult> paretoFront) {
        Result hkResult = ClosedWalkSolver.solve(graph);
        
        System.out.println("Held-Karp rješenje:");
        System.out.println("  Cijena: " + hkResult.cost);
        System.out.println("  Tura: " + formatTour(hkResult.tour));
        
        // Pronađi najbliže rješenje u Pareto fronti
        MultiObjectiveResult closest = Collections.min(paretoFront,
            Comparator.comparingDouble(r -> Math.abs(r.totalCost - hkResult.cost)));
        
        System.out.println("\nNajbliže rješenje iz Pareto fronte:");
        System.out.println("  " + closest);
        
        double improvement = ((hkResult.cost - closest.totalCost) / hkResult.cost) * 100;
        System.out.printf("  Razlika: %.2f%%%n", improvement);
    }
}
