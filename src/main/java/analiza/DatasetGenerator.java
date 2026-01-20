package analiza;


import fer.ClosedWalkSolverParallel;
import utils.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DatasetGenerator {

    public static void main(String[] args) {
        int minN = 21;
        int maxN = 22;
        int instancesPerN = 1000;
        String baseDir = "data";

        System.out.println("Starting dataset generation...");
        System.out.println("N range: " + minN + " to " + maxN);
        System.out.println("Instances per N: " + instancesPerN);
        System.out.println("Output directory: " + baseDir);
        System.out.println();

        for (int n = minN; n <= maxN; n++) {
            System.out.println("Generating for N = " + n + "...");
            
            String nDir = baseDir + File.separator + "n" + n;
            File dir = new File(nDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (int instance = 0; instance < instancesPerN; instance++) {
                if (instance % 100 == 0 && instance > 0) {
                    System.out.println("  Progress: " + instance + "/" + instancesPerN);
                }

                try {
                    // Generate random graph
                    double[][] w = GraphGenerator.generateRandomGraph(n);
                    Graph g = new Graph(w);

                    // Solve with ClosedWalkSolver
                    Result solution = ClosedWalkSolverParallel.solve(g);

                    // Save to file
                    String filename = nDir + File.separator + "instance_" + instance + ".txt";
                    saveInstance(filename, w, solution);

                } catch (Exception e) {
                    System.err.println("Error generating instance " + instance + " for N=" + n);
                    e.printStackTrace();
                }
            }

            System.out.println("  Completed N = " + n + " (" + instancesPerN + " instances)");
            System.out.println();
        }

        System.out.println("Dataset generation complete!");
    }

    private static void saveInstance(String filename, double[][] matrix, Result solution) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            int n = matrix.length;

            // Write metadata
            writer.println("# Graph size: " + n);
            writer.println("# Optimal cost: " + solution.cost);
            writer.println();

            // Write distance matrix
            writer.println("# Distance Matrix");
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    writer.print(matrix[i][j]);
                    if (j < n - 1) {
                        writer.print(" ");
                    }
                }
                writer.println();
            }
            writer.println();

            // Write optimal walk (sequence of vertices)
            writer.println("# Optimal Walk");
            for (int i = 0; i < solution.tour.size(); i++) {
                writer.print(solution.tour.get(i));
                if (i < solution.tour.size() - 1) {
                    writer.print(" ");
                }
            }
            writer.println();
        }
    }
}
