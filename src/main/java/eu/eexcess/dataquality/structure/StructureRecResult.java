package eu.eexcess.dataquality.structure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class StructureRecResult {

	protected boolean structureDetected = false;
	
	protected String pattern ="";
	
	protected int records=0;
	
	protected int lengthMax =-1;
	
	protected int lengthMaxTrimmed = -1;
	
	protected int lengthMin;
	
	protected int lengthMinTrimmed;

	protected int[] lengthHistogram;
	
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
	
	public void addValuePatternToHashMap(String value) {
		value = value.trim();
		if (!valuesPatternHashMap.containsKey(value)) {
			valuesPatternHashMap.put(value, 1);
		} else {
			valuesPatternHashMap.put(value, valuesPatternHashMap.get(value) + 1);
		}
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

	public void setLengthHistogram(int[] lengthHistogram) {
		this.lengthHistogram = lengthHistogram;
	}

	public void setStructureDetected(boolean structureDetected) {
		this.structureDetected = structureDetected;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

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
			Map.Entry pair = (Map.Entry) valuesMapIterator.next();
			out += pair.getKey() + " \t" + pair.getValue() + "\n";
		}
		out += "\n";
		out += "valuesPatternHashMap:\n";
		Iterator<Entry<String, Integer>> valuesPatternMapIterator = valuesPatternHashMap.entrySet().iterator();
		while (valuesPatternMapIterator.hasNext()) {
			Map.Entry pair = (Map.Entry) valuesPatternMapIterator.next();
			out += pair.getKey() + " \t" + pair.getValue() + "\n";
		}
		out += "\n";
		out += "pattern:" + pattern + "\n";
		out += "structureDetected:" + structureDetected + "\n";
		return out;
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
