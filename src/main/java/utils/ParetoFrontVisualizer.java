package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utiliti klasa za vizualizaciju i export Pareto fronte
 */
public class ParetoFrontVisualizer {
    
    /**
     * Izvozi Pareto frontu u CSV format za analizu u Excel/Python
     */
    public static void exportToCSV(List<MultiObjectiveResult> paretoFront, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            // Zaglavlje
            writer.write("TotalCost,EdgeCount,MaxEdgeWeight,Variance,VertexRepetitions,Tour\n");
            
            // Podaci
            for (MultiObjectiveResult result : paretoFront) {
                writer.write(String.format("%.4f,%d,%.4f,%.4f,%d,\"%s\"\n",
                    result.totalCost,
                    result.edgeCount,
                    result.maxEdgeWeight,
                    result.variance,
                    result.vertexRepetitions,
                    result.tour.toString()
                ));
            }
            
            System.out.println("Pareto fronta izvezena u: " + filename);
        } catch (IOException e) {
            System.err.println("Greška pri izvoza u CSV: " + e.getMessage());
        }
    }
    
    /**
     * Generira HTML s interaktivnim grafom Pareto fronte (2D projekcija)
     */
    public static void generateHTML(List<MultiObjectiveResult> paretoFront, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
writer.write("<!DOCTYPE html>\n");
writer.write("<html>\n");
writer.write("<head>\n");
writer.write("    <title>Pareto Fronta - MCW Problem</title>\n");
writer.write("    <script src=\"https://cdn.plot.ly/plotly-latest.min.js\"></script>\n");
writer.write("    <style>\n");
writer.write("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
writer.write("        .container { max-width: 1200px; margin: 0 auto; }\n");
writer.write("        .chart { margin: 20px 0; }\n");
writer.write("        h1 { color: #333; }\n");
writer.write("    </style>\n");
writer.write("</head>\n");
writer.write("<body>\n");
writer.write("    <div class=\"container\">\n");
writer.write("        <h1>Višekriterijska Optimizacija MCW Problema</h1>\n");
writer.write("        <p>Pareto fronta sa " + paretoFront.size() + " rjesenja</p>\n");
writer.write("        \n");
writer.write("        <div id=\"chart1\" class=\"chart\"></div>\n");
writer.write("        <div id=\"chart2\" class=\"chart\"></div>\n");
writer.write("        <div id=\"chart3\" class=\"chart\"></div>\n");
writer.write("        \n");
writer.write("        <script>\n");
writer.write("            // Podaci\n");
writer.write("            var data = [\n");
            
            // Generiraj JavaScript objekt s podacima
            for (int i = 0; i < paretoFront.size(); i++) {
                MultiObjectiveResult r = paretoFront.get(i);
                if (i > 0) writer.write(",\n");
                writer.write(String.format(
                    "                {cost: %.4f, edges: %d, maxWeight: %.4f, variance: %.4f, reps: %d}",
                    r.totalCost, r.edgeCount, r.maxEdgeWeight, r.variance, r.vertexRepetitions
                ));
            }
            
            writer.write("""

            ];
            
            // Graf 1: Cijena vs Broj bridova
            var trace1 = {
                x: data.map(d => d.cost),
                y: data.map(d => d.edges),
                mode: 'markers',
                type: 'scatter',
                marker: { size: 8, color: 'blue' },
                text: data.map((d, i) => `Rješenje ${i+1}<br>Cijena: ${d.cost.toFixed(2)}<br>Bridova: ${d.edges}`),
                hoverinfo: 'text'
            };
            
            var layout1 = {
                title: 'Ukupna Cijena vs Broj Bridova',
                xaxis: { title: 'Ukupna Cijena' },
                yaxis: { title: 'Broj Bridova' }
            };
            
            Plotly.newPlot('chart1', [trace1], layout1);
            
            // Graf 2: Cijena vs Maks težina brida
            var trace2 = {
                x: data.map(d => d.cost),
                y: data.map(d => d.maxWeight),
                mode: 'markers',
                type: 'scatter',
                marker: { size: 8, color: 'green' },
                text: data.map((d, i) => `Rješenje ${i+1}<br>Cijena: ${d.cost.toFixed(2)}<br>Max težina: ${d.maxWeight.toFixed(2)}`),
                hoverinfo: 'text'
            };
            
            var layout2 = {
                title: 'Ukupna Cijena vs Maksimalna Težina Brida',
                xaxis: { title: 'Ukupna Cijena' },
                yaxis: { title: 'Maksimalna Težina Brida' }
            };
            
            Plotly.newPlot('chart2', [trace2], layout2);
            
            // Graf 3: Varijanca vs Ponavljanja
            var trace3 = {
                x: data.map(d => d.variance),
                y: data.map(d => d.reps),
                mode: 'markers',
                type: 'scatter',
                marker: { 
                    size: 8, 
                    color: data.map(d => d.cost),
                    colorscale: 'Viridis',
                    showscale: true,
                    colorbar: { title: 'Cijena' }
                },
                text: data.map((d, i) => `Rješenje ${i+1}<br>Varijanca: ${d.variance.toFixed(2)}<br>Ponavljanja: ${d.reps}`),
                hoverinfo: 'text'
            };
            
            var layout3 = {
                title: 'Varijanca vs Ponavljanja Vrhova',
                xaxis: { title: 'Varijanca' },
                yaxis: { title: 'Broj Ponavljanja' }
            };
            
            Plotly.newPlot('chart3', [trace3], layout3);
        </script>
    </div>
</body>
</html>
""");
            
            System.out.println("HTML vizualizacija kreirana: " + filename);
        } catch (IOException e) {
            System.err.println("Greška pri kreiranju HTML-a: " + e.getMessage());
        }
    }
    
    /**
     * Ispisuje statistički sažetak Pareto fronte
     */
    public static void printStatistics(List<MultiObjectiveResult> paretoFront) {
        if (paretoFront.isEmpty()) {
            System.out.println("Pareto fronta je prazna!");
            return;
        }
        
        // Statistika za svaki kriterij
        double[] costs = paretoFront.stream().mapToDouble(r -> r.totalCost).toArray();
        int[] edges = paretoFront.stream().mapToInt(r -> r.edgeCount).toArray();
        double[] maxWeights = paretoFront.stream().mapToDouble(r -> r.maxEdgeWeight).toArray();
        double[] variances = paretoFront.stream().mapToDouble(r -> r.variance).toArray();
        int[] reps = paretoFront.stream().mapToInt(r -> r.vertexRepetitions).toArray();
        
        System.out.println("=== STATISTIKA PARETO FRONTE ===");
        System.out.println("Broj rješenja: " + paretoFront.size());
        System.out.println();
        
        printCriterionStats("Ukupna cijena", costs);
        printCriterionStatsInt("Broj bridova", edges);
        printCriterionStats("Maks težina", maxWeights);
        printCriterionStats("Varijanca", variances);
        printCriterionStatsInt("Ponavljanja", reps);
    }
    
    private static void printCriterionStats(String name, double[] values) {
        double min = java.util.Arrays.stream(values).min().orElse(0);
        double max = java.util.Arrays.stream(values).max().orElse(0);
        double avg = java.util.Arrays.stream(values).average().orElse(0);
        
        System.out.printf("%-20s Min: %10.2f  Max: %10.2f  Avg: %10.2f  Range: %10.2f%n",
            name + ":", min, max, avg, max - min);
    }
    
    private static void printCriterionStatsInt(String name, int[] values) {
        int min = java.util.Arrays.stream(values).min().orElse(0);
        int max = java.util.Arrays.stream(values).max().orElse(0);
        double avg = java.util.Arrays.stream(values).average().orElse(0);
        
        System.out.printf("%-20s Min: %10d  Max: %10d  Avg: %10.2f  Range: %10d%n",
            name + ":", min, max, avg, max - min);
    }
}
