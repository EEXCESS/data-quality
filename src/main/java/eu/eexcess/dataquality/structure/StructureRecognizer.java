package eu.eexcess.dataquality.structure;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
		
		return result;
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
