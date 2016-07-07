package eu.eexcess.dataquality.wordnet;

public class WordNetSimilarityResultObject {
	WordNetSimilarityResultProxyObject valueProxy;
	WordNetSimilarityResultProxyObject valueEnrichedProxy;
	String filename;
	
	double wuPalmerRelatednessOfWordsMeanDist;
	double wuPalmerRelatednessOfWordsMedianDist;

	public WordNetSimilarityResultObject(WordNetSimilarityResultProxyObject myValueProxy, WordNetSimilarityResultProxyObject myValueEnrichedProxy, String myFilename) {
		this.valueProxy = myValueProxy;
		this.valueEnrichedProxy = myValueEnrichedProxy;
		this.filename = myFilename;
		wuPalmerRelatednessOfWordsMeanDist =  valueEnrichedProxy.getWuPalmerRelatednessOfWordsMean() - valueProxy.getWuPalmerRelatednessOfWordsMean();
		wuPalmerRelatednessOfWordsMedianDist =  valueEnrichedProxy.getWuPalmerRelatednessOfWordsMedian() - valueProxy.getWuPalmerRelatednessOfWordsMedian();
	}

	public WordNetSimilarityResultProxyObject getValueProxy() {
		return valueProxy;
	}

	public void setValueProxy(WordNetSimilarityResultProxyObject valueProxy) {
		this.valueProxy = valueProxy;
	}

	public WordNetSimilarityResultProxyObject getValueEnrichedProxy() {
		return valueEnrichedProxy;
	}

	public void setValueEnrichedProxy(
			WordNetSimilarityResultProxyObject valueEnrichedProxy) {
		this.valueEnrichedProxy = valueEnrichedProxy;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public double getWuPalmerRelatednessOfWordsMeanDist() {
		return wuPalmerRelatednessOfWordsMeanDist;
	}

	public double getWuPalmerRelatednessOfWordsMedianDist() {
		return wuPalmerRelatednessOfWordsMedianDist;
	}

	public void setWuPalmerRelatednessOfWordsMedianDist(
			double wuPalmerRelatednessOfWordsMedianDist) {
		this.wuPalmerRelatednessOfWordsMedianDist = wuPalmerRelatednessOfWordsMedianDist;
	}

	public void setWuPalmerRelatednessOfWordsMeanDist(
			double wuPalmerRelatednessOfWordsMeanDist) {
		this.wuPalmerRelatednessOfWordsMeanDist = wuPalmerRelatednessOfWordsMeanDist;
	}

}
