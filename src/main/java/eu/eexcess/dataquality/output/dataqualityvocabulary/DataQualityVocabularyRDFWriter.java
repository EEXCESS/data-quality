package eu.eexcess.dataquality.output.dataqualityvocabulary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import eu.eexcess.dataquality.Qc_dataprovider;

public class DataQualityVocabularyRDFWriter {
	
	StringBuffer qualityMeasures;
	
	String namespace ="eexdaq:";
	
	String timestamp ="";
	
	ArrayList<String> metricList = new ArrayList<String>();
	
	protected void addMetric(String name) {
		this.metricList.add(name);
	}
	
	private String getMetricName(String name){
		return namespace + "metric#"+name;
	}

	ArrayList<String> distributionList = new ArrayList<String>();
	
	protected void addDistribution(String name) {
		this.distributionList.add(name);
	}

	private String getDatasetName(){
		return namespace +"dataset#" + timestamp;
	}
	
	private String getDistributionName(String name){
		return namespace +"dataset#"+name+"Distribution" + timestamp;
	}

	private String getQualityMeasureName(String metric, String dataprovider){
		return namespace +"metric#"+metric+dataprovider + timestamp;
	}

	public DataQualityVocabularyRDFWriter() {
		initNewFile();
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        this.timestamp = simpleDateFormat.format(date);
	}
	
	public void initNewFile(){
		qualityMeasures =  new StringBuffer();
	}
	
	public void write() {
		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "statistics-dataprovider.xml");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			writerStatisticRecords.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			writerStatisticRecords.append("<rdf:RDF xmlns:eexdaq=\"http://eexcess.eu/ns/dataquality/daq/\" xmlns:daq=\"http://purl.org/eis/vocab/daq#\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:dqv=\"http://www.w3.org/ns/dqv#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prov=\"http://www.w3.org/ns/prov#\" >");
			
			for (int i = 0; i < metricList.size(); i++) {
				writerStatisticRecords.write("<daq:Metric rdf:about=\""+this.getMetricName(metricList.get(i))+"\">");
				writerStatisticRecords.write("</daq:Metric>");
			}
			
			writerStatisticRecords.write("<dcat:Dataset rdf:about=\""+this.getDatasetName()+"\">");
			writerStatisticRecords.write("<dct:title>My EEXCESS dataset</dct:title>");
			for (int i = 0; i < distributionList.size(); i++) {
				writerStatisticRecords.write("<dcat:distribution>");
				writerStatisticRecords.write("<dcat:Distribution rdf:about=\""+this.getDistributionName(distributionList.get(i))+"\">");
				writerStatisticRecords.write("<dct:title>My EEXCESS "+distributionList.get(i)+" dataset </dct:title>");
				writerStatisticRecords.write("<prov:wasGeneratedBy rdf:resource=\""+this.namespace+"dataprovider#"+distributionList.get(i)+"\"/>");
				writerStatisticRecords.write("</dcat:Distribution>");
				writerStatisticRecords.write("</dcat:distribution>");
			}
			
			writerStatisticRecords.write("</dcat:Dataset>");
			
			writerStatisticRecords.append(this.qualityMeasures);
	
			writerStatisticRecords.write("</rdf:RDF>");
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addQualityMeasure(String metric, String dataprovider, String value) {
		if (!this.metricList.contains(metric)) this.metricList.add(metric);
		if (!this.distributionList.contains(dataprovider)) this.distributionList.add(dataprovider);
		
		qualityMeasures.append("<dqv:QualityMeasure rdf:about=\""+getQualityMeasureName(metric,dataprovider)+"\">");
		qualityMeasures.append("<daq:value rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">"+value+"</daq:value>");
		qualityMeasures.append("<daq:computedOn rdf:resource=\""+this.getDistributionName(dataprovider)+"\"/>");
		qualityMeasures.append("<daq:metric rdf:resource=\""+this.getMetricName(metric)+"\"/>");
		qualityMeasures.append("</dqv:QualityMeasure>");
	}
	
	
	public void addQualityMeasure_numberOfRecords(String dataprovider, String value) {
		this.addQualityMeasure("numberOfRecords", dataprovider, value);
	}
	
	public void addQualityMeasure_meanFieldsPerRecord(String dataprovider, String value) {
		this.addQualityMeasure("meanFieldsPerRecord", dataprovider, value);
	}
	
	public void addQualityMeasure_minFieldsPerRecord(String dataprovider, String value) {
		this.addQualityMeasure("minFieldsPerRecord", dataprovider, value);
	}

	public void addQualityMeasure_maxFieldsPerRecord(String dataprovider, String value) {
		this.addQualityMeasure("maxFieldsPerRecord", dataprovider, value);
	}
	
	public void addQualityMeasure_meanNonEmptyFieldsPerRecord(String dataprovider, String value) {
		this.addQualityMeasure("meanNonEmptyFieldsPerRecord", dataprovider, value);
	}
	
	public void addQualityMeasure_meanNonEmptyFieldsPerDatafieldsPerRecord(String dataprovider, String value) {
		this.addQualityMeasure("meanNonEmptyFieldsPerDatafieldsPerRecord", dataprovider, value);
	}
	
	public void addQualityMeasure_meanEmptyFieldsPerRecord(String dataprovider, String value) {
		this.addQualityMeasure("meanEmptyFieldsPerRecord", dataprovider, value);
	}

	public void addQualityMeasure_meanEmptyFieldsPerDatafieldsRecord(String dataprovider, String value) {
		this.addQualityMeasure("meanEmptyFieldsPerDatafieldsRecord", dataprovider, value);
	}

}
