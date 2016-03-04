package eu.eexcess.dataquality.structure;

public class PatternSource {

	protected String value;
	
	protected String filename;

	public PatternSource(String value, String filename) {
		super();
		this.value = value;
		this.filename = filename;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}
