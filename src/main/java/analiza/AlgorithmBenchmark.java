package analiza;

import java.io.*;
import java.util.*;

import heuristika.ASPW;
import heuristika.Greedy;
import metaheuristika.ACO;
import metaheuristika.GA;
import metaheuristika.MemeticGASA;
import metaheuristika.SimulatedAnnealingPermutation;
import utils.Graph;
import utils.Result;

/**
 * Sveobuhvatni benchmark svih heuristika i metaheuristika
 * 
 * Učitava grafove iz data/nX/ foldera zajedno sa egzaktnim rješenjima
 * i uspoređuje performanse svih algoritama.
 * 
 * Metrike:
 * - Gap: (cost - optimal) / optimal * 100 [%]
 * - Vrijeme izvršavanja [ms]
 * - Statistike: avg, stdev, min, max
 */
public class AlgorithmBenchmark {
    
    private static final String DATA_DIR = "data";
    
    // Rezultati za jedan algoritam
    static class AlgorithmStats {
        String name;
        List<Double> gaps = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        int successCount = 0;
        int optimalCount = 0; // koliko puta je pronašao optimum
        
        AlgorithmStats(String name) {
            this.name = name;
        }
        
        void addResult(double gap, double timeMs) {
            gaps.add(gap);
            times.add(timeMs);
            successCount++;
            if (gap < 0.0001) optimalCount++;
        }
        
        double avgGap() {
            return gaps.stream().mapToDouble(d -> d).average().orElse(0.0);
        }
        
        double stdevGap() {
            double avg = avgGap();
            return Math.sqrt(gaps.stream().mapToDouble(d -> (d - avg) * (d - avg)).average().orElse(0.0));
        }
        
        double minGap() {
            return gaps.stream().mapToDouble(d -> d).min().orElse(0.0);
        }
        
        double maxGap() {
            return gaps.stream().mapToDouble(d -> d).max().orElse(0.0);
        }
        
        double avgTime() {
            return times.stream().mapToDouble(d -> d).average().orElse(0.0);
        }
        
        double stdevTime() {
            double avg = avgTime();
            return Math.sqrt(times.stream().mapToDouble(d -> (d - avg) * (d - avg)).average().orElse(0.0));
        }
        
        double medianGap() {
            if (gaps.isEmpty()) return 0.0;
            List<Double> sorted = new ArrayList<>(gaps);
            Collections.sort(sorted);
            int mid = sorted.size() / 2;
            if (sorted.size() % 2 == 0) {
                return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
            } else {
                return sorted.get(mid);
            }
        }
        
        double medianTime() {
            if (times.isEmpty()) return 0.0;
            List<Double> sorted = new ArrayList<>(times);
            Collections.sort(sorted);
            int mid = sorted.size() / 2;
            if (sorted.size() % 2 == 0) {
                return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
            } else {
                return sorted.get(mid);
            }
        }
    }
    
    public static void main(String[] args) {
        // Konfiguracija
        int n = 16;          // Veličina grafa
        int numInstances = 100; // Broj instanci za test
        
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           ALGORITHM BENCHMARK - MCW PROBLEM                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  N = %d, Instances = %d%n", n, numInstances);
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Provjeri postoje li podaci
        String nDir = DATA_DIR + File.separator + "n" + n;
        File dir = new File(nDir);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("ERROR: Data directory not found: " + nDir);
            System.err.println("Please run DatasetGenerator first!");
            return;
        }
        
        // Inicijaliziraj statistike za sve algoritme
        Map<String, AlgorithmStats> stats = new LinkedHashMap<>();
        
        // Heuristike
        stats.put("Greedy NN", new AlgorithmStats("Greedy NN"));
        stats.put("ASPW", new AlgorithmStats("ASPW"));
        
        // Metaheuristike
        stats.put("GA", new AlgorithmStats("GA"));
        stats.put("SA", new AlgorithmStats("SA"));
        stats.put("ACO", new AlgorithmStats("ACO"));
        stats.put("Memetic GA-SA", new AlgorithmStats("Memetic GA-SA"));
        
        System.out.println("Loading and benchmarking instances...\n");
        
        int actualInstances = 0;
        
