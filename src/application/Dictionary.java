package application;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

final class UndersizeException extends Exception {
	public UndersizeException (int size) {
		super("Only " + size + " words found.");
	}
}
final class InvalidCountException extends Exception {
	public InvalidCountException (String word) {
		super("Found duplicate of " + word);
	}
}
final class InvalidRangeException extends Exception {
	public InvalidRangeException (int length) {
		super("Found a word with a length of " + length);
	}
}
final class UnbalancedException extends Exception {
	public UnbalancedException (int count, int size) {
		super("Only " + (int)(count*100/size) + "% of words contain 9 or more letters");
	}
}

public class Dictionary {
	public  String id;
	public  String n_id;
	public  List<String> wordList;
	public int[] percentages;
	
	public HashMap<Integer, Integer> histogram() {
		HashMap<Integer, Integer> histo = new HashMap<Integer, Integer>();
		int sum = 0;
		for (String word : wordList) {
			sum++;
			if (word.length() == 6) {
				percentages[0]++;
			} else if (word.length() < 10) {
				percentages[1]++;
			} else {
				percentages[2]++;
			}
			if (histo.containsKey(word.length()))
				histo.put(word.length(), histo.get(word.length()) + 1);
			else
				histo.put(word.length(), 1);
		}
		for (int i = 0; i < 3; i++) {
			percentages[i] = percentages[i] * 100 / sum;
		}
		return histo;
	}
	
	public void fill_list() {
		StringBuilder urlstr = new StringBuilder("https://openlibrary.org/works/");
		urlstr.append(id);
		urlstr.append(".json");
		URL url = null;
		try {
			url = new URL(urlstr.toString());
		} catch (MalformedURLException e1) {
			System.out.println("Malformed URL");
			e1.printStackTrace();
		}
		InputStream is = null;
		try {
			is = url.openStream();
		} catch (IOException e) {
			System.out.println("Cannot read from URL for given ID");
			e.printStackTrace();
		}
		JSONParser jparser = new JSONParser();
		JSONObject jobject = null;
		try {
			jobject = (JSONObject)jparser.parse(
					new InputStreamReader(is, "UTF-8"));
		} catch (Exception e) {
			System.out.println("Cannot parse json file");
			e.printStackTrace();
		}
		JSONObject description = (JSONObject) jobject.get("description");
		String value = (String) description.get("value"); 
		StringTokenizer tk = new StringTokenizer(value, " ~`!@#$%^&*()_-=+{}[]|:;'<,>.?\"/" + "\n");
		while (tk.hasMoreTokens()) {
			String str = tk.nextToken();
			if (str.length() > 5)
				wordList.add(str.toUpperCase());
		}
	}
	
	public void create_dictionary(boolean duplicates_tolerated) 
			throws UndersizeException, InvalidCountException, InvalidRangeException, UnbalancedException {
		HashSet<String> wordSet = new HashSet<String>();
		int count_9 = 0;
		for (String word : wordList) {
			if (wordSet.contains(word)) {
				if (!duplicates_tolerated)
					throw new InvalidCountException(word);
			} else {
				if (word.length() < 6)
					throw new InvalidRangeException(word.length());
				if (word.length() > 8)
					count_9++;
				wordSet.add(word);
			}
		}
		if (wordSet.size() < 20)
			throw new UndersizeException(wordSet.size());
		if (count_9 < 0.2 * wordSet.size())
			throw new UnbalancedException(count_9, wordSet.size());
		File f = null;
		System.out.println(wordSet);
		try {
			f = new File("hangman_DICTIONARY-" + n_id + ".txt");
			if (f.createNewFile()) {
				System.out.println("Created dictionary: " + f.getName());
		    } else {
		    	System.out.println("Dictionary " + f.getName() + " already exists.");
		    }
		} catch (IOException e) {
			System.out.println("An error occurred upon creating dictionary");
			e.printStackTrace();
		}
		FileWriter writer = null;
		try {
			writer = new FileWriter(f.getName());
		} catch (IOException e1) {
			System.out.println("Error upon creating FileWriter");
			e1.printStackTrace();
		}
		for (String word : wordSet) {
			try {
				System.out.println(word);
				writer.write(word);
				writer.write(System.lineSeparator());
			} catch (IOException e) {
				System.out.println("Error upon writing to dictionary");
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println("Error upon closing FileWriter");
			e.printStackTrace();
		}
	}
	
	public Dictionary (String name_id, String sid, boolean duplicates_tolerated) throws Exception {
		if (sid == "" || sid == null)
			throw new Exception("Please type a valid library ID");
		percentages = new int[3];
		id = sid;
		n_id = name_id;
		File f = new File("hangman_DICTIONARY-" + n_id + ".txt");
		if (!f.exists() || f.isDirectory()) {
			wordList = new ArrayList<String>();
			fill_list();
			create_dictionary(duplicates_tolerated);
		} else {
			wordList = new ArrayList<String>();
			try {
				FileReader fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);
				String word;
				while ((word = br.readLine()) != null) {
					wordList.add(word);
				}
				br.close();
			} catch (Exception e) {
				e.getMessage();
				e.printStackTrace();
			}
		}
	}
}