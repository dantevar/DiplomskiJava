package analiza;

import fer.ClosedWalkSolverParallel;
import utils.*;

import java.io.*;
import java.util.*;

/**
 * Generator instanci s eksponencijalnom distribucijom težina.
 * Sprema u data_exp/nX/ foldere s optimalnim rješenjima.
 */
public class ExpDatasetGenerator {

    // Konfiguracija
    static int[] N_VALUES = {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    static int INSTANCES_PER_N = 1000;
    static String BASE_PATH = "data_log";
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     EXPONENTIAL DATASET GENERATOR (ClosedWalkSolverParallel)         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Kreiraj base folder
        new File(BASE_PATH).mkdirs();
        
        long totalStart = System.currentTimeMillis();
        
        for (int n : N_VALUES) {
            generateForN(n);
        }
        
        long totalTime = System.currentTimeMillis() - totalStart;
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("TOTAL TIME: %.1f seconds%n", totalTime / 1000.0);
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }
    
    static void generateForN(int n) {
        String folderPath = BASE_PATH + "/n" + n;
        File folder = new File(folderPath);
        folder.mkdirs();
        
        System.out.printf("Generating N=%d instances...%n", n);
        long start = System.currentTimeMillis();
        
        int generated = 0;
        
        for (int i = 0; i < INSTANCES_PER_N; i++) {
            String filename = folderPath + "/instance_" + i + ".txt";
            
            // Provjeri postoji li već
            if (new File(filename).exists()) {
                generated++;
                continue;
            }
            
            try {
                // Generiraj eksponencijalni graf
                double[][] distances = GraphGenerator.generateRandomGraphLogNormal(n);
                Graph g = new Graph(distances);
                
                // Riješi s parallel solverom
                Result result = ClosedWalkSolverParallel.solve(g);
                
                // Spremi
                saveInstance(filename, distances, result.cost, result.tour);
                generated++;
                
                if (generated % 100 == 0) {
                    System.out.printf("  N=%d: %d/%d instances generated...%n", n, generated, INSTANCES_PER_N);
                }
                
            } catch (Exception e) {
                System.err.println("Error generating instance " + i + " for N=" + n + ": " + e.getMessage());
            }
        }
        
        long time = System.currentTimeMillis() - start;
        System.out.printf("  N=%d: DONE (%d instances, %.1f sec)%n", n, generated, time / 1000.0);
    }
    
    static void saveInstance(String filename, double[][] distances, double optimalCost, 
                              List<Integer> optimalWalk) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            int n = distances.length;
            
            // Header
            writer.println("# MCW Instance (Exponential distribution)");
            writer.println("# N: " + n);
            writer.println("# Optimal cost: " + optimalCost);
            writer.println("# Optimal walk: " + optimalWalk);
            writer.println();
            
            // Distance matrix
            for (int i = 0; i < n; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    if (j > 0) sb.append(" ");
                    sb.append(String.format("%.10f", distances[i][j]));
                }
                writer.println(sb.toString());
            }
        }
    }
}
