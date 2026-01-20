package heuristika;

import java.util.*;
import utils.*;

/**
 * ASPW Analysis Runner
 * 
 * Testira ASPW algoritam na više instanci iz dataseta.
 * Mjeri performanse: vrijeme izvođenja, prosječan gap, standardna devijacija.
 */
public class ASPWAnalysis {
    
    public static void main(String[] args) {
        // Test parametri
        int n = 20;              // Veličina grafa
        int numInstances = 30;   // Broj instanci za testiranje
        double[] alphaValues = {0.2, 0.3,0.4}; // Različite α vrijednosti
        
        System.out.println("=== ASPW Algorithm Analysis ===");
        System.out.println("Graph size: n=" + n);
        System.out.println("Number of instances: " + numInstances);
        System.out.println();
        
        // Testiraj svaku α vrijednost
        for (double alpha : alphaValues) {
            System.out.println("─".repeat(60));
            System.out.printf("Testing ASPW with α=%.1f%n", alpha);
            System.out.println("─".repeat(60));
            
            runAnalysis(n, numInstances, alpha);
            System.out.println();
        }
        
        System.out.println("=== Analysis Complete ===");
    }
    
    /**
     * Pokreni analizu za zadanu α vrijednost
     */
    private static void runAnalysis(int n, int numInstances, double alpha) {
        List<Double> executionTimes = new ArrayList<>();
        List<Double> costs = new ArrayList<>();
        List<Double> gaps = new ArrayList<>();
        List<Integer> walkLengths = new ArrayList<>();
        
        int successCount = 0;
        
        // Testiraj na više instanci
        for (int i = 0; i < numInstances; i++) {
            try {
                // Učitaj random instancu
                Graph instance = InstanceLoader.loadRandomInstance(n);
                Graph graph = instance;
                
                // Pokreni ASPW
                long startTime = System.nanoTime();
                Result result = ASPW.solve(graph, alpha);
                long endTime = System.nanoTime();
                
                // Izračunaj metriku
                double executionTimeMs = (endTime - startTime) / 1_000_000.0;
                double gap = (result.cost - instance.optimalCost) / instance.optimalCost * 100.0;
                
                // Spremi rezultate
                executionTimes.add(executionTimeMs);
                costs.add(result.cost);
                gaps.add(gap);
                walkLengths.add(result.tour.size());
                
                successCount++;
                
                // Ispis za prvu instancu (detalji)
                if (i == 0) {
                    System.out.println("Sample Instance Details:");
                    System.out.printf("  Optimal cost:   %.4f%n", instance.optimalCost);
                    System.out.printf("  ASPW cost:      %.4f%n", result.cost);
                    System.out.printf("  Gap:            %.2f%%%n", gap);
                    System.out.printf("  Walk length:    %d%n", result.tour.size());
                    System.out.printf("  Time:           %.2f ms%n", executionTimeMs);
                    System.out.println();
                }
                
            } catch (Exception e) {
                System.err.println("Error loading instance " + i + ": " + e.getMessage());
            }
        }
        
        // Statistička analiza
        if (successCount > 0) {
            System.out.println("Aggregate Statistics (" + successCount + " instances):");
            System.out.println();
            
            // Vrijeme izvođenja
            double avgTime = average(executionTimes);
            double stdevTime = standardDeviation(executionTimes);
            System.out.printf("Execution Time:%n");
            System.out.printf("  Average:    %.2f ms%n", avgTime);
            System.out.printf("  Std Dev:    %.2f ms%n", stdevTime);
            System.out.printf("  Min:        %.2f ms%n", Collections.min(executionTimes));
            System.out.printf("  Max:        %.2f ms%n", Collections.max(executionTimes));
            System.out.println();
            
            // Gap do optimuma
            double avgGap = average(gaps);
            double stdevGap = standardDeviation(gaps);
            System.out.printf("Gap to Optimal:%n");
            System.out.printf("  Average:    %.2f%%%n", avgGap);
            System.out.printf("  Std Dev:    %.2f%%%n", stdevGap);
            System.out.printf("  Min:        %.2f%%%n", Collections.min(gaps));
            System.out.printf("  Max:        %.2f%%%n", Collections.max(gaps));
            System.out.printf("  Median:     %.2f%%%n", median(gaps));
            System.out.println();
            
            // Walk duljine
            double avgLength = average(walkLengths.stream()
                .mapToDouble(Integer::doubleValue)
                .boxed()
                .toList());
            System.out.printf("Walk Length:%n");
            System.out.printf("  Average:    %.1f%n", avgLength);
            System.out.printf("  Min:        %d%n", Collections.min(walkLengths));
            System.out.printf("  Max:        %d%n", Collections.max(walkLengths));
            System.out.println();
            
            // Quality distribution
            long excellent = gaps.stream().filter(g -> g < 1.0).count();
            long good = gaps.stream().filter(g -> g >= 1.0 && g < 5.0).count();
            long acceptable = gaps.stream().filter(g -> g >= 5.0 && g < 10.0).count();
            long poor = gaps.stream().filter(g -> g >= 10.0).count();
            
            System.out.printf("Quality Distribution:%n");
            System.out.printf("  Excellent (<1%%):     %d (%.1f%%)%n", 
                excellent, excellent * 100.0 / successCount);
            System.out.printf("  Good (1-5%%):         %d (%.1f%%)%n", 
                good, good * 100.0 / successCount);
            System.out.printf("  Acceptable (5-10%%):  %d (%.1f%%)%n", 
                acceptable, acceptable * 100.0 / successCount);
            System.out.printf("  Poor (>10%%):         %d (%.1f%%)%n", 
                poor, poor * 100.0 / successCount);
            
        } else {
            System.out.println("No successful runs!");
        }
    }
    
    /**
     * Izračunaj prosječnu vrijednost
     */
    private static double average(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    /**
     * Izračunaj standardnu devijaciju
     */
    private static double standardDeviation(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        double mean = average(values);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    /**
     * Izračunaj median
     */
    private static double median(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }
}
