/**
 * 
 */
package model1;

/**
 * @author Qian Yang
 * Purpose of This Class: Find the salient nodes.
 * Other Notes Relating to This Class (Optional):
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

public class step3 {
	public static void step3() throws IOException {
		int hits = 3;

		File logFile = new File("output/step3-out.txt");
		PrintStream outputPrintStream = new PrintStream(logFile);
		System.setOut(outputPrintStream);

		FileReader reader1 = new FileReader("output/step2-out.txt");
		BufferedReader br1 = new BufferedReader(reader1);
		String str1 = null;
		String nextStr1 = null;
		int count = 1;
		
		while ((str1 = br1.readLine()) != null) {

			count = 1;
			// Count the number of occurrences of this vertex. 
			// Vertices with degree >= 3 are chosen as salient.
			nextStr1 = br1.readLine();
			while (nextStr1 != null && nextStr1.equals(str1)) {
				count++;
				nextStr1 = br1.readLine();
			}
			if (count >= hits)
				System.out.println(str1);
			str1 = nextStr1;

		}

	}
}
