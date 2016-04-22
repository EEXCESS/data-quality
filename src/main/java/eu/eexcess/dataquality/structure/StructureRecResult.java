/*
Copyright (C) 2016 
"JOANNEUM RESEARCH Forschungsgesellschaft mbH" 
 Graz, Austria, digital-iis@joanneum.at.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package eu.eexcess.dataquality.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class StructureRecResult {

	protected boolean structureDetected = false;
	
	//protected String pattern ="";
	
	protected int records=0;
	
	protected int lengthMax =-1;
	
	protected int lengthMaxTrimmed = -1;
	
	protected int lengthMin;
	
	protected int lengthMinTrimmed;

	protected int[] lengthHistogram;
	
	protected int datesFound = 0 ;
/*	
	public double getResultFracOutUpper() {
		return resultRegEx.getResultFracOutUpper();
	}

	public void setResultFracOutUpper(double resultFracOutUpper) {
		this.resultRegEx.setResultFracOutUpper(resultFracOutUpper);
	}

	public double getResultFracOutLower() {
		return resultRegEx.getResultFracOutLower();
	}

	public void setResultFracOutLower(double resultFracOutLower) {
		this.resultRegEx.setResultFracOutLower(resultFracOutLower);
	}

	public double getResultFracOutWeighted() {
		return resultRegEx.getResultFracOutWeighted();
	}

	public void setResultFracOutWeighted(double resultFracOutWeighted) {
		this.resultRegEx.setResultFracOutWeighted(resultFracOutWeighted);
	}

	public double getResultMedianPerVaildSamples() {
		return resultRegEx.getResultMedianPerVaildSamples();
	}

	public void setResultMedianPerVaildSamples(double resultMedianPerVaildSamples) {
		this.resultRegEx.setResultMedianPerVaildSamples(resultMedianPerVaildSamples);
	}

	public double getResultSigma() {
		return resultRegEx.getResultSigma();
	}

	public void setResultSigma(double resultSigma) {
		this.resultRegEx.setResultSigma(resultSigma);
	}

	public int getResultDistinctValues() {
		return resultRegEx.getResultDistinctValues();
	}

	public void setResultDistinctValues(int resultDistinctValues) {
		this.resultRegEx.setResultDistinctValues(resultDistinctValues);
	}

	public double getResultDistinctFracComplement() {
		return resultRegEx.getResultDistinctFracComplement();
	}

	public void setResultDistinctFracComplement(double resultDistinctFracComplement) {
		this.resultRegEx.setResultDistinctFracComplement(resultDistinctFracComplement);
	}

	public double getResultMedian() {
		return resultRegEx.getResultMedian();
	}

	public void setResultMedian(double resultMedian) {
		this.resultRegEx.setResultMedian(resultMedian);
	}

	public double getResultCdfl05() {
		return resultRegEx.getResultCdfl05();
	}

	public void setResultCdfl05(double resultCdfl05) {
		this.resultRegEx.setResultCdfl05(resultCdfl05);
	}

	public double getResultCdfl075() {
		return resultRegEx.getResultCdfl075();
	}

	public void setResultCdfl075(double resultCdfl075) {
		this.resultRegEx.setResultCdfl075(resultCdfl075);
	}
*/
	protected StructureRecResultAnalysisResultData resultRegEx = new StructureRecResultAnalysisResultData();



	public StructureRecResultAnalysisResultData getResultRegEx() {
		return resultRegEx;
	}

	public void setResultRegEx(StructureRecResultAnalysisResultData resultRegEx) {
		this.resultRegEx = resultRegEx;
	}

	public int getDatesFound() {
		return datesFound;
	}

	public void setDatesFound(int datesFound) {
		this.datesFound = datesFound;
	}

	public HashMap<String, Integer> getValuesDateformatHashMap() {
		return valuesDateformatHashMap;
	}

	public void setValuesDateformatHashMap(
			HashMap<String, Integer> valuesDateformatHashMap) {
		this.valuesDateformatHashMap = valuesDateformatHashMap;
	}

	public HashMap<String, ArrayList<PatternSource>> getValuesDateformatSourceHashMap() {
		return valuesDateformatSourceHashMap;
	}

	public void setValuesDateformatSourceHashMap(
			HashMap<String, ArrayList<PatternSource>> valuesDateformatSourceHashMap) {
		this.valuesDateformatSourceHashMap = valuesDateformatSourceHashMap;
	}

	public void initLengthHistogramm(int size) {
		this.lengthHistogram = new int[size+1];
		for (int i = 0; i < lengthHistogram.length; i++) {
			lengthHistogram[i] = 0;
		}
	}
	
	protected HashMap<String, Integer> valuesHashMap =  new HashMap<String, Integer>();
	
	public void addValueToHashMap(String value) {
		value = value.trim();
		if (!valuesHashMap.containsKey(value)) {
			valuesHashMap.put(value, 1);
		} else {
			valuesHashMap.put(value, valuesHashMap.get(value) + 1);
		}
	}
	
	protected HashMap<String, Integer> valuesPatternHashMap =  new HashMap<String, Integer>();

	protected HashMap<String, ArrayList<PatternSource>> valuesPatternSourceHashMap =  new HashMap<String, ArrayList<PatternSource>>();
	
	public void addValuePatternToHashMap(String pattern, String value, String filename) {
		pattern = pattern.trim();
		if (!valuesPatternHashMap.containsKey(pattern)) {
			valuesPatternHashMap.put(pattern, 1);
			ArrayList<PatternSource> list = new ArrayList<PatternSource>();
			list.add(new PatternSource(value, filename));
			valuesPatternSourceHashMap.put(pattern, list);
		} else {
			valuesPatternHashMap.put(pattern, valuesPatternHashMap.get(pattern) + 1);
			ArrayList<PatternSource> list = valuesPatternSourceHashMap.get(pattern);
			list.add(new PatternSource(value, filename));
			valuesPatternSourceHashMap.put(pattern, list);
		}
	}

	protected HashMap<String, Integer> valuesPatternRegExHashMap =  new HashMap<String, Integer>();

	protected HashMap<String, ArrayList<PatternSource>> valuesPatternRegExSourceHashMap =  new HashMap<String, ArrayList<PatternSource>>();
	
	public void addValuePatternRegExToHashMap(String pattern, String value, String filename) {
		pattern = pattern.trim();
		if (!valuesPatternRegExHashMap.containsKey(pattern)) {
			valuesPatternRegExHashMap.put(pattern, 1);
			ArrayList<PatternSource> list = new ArrayList<PatternSource>();
			list.add(new PatternSource(value, filename));
			valuesPatternRegExSourceHashMap.put(pattern, list);
		} else {
			valuesPatternRegExHashMap.put(pattern, valuesPatternRegExHashMap.get(pattern) + 1);
			ArrayList<PatternSource> list = valuesPatternRegExSourceHashMap.get(pattern);
			list.add(new PatternSource(value, filename));
			valuesPatternRegExSourceHashMap.put(pattern, list);
		}
	}

	protected HashMap<String, Integer> valuesDateformatHashMap =  new HashMap<String, Integer>();

	protected HashMap<String, ArrayList<PatternSource>> valuesDateformatSourceHashMap =  new HashMap<String, ArrayList<PatternSource>>();

	public void addValueDatePatternToHashMap(String dateformat, String value, String filename) {
		dateformat = dateformat.trim();
		if (!valuesDateformatHashMap.containsKey(dateformat)) {
			valuesDateformatHashMap.put(dateformat, 1);
			ArrayList<PatternSource> list = new ArrayList<PatternSource>();
			list.add(new PatternSource(value, filename));
			valuesDateformatSourceHashMap.put(dateformat, list);
		} else {
			valuesDateformatHashMap.put(dateformat, valuesDateformatHashMap.get(dateformat) + 1);
			ArrayList<PatternSource> list = valuesDateformatSourceHashMap.get(dateformat);
			list.add(new PatternSource(value, filename));
			valuesDateformatSourceHashMap.put(dateformat, list);
		}
	}

	protected HashMap<String, Integer> valuesUrlformatHashMap =  new HashMap<String, Integer>();

	protected HashMap<String, ArrayList<PatternSource>> valuesUrlformatSourceHashMap =  new HashMap<String, ArrayList<PatternSource>>();

	public void addValueURLPatternToHashMap(String urlFormat, String value, String filename) {
		urlFormat = urlFormat.trim();
		if (!valuesUrlformatHashMap.containsKey(urlFormat)) {
			valuesUrlformatHashMap.put(urlFormat, 1);
			ArrayList<PatternSource> list = new ArrayList<PatternSource>();
			list.add(new PatternSource(value, filename));
			valuesUrlformatSourceHashMap.put(urlFormat, list);
		} else {
			valuesUrlformatHashMap.put(urlFormat, valuesUrlformatHashMap.get(urlFormat) + 1);
			ArrayList<PatternSource> list = valuesUrlformatSourceHashMap.get(urlFormat);
			list.add(new PatternSource(value, filename));
			valuesUrlformatSourceHashMap.put(urlFormat, list);
		}
	}

	public HashMap<String, Integer> getValuesUrlformatHashMap() {
		return valuesUrlformatHashMap;
	}

	public void setValuesUrlformatHashMap(
			HashMap<String, Integer> valuesUrlformatHashMap) {
		this.valuesUrlformatHashMap = valuesUrlformatHashMap;
	}

	public HashMap<String, ArrayList<PatternSource>> getValuesUrlformatSourceHashMap() {
		return valuesUrlformatSourceHashMap;
	}

	public void setValuesUrlformatSourceHashMap(
			HashMap<String, ArrayList<PatternSource>> valuesUrlformatSourceHashMap) {
		this.valuesUrlformatSourceHashMap = valuesUrlformatSourceHashMap;
	}

	public HashMap<String, ArrayList<PatternSource>> getValuesPatternSourceHashMap() {
		return valuesPatternSourceHashMap;
	}

	public void setValuesPatternSourceHashMap(
			HashMap<String, ArrayList<PatternSource>> valuesPatternSourceHashMap) {
		this.valuesPatternSourceHashMap = valuesPatternSourceHashMap;
	}

	public void addValueLengthHistogram(int length) {
		lengthHistogram[length]++;
	}
	
	public boolean isStructureDetected() {
		return structureDetected;
	}

	public int getLengthMax() {
		return lengthMax;
	}

	public void setLengthMax(int lengthMax) {
		this.lengthMax = lengthMax;
	}

	public int getLengthMaxTrimmed() {
		return lengthMaxTrimmed;
	}

	public void setLengthMaxTrimmed(int lengthMaxTrimmed) {
		this.lengthMaxTrimmed = lengthMaxTrimmed;
	}

	public int[] getLengthHistogram() {
		return lengthHistogram;
	}

	public HashMap<String, Integer> getValuesPatternRegExHashMap() {
		return valuesPatternRegExHashMap;
	}

	public void setValuesPatternRegExHashMap(
			HashMap<String, Integer> valuesPatternRegExHashMap) {
		this.valuesPatternRegExHashMap = valuesPatternRegExHashMap;
	}

	public HashMap<String, ArrayList<PatternSource>> getValuesPatternRegExSourceHashMap() {
		return valuesPatternRegExSourceHashMap;
	}

	public void setValuesPatternRegExSourceHashMap(
			HashMap<String, ArrayList<PatternSource>> valuesPatternRegExSourceHashMap) {
		this.valuesPatternRegExSourceHashMap = valuesPatternRegExSourceHashMap;
	}

	public void setLengthHistogram(int[] lengthHistogram) {
		this.lengthHistogram = lengthHistogram;
	}

	public void setStructureDetected(boolean structureDetected) {
		this.structureDetected = structureDetected;
	}

