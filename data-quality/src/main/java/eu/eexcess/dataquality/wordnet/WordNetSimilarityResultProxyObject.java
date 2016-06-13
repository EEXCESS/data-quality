package eu.eexcess.dataquality.wordnet;

import java.util.ArrayList;

public class WordNetSimilarityResultProxyObject {

	protected ArrayList<String> wordListUsed;
	
	double wuPalmerRelatednessOfWordsMedian;

	public ArrayList<String> getWordListUsed() {
		return wordListUsed;
	}

	public void setWordListUsed(ArrayList<String> wordListUsed) {
		this.wordListUsed = wordListUsed;
	}

	public double getWuPalmerRelatednessOfWordsMedian() {
		return wuPalmerRelatednessOfWordsMedian;
	}

	public void setWuPalmerRelatednessOfWordsMedian(
			double wuPalmerRelatednessOfWordsMedian) {
		this.wuPalmerRelatednessOfWordsMedian = wuPalmerRelatednessOfWordsMedian;
	}
	
	
	public String toString(){
		String ret ="";
		ret += "wuPalmerRelatednessOfWordsMedian:" + WordNetSimilarity.formatNumber(wuPalmerRelatednessOfWordsMedian);
		return ret;
	}

}
