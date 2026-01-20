package fer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import utils.*;
public class MyAlg {
	
	public static Walk bfsWalk(Graph g) {
		
		int n = g.n;
		int max = 2*n -2 + 1;
		
		double bestCost = Double.POSITIVE_INFINITY;
		Walk bestWalk = null;
		
		Map<Long, Double> visited = new HashMap<>();
		
		PriorityQueue<Walk> walks = new PriorityQueue<>(
			    Comparator.comparingDouble(w -> w.cost)
			);
	
		walks.add(new Walk(0, 0));
		
		while(!walks.isEmpty()) {
			
			Walk currentWalk = walks.poll();
			//System.out.println(currentWalk + " , "+ walks.size() + " , " + bestCost);
			
			double bestPossibleCost =  currentWalk.cost + g.min_distances[currentWalk.getHead()][0];
			if(bestPossibleCost >= bestCost || currentWalk.length >= max) continue;
			
			Double prevCost = visited.get(currentWalk.state);
			if(prevCost != null && prevCost <= currentWalk.cost) {
				continue;
			}
			visited.put(currentWalk.state, currentWalk.cost);
				
			
			if(currentWalk.isWalkDone(n)) {
				
				bestCost = bestPossibleCost;
				bestWalk = currentWalk;
			}
			
			for(int hop : g.getHops(currentWalk.getHead())) {
				double newCost = currentWalk.cost + g.distance_matrix[currentWalk.getHead()][hop];
				if(newCost >= bestCost) continue;
				
				walks.add(new Walk(currentWalk, hop, newCost));
			}
		}
			
		return bestWalk;
	}
	
	

}
