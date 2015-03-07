package knntext;

import java.util.HashMap;

public class SaveWord {
	public HashMap<String,Double> _wordmap;
	public double sum_word_of_this_text;
	public String classyString;
	public SaveWord(){
		sum_word_of_this_text = 0.0d;
		classyString = null;
		_wordmap = new HashMap<String,Double>();
	}
}
