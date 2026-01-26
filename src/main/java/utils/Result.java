package utils;

import java.util.List;

public class Result {

    public final double cost;
    public final List<Integer> tour;
    public Object walk;
    
    public Result(double cost, List<Integer> tour) {
        this.cost = cost;
        this.tour = tour;
    }

    @Override
    public String toString() {
        
        return "Cost: " + cost + ", Tour: " + tour.toString();
    }

}