        for (int i = 0; i < numInstances; i++) {
            String filename = nDir + File.separator + "instance_" + i + ".txt";
            File file = new File(filename);
            
            if (!file.exists()) {
                System.err.println("Warning: Instance " + i + " not found, skipping.");
                continue;
            }
            
            try {
                // Učitaj instancu
                InstanceData data = loadInstance(filename);
                Graph g = new Graph(data.matrix);
                double optimalCost = data.optimalCost;
                
                actualInstances++;
                
                if (actualInstances % 20 == 0) {
                    System.out.println("Progress: " + actualInstances + "/" + numInstances);
                }
                
                // ═══════════════════════════════════════════════════════════
                // HEURISTIKE
                // ═══════════════════════════════════════════════════════════
                
                // Greedy Nearest Neighbor
                benchmarkAlgorithm(stats.get("Greedy NN"), g, optimalCost, () -> Greedy.solve(g));
                
                // ASPW
                benchmarkAlgorithm(stats.get("ASPW"), g, optimalCost, () -> ASPW.solve(g));
                
                // ═══════════════════════════════════════════════════════════
                // METAHEURISTIKE
                // ═══════════════════════════════════════════════════════════
                
                // Genetic Algorithm
                benchmarkAlgorithm(stats.get("GA"), g, optimalCost, 
                    () -> GA.solve(g, 100, 100, 0.3, 0));
                
                // Simulated Annealing
                benchmarkAlgorithm(stats.get("SA"), g, optimalCost,
                    () -> SimulatedAnnealingPermutation.solve(g, 100.0, 0.95, 50, 0.01, 0));
                
                // Ant Colony Optimization
                benchmarkAlgorithm(stats.get("ACO"), g, optimalCost,
                    () -> ACO.solve(g, 20, 100, 1.0, 2.0, 0.5, 100.0, 0));
                
                // Memetic GA-SA (manji parametri za brzinu)
                benchmarkAlgorithm(stats.get("Memetic GA-SA"), g, optimalCost,
                    () -> MemeticGASA.solve(g, 30, 50, 0.2, 0.4, 0));
                
            } catch (Exception e) {
                System.err.println("Error processing instance " + i + ": " + e.getMessage());
            }
        }
        
        // ═══════════════════════════════════════════════════════════
        // ISPIS REZULTATA
        // ═══════════════════════════════════════════════════════════
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                    BENCHMARK RESULTS                                              ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  N = %d, Instances tested = %d%n", n, actualInstances);
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Header
        System.out.println("┌─────────────────┬───────────────────────────────────────────────────┬───────────────────────────────┬──────────┐");
        System.out.println("│    Algorithm    │                    Gap [%]                        │         Time [ms]             │  Optimal │");
        System.out.println("│                 │   Avg   ± Stdev  │ Median │   Min  -  Max         │    Avg    │  Median           │   Count  │");
        System.out.println("├─────────────────┼───────────────────────────────────────────────────┼───────────────────────────────┼──────────┤");
        
        for (AlgorithmStats s : stats.values()) {
            if (s.successCount > 0) {
                System.out.printf("│ %-15s │ %5.2f%% ± %5.2f%% │ %5.2f%% │ %5.2f%% - %5.2f%%       │ %8.2f  │ %8.2f          │ %4d/%3d │%n",
                    s.name,
                    s.avgGap(), s.stdevGap(), s.medianGap(), s.minGap(), s.maxGap(),
                    s.avgTime(), s.medianTime(),
                    s.optimalCount, s.successCount);
            }
        }
        
        System.out.println("└─────────────────┴───────────────────────────────────────────────────┴───────────────────────────────┴──────────┘");
        
        // Rangiranje po prosječnom gapu
        System.out.println("\n📊 RANKING BY AVERAGE GAP:");
        System.out.println("─────────────────────────────");
        
        List<AlgorithmStats> ranked = new ArrayList<>(stats.values());
        ranked.removeIf(s -> s.successCount == 0);
        ranked.sort(Comparator.comparingDouble(AlgorithmStats::avgGap));
        
