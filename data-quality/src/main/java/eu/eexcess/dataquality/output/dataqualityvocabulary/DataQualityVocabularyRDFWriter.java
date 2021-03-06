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
package eu.eexcess.dataquality.output.dataqualityvocabulary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import eu.eexcess.dataquality.DataQualityApp;
import eu.eexcess.dataquality.Qc_dataprovider;
import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;

public class DataQualityVocabularyRDFWriter {
	
	public static final String STATISTICS_DATAPROVIDER_XML_FILENAME = "statistics-dataprovider.xml";

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
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ STATISTICS_DATAPROVIDER_XML_FILENAME);
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			StringBuffer writerBuffer = new StringBuffer();
			writerBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			writerBuffer.append("<rdf:RDF xmlns:eexdaq=\"http://eexcess.eu/ns/dataquality/daq/\" xmlns:daq=\"http://purl.org/eis/vocab/daq#\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:dqv=\"http://www.w3.org/ns/dqv#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:prov=\"http://www.w3.org/ns/prov#\" >");
			
			for (int i = 0; i < metricList.size(); i++) {
				writerBuffer.append("<daq:Metric rdf:about=\""+this.getMetricName(metricList.get(i))+"\">");
				writerBuffer.append("</daq:Metric>");
			}
			
			writerBuffer.append("<dcat:Dataset rdf:about=\""+this.getDatasetName()+"\">");
			writerBuffer.append("<dct:title>My EEXCESS dataset</dct:title>");
			for (int i = 0; i < distributionList.size(); i++) {
				writerBuffer.append("<dcat:distribution>");
				writerBuffer.append("<dcat:Distribution rdf:about=\""+this.getDistributionName(distributionList.get(i))+"\">");
				writerBuffer.append("<dct:title>My EEXCESS "+distributionList.get(i)+" dataset </dct:title>");
				writerBuffer.append("<prov:wasGeneratedBy rdf:resource=\""+this.namespace+"dataprovider#"+distributionList.get(i)+"\"/>");
				writerBuffer.append("</dcat:Distribution>");
				writerBuffer.append("</dcat:distribution>");
			}
			
			writerBuffer.append("</dcat:Dataset>");
			
			writerBuffer.append(this.qualityMeasures);
	
			writerBuffer.append("</rdf:RDF>");
			String out = writerBuffer.toString();
			if (out.contains(DataProvider.unknown.toString())) {
				out = out.replaceAll(DataProvider.unknown.toString(), Qc_dataprovider.cmdParameterDataprovider);
			}
			writerStatisticRecords.write(out);
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
