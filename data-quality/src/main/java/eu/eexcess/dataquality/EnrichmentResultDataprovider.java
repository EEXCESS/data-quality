package eu.eexcess.dataquality;

public class EnrichmentResultDataprovider {

	protected String dataprovider;
    public String getDataprovider() {
		return dataprovider;
	}
	public void setDataprovider(String dataprovider) {
		this.dataprovider = dataprovider;
	}
	public long getCountRecordsEnriched() {
		return countRecordsEnriched;
	}
	public void setCountRecordsEnriched(long countRecordsEnriched) {
		this.countRecordsEnriched = countRecordsEnriched;
	}
	public long getCountRecordsEnrichedWithEnrichedMetadata() {
		return countRecordsEnrichedWithEnrichedMetadata;
	}
	public void setCountRecordsEnrichedWithEnrichedMetadata(
			long countRecordsEnrichedWithEnrichedMetadata) {
		this.countRecordsEnrichedWithEnrichedMetadata = countRecordsEnrichedWithEnrichedMetadata;
	}
	protected long countRecordsEnriched =0;
    protected long countRecordsEnrichedWithEnrichedMetadata =0;

    public void increaseRecordsEnriched() {
    	this.countRecordsEnriched++;
    }
    public void increaseRecordsEnrichedWithEnrichedMetadata() {
    	this.countRecordsEnrichedWithEnrichedMetadata++;
    }
}