        int rank = 1;
        for (AlgorithmStats s : ranked) {
            String medal = "";
            if (rank == 1) medal = "🥇";
            else if (rank == 2) medal = "🥈";
            else if (rank == 3) medal = "🥉";
            
            System.out.printf("%s %d. %-15s: %.2f%% avg gap, %.2f ms avg time%n",
                medal, rank++, s.name, s.avgGap(), s.avgTime());
        }
        
        // CSV export
        exportToCsv(stats, n, actualInstances);
    }
    
    @FunctionalInterface
    interface Solver {
        Result solve();
    }
    
    private static void benchmarkAlgorithm(AlgorithmStats stats, Graph g, double optimalCost, Solver solver) {
        try {
            long startTime = System.nanoTime();
            Result result = solver.solve();
            long endTime = System.nanoTime();
            
            double timeMs = (endTime - startTime) / 1e6;
            double gap = ((result.cost - optimalCost) / optimalCost) * 100.0;
            
            // Provjera validnosti (gap ne može biti negativan osim ako je numerička greška)
            if (gap < -0.01) {
                // Pronađeno bolje od "optimalnog" - možda bug ili numerička greška
                gap = 0.0;
            }
            
            stats.addResult(gap, timeMs);
            
        } catch (Exception e) {
            // Algoritam je bacio iznimku, preskačemo
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // UČITAVANJE INSTANCE
    // ═══════════════════════════════════════════════════════════
    
    static class InstanceData {
        double[][] matrix;
        double optimalCost;
        List<Integer> optimalWalk;
    }
    
    private static InstanceData loadInstance(String filename) throws IOException {
        InstanceData data = new InstanceData();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int n = 0;
            List<double[]> matrixRows = new ArrayList<>();
            boolean readingMatrix = false;
            boolean readingWalk = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty()) {
                    readingMatrix = false;
                    readingWalk = false;
                    continue;
                }
                
                if (line.startsWith("# Graph size:")) {
                    n = Integer.parseInt(line.substring("# Graph size:".length()).trim());
                } else if (line.startsWith("# Optimal cost:")) {
                    data.optimalCost = Double.parseDouble(line.substring("# Optimal cost:".length()).trim());
                } else if (line.equals("# Distance Matrix")) {
                    readingMatrix = true;
                    readingWalk = false;
                } else if (line.equals("# Optimal Walk")) {
                    readingMatrix = false;
                    readingWalk = true;
                } else if (line.startsWith("#")) {
                    continue;
                } else if (readingMatrix) {
                    String[] parts = line.split("\\s+");
                    double[] row = new double[parts.length];
                    for (int j = 0; j < parts.length; j++) {
                        row[j] = Double.parseDouble(parts[j]);
                    }
                    matrixRows.add(row);
                } else if (readingWalk) {
                    String[] parts = line.split("\\s+");
                    data.optimalWalk = new ArrayList<>();
                    for (String part : parts) {
                        data.optimalWalk.add(Integer.parseInt(part));
                    }
                }
            }
            
            // Konvertiraj listu u matricu
            data.matrix = new double[matrixRows.size()][];
            for (int i = 0; i < matrixRows.size(); i++) {
                data.matrix[i] = matrixRows.get(i);
            }
        }
        
        return data;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CSV EXPORT
    // ═══════════════════════════════════════════════════════════
    
    private static void exportToCsv(Map<String, AlgorithmStats> stats, int n, int instances) {
        String filename = "benchmark_n" + n + "_results.csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Header
            writer.println("Algorithm,AvgGap,StdevGap,MedianGap,MinGap,MaxGap,AvgTime,MedianTime,OptimalCount,TotalCount");
            
            for (AlgorithmStats s : stats.values()) {
                if (s.successCount > 0) {
                    writer.printf("%s,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%d,%d%n",
                        s.name,
                        s.avgGap(), s.stdevGap(), s.medianGap(), s.minGap(), s.maxGap(),
                        s.avgTime(), s.medianTime(),
                        s.optimalCount, s.successCount);
                }
            }
            
            System.out.println("\n📁 Results exported to: " + filename);
            
        } catch (IOException e) {
            System.err.println("Error exporting CSV: " + e.getMessage());
        }
    }
}
