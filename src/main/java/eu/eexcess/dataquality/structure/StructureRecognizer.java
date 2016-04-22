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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class StructureRecognizer {

	
	public StructureRecResult analyse(List<ValueSource> values) {
		StructureRecResult result = new StructureRecResult();
		result = calcLengths(values, result);
		if (result.getRecords() == 0) {
			return result;
		}
		result.initLengthHistogramm(result.getLengthMaxTrimmed());
		result = dateFormatCheck(values, result);
		result = urlFormatCheck(values, result);
		
		result = calcLengthHistogramm(values, result);
		result = calcValuesHashmap(values, result);
		result = calcValuesPatternHashmap(values, result);
		result = calcValuesPatternRegExHashmap(values, result);
		
		result = calcMetrics(values, result);
		return result;
	}
	
	private StructureRecResult calcMetrics(List<ValueSource> values,
			StructureRecResult result) {
		result.setResultRegEx(calcStructuredResults(result, result.getValuesPatternRegExHashMap()));
		result.setResultValue(calcStructuredResults(result, result.getValuesHashMap()));
        return result;
	}

	private StructureRecResultAnalysisResultData calcStructuredResults(StructureRecResult result,
			HashMap<String, Integer> myData) {
		int nrValidSamples = 0; 
		// number of samples with non-empty value
        Iterator<Entry<String, Integer>> iteratorHashMap = myData.entrySet().iterator();
        ArrayList<Integer> myDataArrayList = new ArrayList<Integer>();

        while (iteratorHashMap.hasNext()) {
            Entry<String, Integer> entry = iteratorHashMap.next();
            nrValidSamples += entry.getValue();
            myDataArrayList.add(entry.getValue());
        }

        int nrDistinctValues = myData.size();//-1; // number of distinct values (patterns) of samples
        // note: subtract one, as there is an empty cell at the end of the line

        double distinctFracComplement = 1 - nrDistinctValues*1.0/nrValidSamples;

        HashMap myDataSorted = sortByValues(myData); //% sort values descendingly

        ArrayList<Double> myCumulativeData = new ArrayList<Double>();
        ArrayList<Double> mySortedData = new ArrayList<Double>();
        Iterator<Entry<String, Integer>> iteratorMySortedDataHashMap = myDataSorted.entrySet().iterator();
        while (iteratorMySortedDataHashMap.hasNext()) {
            Entry<String, Integer> entry = iteratorMySortedDataHashMap.next();
            myCumulativeData.add(new Double(entry.getValue()));
            mySortedData.add(new Double(entry.getValue()));
        }

        for (int j = 1; j < myCumulativeData.size(); j++) {
        	myCumulativeData.set(j, myCumulativeData.get(j) + myCumulativeData.get(j-1));
		}

        for (int j = 0; j < myCumulativeData.size(); j++) {
        	myCumulativeData.set(j, myCumulativeData.get(j)/nrValidSamples);
		}
        
        SummaryStatistics stats = new SummaryStatistics();

        // Read data from an input stream,
        // adding values and updating sums, counters, etc.
        for (int j = 0; j < mySortedData.size(); j++) {
            stats.addValue(mySortedData.get(j));
        }

	    // Compute the statistics
	    double median = stats.getMean();
	    double sigma = stats.getStandardDeviation();

	    double medPerVaildSamples = median/ nrValidSamples;
	    
	    double cdfl05= myCumulativeData.get((int) Math.floor(myCumulativeData.size()*0.5+1)-1); //% cumulative distribution at 0.5
	    
	    double cdfl075= myCumulativeData.get((int) Math.floor(myCumulativeData.size()*0.75+1)-1); //% cumulative distribution at 0.5
	    
        ArrayList<Double> out_upper = new ArrayList<Double>();
        for (int i = 0; i < myDataArrayList.size(); i++) {
			if (myDataArrayList.get(i) > (median + 2*sigma))
				out_upper.add(new Double(myDataArrayList.get(i)));
		}

        ArrayList<Double> out_lower = new ArrayList<Double>();
        for (int i = 0; i < myDataArrayList.size(); i++) {
			if (myDataArrayList.get(i) < (median - 2*sigma))
				out_lower.add(new Double(myDataArrayList.get(i)));
		}

	    double fracOutUpper = new Double(out_upper.size())/nrDistinctValues; //% fraction upper outliers
	    double fracOutLower = new Double(out_lower.size())/nrDistinctValues; //% fration lower outliers

	    double fracOutWeighted = fracOutUpper*(1-distinctFracComplement)+fracOutLower*distinctFracComplement;

	    StructureRecResultAnalysisResultData data = new StructureRecResultAnalysisResultData();
	    data.setResultDistinctValues(nrDistinctValues);
	    data.setResultMedian(median);
	    data.setResultMedianPerVaildSamples(medPerVaildSamples);
	    data.setResultSigma(sigma);
	    data.setResultDistinctFracComplement(distinctFracComplement);
	    data.setResultCdfl05(cdfl05);
	    data.setResultCdfl075(cdfl075);
	    
	    data.setResultFracOutLower(fracOutLower);
	    data.setResultFracOutUpper(fracOutUpper);
	    data.setResultFracOutWeighted(fracOutWeighted);
	    return data;
	}

	
	private HashMap sortByValues(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	                  .compareTo(((Map.Entry) (o2)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
	
	
	private StructureRecResult dateFormatCheck(List<ValueSource> values,
			StructureRecResult result) {
		for (ValueSource actValueObject : values) {
			String actValue = actValueObject.getValue();
			if (actValue != null ) {
				actValue = actValue.trim();
				String format = this.parseDate(actValue);
				if (!format.isEmpty()) {
					result.addValueDatePatternToHashMap(format, actValue, actValueObject.getFilename());
				}
			}			
		}
		return result;
	}
	
	private StructureRecResult urlFormatCheck(List<ValueSource> values,
			StructureRecResult result) {
		for (ValueSource actValueObject : values) {
			String actValue = actValueObject.getValue();
			if (actValue != null ) {
				actValue = actValue.trim();
				String format = this.parseURL(actValue);
				if (!format.isEmpty()) {
					result.addValueURLPatternToHashMap(format, actValue, actValueObject.getFilename());
				}
			}			
		}
		return result;
	}
	
	private String parseDate(String strDate) {
		ArrayList<String> inputFormatList = new ArrayList<String>();
		inputFormatList.add("yyyy-MM-dd'T'HH:mm:ss");
		inputFormatList.add("yyyy/MM/dd'T'HH:mm:ss");
		inputFormatList.add("yyyy-MM-dd'T'HH:mm");
		inputFormatList.add("yyyy/MM/dd'T'HH:mm");
		inputFormatList.add("yyyy-MM-dd");
		inputFormatList.add("yyyy/MM/dd");
		inputFormatList.add("yyyy-MM");
		inputFormatList.add("yyyy/MM");
		inputFormatList.add("yyyy");
		inputFormatList.add("M/d/yyyy");
		inputFormatList.add("M/d/yy");
		inputFormatList.add("MM/dd/yy");
		inputFormatList.add("MM/dd/yyyy");
		String detecedFormat = "";
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < inputFormatList.size(); i++) {
			SimpleDateFormat inputFormat = new SimpleDateFormat(inputFormatList.get(i));
			inputFormat.setLenient(true);
			try {
				Date javaDate = inputFormat.parse(strDate);
				calendar.setTime(javaDate);
				if (calendar.get(Calendar.YEAR) < 3000) {
					detecedFormat = inputFormatList.get(i); 
					break;
				}
			}
			catch (ParseException e) {
			}
		}
		return detecedFormat;
	}

	private String parseURL(String strValue) {
		try {
			URL inputFormat = new URL(strValue);
			String host = inputFormat.getHost();
			String protokoll = inputFormat.getProtocol();
			String ret = protokoll + "://" + host;
			return ret;
		} catch (MalformedURLException e) {
			return "";
		}
	}

	protected StructureRecResult calcLengths(List<ValueSource> values, StructureRecResult result) {
		boolean firstLoop = true;
		for (ValueSource actValue : values) {
			result.records++;
			if (actValue != null ) {
				if( actValue.getValue().length() > result.getLengthMax())
					result.setLengthMax(actValue.getValue().length());
				if( actValue.getValue().length() <= result.getLengthMin() || firstLoop)
					result.setLengthMin(actValue.getValue().length());
				String actValueString = actValue.getValue().trim();
				if( actValueString.length() > result.getLengthMaxTrimmed())
					result.setLengthMaxTrimmed(actValueString.length());
				if( actValueString.length() <= result.getLengthMinTrimmed() || firstLoop)
					result.setLengthMinTrimmed(actValueString.length());
				firstLoop = false;
			}			
		}
		return result;
	}
	
	protected StructureRecResult calcLengthHistogramm(List<ValueSource> values, StructureRecResult result) {
		for (ValueSource actValueObject : values) {
			String actValue = actValueObject.getValue();
			if (actValue != null ) {
				actValue = actValue.trim();
				result.addValueLengthHistogram(actValue.length());
			}			
		}
		return result;
	}
	
	protected StructureRecResult calcValuesHashmap(List<ValueSource> values, StructureRecResult result) {
		for (ValueSource actValueObject : values) {
			String actValue = actValueObject.getValue();
			if (actValue != null ) {
				actValue = actValue.trim();
				result.addValueToHashMap(actValue);
			}			
		}
		return result;
	}
	
	protected StructureRecResult calcValuesPatternHashmap(List<ValueSource> values, StructureRecResult result) {
		for (ValueSource actValueObject : values) {
			String actValue = actValueObject.getValue();
			if (actValue != null ) {
				actValue = actValue.trim();
				result.addValuePatternToHashMap(calcPattern(actValue),actValue,actValueObject.getFilename());
			}			
		}
		return result;
	}
	
	protected StructureRecResult calcValuesPatternRegExHashmap(List<ValueSource> values, StructureRecResult result) {
		for (ValueSource actValueObject : values) {
			String actValue = actValueObject.getValue();
			if (actValue != null ) {
				actValue = actValue.trim();
				result.addValuePatternRegExToHashMap(calcPatternRegEx(actValue),actValue,actValueObject.getFilename());
			}			
		}
		return result;
	}
	
	protected String calcPattern(String value) {
		if (value == null) return null;
		value = value.replaceAll("\\r\\n|\\r|\\n", " ");
		value = value.replaceAll("\\p{L}", "a");
		value = value.replaceAll("\\d", "0");
		value = value.replaceAll("\\s+", " ");
		return value;
	}

	protected String calcPatternRegEx(String value) {
		if (value == null) return null;
		value = value.replaceAll("\\r\\n|\\r|\\n", " ");
		value = value.replaceAll("\\p{L}+", "a+");
		value = value.replaceAll("\\d+", "0+");
		value = value.replaceAll("\\s+", " ");
		return value;
	}
}
