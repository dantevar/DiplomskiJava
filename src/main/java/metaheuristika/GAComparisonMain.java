package metaheuristika;

import utils.*;
import fer.*;
import java.io.*;
import java.util.*;

/**
 * Grid Search za parametre GA i GAWalk algoritama.
 * Koristi instance iz data/ foldera s poznatim optimalnim rješenjima.
 * PRIKAZUJE ZASEBNE REZULTATE ZA SVAKI N!
 */
public class GAComparisonMain {
    
    // Grid search parametri
    static int[] POPULATION_SIZES = {20, 50, 100};
    static int[] GENERATIONS = {50, 100, 200};
    static double[] MUTATION_RATES = {0.1, 0.2, 0.3};
    static double[] CROSSOVER_RATES = {0.7, 0.8, 0.9};
    
    // Koliko instanci testirati po N
    static int INSTANCES_PER_N = 20;
    static int[] TEST_N_VALUES = {10, 15, 20};
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║       GA vs GAWalk - GRID SEARCH (ZASEBNO ZA SVAKI N)                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Učitaj instance
        Map<Integer, List<TestInstance>> allInstances = loadInstances();
        
        if (allInstances.isEmpty()) {
            System.err.println("No instances found!");
            return;
        }
        
        System.out.println("Loaded instances: ");
        for (var entry : allInstances.entrySet()) {
            System.out.println("  N=" + entry.getKey() + ": " + entry.getValue().size() + " instances");
        }
        System.out.println();
        
        // Rezultati po N
        Map<Integer, GridResult> bestGAbyN = new TreeMap<>();
        Map<Integer, GridResult> bestGAWalkByN = new TreeMap<>();
        
        // Grid search ZA SVAKI N ZASEBNO
        for (int n : TEST_N_VALUES) {
            if (!allInstances.containsKey(n)) {
                System.out.println("Skipping N=" + n + " (no instances)");
                continue;
            }
            
            Map<Integer, List<TestInstance>> singleN = new TreeMap<>();
            singleN.put(n, allInstances.get(n));
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════════════════");
            System.out.println("                        N = " + n + " - GA STANDARD");
            System.out.println("═══════════════════════════════════════════════════════════════════════════");
            GridResult bestGA = gridSearchGA(singleN);
            bestGAbyN.put(n, bestGA);
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════════════════");
            System.out.println("                        N = " + n + " - GA WALK");
            System.out.println("═══════════════════════════════════════════════════════════════════════════");
            GridResult bestGAWalk = gridSearchGAWalk(singleN);
            bestGAWalkByN.put(n, bestGAWalk);
        }
        
        // Finalna usporedba
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     FINAL COMPARISON BY N                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        System.out.println("┌────────┬─────────────────────────────────────────┬─────────────────────────────────────────┐");
        System.out.println("│   N    │              GA STANDARD                │               GA WALK                   │");
        System.out.println("├────────┼─────────────────────────────────────────┼─────────────────────────────────────────┤");
        
        for (int n : TEST_N_VALUES) {
            GridResult ga = bestGAbyN.get(n);
            GridResult gawalk = bestGAWalkByN.get(n);
            if (ga == null || gawalk == null) continue;
            
            String gaParams = String.format("P=%d G=%d M=%.1f C=%.1f Gap=%.2f%%", 
                ga.popSize, ga.generations, ga.mutationRate, ga.crossoverRate, ga.avgGap);
            String walkParams = String.format("P=%d G=%d M=%.1f C=%.1f Gap=%.2f%%",
                gawalk.popSize, gawalk.generations, gawalk.mutationRate, gawalk.crossoverRate, gawalk.avgGap);
            
            String winner = ga.avgGap < gawalk.avgGap ? "←" : (gawalk.avgGap < ga.avgGap ? "→" : "=");
            
            System.out.printf("│  %2d    │ %-39s │ %-39s │ %s%n", n, gaParams, walkParams, winner);
        }
        System.out.println("└────────┴─────────────────────────────────────────┴─────────────────────────────────────────┘");
        
