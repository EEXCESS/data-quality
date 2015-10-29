package eu.eexcess.dataquality.structure;

public class StructureRecResult {

	protected boolean structureDetected = false;
	
	protected String pattern ="";
	
	protected int records=0;
	
	public boolean isStructureDetected() {
		return structureDetected;
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
		out += "structureDetected:" + structureDetected + "\n";
		out += "pattern:" + pattern + "\n";
		return out;
	}
}
