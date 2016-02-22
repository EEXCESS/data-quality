package eu.eexcess.dataquality.structure;

import java.util.ArrayList;
import java.util.List;

public class StructureRecognizer {

	
	public StructureRecResult analyse(List<String> values) {
		StructureRecResult result = new StructureRecResult();
		result = calcLengths(values, result);
		if (result.getRecords() == 0) {
			return result;
		}
		result.initLengthHistogramm(result.getLengthMaxTrimmed());
		result = calcLengthHistogramm(values, result);
		result = calcValuesHashmap(values, result);
		result = calcValuesPatternHashmap(values, result);
//		System.out.println("records:"+result.records + " with " + result.getValuesPatternHashMap().size() + " different patterns");
//		double value = result.records / result.getValuesPatternHashMap().size();
//		System.out.println(value);
//		result = trimLengthHistogramm(result);
		return result;
	}
	
	private StructureRecResult trimLengthHistogramm(StructureRecResult result) {
		ArrayList<Integer> tempLengthHistogramm = new ArrayList<Integer>();
		for (int i = 0; i < result.getLengthHistogram().length; i++) {
			if (result.getLengthHistogram()[i] > 0 )
				tempLengthHistogramm.add(new Integer(result.getLengthHistogram()[i]));
		}
		
		return null;
	}

	protected StructureRecResult calcLengths(List<String> values, StructureRecResult result) {
		boolean firstLoop = true;
		for (String actValue : values) {
			result.records++;
			if (actValue != null ) {
				if( actValue.length() > result.getLengthMax())
					result.setLengthMax(actValue.length());
				if( actValue.length() <= result.getLengthMin() || firstLoop)
					result.setLengthMin(actValue.length());
				actValue = actValue.trim();
				if( actValue.length() > result.getLengthMaxTrimmed())
					result.setLengthMaxTrimmed(actValue.length());
				if( actValue.length() <= result.getLengthMinTrimmed() || firstLoop)
					result.setLengthMinTrimmed(actValue.length());
				firstLoop = false;
			}			
		}
		return result;
	}
	
	protected StructureRecResult calcLengthHistogramm(List<String> values, StructureRecResult result) {
		for (String actValue : values) {
			if (actValue != null ) {
				actValue = actValue.trim();
				result.addValueLengthHistogram(actValue.length());
			}			
		}
		return result;
	}
	
	protected StructureRecResult calcValuesHashmap(List<String> values, StructureRecResult result) {
		for (String actValue : values) {
			if (actValue != null ) {
				actValue = actValue.trim();
				result.addValueToHashMap(actValue);
			}			
		}
		return result;
	}
	
	protected StructureRecResult calcValuesPatternHashmap(List<String> values, StructureRecResult result) {
		for (String actValue : values) {
			if (actValue != null ) {
				actValue = actValue.trim();
				result.addValuePatternToHashMap(calcPattern(actValue));
			}			
		}
		return result;
	}
	
	protected String calcPattern(String value) {
		if (value == null) return null;
		value = value.replaceAll("\\p{L}", "a");
		value = value.replaceAll("\\d", "0");
		value = value.replaceAll("\\s\\s+", " ");
		return value;
	}
}
