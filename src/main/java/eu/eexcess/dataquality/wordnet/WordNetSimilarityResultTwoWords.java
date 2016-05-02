package eu.eexcess.dataquality.wordnet;

public class WordNetSimilarityResultTwoWords {

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
