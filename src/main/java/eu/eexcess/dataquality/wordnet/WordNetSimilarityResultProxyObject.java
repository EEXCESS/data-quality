package eu.eexcess.dataquality.wordnet;

public class WordNetSimilarityResultProxyObject {

	
	double wuPalmerRelatednessOfWordsMedian;

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
