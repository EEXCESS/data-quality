package eu.eexcess.dataquality.wordnet;
 
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
 
public class WordNetSimilarity {
 
	private static ILexicalDatabase db = new NictWordNet();
	private static NumberFormat numberFormater = NumberFormat.getNumberInstance( new Locale.Builder().setLanguage("en").setRegion("GB").build());
	
	private boolean traceOutput = true;
	
	public double SIM_MAX = Double.MAX_VALUE;
	public boolean isTraceOn() {
		return this.traceOutput;
	}
	
	public void setTraceOn(){
		this.traceOutput = true;
	}
		
	/*
	//available options of metrics
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };
	*/
	public WordNetSimilarityResultTwoWords compute(String word1, String word2) {
		WordNetSimilarityResultTwoWords result = new WordNetSimilarityResultTwoWords();
		result.setWordOne(word1);
		result.setWordTwo(word2);
		if (word1.equalsIgnoreCase(word2) ) {
			result.setWuPalmerRelatednessOfWords( 0 );
			return result;
		}
		if (word1 == null || word2 == null || word1.trim().isEmpty() || word2.trim().isEmpty()) {
			result.setWuPalmerRelatednessOfWords( SIM_MAX);
			return result;
		}
		WS4JConfiguration.getInstance().setMFS(true);
		result.setWuPalmerRelatednessOfWords( new WuPalmer(db).calcRelatednessOfWords(word1, word2));
		return result;
	}
	
	protected WordNetSimilarityResultProxyObject compute(ArrayList<String> wordList) {
		ArrayList<WordNetSimilarityResultTwoWords> myData = new ArrayList<WordNetSimilarityResultTwoWords>();
		String SYSTEM_OUT_DELIMITER = ";";
		if (this.isTraceOn()) {
			System.out.print("\n");
			for(int i=0; i<wordList.size(); i++){
				System.out.print(SYSTEM_OUT_DELIMITER + wordList.get(i));
			}
			System.out.print("\n");
		}
		for(int i=0; i<wordList.size()-1; i++){
			if (this.isTraceOn()) {
				System.out.print(wordList.get(i)+SYSTEM_OUT_DELIMITER);
				for (int j = 0; j < i; j++) {
					System.out.print(SYSTEM_OUT_DELIMITER);
				}
			}
			for(int j=i+1; j<wordList.size(); j++){
				WordNetSimilarityResultTwoWords distance = compute(wordList.get(i), wordList.get(j));
				myData.add(distance);
				if (this.isTraceOn()) System.out.print(SYSTEM_OUT_DELIMITER+ WordNetSimilarity.formatNumber(distance.getWuPalmerRelatednessOfWords()));
			}
			if (this.isTraceOn()) System.out.print("\n");
		}
		if (this.isTraceOn()) System.out.print("\n");
		
        SummaryStatistics stats = new SummaryStatistics();

        for (int j = 0; j < myData.size(); j++) {
            stats.addValue(myData.get(j).getWuPalmerRelatednessOfWords());
        }
        WordNetSimilarityResultProxyObject result = new WordNetSimilarityResultProxyObject();
	    double median = stats.getMean();
	    double sigma = stats.getStandardDeviation();
	    result.setWuPalmerRelatednessOfWordsMedian(median);
	    result.setWordListUsed(wordList);
		if (this.isTraceOn()){
			System.out.print("used words:\n"+ wordList.toString());
		}
		return result;
	}
	
	public WordNetSimilarityResultProxyObject compute(String words) {
		ArrayList<String> myWordList = new ArrayList<String>();
		StringTokenizer wordsTokenizer =  new StringTokenizer(words);
		while (wordsTokenizer.hasMoreElements()) {
			String tempWord = (String) wordsTokenizer.nextElement();
			if (tempWord.length() > 3) {
				List<Synset> wordNetResult = WordNetUtil.wordToSynsets(tempWord, POS.n);
				if (wordNetResult.size() > 0) {
					boolean found = false;
					for (int i = 0; i < myWordList.size(); i++) {
						if (myWordList.get(i).equalsIgnoreCase(tempWord))
							found = true;
					}
					if (!found) {
						myWordList.add(tempWord);
						if (this.isTraceOn()) System.out.println("wordNet -> add to List:" + tempWord);
					} else {
						if (this.isTraceOn()) System.out.println("wordNet -> skip(already added):" + tempWord);
					}
				} else {
					if (this.isTraceOn()) System.out.println("wordNet -> skip:" + tempWord);
				}
			} else {
				if (this.isTraceOn()) System.out.println("wordNet -> skip:" + tempWord);
			}
		}
		return this.compute(myWordList);
	}

	protected static String formatNumber(double number) {
		return numberFormater.format(number);
	}

}