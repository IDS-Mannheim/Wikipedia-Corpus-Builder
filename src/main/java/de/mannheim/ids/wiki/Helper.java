package de.mannheim.ids.wiki;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author margaretha
 *
 */
public class Helper {
	public void createDirectory(String directory){
		File dir = new File(directory);
		if (!dir.exists()) { dir.mkdirs(); }
	}

	public OutputStreamWriter createWriter (String outputFile) throws IOException {			
		File file = new File(outputFile);		
		if (!file.exists()) file.createNewFile();

		OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(file)), "UTF-8");		

		return os;	
	}	
		
	public List<String> createIndexList(){
		 String[] index = {"A","B","C","D","E","F","G","H","I","J","K","L",
			    "M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
			    "0","1","2","3","4","5","6","7","8","9","Char"};
		 
		 return Arrays.asList(index);
	}
	
	public String normalizeIndex(String input, List<String> indexList) throws IOException{
		String normalizedStr = Normalizer.normalize(input,Form.NFKD).toUpperCase();
		normalizedStr = normalizedStr.substring(0,1);	
		
//			if (Character.isLetterOrDigit(normalizedStr.charAt(0))){
//				return normalizedStr.substring(0,1);	
//			}
		if (indexList.contains(normalizedStr)){
			return normalizedStr;
		}
		else{ return "Char"; }		
	}
}
