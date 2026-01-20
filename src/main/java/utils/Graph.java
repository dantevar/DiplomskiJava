package utils;

import java.util.ArrayList;
import java.util.List;
public class Graph {
	
	public double[][] distance_matrix ;
	public double[][] min_distances;
	public List<int[]> nextHops;
	public int n ;
	
	public Graph(double[][] distance_matrix) {
		
		this.n = distance_matrix.length;
		this.distance_matrix = distance_matrix;
		this.min_distances = floydWarshall(distance_matrix);
		this.nextHops = floydWarshallNext(distance_matrix, min_distances);
		
			
	}
	
	public int[] getHops(int i) {
		return nextHops.get(i);
	}
	
	@Override
	public String toString() {
		for(int i = 0; i < distance_matrix.length; i++) {
			for(int j= 0; j < distance_matrix.length; j++) {
				System.out.print(distance_matrix[i][j] + " ");
			}
			System.out.println();
		}
		for(int i = 0; i < min_distances.length; i++) {
			for(int j= 0; j < min_distances.length; j++) {
				System.out.print(min_distances[i][j] + " ");
			}
			System.out.println();
		}
		
		for(int[] hops : nextHops) {
			for(int i = 0; i < hops.length; i++)
				System.out.print(hops[i]);
			System.out.println();
		}
		return "";
	}

    public static double[][] floydWarshall(double[][] w) {
	    int n = w.length;
	    double[][] dist = new double[n][n];

	    // copy
	    for (int i = 0; i < n; i++) {
	        System.arraycopy(w[i], 0, dist[i], 0, n);
	    }

	    for (int k = 0; k < n; k++) {
	        for (int i = 0; i < n; i++) {
	            double dik = dist[i][k];
	            if (dik == Double.POSITIVE_INFINITY) continue;
	            for (int j = 0; j < n; j++) {
	                double alt = dik + dist[k][j];
	                if (alt < dist[i][j]) dist[i][j] = alt;
	            }
	        }
	    }
	    return dist;
	}

	public static List<int[]> floydWarshallNext(double[][] w, double[][] dist) {
		
		int n = w.length;
	    List<int[]> result = new ArrayList<>();

	    for (int u = 0; u < n; u++) {
	        List<Integer> hops = new ArrayList<>();

	        for (int k = 0; k < n; k++) {
	            if (u == k) continue;
	            if (w[u][k] == Double.POSITIVE_INFINITY) continue; // nije susjed

	            boolean useful = false;

	            // tražimo barem jedno v kojem je k prvi korak optimalnog puta
	            for (int v = 0; v < n; v++) {
	                if (u == v) continue;

	                double direct = dist[u][v];
	                double viaK = w[u][k] + dist[k][v];

	                // ako je k dopustiv prvi korak za ijedan v
	                if (viaK == direct) {
	                    useful = true;
	                    break;
	                }
	            }

	            if (useful) hops.add(k);
	        }

	        result.add(hops.stream().mapToInt(x -> x).toArray());
	    }

	    return result;

	}
	
	
	
	
	

}
