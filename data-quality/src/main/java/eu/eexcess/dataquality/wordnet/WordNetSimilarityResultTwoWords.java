package eu.eexcess.dataquality.wordnet;

public class WordNetSimilarityResultTwoWords {

	String wordOne;
	public String getWordOne() {
		return wordOne;
	}

	public void setWordOne(String wordOne) {
		this.wordOne = wordOne;
	}

	public String getWordTwo() {
		return wordTwo;
	}

	public void setWordTwo(String wordTwo) {
		this.wordTwo = wordTwo;
	}

	String wordTwo;
	
	double wuPalmerRelatednessOfWords;

	public double getWuPalmerRelatednessOfWords() {
		return wuPalmerRelatednessOfWords;
	}

	public void setWuPalmerRelatednessOfWords(double myWuPalmerRelatednessOfWords) {
		wuPalmerRelatednessOfWords = myWuPalmerRelatednessOfWords;
	}

	public String toString(){
		String ret ="";
		ret += "wuPalmerRelatednessOfWords:" + WordNetSimilarity.formatNumber(wuPalmerRelatednessOfWords);
		return ret;
	}
}
