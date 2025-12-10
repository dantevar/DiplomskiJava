package fer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

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
	
		List<Integer> nw = new ArrayList<>();
		nw.add(0);
		walks.add(new Walk(0, nw, 0 ));
		
		while(!walks.isEmpty()) {
			
			Walk currentWalk = walks.poll();
			//System.out.println(currentWalk + " , "+ walks.size() + " , " + bestCost);
			
			double bestPossibleCost =  currentWalk.cost + g.min_distances[currentWalk.getHead()][0];
			if(bestPossibleCost >= bestCost || currentWalk.length >= max) continue;
			
			if(!visited.containsKey(currentWalk.state)) {
				visited.put(currentWalk.state, currentWalk.cost);
			}
			else {
				if(visited.get(currentWalk.state) <= currentWalk.cost) {
				//	System.out.println("terminated");
					continue;
				}
				visited.put(currentWalk.state, currentWalk.cost);
			}
				
			
			if(currentWalk.isWalkDone(n)) {
				
				bestCost = bestPossibleCost;
				bestWalk = currentWalk;
			}
			
			for(int hop : g.getHops(currentWalk.getHead())) {
				double newCost = currentWalk.cost + g.distance_matrix[currentWalk.getHead()][hop];
				if(newCost >= bestCost) continue;
				List<Integer> newWalk = new ArrayList<>(currentWalk.walk);
				newWalk.add(hop);
				walks.add(new Walk(hop,newWalk, newCost));
			}
		}
			
		return bestWalk;
	}
	
	

}
