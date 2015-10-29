package eu.eexcess.dataquality.structure;

import java.util.List;

public class StructureRecognizer {

	
	public StructureRecResult analyse(List<String> values) {
		StructureRecResult result = new StructureRecResult();
		for (String string : values) {
			result.records++;
		}
		return result;
	}
	
	
}
