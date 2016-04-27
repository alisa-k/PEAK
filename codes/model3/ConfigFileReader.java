package model3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigFileReader {
	
	private FileReader configReader;
	
	public ConfigFileReader(String fileName) throws FileNotFoundException {
		configReader = new FileReader(fileName);
	}
	
	public File[] getModelSummaries() throws IOException{
		BufferedReader br = new BufferedReader(configReader);
		File modelsDir = new File(br.readLine());
		File[] models = modelsDir.listFiles();
		br.close();
		return models;
	}
	
	public File[] getTargetSummaries() throws IOException{
		BufferedReader br = new BufferedReader(configReader);
		br.readLine();
		File targetDir = new File(br.readLine());
		File[] targets = targetDir.listFiles();
		br.close();
		return targets;
	}

}
