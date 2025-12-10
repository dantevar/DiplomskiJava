package fer;

import java.util.List;
public class Graph {
	
	double[][] distance_matrix ;
	double[][] min_distances;
	List<int[]> nextHops;
	int n ;
	
	public Graph(double[][] distance_matrix) {
		
		this.n = distance_matrix.length;
		this.distance_matrix = distance_matrix;
		this.min_distances = Utils.floydWarshall(distance_matrix);
		this.nextHops = Utils.floydWarshallNext(distance_matrix, min_distances);
		
		
			
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
	
	
	
	
	

}
