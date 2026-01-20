package analiza;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import java.awt.*;

import utils.*;


public class BruteAndMinPaths {
	
	public static void main(String[] args) {
		int n = 10; // Manji n za brute force
		double[][] w = GraphGenerator.generateRandomGraph(n);
		Graph g = new Graph(w);
		
		System.out.println("Analiza permutacija za n=" + n);
		
		// Mapa: Cijena (zaokružena) -> Broj pojavljivanja
		// Koristimo TreeMap da bude sortirano po cijeni
		Map<Long, Integer> distribution = new TreeMap<>();
		
		List<Integer> vertices = new ArrayList<>();
		for(int i = 1; i < n; i++) {
			vertices.add(i);
		}
		
		generatePermutations(g, vertices, 0, distribution);
		
		System.out.println("Distribucija cijena (Cijena -> Broj):");
		for(Map.Entry<Long, Integer> entry : distribution.entrySet()) {
			double cost = entry.getKey() / 1e14;
			System.out.printf("%.14f : %d%n", cost, entry.getValue());
		}

		// Prikaz grafa
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Distribucija cijena permutacija");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 600);
			frame.add(new DistributionPanel(distribution));
			frame.setVisible(true);
		});
	}
	
	private static void generatePermutations(Graph g, List<Integer> vertices, int index, Map<Long, Integer> distribution) {
		if (index == vertices.size()) {
			double cost = calculateTourCost(g, vertices);
			if (!Double.isInfinite(cost)) {
				// Zaokruživanje na 14 decimala da grupiramo iste vrijednosti
				long key = Math.round(cost * 1e14);
				distribution.put(key, distribution.getOrDefault(key, 0) + 1);
			}
			return;
		}
		
		for (int i = index; i < vertices.size(); i++) {
			Collections.swap(vertices, i, index);
			generatePermutations(g, vertices, index + 1, distribution);
			Collections.swap(vertices, i, index);
		}
	}
	
	private static double calculateTourCost(Graph g, List<Integer> vertices) {
		double cost = 0.0;
		int current = 0;
		
		for (int next : vertices) {
			cost += g.min_distances[current][next];
			current = next;
		}
		cost += g.min_distances[current][0];
		
		return cost;
	}

	static class DistributionPanel extends JPanel {
		private Map<Long, Integer> distribution;

		public DistributionPanel(Map<Long, Integer> distribution) {
			this.distribution = distribution;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int width = getWidth();
			int height = getHeight();
			int padding = 50;
			//int labelPadding = 25;

			// Draw background
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, width, height);

			if (distribution.isEmpty()) return;

			long minXKey = distribution.keySet().stream().min(Long::compare).get();
			long maxXKey = distribution.keySet().stream().max(Long::compare).get();
			int maxY = distribution.values().stream().max(Integer::compare).get();
			
			double minX = minXKey / 1e14;
			double maxX = maxXKey / 1e14;
			double xRange = maxX - minX;
			if (xRange == 0) xRange = 1;

			// Axes
			g2.setColor(Color.BLACK);
			g2.drawLine(padding, height - padding, width - padding, height - padding); // X axis
			g2.drawLine(padding, height - padding, padding, padding); // Y axis

			// Labels
			g2.drawString("Cijena", width / 2, height - 10);
			g2.drawString("Broj ponavljanja", 10, height / 2);
			
			// Max Y label
			g2.drawString(String.valueOf(maxY), 10, padding);
			// Min/Max X labels
			g2.drawString(String.format("%.2f", minX), padding, height - padding + 20);
			g2.drawString(String.format("%.2f", maxX), width - padding - 30, height - padding + 20);

			// Plot data
			g2.setColor(Color.BLUE);
			double xScale = (double) (width - 2 * padding) / xRange;
			double yScale = (double) (height - 2 * padding) / maxY;

			for (Map.Entry<Long, Integer> entry : distribution.entrySet()) {
				double xVal = entry.getKey() / 1e14;
				int yVal = entry.getValue();

				int x = (int) (padding + (xVal - minX) * xScale);
				int y = (int) (height - padding - (yVal * yScale));
				
				// Draw bar/line
				g2.drawLine(x, height - padding, x, y);
				g2.fillOval(x - 2, y - 2, 4, 4);
			}
		}
	}

}
