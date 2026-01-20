package fer;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import utils.*;
public class MyAlgOptimized {

    private static class SharedState {
        volatile double bestCost = Double.POSITIVE_INFINITY;
        volatile Walk bestWalk = null;
        final Map<Long, Double> visited = new ConcurrentHashMap<>();
        final PriorityBlockingQueue<Walk> queue = new PriorityBlockingQueue<>(1000, Comparator.comparingDouble(w -> w.cost));
        final AtomicInteger activeThreads = new AtomicInteger(0);
        final int n;
        final int maxLen;
        final Graph g;
        final double[] minOutgoing; // Optimization: Precomputed min outgoing edge for each node

        SharedState(Graph g) {
            this.g = g;
            this.n = g.n;
            this.maxLen = 2 * n - 2 + 1;
            
            // Precompute min outgoing edges for heuristic
            this.minOutgoing = new double[n];
            for (int i = 0; i < n; i++) {
                double min = Double.POSITIVE_INFINITY;
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        min = Math.min(min, g.distance_matrix[i][j]);
                    }
                }
                this.minOutgoing[i] = min;
            }
        }

        synchronized void updateBest(Walk w, double cost) {
            if (cost < bestCost) {
                bestCost = cost;
                bestWalk = w;
            }
        }
    }

    public static Walk bfsWalkParallel(Graph g) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        // Limit threads if graph is small to avoid overhead
        if (g.n < 10) numThreads = 2;
        
        SharedState state = new SharedState(g);
        state.queue.add(new Walk(0, 0));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> worker(state));
        }

        executor.shutdown();
        try {
            // Wait until all tasks are finished
            // We rely on the worker logic to terminate when queue is empty and no active threads
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return state.bestWalk;
    }

    private static void worker(SharedState state) {
        while (true) {
            Walk currentWalk = state.queue.poll();

            if (currentWalk == null) {
                // Queue is empty. Check if we should terminate.
                if (state.activeThreads.get() == 0) {
                    // No one is working, queue is empty -> Done.
                    break;
                }
                
                // Wait a bit for other threads to produce work
                try {
                    currentWalk = state.queue.poll(5, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                if (currentWalk == null) {
                    // Still empty. Double check termination condition.
                    if (state.activeThreads.get() == 0 && state.queue.isEmpty()) {
                        break;
                    }
                    continue;
                }
            }

            state.activeThreads.incrementAndGet();
            try {
                processWalk(currentWalk, state);
            } finally {
                state.activeThreads.decrementAndGet();
            }
        }
    }

    private static void processWalk(Walk currentWalk, SharedState state) {
        Graph g = state.g;
        int n = state.n;
        int head = currentWalk.getHead();

        // --- Optimization: Improved Heuristic Pruning ---
        // Basic lower bound: current cost + shortest path to 0
        double heuristic = g.min_distances[head][0];
        
        // Advanced lower bound: sum of min outgoing edges for all unvisited nodes
        // We must leave every unvisited node at least once.
        double unvisitedSum = 0;
        int visitedBits = currentWalk.getVisitedBits();
        
        // Check if we can prune based on unvisited nodes
        // Iterate through all nodes to check if they are visited
        // This loop is O(N), which is small (N <= 20 usually)
        for (int i = 1; i < n; i++) { // Skip 0 as it's start/end
            if ((visitedBits & (1 << i)) == 0) {
                unvisitedSum += state.minOutgoing[i];
            }
        }
        
        // The heuristic is the maximum of the simple return cost and the unvisited cost
        // Note: This is a loose bound. A tighter one would be MST, but this is O(N) vs O(N^2).
        // Actually, we need to visit unvisited AND return to 0.
        // So cost >= current + unvisitedSum.
        // Also cost >= current + min_dist[head][0].
        // We can take the max.
        
        double estimatedTotal = currentWalk.cost + Math.max(heuristic, unvisitedSum);

        if (estimatedTotal >= state.bestCost || currentWalk.length >= state.maxLen) {
            return;
        }

        // --- Optimization: Concurrent Visited Check ---
        // We use the state (visited mask + head) as key
        Double prevCost = state.visited.get(currentWalk.state);
        if (prevCost != null && prevCost <= currentWalk.cost) {
            return;
        }
        state.visited.put(currentWalk.state, currentWalk.cost);

        // --- Check Goal ---
        if (currentWalk.isWalkDone(n)) {
            // We are done visiting all nodes. Now we just need to return to 0.
            // The cost to return is g.min_distances[head][0] (which might involve multiple hops)
            // But Walk object represents actual hops.
            // If we are at a node where we can jump to 0 directly or via path.
            // The problem asks for a Walk.
            // If isWalkDone is true, it means we visited all bits.
            // We still need to close the loop to 0.
            // The cost to close is g.min_distances[head][0].
            double finalCost = currentWalk.cost + g.min_distances[head][0];
            state.updateBest(currentWalk, finalCost);
            return; 
        }

        // --- Expand ---
        // Optimization: Sort hops by edge weight? 
        // PriorityQueue handles the order of processing, but adding better children first 
        // might help finding a solution faster (though PQ will reorder them anyway).
        // Just add them.
        
        for (int hop : g.getHops(head)) {
            double newCost = currentWalk.cost + g.distance_matrix[head][hop];
            if (newCost >= state.bestCost) continue;

            state.queue.add(new Walk(currentWalk, hop, newCost));
        }
    }
}
