package fer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Walk {
	
  	long state;       // gornjih 48 bitova: visitedBits, donjih 16 bitova: head
    Walk parent;      // prethodni walk (za rekonstrukciju puta)
    public double cost;
    int length;

    // Konstruktor za početni walk (samo vrh 0)
    public Walk(int head, double cost) {
        this.state = (1L << (16 + head)) | head; // postavi bit za head u visitedBits i spremi head
        this.parent = null;
        this.cost = cost;
        this.length = 1;
    }

    // Konstruktor za proširenje postojećeg walk-a
    public Walk(Walk parent, int newHead, double cost) {
        this.parent = parent;
        this.cost = cost;
        this.length = parent.length + 1;
        
        // Inkrementalno ažuriraj state: dodaj novi bit i promijeni head
        long visitedBits = (parent.state >>> 16) | (1L << newHead); // dodaj novi vrh u visited
        this.state = (visitedBits << 16) | newHead;
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
    
    // Rekonstruiraj cijeli walk prateći parent pointere
    public List<Integer> reconstructWalk() {
        List<Integer> walk = new ArrayList<>(length);
        Walk current = this;
        while (current != null) {
            walk.add(current.getHead());
            current = current.parent;
        }
        Collections.reverse(walk);
        return walk;
    }

    @Override
    public String toString() {
        return reconstructWalk().toString() + " head:" + getHead() + " cost:" + cost;
    }

	
}
