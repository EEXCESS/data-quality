package eu.eexcess.dataquality.wordnet;

import java.util.ArrayList;

public class WordNetSimilarityResultProxyObject {

	protected ArrayList<String> wordListUsed;
	
	double wuPalmerRelatednessOfWordsMean;

	public ArrayList<String> getWordListUsed() {
		return wordListUsed;
	}

	public void setWordListUsed(ArrayList<String> wordListUsed) {
		this.wordListUsed = wordListUsed;
	}

	public double getWuPalmerRelatednessOfWordsMean() {
		return wuPalmerRelatednessOfWordsMean;
	}

	public void setWuPalmerRelatednessOfWordsMean(
			double wuPalmerRelatednessOfWordsMean) {
		this.wuPalmerRelatednessOfWordsMean = wuPalmerRelatednessOfWordsMean;
	}
	
	
	public String toString(){
		String ret ="";
		ret += "wuPalmerRelatednessOfWordsMean:" + WordNetSimilarity.formatNumber(wuPalmerRelatednessOfWordsMean);
		return ret;
	}

}
