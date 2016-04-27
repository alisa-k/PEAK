/**
 * 
 */
package model1;

/**
 * @author Qian Yang
 * Purpose of This Class: Sort step2-out-SimilarityClass in reverse order.
 * Other Notes Relating to This Class (Optional):
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;

public class step4 {
	public static void step4() throws IOException {

		String file = "output/step2-out-SimilarityClass.txt";

		FileReader reader = new FileReader(file);
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

		Collections.sort(lists, Collections.reverseOrder());
		FileUtils.writeLines(new File("output/step2-out-SimilarityClass" + "Sorted.txt"), lists);

	}
}
