package application;
import java.io.*;
import java.util.*;



public class Game {
	static final class ProbabilityComparator implements Comparator<Character> {
		private int index;
		public ProbabilityComparator(int ind) {
			index = ind;
		}
		@Override
		public int compare(Character c1, Character c2) {
			if (map.get(index).get(c1) == map.get(index).get(c2)) {
				return c1.compareTo(c2);
			} else {
				return map.get(index).get(c2).compareTo(map.get(index).get(c1));
			}
		}
	}
	
	public static String g_id;
	public static List<String> wordList;
	public static HashMap<Integer, HashMap<Character, Integer>> map;
	public static String target;
	public static boolean[] active;
	public static int  points;
	public static int lives;
	public static int correct;
	
	public Game(Dictionary dict) {
		g_id = id;
		correct = 0;
		points = 0;
		lives = 6;
		wordList = new ArrayList<>(dict.wordList);
		try {
			File f = new File("hangman_DICTIONARY-" + id + ".txt");
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
		target = wordList.get((int) (Math.random() * (wordList.size() - 1)));
		active = new boolean[target.length()];
		Arrays.fill(active, true);
		wordList.remove(target);
		//Creating a new list to avoid O(N^2) time by removals
		List<String> sameLength = new ArrayList<String>();
		for (String str : wordList) {
			if (str.length() == target.length())
				sameLength.add(str);
		}
		wordList = sameLength;
		map = new HashMap<Integer, HashMap<Character, Integer>>();
		//Map initialization
		for (int i = 0; i < target.length(); i++) {
			HashMap<Character, Integer> inner = new HashMap<Character, Integer>();
			for (char ch = 'A'; ch <= 'Z'; ch++) {
				inner.put(ch, 0);
			}
			map.put(i, inner);
		}
		//Filling the map
		for (int i = 0; i < target.length(); i++) {
			HashMap<Character, Integer> current = map.get(i);
			for (String word : wordList) {
				current.put(word.charAt(i), current.get(word.charAt(i)) + 1);
			}
		}
	}
	
	public static HashMap<Integer, Integer> histogram() {
		HashMap<Integer, Integer> histo = new HashMap<Integer, Integer>();
		for (String word : wordList) {
			if (histo.containsKey(word.length()))
				histo.put(word.length(), histo.get(word.length()) + 1);
			else
				histo.put(word.length(), 1);
		}
		return histo;
	}
	
	public static void remove (String word) {
		for (int i = 0; i < target.length(); i++) {
			if (!active[i])
				continue;
			map.get(i).put(word.charAt(i), map.get(i).get(word.charAt(i)) - 1);
		}
	}
	
	public static void found (int position) {
		correct++;
		char letter = target.charAt(position);
		active[position] = false;
		float probability = (float)map.get(position).get(letter)/wordList.size(); 
		if (probability >= 0.6) {
			points += 5;
		} else if (probability >= 0.4) {
			points += 10;
		} else if (probability >= 0.25) {
			points += 15;
		} else {
			points += 30;
		}
		List<String> newList = new ArrayList<String>();
		for (String word : wordList) {
			if (word.charAt(position) != letter) 
				remove(word);
			else
				newList.add(word);
		}
		wordList = newList;
	}
	
	public static void not_found (int position, char wrongLetter) {
		points -= 15;
		lives--;
		List<String> newList= new ArrayList<String>();
		for (String word : wordList) {
			if (word.charAt(position) == wrongLetter)
				remove(word);
			else
				newList.add(word);
		}
		wordList = newList;
	}

	public static ArrayList<Character> sort_by_probability(int index) {
		ArrayList<Character> result = new ArrayList<Character>();
		for (char ch = 'A'; ch <= 'Z'; ch++)
			result.add(ch);
		Collections.sort(result, new ProbabilityComparator(index));
		return result;
	}
	
	public static void play (int position, char letter) {
		if (target.charAt(position) == letter)
			found(position);
		else 
			not_found(position, letter);
	}
}