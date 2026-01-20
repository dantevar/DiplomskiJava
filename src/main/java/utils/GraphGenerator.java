package utils;

import java.util.Random;

public class GraphGenerator {
	

	public static double exponential(Random rand, double lambda) {
	    // lambda > 0
	    double u = rand.nextDouble();
	    return -Math.log(1.0 - u) / lambda;
	}
	
	public static double logNormal(Random rand, double mu, double sigma) {
	    double z = rand.nextGaussian(); // standard normal
	    return Math.exp(mu + sigma * z);
	    
	}
	
    public static double[][] generateRandomGraphExp(int n) {
    	
        double[][] graph = new double[n][n];
        Random rand = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) { 
                double edge = exponential(rand, 1);
                graph[i][j] = edge;
                graph[j][i] = edge; 
            }
            graph[i][i] = 0; 
        }
		return graph;
        
    }
    public static double[][] generateRandomGraph(int n) {
    	
        double[][] graph = new double[n][n];
        Random rand = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) { 
                double edge = rand.nextDouble();
                graph[i][j] = edge;
                graph[j][i] = edge; 
            }
            graph[i][i] = 0; 
        }
		return graph;
        
    }

        public static double[][] generateRandomGraphSeed(int n, long seed) {
    	
        double[][] graph = new double[n][n];
        Random rand = new Random(seed);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) { 
                double edge = rand.nextDouble();
                graph[i][j] = edge;
                graph[j][i] = edge; 
            }
            graph[i][i] = 0; 
        }
		return graph;
        
    }
    
    public static double[][] generateMetricGraph(int n) {
        double[][] graph = new double[n][n];
        Random rand = new Random();
        
        // Generiraj n točaka u 2D prostoru (0-1, 0-1)
        double[][] points = new double[n][2];
        for(int i=0; i<n; i++) {
            points[i][0] = rand.nextDouble(); // x
            points[i][1] = rand.nextDouble(); // y
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) { 
                double dx = points[i][0] - points[j][0];
                double dy = points[i][1] - points[j][1];
                double dist = Math.sqrt(dx*dx + dy*dy);
                
                graph[i][j] = dist;
                graph[j][i] = dist; 
            }
            graph[i][i] = 0; 
        }
		return graph;
    }
    

}