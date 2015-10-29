package eu.eexcess.dataquality.structure;

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
		return result;
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
}
