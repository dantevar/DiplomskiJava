package analiza;

import utils.Graph;

import java.io.*;
import java.util.*;

/**
 * Ažurira sve instance u data i data_exp folderima:
 * 1. Unificira format
 * 2. Dodaje pravi optimal walk (s posrednim čvorovima) 
 * 3. Dodaje walk size
 */
public class DatasetFormatter {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║              DATASET FORMATTER - Unifying & Adding Walks            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Process both datasets
        processDataset("data", "UNIFORM");
        System.out.println();
        processDataset("data_exp", "EXPONENTIAL");
    }
    
    static void processDataset(String basePath, String type) {
        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            System.out.println("Skipping " + basePath + " - not found");
            return;
        }
        
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("  Processing: %s (%s)%n", basePath, type);
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        
        int totalProcessed = 0;
        int totalWalks = 0;
        
        // Get all n* directories
        File[] nDirs = baseDir.listFiles(f -> f.isDirectory() && f.getName().startsWith("n"));
        if (nDirs == null) return;
        
        Arrays.sort(nDirs, Comparator.comparingInt(f -> Integer.parseInt(f.getName().substring(1))));
        
        for (File nDir : nDirs) {
            int n = Integer.parseInt(nDir.getName().substring(1));
            
            File[] instances = nDir.listFiles((d, name) -> name.startsWith("instance_") && name.endsWith(".txt"));
            if (instances == null) continue;
            
            int walksInN = 0;
            
            for (File instance : instances) {
                try {
                    boolean isWalk = processInstance(instance, n, type);
                    if (isWalk) walksInN++;
                    totalProcessed++;
                } catch (Exception e) {
                    System.err.println("Error processing " + instance.getName() + ": " + e.getMessage());
                }
            }
            
            totalWalks += walksInN;
            System.out.printf("  N=%d: %d instances processed, %d walks%n", n, instances.length, walksInN);
        }
        
        System.out.printf("  TOTAL: %d instances, %d walks (%.2f%%)%n", 
            totalProcessed, totalWalks, (totalWalks * 100.0 / totalProcessed));
    }
    
    static boolean processInstance(File file, int n, String type) throws IOException {
        // 1. Učitaj postojeće podatke
        InstanceData data = loadInstance(file, n);
        
        if (data.matrix == null || data.optimalTour == null || data.matrix.length != n) {
            System.err.println("  Skipping " + file.getName() + " - invalid data (matrix size: " + 
                (data.matrix != null ? data.matrix.length : "null") + ", expected: " + n + ")");
            return false;
        }
        
        // 2. Kreiraj Graph za Floyd-Warshall
        Graph g = new Graph(data.matrix);
        
        // 3. Rekonstruiraj pravu šetnju s posrednim čvorovima
        List<Integer> fullWalk = reconstructFullWalk(data.optimalTour, g);
        
        // 4. Provjeri je li šetnja (ima ponavljanja)
        boolean isWalk = fullWalk.size() > n + 1 || hasDuplicates(fullWalk, n);
        
        // 5. Zapiši ažurirani file
        writeInstance(file, data.matrix, data.optimalCost, data.optimalTour, fullWalk, isWalk, n, type);
        
        return isWalk;
    }
    
    static boolean hasDuplicates(List<Integer> walk, int n) {
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < walk.size() - 1; i++) {
            if (seen.contains(walk.get(i))) return true;
            seen.add(walk.get(i));
        }
        return false;
    }
    
    static List<Integer> reconstructFullWalk(List<Integer> tour, Graph g) {
        List<Integer> fullWalk = new ArrayList<>();
        
        for (int i = 0; i < tour.size() - 1; i++) {
            int from = tour.get(i);
            int to = tour.get(i + 1);
            
            // Dodaj path od from do to
            List<Integer> path = getShortestPath(from, to, g);
            for (int j = 0; j < path.size() - 1; j++) {
                fullWalk.add(path.get(j));
            }
        }
        
        fullWalk.add(tour.get(tour.size() - 1));
        return fullWalk;
    }
    
    static List<Integer> getShortestPath(int from, int to, Graph g) {
        List<Integer> path = new ArrayList<>();
        path.add(from);
        
        if (from == to) return path;
        
        int current = from;
        int maxSteps = g.n * 2;
        
        while (current != to && maxSteps-- > 0) {
            int nextNode = findNextHop(current, to, g);
            if (nextNode == -1 || nextNode == current) {
                path.add(to);
                break;
            }
            path.add(nextNode);
            current = nextNode;
        }
        
        return path;
    }
    
    static int findNextHop(int from, int to, Graph g) {
        if (from == to) return -1;
        
        double targetDist = g.min_distances[from][to];
        
        for (int neighbor = 0; neighbor < g.n; neighbor++) {
            if (neighbor == from) continue;
            
            double directEdge = g.distance_matrix[from][neighbor];
            if (directEdge >= Double.MAX_VALUE / 4) continue;
            
            if (Math.abs(directEdge + g.min_distances[neighbor][to] - targetDist) < 1e-9) {
                return neighbor;
            }
        }
        
        return to;
    }
    
    static void writeInstance(File file, double[][] matrix, double optimalCost, 
                               List<Integer> optimalTour, List<Integer> fullWalk,
                               boolean isWalk, int n, String type) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Unified header format
            writer.println("# MCW Instance");
            writer.println("# Type: " + type);
            writer.println("# N: " + n);
            writer.println("# Optimal cost: " + optimalCost);
            writer.println("# Optimal tour (permutation): " + optimalTour);
            writer.println("# Optimal walk (full path): " + fullWalk);
            writer.println("# Walk size: " + fullWalk.size());
            writer.println("# Is walk (has repeats): " + isWalk);
            writer.println();
            
            // Distance matrix
            writer.println("# Distance Matrix");
            for (int i = 0; i < n; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    if (j > 0) sb.append(" ");
                    sb.append(String.format("%.10f", matrix[i][j]));
                }
                writer.println(sb.toString());
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // LOADING
    // ═══════════════════════════════════════════════════════════════════════
    
    static class InstanceData {
        double[][] matrix;
        double optimalCost;
        List<Integer> optimalTour;
    }
    
    static InstanceData loadInstance(File file, int expectedN) throws IOException {
        InstanceData data = new InstanceData();
        List<double[]> matrixRows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean readingMatrix = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty()) {
                    if (!matrixRows.isEmpty()) readingMatrix = false;
                    continue;
                }
                
                // Parse headers
                if (line.startsWith("# Optimal cost:")) {
                    data.optimalCost = Double.parseDouble(line.substring("# Optimal cost:".length()).trim());
                } 
                else if (line.startsWith("# Optimal walk:") && !line.contains("full")) {
                    data.optimalTour = parseIntList(line.substring("# Optimal walk:".length()).trim());
                }
                else if (line.startsWith("# Optimal tour")) {
                    data.optimalTour = parseIntList(line.substring(line.indexOf(':') + 1).trim());
                }
                else if (line.equals("# Distance Matrix") || line.equals("# Optimal Walk")) {
                    readingMatrix = line.equals("# Distance Matrix");
                }
                else if (line.startsWith("#")) {
                    continue;
                }
                else {
                    // Try to parse as matrix row
                    String[] parts = line.split("\\s+");
                    // Only accept rows with exactly expectedN columns
                    if (parts.length == expectedN) {
                        try {
                            double[] row = new double[parts.length];
                            for (int j = 0; j < parts.length; j++) {
                                row[j] = Double.parseDouble(parts[j]);
                            }
                            matrixRows.add(row);
                        } catch (NumberFormatException e) {
                            // Not a matrix row
                        }
                    }
                }
            }
            
            // Also try to read walk from "# Optimal Walk" section with single numbers per line
            if (data.optimalTour == null) {
                data.optimalTour = loadWalkFromFile(file);
            }
        }
        
        if (matrixRows.size() == expectedN) {
            data.matrix = new double[matrixRows.size()][];
            for (int i = 0; i < matrixRows.size(); i++) {
                data.matrix[i] = matrixRows.get(i);
            }
        }
        
        return data;
    }
    
    static List<Integer> loadWalkFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inWalkSection = false;
            List<Integer> walk = new ArrayList<>();
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.equals("# Optimal Walk")) {
                    inWalkSection = true;
                    continue;
                }
                
                if (inWalkSection) {
                    if (line.isEmpty() || line.startsWith("#")) {
                        if (!walk.isEmpty()) return walk;
                        inWalkSection = false;
                        continue;
                    }
                    
                    // Parse numbers (space separated or one per line)
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        try {
                            walk.add(Integer.parseInt(part));
                        } catch (NumberFormatException e) {
                            // Skip
                        }
                    }
                }
            }
            
            return walk.isEmpty() ? null : walk;
        }
    }
    
    static List<Integer> parseIntList(String s) {
        s = s.replace("[", "").replace("]", "");
        String[] parts = s.split(",\\s*");
        List<Integer> result = new ArrayList<>();
        for (String part : parts) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                // Skip
            }
        }
        return result.isEmpty() ? null : result;
    }
}
