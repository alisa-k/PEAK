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
import java.util.Set;

import model3.ConfigFileReader;

public class step6 {
	public static void step6() throws IOException {
		
		File[] models = (new ConfigFileReader("config.txt")).getModelSummaries();
		
		File logFile = new File("output/step6-out.txt");
		PrintStream outputPrintStream = new PrintStream(logFile);
		System.setOut(outputPrintStream);

		// Add all salient nodes into a HashSet
		HashMap<String, Integer> allPhrases = new HashMap<String, Integer>();

		FileReader reader = new FileReader("output/step3-out.txt");
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		while ((str = br.readLine()) != null) {
			allPhrases.put(str, 1);
		}
		
		/*
		 * Identifying Potential SCUS:
		 * For each model summary, check to see if at least two parts of each
		 * triple in the model summary are salient (by seeing if it is 
		 * in the HashSet) 
		 * If two or more parts are salient, we have a potential SCU.
		 * 
		 * */
		 for (File model : models) {
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
					String hitAhitB1 = "*";// for the output
	
					String[] splited1 = text1.split("\"");
					
					for (String iterator1 : splited1) {
						if (iterator1.matches("^[a-zA-Z].*$")) {
							triple1 = triple1.concat(" " + iterator1);
							if (allPhrases.get(iterator1) != null) {
								// We've found the current triple part in the HashSet of salient nodes
								hits++;
								hitAhitB1 = hitAhitB1.concat(" " + "\"" + iterator1
										+ "\"");
							}
						}
					}
					if (hits >= 2) {
						// At least two parts of the triple are salient, so we output. 
						System.out.println(hitAhitB1.replace("* ", "#") + "\n"
								+ triple1.replace("* ", "") + "\n" + sentence1
								+ "\n");
					}
	
				} else if (str1.startsWith("# Line")) {
					String model_name = (model.getName()).split("-")[0];
					sentence1 = str1.replace("# Line", "FROM: " + model_name + "-# Line");
				}
			}
			br1.close();
		}

	}

}
