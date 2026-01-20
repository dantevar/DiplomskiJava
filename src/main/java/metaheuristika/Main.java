package metaheuristika;

import utils.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Metaheuristika Main");

        double[][] distances = GraphGenerator.generateRandomGraph(15);
        Graph g = new Graph(distances);
        int popSize = 20;
        int generations = 1000;
        double mutationRate = 0.01;
        int printEvery = generations / 10;
        Result res = GA.solve(g,popSize, generations, mutationRate, printEvery);
        System.out.println(res);





    }

}
