package application;
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
		g_id = dict.id;
		correct = 0;
		points = 0;
		lives = Main.initial_lives;
		wordList = new ArrayList<>(dict.wordList);
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
	
	public boolean won() {
		boolean result = false;
		for (boolean check : active)
			result |= check;
		return !result;
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
		points = points - 15 > 0 ? points - 15 : 0;
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
	
	public static String probability_string (int index) {
		String result = "|";
		for (Character ch : sort_by_probability(index)) {
			result += String.valueOf(ch) + "|";
		}
		return result;
	}
	
	public static void play (int position, char letter) {
		if (target.charAt(position) == letter)
			found(position);
		else 
			not_found(position, letter);
	}
}