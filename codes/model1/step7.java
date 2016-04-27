/**
 * 
 */
package model1;

/**
 * @author Qian Yang
 * Purpose of This Class:
 * Other Notes Relating to This Class (Optional):
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import model3.ConfigFileReader;

public class step7 {
	public static void step7() throws IOException {
		
		File[] models = (new ConfigFileReader("config.txt")).getModelSummaries();

		File logFile = new File("output/step7-out-weight.txt");
		PrintStream outputPrintStream = new PrintStream(logFile);
		System.setOut(outputPrintStream);

		File logFile1 = new File("output/step7-out-PyramidTriple.txt");
		PrintStream outputPrintStream1 = new PrintStream(logFile1);
		System.setOut(outputPrintStream1);

		File logFile2 = new File("output/step7-out-PyramidSenten.txt");
		PrintStream outputPrintStream2 = new PrintStream(logFile2);
		System.setOut(outputPrintStream2);

		File logFile3 = new File("output/step7-out-PyramidOnlyWeightForScoring.txt");
		PrintStream outputPrintStream3 = new PrintStream(logFile3);
		System.setOut(outputPrintStream3);

		HashMap<String, Integer> allPhrases = new HashMap<String, Integer>();
		
		HashMap<String, Integer> allPhrases1 = new HashMap<String, Integer>();
		HashMap<String, String> allPhrases2forContributor = new HashMap<String, String>();
		HashMap<String, String> allPhrases2forContributorTriple = new HashMap<String, String>();

		
		FileReader reader = new FileReader("output/step3-out.txt");
		BufferedReader br = new BufferedReader(reader);
		String str = null;

		final DirectedWeightedMultigraph<String, RelationshipEdge> g = new DirectedWeightedMultigraph<String, RelationshipEdge>(
				RelationshipEdge.class);

		// Add all salient nodes into a HashSet "allPhrases"
		// Add all salient nodes as vertices in graph g
		while ((str = br.readLine()) != null) {
			allPhrases.put(str, 1);
			g.addVertex(str);   // Capital "Matter" is different from small
								// "matter" because we want to consider both of
								// them in different sentences, different
								// positions.
		}
		
		/*
		 * For each pair of salient nodes, compute the weight, set of contributor sentences and triples
		 * from each model. 
		 * */
		for (String vertexIterator1 : g.vertexSet()) {
			for (String vertexIterator2 : g.vertexSet()) {
				if (!vertexIterator1.equals(vertexIterator2)) {
					int weight = 0;
					String contributorTriple = "*";// for the output1
					String contributorSenten = "*";// for the output2

					// Find g1 for vertexIterator1, g2 for vertexIterator2, they
					// are all equivalent set
					FileReader readerESet = new FileReader("output/step2-out-SimilarityClassAfterAscending.txt");
					BufferedReader brESet = new BufferedReader(readerESet);
					String strESet = null;
					DirectedWeightedMultigraph<String, RelationshipEdge> g1 = new DirectedWeightedMultigraph<String, RelationshipEdge>(
							RelationshipEdge.class);
					DirectedWeightedMultigraph<String, RelationshipEdge> g2 = new DirectedWeightedMultigraph<String, RelationshipEdge>(
							RelationshipEdge.class);

					// Add the element itself to the equivalent set
					g1.addVertex(vertexIterator1);
					g2.addVertex(vertexIterator2); 

					// Add all neighbors of vertexIterator1 to g1
					// Add all neighbors of vertexIterator2 to g2
					while ((strESet = brESet.readLine()) != null) {
						if (strESet.startsWith(vertexIterator1))
							g1.addVertex(strESet.replaceFirst(vertexIterator1,""));
						else if (strESet.startsWith(vertexIterator2))
							g2.addVertex(strESet.replaceFirst(vertexIterator2,""));
					}
					brESet.close();
					
					// For every model, we go through all the triples, and if there exists a triple which has 
					// a neighbor of vertexIterator1 and a neighbor of vertexIterator2 (each of the 2 salient nodes),
					// then this model contributes one unit of weight to the pair of salient nodes, vertexIterator1 and vertexIterator2
					for (File model : models) {
						String model_name = (model.getName()).split("-")[0];
						
						FileReader reader1 = new FileReader(model);
						BufferedReader br1 = new BufferedReader(reader1);
						String str1 = null;
						String sentence1 = null;
						int flag1 = 1;
						while ((str1 = br1.readLine()) != null && flag1 == 1) {
							if (str1.equals("\n"))
								str1 = br1.readLine();
							else if (!str1.startsWith("#")) {
								String text1 = str1;
								String triple1 = "*";// for the output
	
								String[] splited1 = text1.split("\"");
								
								// Get the triple in a proper format (without "1. ")
								for (String iterator1 : splited1) {
									if (iterator1.matches("^[a-zA-Z].*$")) {
										triple1 = triple1.concat(" " + "\""
												+ iterator1 + "\"");
									}
								}
								triple1 = triple1.replace("* ", "");
								
								// For every pair of vertices such that first is from g1, second from g2, 
								// if the pair is contained in the current triple, we increase the weight
								// corresponding to that triple pair, and concatenate the contributor info to
								// contributorTriple and contributorSenten. 
								for (String vertexIteratorG1 : g1.vertexSet()) {
									if (flag1 == 1) {
										for (String vertexIteratorG2 : g2.vertexSet()) {
											if (flag1 == 1) {
												if (triple1.contains(vertexIteratorG1) && 
													triple1.contains(vertexIteratorG2)) {
													weight++;
													contributorTriple = contributorTriple
															.concat("#\""+ model_name + ":\" "
																	+ triple1
																	+ "\n"
																	+ "*"
																	+ "\""
																	+ vertexIteratorG1
																	+ "\" \""
																	+ vertexIteratorG2
																	+ "\"" + "\n");
													
													contributorSenten = contributorSenten
															.concat("*Sen-" + model_name + ": "
																	+ sentence1
																	+ "\n");
													flag1 = 0;
												}
											}
										}
									}
								}
	
							} else if (str1.startsWith("# Line")) {
								sentence1 = str1.replace("# Line",
										"FROM: " + model_name + "-# Line");
							}
						}
						br1.close();
					} 
					
					allPhrases1.put("*\"" + vertexIterator1 + "\"" + " " + "\""
							+ vertexIterator2 + "\"" + " ", weight);
					allPhrases2forContributor.put("*\"" + vertexIterator1
							+ "\"" + " " + "\"" + vertexIterator2 + "\"" + " ",
							contributorSenten.replace("\\*", ""));
					allPhrases2forContributorTriple.put("*\"" + vertexIterator1
							+ "\"" + " " + "\"" + vertexIterator2 + "\"" + " ",
							contributorTriple.replace("\\*", ""));

					System.setOut(outputPrintStream);
					System.out.println("\"" + vertexIterator1 + "\"" + " "
							+ "\"" + vertexIterator2 + "\"" + " " + "\""
							+ weight + "\"");
					System.out.println(contributorTriple.replace("\\*", ""));
				}
			}
		}
		
		/*
		 * For each model summary, we assign weights to salient triples
		 * For each salient triple (anchor), output its weight, the sentence & summary it came from,
		 * all contributor triples that contributed to its weight
		 * */
		for (File model : models) {
			String model_name = (model.getName()).split("-")[0];
			
			FileReader reader1 = new FileReader(model);
			BufferedReader br1 = new BufferedReader(reader1);
			String str1 = null;
			String sentence1 = null;
			while ((str1 = br1.readLine()) != null) {
				if (str1.equals("\n"))
					str1 = br1.readLine();
				else if (!str1.startsWith("#")) {
					int hits = 0;
					String text1 = str1;
					String triple1 = "*";// for the output
	
					String twoPhrases1 = "*";// for the weight
	
					String[] splited1 = text1.split("\"");
					// String lastIterator1 = null;
					for (String iterator1 : splited1) {
						if (iterator1.matches("^[a-zA-Z].*$")) {
							triple1 = triple1.concat(" " + "\"" + iterator1 + "\"");
							if (allPhrases.get(iterator1) != null) {
								hits++;
								twoPhrases1 = twoPhrases1.concat("\"" + iterator1
										+ "\"" + " ");
							}
						}
					}
					// 2 salient nodes in the triple
					if (hits == 2) {
						System.setOut(outputPrintStream2);
						System.out
								.println("\""
										+ allPhrases1.get(twoPhrases1) // weight
										+ "\""
										+ " "
										+ triple1.replace("* ", "") // triple
										+ "\n"
										+ sentence1 // sentence that the triple is from
										+ "\n"
										+ twoPhrases1 // salient nodes in the triple
										+ "\n"
										+ allPhrases2forContributor.get(twoPhrases1) + "\n"); // contributor sentences that contribute to the weight of the triple
						System.setOut(outputPrintStream1);
						System.out.println("\"" + allPhrases1.get(twoPhrases1) // weight
								+ "\"" + " " + triple1.replace("* ", "") + "\n" // triple
								+ twoPhrases1 + "\n" // salient nodes from the triple
								+ allPhrases2forContributorTriple.get(twoPhrases1) // contributor triples that contribute to weight of the triple
								+ "\n");
						System.setOut(outputPrintStream3);
						// just outputs weight and anchor triple
						System.out.println("\"" + allPhrases1.get(twoPhrases1) 
								+ "\"" + " " + triple1.replace("* ", "") + "\n");
					}
					// 3 salient nodes in the triple (reduce to 2 salient nodes)
					else if (hits == 3) {
						System.setOut(outputPrintStream2);
						String[] splited1Second = twoPhrases1.replace("*", "")
								.split("\"");

						String string12 = "*\"" + splited1Second[1] + "\"" + " "
								+ "\"" + splited1Second[3] + "\"" + " ";
						String string13 = "*\"" + splited1Second[1] + "\"" + " "
								+ "\"" + splited1Second[5] + "\"" + " ";
						String string23 = "*\"" + splited1Second[3] + "\"" + " "
								+ "\"" + splited1Second[5] + "\"" + " ";
						String string21 = "*\"" + splited1Second[3] + "\"" + " "
								+ "\"" + splited1Second[1] + "\"" + " ";
						String string31 = "*\"" + splited1Second[5] + "\"" + " "
								+ "\"" + splited1Second[1] + "\"" + " ";
						String string32 = "*\"" + splited1Second[5] + "\"" + " "
								+ "\"" + splited1Second[3] + "\"" + " ";
	
						String highestString = null;
						int highest = 0;
						if (allPhrases1.get(string12) != null
								&& allPhrases1.get(string12) > highest) {
							twoPhrases1 = string12;
							highest = allPhrases1.get(string12);
						} else if (allPhrases1.get(string13) != null
								&& allPhrases1.get(string13) > highest) {
							twoPhrases1 = string13;
							highest = allPhrases1.get(string13);
						} else if (allPhrases1.get(string23) != null
								&& allPhrases1.get(string23) > highest) {
							twoPhrases1 = string23;
							highest = allPhrases1.get(string23);
						} else if (allPhrases1.get(string21) != null
								&& allPhrases1.get(string21) > highest) {
							twoPhrases1 = string21;
							highest = allPhrases1.get(string21);
						} else if (allPhrases1.get(string31) != null
								&& allPhrases1.get(string31) > highest) {
							twoPhrases1 = string31;
							highest = allPhrases1.get(string31);
						} else if (allPhrases1.get(string32) != null
								&& allPhrases1.get(string32) > highest) {
							twoPhrases1 = string32;
							highest = allPhrases1.get(string32);
						}
						System.setOut(outputPrintStream2);
						System.out
								.println("\""
										+ allPhrases1.get(twoPhrases1)
										+ "\""
										+ " "
										+ triple1.replace("* ", "")
										+ "\n"
										+ sentence1
										+ "\n"
										+ twoPhrases1
										+ "\n"
										+ allPhrases2forContributor.get(twoPhrases1) + "\n");
						System.setOut(outputPrintStream1);
						System.out.println("\"" + allPhrases1.get(twoPhrases1)
								+ "\"" + " " + triple1.replace("* ", "") + "\n"
								+ twoPhrases1 + "\n"
								+ allPhrases2forContributorTriple.get(twoPhrases1)
								+ "\n");
						System.setOut(outputPrintStream3);
						System.out.println("\"" + allPhrases1.get(twoPhrases1)
								+ "\"" + " " + triple1.replace("* ", "") + "\n");
					}
	
				} else if (str1.startsWith("# Line")) {
					sentence1 = str1.replace("# Line", "FROM: " + model_name + "-# Line");
				}
			}
			br1.close();
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
