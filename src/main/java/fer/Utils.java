package fer;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	
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