//	public String getPattern() {
//		return pattern;
//	}
//
//	public void setPattern(String pattern) {
//		this.pattern = pattern;
//	}

	public int getRecords() {
		return records;
	}

	public void setRecords(int records) {
		this.records = records;
	}

	public String toString()
	{
		String out = "records:" + records + "\n";
		out += "lengthMax:" + lengthMax + "\n";
		out += "lengthMaxTrimmed:" + lengthMaxTrimmed + "\n";
		out += "lengthMin:" + lengthMin + "\n";
		out += "lengthMinTrimmed:" + lengthMinTrimmed + "\n";
		out += "lengthHistogram:\n";
		for (int i = 0; i < lengthHistogram.length; i++) {
			out += i + "\t";
		}
		out += "\n";
		for (int i = 0; i < lengthHistogram.length; i++) {
			out += lengthHistogram[i] + "\t";
		}
		out += "\n";
		out += "valuesMap:\n";
		Iterator<Entry<String, Integer>> valuesMapIterator = valuesHashMap.entrySet().iterator();
		while (valuesMapIterator.hasNext()) {
			Entry<String, Integer> pair = valuesMapIterator.next();
			out += pair.getKey() + " \t" + pair.getValue() + "\n";
		}
		out += "\n";
		out += "valuesPatternHashMap:\n";
		Iterator<Entry<String, Integer>> valuesPatternMapIterator = valuesPatternHashMap.entrySet().iterator();
		while (valuesPatternMapIterator.hasNext()) {
			Entry<String, Integer> pair = valuesPatternMapIterator.next();
			out += pair.getKey() + " \t" + pair.getValue() + "\n";
		}
		out += "\n";
//		out += "pattern:" + pattern + "\n";
		out += "structureDetected:" + structureDetected + "\n";
		return out;
	}

	public HashMap<String, Integer> getValuesHashMap() {
		return valuesHashMap;
	}

	public void setValuesHashMap(HashMap<String, Integer> valuesHashMap) {
		this.valuesHashMap = valuesHashMap;
	}

	public HashMap<String, Integer> getValuesPatternHashMap() {
		return valuesPatternHashMap;
	}

	public void setValuesPatternHashMap(
			HashMap<String, Integer> valuesPatternHashMap) {
		this.valuesPatternHashMap = valuesPatternHashMap;
	}

	public int getLengthMin() {
		return lengthMin;
	}

	public void setLengthMin(int lengthMin) {
		this.lengthMin = lengthMin;
	}

	public int getLengthMinTrimmed() {
		return lengthMinTrimmed;
	}

	public void setLengthMinTrimmed(int lengthMinTrimmed) {
		this.lengthMinTrimmed = lengthMinTrimmed;
	}
}
