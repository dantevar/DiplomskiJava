package fer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Optimizing {
	
	public static Walk bfsHeldKarp(Graph g) {
	    int n = g.n;
	    Map<Long, Double> visited = new HashMap<>(); // DP tablica: stanje -> min cost
	    Map<Long, Walk> walkMap = new HashMap<>();    // za praćenje puta

	    // početno stanje: head=0, visitedBits=1 (samo početni čvor)
	    long startState = 1L; 
	    Walk startWalk = new Walk(0, List.of(0), 0.0);
	    visited.put((startState << 16) | 0, 0.0);
	    walkMap.put((startState << 16) | 0, startWalk);

	    for (int size = 1; size <= n; size++) { // broj čvorova u subsetu
	        Map<Long, Double> newVisited = new HashMap<>();
	        Map<Long, Walk> newWalkMap = new HashMap<>();

	        for (Map.Entry<Long, Double> entry : visited.entrySet()) {
	            long state = entry.getKey();
	            double cost = entry.getValue();
	            int head = (int) (state & 0xFFFF);
	            int visitedBits = (int) (state >>> 16);
	            Walk walk = walkMap.get(state);

	            // probaj hop u sve čvorove koji još nisu posjećeni
	            for (int hop : g.getHops(head)) {
	                if ((visitedBits & (1 << hop)) != 0) continue; // već posjećeno

	                int newVisitedBits = visitedBits | (1 << hop);
	                long newState = (((long)newVisitedBits) << 16) | hop;
	                double newCost = cost + g.distance_matrix[head][hop];

	                // update samo ako je novi cost bolji
	                if (!newVisited.containsKey(newState) || newCost < newVisited.get(newState)) {
	                    List<Integer> newWalkList = new ArrayList<>(walk.walk);
	                    newWalkList.add(hop);
	                    Walk newWalk = new Walk(hop, newWalkList, newCost);

	                    newVisited.put(newState, newCost);
	                    newWalkMap.put(newState, newWalk);
	                }
	            }
	        }
	        visited = newVisited;
	        walkMap = newWalkMap;
	    }

	    // pronađi minimalni cost za cijeli tour (svi čvorovi posjećeni)
	    double bestCost = Double.POSITIVE_INFINITY;
	    Walk bestWalk = null;
	    int allVisitedMask = (1 << n) - 1;

	    for (Map.Entry<Long, Walk> entry : walkMap.entrySet()) {
	        Walk w = entry.getValue();
	        if (w.getVisitedBits() != allVisitedMask) continue;

	        double tourCost = w.cost + g.distance_matrix[w.getHead()][0]; // povratak u start
	        if (tourCost < bestCost) {
	            bestCost = tourCost;
	            bestWalk = w;
	        }
	    }

	    return bestWalk;
	}


}