        // Export rezultata
        exportResultsByN(bestGAbyN, bestGAWalkByN);
    }
    
    static List<GridResult> gaResults = new ArrayList<>();
    static List<GridResult> gaWalkResults = new ArrayList<>();
    
    static GridResult gridSearchGA(Map<Integer, List<TestInstance>> instances) {
        gaResults.clear();
        int totalConfigs = POPULATION_SIZES.length * GENERATIONS.length * 
                           MUTATION_RATES.length * CROSSOVER_RATES.length;
        int currentConfig = 0;
        
        System.out.println("Testing " + totalConfigs + " parameter configurations...");
        System.out.println();
        System.out.println("Pop\tGen\tMut\tCross\tAvgGap%\t\tAvgTime(ms)");
        System.out.println("─".repeat(60));
        
        for (int popSize : POPULATION_SIZES) {
            for (int generations : GENERATIONS) {
                for (double mutRate : MUTATION_RATES) {
                    for (double crossRate : CROSSOVER_RATES) {
                        currentConfig++;
                        
                        double totalGap = 0;
                        long totalTime = 0;
                        int count = 0;
                        
                        for (var entry : instances.entrySet()) {
                            for (TestInstance inst : entry.getValue()) {
                                long start = System.currentTimeMillis();
                                Result result = GA.solve(inst.graph, popSize, generations, mutRate, 0);
                                long time = System.currentTimeMillis() - start;
                                
                                double gap = (result.cost - inst.optimalCost) / inst.optimalCost * 100;
                                totalGap += gap;
                                totalTime += time;
                                count++;
                            }
                        }
                        
                        double avgGap = totalGap / count;
                        double avgTime = (double) totalTime / count;
                        
                        gaResults.add(new GridResult(popSize, generations, mutRate, crossRate, avgGap, avgTime));
                        
                        System.out.printf("%d\t%d\t%.2f\t%.2f\t%.4f\t\t%.1f%n",
                            popSize, generations, mutRate, crossRate, avgGap, avgTime);
                    }
                }
            }
        }
        
        // Pronađi najbolji rezultat
        GridResult best = gaResults.stream()
            .min(Comparator.comparingDouble(r -> r.avgGap))
            .orElseThrow();
        
        System.out.println("─".repeat(60));
        System.out.printf("BEST: Pop=%d, Gen=%d, Mut=%.2f, Cross=%.2f → Gap=%.4f%%%n",
            best.popSize, best.generations, best.mutationRate, best.crossoverRate, best.avgGap);
        
        return best;
    }
    
    static GridResult gridSearchGAWalk(Map<Integer, List<TestInstance>> instances) {
        gaWalkResults.clear();
        int totalConfigs = POPULATION_SIZES.length * GENERATIONS.length * 
                           MUTATION_RATES.length * CROSSOVER_RATES.length;
        
        System.out.println("Testing " + totalConfigs + " parameter configurations...");
        System.out.println();
        System.out.println("Pop\tGen\tMut\tCross\tAvgGap%\t\tAvgTime(ms)");
        System.out.println("─".repeat(60));
        
        for (int popSize : POPULATION_SIZES) {
            for (int generations : GENERATIONS) {
                for (double mutRate : MUTATION_RATES) {
                    for (double crossRate : CROSSOVER_RATES) {
                        double totalGap = 0;
                        long totalTime = 0;
                        int count = 0;
                        
                        for (var entry : instances.entrySet()) {
                            for (TestInstance inst : entry.getValue()) {
                                long start = System.currentTimeMillis();
                                Result result = GAWalk.solve(inst.graph, popSize, generations, mutRate, 0);
                                long time = System.currentTimeMillis() - start;
                                
                                double gap = (result.cost - inst.optimalCost) / inst.optimalCost * 100;
                                totalGap += gap;
                                totalTime += time;
                                count++;
                            }
                        }
                        
                        double avgGap = totalGap / count;
                        double avgTime = (double) totalTime / count;
                        
                        gaWalkResults.add(new GridResult(popSize, generations, mutRate, crossRate, avgGap, avgTime));
                        
                        System.out.printf("%d\t%d\t%.2f\t%.2f\t%.4f\t\t%.1f%n",
                            popSize, generations, mutRate, crossRate, avgGap, avgTime);
                    }
                }
            }
        }
        
        GridResult best = gaWalkResults.stream()
            .min(Comparator.comparingDouble(r -> r.avgGap))
            .orElseThrow();
        
        System.out.println("─".repeat(60));
        System.out.printf("BEST: Pop=%d, Gen=%d, Mut=%.2f, Cross=%.2f → Gap=%.4f%%%n",
            best.popSize, best.generations, best.mutationRate, best.crossoverRate, best.avgGap);
        
        return best;
    }
    
    static Map<Integer, List<TestInstance>> loadInstances() {
        Map<Integer, List<TestInstance>> result = new TreeMap<>();
        String basePath = "data";
        
        for (int n : TEST_N_VALUES) {
            String folderPath = basePath + "/n" + n;
            File folder = new File(folderPath);
            
            if (!folder.exists() || !folder.isDirectory()) {
                continue;
            }
            
            List<TestInstance> instances = new ArrayList<>();
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
            
            if (files == null) continue;
            
            // Sortiraj numerički (instance_0, instance_1, ..., instance_10, ...)
            Arrays.sort(files, (f1, f2) -> {
                int num1 = extractInstanceNumber(f1.getName());
                int num2 = extractInstanceNumber(f2.getName());
                return Integer.compare(num1, num2);
            });
            int count = 0;
            
            for (File file : files) {
                if (count >= INSTANCES_PER_N) break;
                
                try {
                    TestInstance inst = loadInstance(file, n);
                    if (inst != null) {
                        instances.add(inst);
                        count++;
                    }
                } catch (Exception e) {
                    // Skip instances that can't be loaded
                }
            }
            
            if (!instances.isEmpty()) {
                result.put(n, instances);
                System.out.println("  N=" + n + ": loaded " + instances.size() + " instances");
            }
        }
        
        return result;
    }
    
    static int extractInstanceNumber(String filename) {
        // instance_123.txt -> 123
        try {
            String numPart = filename.replace("instance_", "").replace(".txt", "");
            return Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
    
    static TestInstance loadInstance(File file, int n) throws IOException {
        double[][] distances = new double[n][n];
        double optimalCost = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("# Optimal cost:")) {
                    optimalCost = Double.parseDouble(line.substring("# Optimal cost:".length()).trim());
                } else if (!line.isEmpty() && !line.startsWith("#") && row < n) {
                    String[] parts = line.split("\\s+");
                    for (int col = 0; col < parts.length && col < n; col++) {
                        distances[row][col] = Double.parseDouble(parts[col]);
                    }
                    row++;
                }
            }
        }
        
        if (optimalCost < 0) {
            return null; // Nema optimalne cijene
        }
        
        return new TestInstance(new Graph(distances), optimalCost, file.getName());
    }
    
    static void exportResultsByN(Map<Integer, GridResult> bestGAbyN, Map<Integer, GridResult> bestGAWalkByN) {
        String filename = "ga_grid_search_by_N.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("N,Algorithm,PopSize,Generations,MutationRate,CrossoverRate,AvgGap%,AvgTime_ms");
            
            for (int n : TEST_N_VALUES) {
                GridResult ga = bestGAbyN.get(n);
                GridResult walk = bestGAWalkByN.get(n);
                
                if (ga != null) {
                    writer.printf("%d,GA,%d,%d,%.2f,%.2f,%.4f,%.1f%n",
                        n, ga.popSize, ga.generations, ga.mutationRate, 
                        ga.crossoverRate, ga.avgGap, ga.avgTime);
                }
                if (walk != null) {
                    writer.printf("%d,GAWalk,%d,%d,%.2f,%.2f,%.4f,%.1f%n",
                        n, walk.popSize, walk.generations, walk.mutationRate,
                        walk.crossoverRate, walk.avgGap, walk.avgTime);
                }
            }
            
            System.out.println("\n📁 Results exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting: " + e.getMessage());
        }
    }
    
    // Helper classes
    static class TestInstance {
        Graph graph;
        double optimalCost;
        String name;
        
        TestInstance(Graph graph, double optimalCost, String name) {
            this.graph = graph;
            this.optimalCost = optimalCost;
            this.name = name;
        }
    }
    
    static class GridResult {
        int popSize, generations;
        double mutationRate, crossoverRate;
        double avgGap, avgTime;
        
        GridResult(int popSize, int generations, double mutRate, double crossRate, 
                   double avgGap, double avgTime) {
            this.popSize = popSize;
            this.generations = generations;
            this.mutationRate = mutRate;
            this.crossoverRate = crossRate;
            this.avgGap = avgGap;
            this.avgTime = avgTime;
        }
    }
}
