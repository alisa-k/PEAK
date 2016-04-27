/**
 * 
 */
package model1;

/**
 * @author Qian Yang
 * Purpose of This Class:
 * Other Notes Relating to This Class (Optional):
 */

import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Set;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.apache.commons.io.FileUtils;
import org.jgrapht.alg.ConnectivityInspector;


import it.uniroma1.lcl.adw.ADW;

public class step11 {
	//public static void step11(HashMap<String, String> trip, HashMap<String, String> sen) throws IOException {
	public static void step11() throws IOException {
		
		HashMap<String, String> trip = new HashMap<String, String>();
		HashMap<String, String> sen = new HashMap<String, String>();
		
		File file = new File("output/step8-out-TripSen.txt");
		String content = FileUtils.readFileToString(file);
		String[] content_parts = content.split("&");
		for(int i = 0; i < content_parts.length-1; i+=3) {
			trip.put(content_parts[i], content_parts[i+1]);
			sen.put(content_parts[i], content_parts[i+2]);
		}
		
		ADW pipeLine = new ADW();

		double similarityThreshold = 0.8;

		File logFile = new File("output/step11-output-labels.txt");
		PrintStream outputPrintStream = new PrintStream(logFile);
		System.setOut(outputPrintStream);

		final DirectedWeightedMultigraph<String, RelationshipEdge> g = new DirectedWeightedMultigraph<String, RelationshipEdge>(
				RelationshipEdge.class);
		final DirectedWeightedMultigraph<String, RelationshipEdge> g2 = new DirectedWeightedMultigraph<String, RelationshipEdge>(
				RelationshipEdge.class);

		HashMap<String, String> allPhrases = new HashMap<String, String>();// all
																			// hit
																			// S/P/O

		// maintain a weight hashSet
		FileReader reader1 = new FileReader("output/step8-out-PyramidForCombination.txt");
		BufferedReader br1 = new BufferedReader(reader1);
		String str1 = null;

		str1 = br1.readLine();
		while (str1 != null) {
			if (str1.startsWith("FROM"))
				str1 = br1.readLine();
			else {
				String text1 = str1;
				String[] splited1 = text1.split("&");
				String vertex1 = splited1[3];
				String vertex2 = splited1[2];
				allPhrases.put(vertex1, vertex2);

				g2.addVertex(vertex1);

				str1 = br1.readLine();
			}

		}
		br1.close();

		// add all contributors into a graph
		FileReader reader = new FileReader("output/step9-output-combineLog.txt");
		BufferedReader br = new BufferedReader(reader);
		String str = null;

		str = br.readLine();
		while (str != null) {
			String text = str;
			String[] splited = text.split("&");
			String vertex1 = splited[1];
			String vertex2 = splited[3];
			if (g.containsVertex(vertex1) && g.containsVertex(vertex2))
				;
			else {
				g.addVertex(vertex1);
				g.addVertex(vertex2);
				g.addEdge(vertex1, vertex2, new RelationshipEdge<String>(
						vertex1, vertex2, "any", 1));
			}
			str = br.readLine();
		}

		// start to calculate the similarity score between triples from
		// different sentences
		for (String vertexIterator2 : g2.vertexSet()) {
			if (g.containsVertex(vertexIterator2))
				;
			else {
				g.addVertex(vertexIterator2);
			}

		}
		for (String vertexIterator2 : g2.vertexSet()) {
			for (String vertexIterator1 : g.vertexSet()) {
				if (!vertexIterator2.equals(vertexIterator1)) {
					ItemType text1Type = ItemType.SURFACE;
					ItemType text2Type = ItemType.SURFACE;

					// measure for comparing semantic signatures
					SignatureComparison measure = new WeightedOverlap();

					// calculate the similarity of text1 and text2
					double similarity = pipeLine.getPairSimilarity(
							vertexIterator1, vertexIterator2,
							DisambiguationMethod.ALIGNMENT_BASED, measure,
							text1Type, text2Type);
					if (similarity >= similarityThreshold) {
						g.addEdge(vertexIterator1, vertexIterator2,
								new RelationshipEdge<String>(vertexIterator1,
										vertexIterator2, "any", 1));

					}
				}

			}
		}

		ConnectivityInspector<String, RelationshipEdge> ci = new ConnectivityInspector<String, RelationshipEdge>(g);
		
		
		String pyramidInspectionDebugOutput = "";
		int scuCounter = 1;
		String scuSeparator = String.format("%0" + 80 + "d", 0).replace("0", "*").concat("\n");
		String anchorSeparator = String.format("%0" + 80 + "d", 0).replace("0", "-").concat("\n");
		// output to the file step-11-output-labels.txt
		for (Set<String> iteratorCi : ci.connectedSets()) {// Returns a list of
															// Set s, where each
															// set contains all
															// vertices that are
															// in the same
															// maximally
															// connected
															// component. All
															// graph vertices
															// occur in exactly
															// one set. For more
															// on maximally
															// connected
															// component
			pyramidInspectionDebugOutput = pyramidInspectionDebugOutput.concat(scuSeparator + "SCU Number: " + Integer.toString(scuCounter++) + "\n" + scuSeparator);
			System.out.print("&");
			int highestScore = 0;
			for (String subIteratorCi : iteratorCi) {
				
				pyramidInspectionDebugOutput = pyramidInspectionDebugOutput.concat(
						anchorSeparator + "Anchor Triple: " + subIteratorCi + " \n "
						+ "Weight: " + Integer.parseInt(allPhrases.get(subIteratorCi)) + "\n" + anchorSeparator);
				
				String s = sen.get(subIteratorCi);
				String t = trip.get(subIteratorCi);
				
				// Formatting Output
				String contribs[] = s.replaceFirst("\\*", "").split("\n");
		        for (int i = 0; i < contribs.length; i++) {
		            contribs[i] = contribs[i].replaceAll("\\*Sen-", "").replaceAll(":(.*)", "");
		        }
		        
		        String lines[] = s.replaceFirst("\\*", "").replaceFirst("\\*Sen-", "").split("\\*Sen-");
		        for (int i = 0; i < lines.length; i++) {
		            lines[i] = lines[i].replaceAll("(.*)# ", "* ");
		        }
		        
		        String triples[] = t.replaceFirst("\\*#", "").split("#");
		        for (int i = 0; i < triples.length; i++) {
		            triples[i] = triples[i].replaceAll("\"(.*):\"", "\\*");
		            triples[i] = triples[i].replaceAll("\\*\"", "\\* \"");
		        }
		        
		        pyramidInspectionDebugOutput = pyramidInspectionDebugOutput.concat("Sentences & Triples" + "\n");
		        // Concatenate Sentences and Triples
		        for (int i = 0; i < contribs.length; i++) {
		        	pyramidInspectionDebugOutput = pyramidInspectionDebugOutput.concat("# " + contribs[i] + " #" + "\n");
		        	pyramidInspectionDebugOutput = pyramidInspectionDebugOutput.concat(lines[i] + triples[i] + "\n");
		        }
				
				System.out.print(subIteratorCi + "&");
				if (Integer.parseInt(allPhrases.get(subIteratorCi)) > highestScore)
					highestScore = Integer.parseInt(allPhrases.get(subIteratorCi));
			}
			System.out.println(highestScore + "&");
		}
		br.close();
		
		File pyramidOutput = new File("output/pyramid-inspection-debug-output.txt");
		PrintStream outputPrintStream1 = new PrintStream(pyramidOutput);
		System.setOut(outputPrintStream1);
		System.out.print(pyramidInspectionDebugOutput);

		/*
		 * System.out.println(
		 * "=========================normal SCU=========================");
		 * for(String vertexIterator2:g2.vertexSet()){
		 * if(g.containsVertex(vertexIterator2)); else
		 * System.out.println("&"+vertexIterator2
		 * +"&"+allPhrases.get(vertexIterator2)+"&"); }
		 */

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
