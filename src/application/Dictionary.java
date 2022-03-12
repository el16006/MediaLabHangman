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
/**
 * <h1>A class for dictionaries</h1>
 * <p>
 * Objects of this class represent sets of words a game can be played on, 
 * along with information about word length distribution and the names of
 * the related local files and open library works.
 * A Dictionary object is the only argument of {@link application.Game Game}'s
 * constructor
 * @author P_Eleftherakis
 * @version 1.0
 * @since 2022
*/
public class Dictionary {
	/**
	 * This field is the actual open library ID of the work the words 
	 * will be generated from.
	 */
	public  String id;
	/**
	 * The dictionary will be generated in the file {@code hangman_DICTIONARY-n_id.txt}
	 */
	public  String n_id;
	/**
	 * A list that contains all words in the dictionary
	 */
	public  List<String> wordList;
	/**
	 * An array of length 3, whose elements represent the amount of words with
	 * 6, 7-9 and 10+ letters respectively.
	 */
	public int[] percentages;
	/**
	 * Updates percentages
	 * @return A histogram of word lengths in the dictionary.
	 */
	public HashMap<Integer, Integer> histogram() {
		HashMap<Integer, Integer> histo = new HashMap<Integer, Integer>();
		percentages = new int[3];
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
	/**
	 * Builds the URL of the work we are going to use, opens a stream to it,
	 * parses the JSON file, checks if the "description" field is a JSONObject
	 * or a String, tokenizes it avoiding numbers and punctuation, and adds all 
	 * words with 6 or more characters to wordList
	 */
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
		String value;
		if (jobject.get("description").getClass() == JSONObject.class) {
			JSONObject description = (JSONObject) jobject.get("description");
			value = (String) description.get("value"); 
		} else {
			value = (String) jobject.get("description");
		}
		StringTokenizer tk = new StringTokenizer(value, " 1234567890~`!@#$%^&*()_-=+{}[]|:;'<,>.?\"/" + "\n");
		while (tk.hasMoreTokens()) {
			String str = tk.nextToken();
			if (str.length() > 5)
				wordList.add(str.toUpperCase());
		}
	}
	/**
	 * Checks for all exception types below in wordList and then creates the 
	 * local dictionary file
	 * @param duplicates_tolerated If true, it won't throw @see InvalidCountException
	 * @throws UndersizeException When fewer than 20 words are found
	 * @throws InvalidCountException When duplicates are found
	 * @throws InvalidRangeException When a word with less than 6 letters is found
	 * @throws UnbalancedException When words with 9+ letters are more than 20%
	 */
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
	/**
	 * Checks if a local file with the given name id exists. If not it creates
	 * it by calling the above methods, else it loads it in the new object;
	 * @param name_id Local file name id
	 * @param sid Actual Open library id
	 * @param duplicates_tolerated Passed to {@link application.Dictionary#create_dictionary 
	 * create_dictionary}
	 * @throws Exception When invalid IDs are given
	 */
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