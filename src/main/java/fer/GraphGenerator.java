package fer;

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
    
    public static void main(String[] args) {
		
    	for(int i = 0; i<200; i++) {
        	int n =17;
        	double[][] w = generateRandomGraph(n);
        	
        	
        	Graph g = new Graph(w);
        	
        	System.out.println(MyAlg.bfsWalk(g));
    	}

    	System.out.println("done");
    	
	}
}