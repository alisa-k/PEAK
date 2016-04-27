/**
 * 
 */
package model1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import model3.ConfigFileReader;

/**
 * @author Qian Yang 
 * Purpose of This Class: Build a graph. Set the threshold = 0.5, add a
 *         directed edge only between those vertices whose edge's weight is
 *         above this threshold. 
 *  
 * Other Notes Relating to This Class (Optional):
 *        
 */
public class step1 {

	private static final int WeightIntraSentence = 1;
	static double threshold = 0.5;

	public static void step1() throws IOException {
		
		File[] models = (new ConfigFileReader("config.txt")).getModelSummaries();
		
		ADW pipeLine = new ADW();
		
		final DirectedWeightedMultigraph<String, RelationshipEdge> g = new DirectedWeightedMultigraph<String, RelationshipEdge>(
				RelationshipEdge.class);

		File logFile = new File("output/step1-out.txt");
		PrintStream outputPrintStream = new PrintStream(logFile);
		System.setOut(outputPrintStream);

		File logFile2 = new File("output/step1-out-afterHash.txt");
		PrintStream outputPrintStream2 = new PrintStream(logFile2);
		
		/*
		 * For each model summary, pick out the triples and put them into the graph, g,
		 * as vertices. Join each triple part (S/P/O) by a hyper-edge.
		 * 
		 * */
		for (File model : models) {
			FileReader reader1 = new FileReader(model);
			BufferedReader br1 = new BufferedReader(reader1);
			String str1 = null;
	
			// Go through clausIE output and find the triples
			while ((str1 = br1.readLine()) != null) {
				if (str1.equals("\n"))
					str1 = br1.readLine();
				else if (!str1.startsWith("#")) {
					// Triple found
					String text1 = str1;
					String[] splited1 = text1.split("\"");
	
					// Iterate through parts and add each part of the triple as a vertex.
					// Join each triple part by a hyper-edge.
					String lastIterator1 = null;
					for (String iterator1 : splited1) {
						if (iterator1.matches("^[a-zA-Z].*$")) {
							if (lastIterator1 != null) {
								g.addVertex(iterator1);
								g.addEdge(lastIterator1, iterator1,
										new RelationshipEdge<String>(lastIterator1,
												iterator1, "intraSentence",
												WeightIntraSentence));
								lastIterator1 = iterator1;
							} else {
								g.addVertex(iterator1);
								lastIterator1 = iterator1;
							}
	
						}
					}
				}
			}
			br1.close();
		}

		// Keep a HashSet to avoid the duplication
		Set<java.lang.String> allNodes = new HashSet<java.lang.String>();

		/*
		 * Find similarities between each pair of vertices in g. 
		 * If similarity is > 0.5, we add an edge between the pair
		 * whose weight is the similarity, and write the pair and 
		 * similarity to step1-out.txt. 
		 * 
		 * */
		for (String vertexIterator1 : g.vertexSet()) {
			for (String vertexIterator2 : g.vertexSet()) {
				if (!vertexIterator1.equals(vertexIterator2)) {
					ItemType text1Type = ItemType.SURFACE;
					ItemType text2Type = ItemType.SURFACE;

					SignatureComparison measure = new WeightedOverlap();

					// calculate the similarity between text1 and text2
					double similarity = pipeLine.getPairSimilarity(
							vertexIterator1, vertexIterator2,
							DisambiguationMethod.ALIGNMENT_BASED, measure,
							text1Type, text2Type);
					if (similarity >= threshold) {
						System.setOut(outputPrintStream);
						System.out.println("\"" + vertexIterator1 + "\""
								+ "   " + "\"" + vertexIterator2 + "\"" + "   "
								+ "\"" + similarity + "\"");
						g.addEdge(vertexIterator1, vertexIterator2,
								new RelationshipEdge<String>(vertexIterator1,
										vertexIterator2, "interSentence",
										similarity));
						allNodes.add(vertexIterator1);
					}
				}

			}
		}

		// Use a HashMap to map our vertices into integers
		HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
		HashMap<Integer, String> hashMapReverse = new HashMap<Integer, String>();

		File logFileForHashMapReverseOriginal = new File("step1-out-hashMapReverse.db");
		PrintStream logPrintStreamForHashMapReverseOriginal = new PrintStream(logFileForHashMapReverseOriginal);
		System.setOut(logPrintStreamForHashMapReverseOriginal);

		int integerOfHashMap = 0;
		for (String vertex : allNodes) {
			// Put elements to the hashMap
			hashMap.put(vertex, new Integer(integerOfHashMap));
			hashMapReverse.put(integerOfHashMap, vertex);
			System.out.printf(integerOfHashMap + "\n" + hashMapReverse.get(integerOfHashMap) + "\n");
			integerOfHashMap++;
		}

		// preProcessing for hierarchical-clustering
		for (String vertexIterator1 : g.vertexSet()) {
			for (String vertexIterator2 : g.vertexSet()) {
				if (!vertexIterator1.equals(vertexIterator2)) {
					ItemType text1Type = ItemType.SURFACE;
					ItemType text2Type = ItemType.SURFACE;

					// measure for comparing semantic signatures
					SignatureComparison measure = new WeightedOverlap();

					// calculate the similarity of text1 and text2
					double similarity = pipeLine.getPairSimilarity(
							vertexIterator1, vertexIterator2,
							DisambiguationMethod.ALIGNMENT_BASED, measure,
							text1Type, text2Type);
					if (similarity >= 0.5) {
						System.setOut(outputPrintStream2);
						System.out.println("\"" + hashMap.get(vertexIterator1)
								+ "\"" + "   " + "\""
								+ hashMap.get(vertexIterator1) + "\"" + "   "
								+ "\"" + similarity + "\"");
					}
				}

			}
		}

	}

	public static class RelationshipEdge<V> extends DefaultWeightedEdge {
		private V v1;
		private V v2;
		private String label;
		private double weight;

		public RelationshipEdge(V v1, V v2, String label, double weight) {
			this.v1 = v1;
			this.v2 = v2;
			this.label = label;
			this.weight = weight;

		}

		public V getV1() {
			return v1;
		}

		public V getV2() {
			return v2;
		}

		public String toString() {
			return label;
		}

		public double getWeight() {
			return weight;
		}

	}
}
