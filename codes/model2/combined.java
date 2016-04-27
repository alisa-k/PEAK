package model2;

import java.io.IOException;

import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class combined {
	
	public static void combined() throws IOException {
		
		// get Pyramids needed for step 12
		HashMap<String, Integer> Pyramid = new HashMap<String, Integer>();
		HashMap<String, Integer> PyramidDivided = new HashMap<String, Integer>();
		Set<String> ADWSet = new HashSet<String>();

		FileReader reader = new FileReader("output/step11-output-labels.txt");// 0.8backup
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		int indexOfPyramid = 0;
	
		while ((str = br.readLine()) != null) {
			if (str.equals("\n") || str.startsWith("===")) {
				str = br.readLine();
			} else {
				indexOfPyramid++;
				String text = str;
				String[] splited = text.split("&");
	
				// first find the weight: tempW1
				int i1 = -1;
				int tempW1 = 0;
				for (String testString1 : splited) {
					i1++;
					if (splited[i1].matches("^(\\d+)(.*)"))
						tempW1 = Integer.parseInt(splited[i1]);
				}
				// then build the hashSet
				int i = -1;
				String tempS = "*";
				int tempW = 0;
				for (String testString : splited) {
					i++;
					if (splited[i].startsWith("\"")) {
						tempS = splited[i];
	
						// delete the "\"" of "Matter" "is"
						// "all the objects and substances"
						String[] splited3 = tempS.split("\"");
						String concate = "*";
						for (String iterator3 : splited3) {
	
							concate = concate.concat(iterator3);
						}
						tempS = concate.replace("*", "");
	
						// end of deleting
						Pyramid.put(tempS, tempW1); 
						PyramidDivided.put(tempS, indexOfPyramid);
						ADWSet.add(tempS);
					}
				}
	
			}
		}
		System.out.println(indexOfPyramid);
		System.out.println(ADWSet.size());
		
		// For each file: do steps 12, 13, and 14
		String path = "output/student-summaries-afterproAfterClauseIE/";
		File file = new File(path);
		String[] files = file.list();
		for (String f : files) {
				step12(f, Pyramid, PyramidDivided, ADWSet);
				step13(f);
				step14(f, Pyramid);
		}
	
	}
	
	public static void step12(String f, HashMap<String, Integer> Pyramid, HashMap<String, Integer> PyramidDivided, Set<String> ADWSet) throws IOException {
				
		String path = "output/student-summaries-afterproAfterClauseIE/";
	
		// output
		String path1 = "output/student-summaries-afterproAfterClauseIEAfterScoringLables/";
		File logFile = new File(path1 + f.replace(".txt", "") + "ForScoring.txt");
		File logFile1 = new File(path1 + f.replace(".txt", "") + "ForDetails.txt");
		File tripleInfo = new File(path1 + f.replace(".txt", "") + "TripleInfo.txt");
		PrintStream outputPrintStream = new PrintStream(logFile);
		PrintStream outputPrintStream1 = new PrintStream(logFile1);
		PrintStream tripleInfoPrintStream = new PrintStream(tripleInfo);
		
		FileReader reader1 = new FileReader(path + f);
		BufferedReader br1 = new BufferedReader(reader1);
		String str1 = null;
		String sentence1 = null;
		int currentLineNum = 0;
		int lineNum = 0;
		int tripleNum = 0;
		
		StringBuilder tmp = new StringBuilder(); // Using default 16 character size
		while ((str1 = br1.readLine()) != null) {
	
			if (str1.equals("\n")) {
				str1 = br1.readLine();
			} 
			else if (!str1.startsWith("#")) {
				String text1 = str1;
				String triple1 = "*";
				tripleNum++;
	
				String[] splited1 = text1.split("\"");
				// String lastIterator1 = null;
				for (String iterator1 : splited1) {
					if (iterator1.matches("^[a-zA-Z].*$")) {
						triple1 = triple1.concat(" " + iterator1);
					} else if (iterator1.matches("^(\\d+)(.*)")) {// start
																	// with
																	// numbers
						lineNum = Integer.parseInt(getNumbers(iterator1));
						if (lineNum != currentLineNum)
							System.out.println(lineNum);
					}
				}
	
				triple1 = triple1.replace("* ", "");
				
				System.setOut(tripleInfoPrintStream);
				System.out.println(tripleNum + "&" + triple1 + "&" + sentence1);
				
				// ADW part
				ADW pipeLine = new ADW();
				ItemType text1Type = ItemType.SURFACE;
				ItemType text2Type = ItemType.SURFACE;
	
				// measure for comparing semantic signatures
				SignatureComparison measure = new WeightedOverlap();
				
				for (String iterator : ADWSet) {
					if (iterator != null) {
						double similarity = pipeLine.getPairSimilarity(
								triple1, iterator,
								DisambiguationMethod.ALIGNMENT_BASED,
								measure, text1Type, text2Type);
						if (similarity >= 0.5) {
							
							System.setOut(outputPrintStream);
							System.out.println("\"" + lineNum + "\" "
									+ "\"" + tripleNum + "\" " 
									+ "\"" + similarity + "\" " + "\""
									+ PyramidDivided.get(iterator)
									+ "\" " + "\""
									+ Pyramid.get(iterator) + "\" ");
				
							
							System.setOut(outputPrintStream1);
							//System.out.println("\""+lineNum+"\" "+"\""+Pyramid.get(iterator)+"\"");
							System.out.println("\"" + lineNum + "\" "
									+ "\"" + triple1 + "\" " + "\""
									+ iterator + "\" " + "\""
									+ PyramidDivided.get(iterator)
									+ "\" " + "\"" + similarity + "\" "
									+ "\"" + Pyramid.get(iterator)
									+ "\"");
							
						}
					}
				}
	
			} else if (str1.startsWith("# Line")) {
				
				sentence1 = str1.replace("# ", "");
				currentLineNum = Integer.parseInt(getNumbers(str1));
			}
	
		}
		br1.close();

	}
	
	public static void step13(String f) throws IOException {
		String path = "output/student-summaries-afterproAfterClauseIEAfterScoringLables/";
		f = f.replace(".txt", "") + "ForScoring.txt";
			
		FileReader reader = new FileReader(path + f);
		BufferedReader br = new BufferedReader(reader);

		String str = null;
		List<String> lists = new ArrayList<String>();

		str = br.readLine();
		while (str != null) {
			lists.add(str);
			str = br.readLine();
		}
		br.close();
		reader.close();

		// Collections.sort(lists);
		Collections.sort(lists, Collections.reverseOrder());

		// output
		String path1 = "output/student-summaries-afterproAfterClauseIEAfterScoringSorting/";
		FileUtils.writeLines(new File(path1 + f.replace(".txt", "") + "Scoring.txt"), lists);
	}
	
	
	public static void step14(String f, HashMap<String, Integer> Pyramid) throws IOException{
	
		double SIMILARITY = 0.6;
		
		//String path1 = "student-summaries-afterproAfterClauseIEAfterScoringSortingHighestAssignment/";
		String path1 = "output/student-summaries-afterproAfterClauseIEAfterScoringSortingHighest/";
		
		File logFile1 = new File(path1+"forR.txt");
		PrintStream outputPrintStream1 = new PrintStream(logFile1);
			
		String path = "output/student-summaries-afterproAfterClauseIEAfterScoringSorting/";
		f = f.replace(".txt", "") + "ForScoringScoring.txt";

				
		int[][] costs = new int[1000][1000];
		//output
		
		//File logFile = new File(path1+f.replace(".txt", "")+"HighestAssignment.txt");
		File logFile = new File(path1+f.replace(".txt", "")+"Highest.txt");
		
		PrintStream outputPrintStream = new PrintStream(logFile);
		System.setOut(outputPrintStream);
	
    	 //input
		 FileReader reader = new FileReader(path+f);
		 BufferedReader br = new BufferedReader(reader);
		 
	     String str = null;
	     String strCurrent = null;
	     	    
//	     while((str = br.readLine()) != null) {
//	    	 
//	    	 	String[] splitedStr = str.split("\"");
//	    	 	double similarity = Double.parseDouble(splitedStr[5]);
//	    	 	if(similarity >= SIMILARITY){
//	    	 		int row = Integer.parseInt(splitedStr[3]);
//	    	 		int col = Integer.parseInt(splitedStr[7]);
//	    	 		int weight = Integer.parseInt(splitedStr[9]);
//	    	 		costs[row][col] = weight;
//	    	 	}       	 
//	     }
	     
	     int numLines = 0;
	     while((str = br.readLine()) != null) {
	    	 
	    	 	String[] splitedStr = str.split("\"");
	    	 	double similarity = Double.parseDouble(splitedStr[5]);
	    	 	if(similarity >= SIMILARITY){
	    	 		int row = Integer.parseInt(splitedStr[1]);
	    	 		int col = Integer.parseInt(splitedStr[7]);
	    	 		int weight = Integer.parseInt(splitedStr[9]);
	    	 		costs[row][col] = weight;
	    	 		if (row > numLines) {
	    	 			numLines = row;
	    	 		}
	    	 	}       	 
	     }

	     f = "output/student-summaries-afterproAfterClauseIEAfterScoringLables/" + f.replace("ForScoringScoring.txt", "") + "TripleInfo.txt";
	     
	     //int score = HungarianAlgorithm.hgAlgorithm(costs, "max");
	     int[] score = HungarianAlgorithm.hgAlgorithm(costs, "max", f);
	     //int score[][] = HungarianAlgorithm.hgAlgorithm(costs, "max"); 
	     //System.out.println(Arrays.deepToString(score));
	     //System.setOut(outputPrintStream1);
	     //System.out.print(score+","); 
	     
	     // recall, precision, fmeasure
	     float [] rpf = rpf(Pyramid, score, numLines);
	     
	     System.setOut(outputPrintStream);
	     System.out.println("Raw Score: " + score[0]);
	     System.out.println("Number of SCUs: " + score[1]);
	     System.out.println("Recall: " + rpf[0]);
	     System.out.println("Precision: " + rpf[1]);
	     System.out.println("F-Measure: " + rpf[2]);
	     
	     br.close();
	     reader.close();
	   
	}
	
	public static int maxSum(HashMap<String, Integer> Pyramid, int numSCUs) {
	    Object[] SCUWeightList = Pyramid.entrySet().toArray();
	    Arrays.sort(SCUWeightList, new Comparator() {
	        public int compare(Object o1, Object o2) {
	            return ((Map.Entry<String, Integer>) o2).getValue().compareTo(
	                    ((Map.Entry<String, Integer>) o1).getValue());
	        } 
	    });
	    
	    int sum = 0;
	    for (int i = 0; i < numSCUs; i++) {
	    	sum += ((Map.Entry<String, Integer>) SCUWeightList[i]).getValue();
	    }
	 
	    return sum; 
	} 
	
	// precision, recall, f-measure
	public static float[] rpf(HashMap<String, Integer> Pyramid, int[] Score, int numLines) {
		// # of SCUs in pyramid
		int sum = 0;
		for (int val : Pyramid.values()) {
		    sum += val;
		}
		
		// recall (coverage)
		int avgSCUs = sum / 5; 
		int maxSumRecall = maxSum(Pyramid, avgSCUs);
		float recall = (float)Score[0]/maxSumRecall;
		
		// precision (quality)
		int maxSumPrecision = maxSum(Pyramid, numLines);
		float precision = (float)Score[0]/maxSumPrecision;
		
		float fmeasure = (float)(recall + precision)/2; 
		
		float recallPrecision[] = {recall, precision, fmeasure};
		return recallPrecision;
	}

	
	// used in step 12
	public static String getNumbers(String content) {  
	       Pattern pattern = Pattern.compile("\\d+");  
	       Matcher matcher = pattern.matcher(content);  
	       while (matcher.find()) {  
	           return matcher.group(0);  
	       }  
	       return "";  
	}
	
}
