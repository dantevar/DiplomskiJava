package fer;

import java.util.List;

public class Walk {
	
  	long state;       // gornjih 48 bitova: visitedBits, donjih 16 bitova: head
    List<Integer> walk;
    double cost;
    int length;

    public Walk(int head, List<Integer> walk, double cost) {
        this.walk = walk;
        this.cost = cost;

        int visitedBits = 0;
        for (int v : walk) {
            visitedBits |= 1 << v; // postavi bit za svaki vrh u walk
        }

        // spojimo visitedBits i head u jedan long
        this.state = (((long) visitedBits) << 16) | head;
        this.length = walk.size();
    }

    public int getHead() {
        return (int) (state & 0xFFFF); // donjih 16 bita
    }

    public int getVisitedBits() {
        return (int) (state >>> 16); // gornjih bitova
    }

    public boolean isWalkDone(int n) {
        int visited = getVisitedBits();
        int allVisitedMask = (1 << n) - 1; // n donjih bitova postavljeno na 1
        return (visited & allVisitedMask) == allVisitedMask;
    }
    

    @Override
    public String toString() {
        return this.walk.toString() + " head:" + getHead() + " cost:" + cost;
    }

	
}
