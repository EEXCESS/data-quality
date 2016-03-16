/*
Copyright (C) 2015
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
package eu.eexcess.dataquality;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.eexcess.dataquality.graphs.Qc_graphs;
import eu.eexcess.dataquality.output.dataqualityvocabulary.DataQualityVocabularyRDFWriter;
import eu.eexcess.dataquality.providers.Qc_DDB;
import eu.eexcess.dataquality.providers.Qc_ZBW;
import eu.eexcess.dataquality.providers.Qc_base;
import eu.eexcess.dataquality.providers.Qc_base.SearchType;
import eu.eexcess.dataquality.providers.Qc_cultureWeb;
import eu.eexcess.dataquality.providers.Qc_eexcess;
import eu.eexcess.dataquality.providers.Qc_eexcess_enriched;
import eu.eexcess.dataquality.providers.Qc_europeana;
import eu.eexcess.dataquality.providers.Qc_kimcollect;
import eu.eexcess.dataquality.providers.Qc_mendeley;
import eu.eexcess.dataquality.providers.Qc_wissenmedia;
import eu.eexcess.dataquality.structure.PatternSource;
import eu.eexcess.dataquality.structure.StructureRecResult;
import eu.eexcess.dataquality.structure.StructureRecognizer;
import eu.eexcess.dataquality.structure.ValueSource;

// Check for data provider
public class Qc_dataprovider {

	public static final String OUTPUT_STRUCT = "struct";
	public static final String OUTPUT_STRUCT_IMG = "img";
	public static final String OUTPUT_STRUCT_IMG_DIR = OUTPUT_STRUCT + "/" + OUTPUT_STRUCT_IMG + "/";
	public static final String OUTPUT_STRUCT_CSV = "csv";
	public static final String OUTPUT_STRUCT_CSV_DIR = OUTPUT_STRUCT + "/" + OUTPUT_STRUCT_CSV + "/";
	
	private static final String DATAQUALITY_REPORT_PLOT_HTML_FILENAME = "dataquality-report-plot.html";
	private static final String STATISTIC_FILE_FIELD_SEPERATOR = ";";
	private static final String STATISTIC_SYSTEMOUT_FIELD_SEPERATOR = "\t";
	private static final int CHART_WIDTH_HIGH = 1600;
	private static final int CHART_HEIGHT_HIGH = 1200;
	private static final int CHART_WIDTH_MID = 800;
	private static final int CHART_HEIGHT_MID = 600;
//	private static final int CHART_WIDTH_LOW = 400;
//	private static final int CHART_HEIGHT_LOW = 350;
	Qc_base currentProvider = null;
	Qc_paramDataList paramDataList = new Qc_paramDataList();
	
	public static String outputDir ="./output/";
	
	HashMap<String,HashMap<String, StructureRecResult>> structurednessResults = new HashMap<String, HashMap<String, StructureRecResult>>();

	// XML files of known partners
	public enum DataProvider {
		KIMCollect, ZBW, Europeana, Wissenmedia, Mendeley, EEXCESS, EEXCESS_enriched, DDB, cultureWeb, unknown
	}

	public void process(String[] sParams) {
		timestampStart = System.currentTimeMillis();
		inputDirs = new ArrayList<String>();
		for (int i = 0; i < sParams.length; i++) {
			File f = new File(sParams[i]);
			if (f.isFile() == true && f.isDirectory() == false) {
				checkDataProviderFile(sParams[i]);
			} else if (f.isDirectory() == true) {
				inputDirs.add(f.getPath());
				File[] files = f.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".xml");
					}
				});
				for (int j = 0; j < files.length; j++) {
					checkDataProviderFile(files[j].getPath());
				}
			}
		}
		// check structuredness
		checkStructuredness(sParams);
		// output results
		copyResources();
		printStatisticsCharts();
		printRDFXMLVisWithJQPlot();
		printReports();
		long timestampEnd = System.currentTimeMillis();
		long timespanMS = timestampEnd - timestampStart;
		double timespanS = timespanMS / 1000;
		double timespanM = timespanS / 60;
		System.out.println("\nElapsed time for processing: " + (timestampEnd - timestampStart) + "ms. ("+timespanS+"s or "+timespanM+"m)");

	}

	NumberFormat numberFormater = NumberFormat.getNumberInstance( new Locale.Builder().setLanguage("en").setRegion("GB").build());

	private String formatNumber(double number) {
		return numberFormater.format(number);
	}
	
	private void printStatisticsCharts() {
		htmlReportInputDataStatisticsResults = "<table>";
		
		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "statistics-results.csv");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			writerStatisticRecords.write("file"+STATISTIC_FILE_FIELD_SEPERATOR+"provider"+STATISTIC_FILE_FIELD_SEPERATOR+"#records"+STATISTIC_FILE_FIELD_SEPERATOR+"mean fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+"min fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+"max fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean non empty fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean non empty fields per datafields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean empty fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean empty fields per datafields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
//uriCheck					
//					"links/record");
					"links/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"accessible links/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"#links"+STATISTIC_FILE_FIELD_SEPERATOR+					
					"#accessible links");
			writerStatisticRecords.newLine();
			System.out.println("file"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"provider"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"#records"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"mean fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"min fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"max fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean non empty fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean non empty fields per datafields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean empty fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean empty fields per datafields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//uriCheck
//					"mean links/record");
					"links/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"accessible links/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"#links"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"#accessible links");					
			htmlReportInputDataStatisticsResults += "<tr><th>file</th><th>provider</th><th>#records</th><th>mean fields/record</th><th>min fields/record</th>"+
					"<th>max fields/record</th><th>mean non empty fields/record</th><th>mean non empty fields per datafields/record</th><th>mean empty fields/record</th>"+
					"<th>mean empty fields per datafields/record</th><th>links/record</th><th>accessible links/record</th><th>#links</th><th>#accessible links</th></tr>";
			
			for (int i = 0; i < paramDataList.size(); i++) {
				Qc_params param = paramDataList.get(i);
				System.out.println(param.getXmlFileName() + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ param.getProvider().toString() + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ param.getRecordCount() + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getMinDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getMaxDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getNonEmptyDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getNonEmptyDataFieldsPerRecordPerDatafields()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getEmptyDataFieldsPerRecord())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getEmptyDataFieldsPerRecordPerDatafields())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//uriCheck						
//						+ formatNumber(param.getLinkDataFieldsPerRecord()));
						+ formatNumber(param.getLinkDataFieldsPerRecord())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getAccessibleLinksDataFieldsPerRecord())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getNumberOfAllLinkDataFields())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
						+ formatNumber(param.getNumberOfAllAccessibleLinks()));		
				htmlReportInputDataStatisticsResults += "<tr><td><a href=\".\\input\\"+param.getXmlFileName()+"\">" + param.getXmlFileName() + "</a></td><td>"
								+ param.getProvider().toString() + "</td><td>"
								+ param.getRecordCount() + "</td><td>"
								+ formatNumber(param.getDataFieldsPerRecord()) + "</td><td>"
								+ formatNumber(param.getMinDataFieldsPerRecord()) + "</td><td>"
								+ formatNumber(param.getMaxDataFieldsPerRecord()) + "</td><td>"
								+ formatNumber(param.getNonEmptyDataFieldsPerRecord()) + "</td><td>"
								+ formatNumber(param.getNonEmptyDataFieldsPerRecordPerDatafields()) + "</td><td>"
								+ formatNumber(param.getEmptyDataFieldsPerRecord())+ "</td><td>"
								+ formatNumber(param.getEmptyDataFieldsPerRecordPerDatafields())+ "</td><td>"
		//uriCheck						
//								+ formatNumber(param.getLinkDataFieldsPerRecord()));
								+ formatNumber(param.getLinkDataFieldsPerRecord())+ "</td><td>"
								+ formatNumber(param.getAccessibleLinksDataFieldsPerRecord())+ "</td><td>"
								+ formatNumber(param.getNumberOfAllLinkDataFields())+ "</td><td>"
								+ formatNumber(param.getNumberOfAllAccessibleLinks())
								+ "</td></tr>";
						
				writerStatisticRecords.write(param.getXmlFileName() + STATISTIC_FILE_FIELD_SEPERATOR
						+ param.getProvider().toString() + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getRecordCount()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getDataFieldsPerRecord()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getMinDataFieldsPerRecord()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getMaxDataFieldsPerRecord()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getNonEmptyDataFieldsPerRecord()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getNonEmptyDataFieldsPerRecordPerDatafields()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getEmptyDataFieldsPerRecord()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getEmptyDataFieldsPerRecordPerDatafields()) + STATISTIC_FILE_FIELD_SEPERATOR
//uriCheck
//						+ formatNumber(param.getLinkDataFieldsPerRecord()));
						+ formatNumber(param.getLinkDataFieldsPerRecord()) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getAccessibleLinksDataFieldsPerRecord())+ STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getNumberOfAllLinkDataFields())+ STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(param.getNumberOfAllAccessibleLinks()));						
				writerStatisticRecords.newLine();
			}
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		htmlReportInputDataStatisticsResults += "</table><p><a href=\".\\statistics-results.csv\">data as CSV</a></p>";
		htmlReportInputDataStatisticsDataprovider = "<table>";

		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "statistics-dataprovider.csv");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			writerStatisticRecords.write("file"+STATISTIC_FILE_FIELD_SEPERATOR+"provider"+STATISTIC_FILE_FIELD_SEPERATOR+"#records"+STATISTIC_FILE_FIELD_SEPERATOR+"mean fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+"min fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+"max fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
						"mean non empty fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
						"mean non empty fields per datafields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
						"mean empty fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
						"mean empty fields per datafields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
						//uriCheck
						//"mean links/record");
						"mean links/record"+STATISTIC_FILE_FIELD_SEPERATOR+
						"mean accessible links/record"+STATISTIC_FILE_FIELD_SEPERATOR+
						"#links"+STATISTIC_FILE_FIELD_SEPERATOR+
						"#accessible links");											
			writerStatisticRecords.newLine();
			htmlReportInputDataStatisticsDataprovider += "<tr><th>"+"provider"+"</th><th>"+"#records"+"</th><th>"+"mean fields/record"+"</th><th>"+"min fields/record"+"</th><th>"+"max fields/record"+"</th><th>"+
					"mean non empty fields/record"+"</th><th>"+
					"mean non empty fields per datafields/record"+"</th><th>"+
					"mean empty fields/record"+"</th><th>"+
					"mean empty fields per datafields/record"+"</th><th>"+
					//uriCheck
					//"mean links/record");
					"mean links/record"+"</th><th>"+
					"mean accessible links/record"+"</th><th>"+
					"#links"+"</th><th>"+
					"#accessible links</th></tr>";
			
			for (int i=0;i<DataProvider.values().length; i++)
			{
				writerStatisticRecords.write("" + STATISTIC_FILE_FIELD_SEPERATOR
						+ DataProvider.values()[i].toString() + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR
						+ paramDataList.getMinDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) + STATISTIC_FILE_FIELD_SEPERATOR
						+ paramDataList.getMaxDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR

						//uriCheck+ formatNumber(paramDataList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
						+ formatNumber(paramDataList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getAccesibleLinksPerRecordsPerProvider(DataProvider.values()[i])) + STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getNumberOfLinkDataFieldsPerProvider(DataProvider.values()[i]))+ STATISTIC_FILE_FIELD_SEPERATOR
						+ formatNumber(paramDataList.getNumberOfAccesibleLinksPerProvider(DataProvider.values()[i]))
						);
				writerStatisticRecords.newLine();
				htmlReportInputDataStatisticsDataprovider += "<tr><td>"				
						+ DataProvider.values()[i].toString() + "</td><td>"
						+ formatNumber(paramDataList.getRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
						+ formatNumber(paramDataList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
						+ paramDataList.getMinDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) + "</td><td>"
						+ paramDataList.getMaxDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) + "</td><td>"
						+ formatNumber(paramDataList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
						+ formatNumber(paramDataList.getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
						+ formatNumber(paramDataList.getEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
						+ formatNumber(paramDataList.getEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
		
						//uriCheck+ formatNumber(paramDataList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
						+ formatNumber(paramDataList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
						+ formatNumber(paramDataList.getAccesibleLinksPerRecordsPerProvider(DataProvider.values()[i])) + "</td><td>"
						+ formatNumber(paramDataList.getNumberOfLinkDataFieldsPerProvider(DataProvider.values()[i]))+ "</td><td>"
						+ formatNumber(paramDataList.getNumberOfAccesibleLinksPerProvider(DataProvider.values()[i]))
						+ "</td></tr>";

			}
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		htmlReportInputDataStatisticsDataprovider += "</table><p><a href=\".\\statistics-dataprovider.csv\">data as CSV</a></p>";

		DataQualityVocabularyRDFWriter dQVRDFWriter = new DataQualityVocabularyRDFWriter();
		for (int i=0;i<DataProvider.values().length; i++)
		{
			dQVRDFWriter.addQualityMeasure_numberOfRecords(DataProvider.values()[i].toString(), formatNumber(paramDataList.getRecordsPerProvider(DataProvider.values()[i])));
			dQVRDFWriter.addQualityMeasure_meanFieldsPerRecord(DataProvider.values()[i].toString(), formatNumber(paramDataList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
			dQVRDFWriter.addQualityMeasure_minFieldsPerRecord(DataProvider.values()[i].toString(), ""+paramDataList.getMinDataFieldsPerRecordsPerProvider(DataProvider.values()[i]));
			dQVRDFWriter.addQualityMeasure_maxFieldsPerRecord(DataProvider.values()[i].toString(), ""+paramDataList.getMaxDataFieldsPerRecordsPerProvider(DataProvider.values()[i]));
			
			dQVRDFWriter.addQualityMeasure_meanNonEmptyFieldsPerRecord(DataProvider.values()[i].toString(), formatNumber(paramDataList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
			dQVRDFWriter.addQualityMeasure_meanNonEmptyFieldsPerDatafieldsPerRecord(DataProvider.values()[i].toString(), formatNumber(paramDataList.getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i])));
			
			//dQVRDFWriter.addQualityMeasure_meanEmptyFieldsPerRecord(DataProvider.values()[i].toString(), formatNumber(paramDataList.getEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
			//dQVRDFWriter.addQualityMeasure_meanEmptyFieldsPerDatafieldsRecord(DataProvider.values()[i].toString(), formatNumber(paramDataList.getEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i])));
			
		}
		dQVRDFWriter.write();
		

//		Qc_graphs.allProviderDataFieldsPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderDataFieldsPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderDataFieldsPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);

//		Qc_graphs.allProviderNonEmptyDataFieldsPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);

//		Qc_graphs.allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);

//		Qc_graphs.allProviderLinksPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderLinksPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderLinksPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);
		
		//uriCheck
		Qc_graphs.allProviderLinksNotAccessiblePerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderLinksNotAccessiblePerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);
	}

	// try to check from which partner the XML file is.
	private void checkDataProviderFile(String xmlFile) {

		// System.out.println(xmlFile);
		
		File f = new File(xmlFile);
		if (f.exists() == true && f.isDirectory() == false) {
			for (int i = 0; i < DataProvider.values().length; i++) {
				switch (DataProvider.values()[i]) {
				case DDB:
					currentProvider = new Qc_DDB();
					break;

				case Europeana:
					currentProvider = new Qc_europeana();
					break;

				case Mendeley:
					currentProvider = new Qc_mendeley();
					break;

				case Wissenmedia:
					currentProvider = new Qc_wissenmedia();
					break;
					
				case cultureWeb:
					currentProvider = new Qc_cultureWeb();
					break;

				case KIMCollect:
					currentProvider = new Qc_kimcollect();
					break;
					
				case EEXCESS_enriched:
					currentProvider = new Qc_eexcess_enriched();
					break;

				case EEXCESS:
					currentProvider = new Qc_eexcess();
					break;

				case ZBW:
					currentProvider = new Qc_ZBW();
					break;

				default:
					currentProvider = null;
					break;
				}

				if (currentProvider != null) {
					currentProvider.setXmlFileName(xmlFile);
					if (currentProvider.isProviderRecord() == true) {
						currentProvider.getDataProvider();
						
						// System.out.println("Records Count: " + currentProvider.getRecordsCount());
						currentProvider.countDataFields(SearchType.allDataFields);
						currentProvider.countDataFields(SearchType.notEmptyDataFields);
						currentProvider.countDataFields(SearchType.linkDataFields);
						//uriCheck
						currentProvider.countDataFields(SearchType.uriDataFields);
						Qc_params param = currentProvider.getParam();
						paramDataList.addParam(param);
						
						// System.out.println(currentProvider + " " + xmlFile);
						
						break;
					}
				}
			}
			if (currentProvider == null) {
				Qc_params param = new Qc_params(DataProvider.unknown);
				param.setXmlPath(xmlFile);
				paramDataList.addParam(param);
			}
		}
	}

	private void checkStructuredness(String[] sParams) {
		// get a list of all Files
		ArrayList<String> fileNames = new ArrayList<String>();
		for (int i = 0; i < sParams.length; i++) {
			File f = new File(sParams[i]);
			if (f.isFile() == true && f.isDirectory() == false) {
				fileNames.add(sParams[i]);
			} else if (f.isDirectory() == true) {
				File[] files = f.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".xml");
					}
				});
				for (int j = 0; j < files.length; j++) {
					fileNames.add(files[j].getPath());
				}
			}
		}
		ArrayList<String> dataproviders = new ArrayList<String>();
		for (Iterator<String> iteratorFileNames = fileNames.iterator(); iteratorFileNames.hasNext();) {
			String actFileName = (String) iteratorFileNames.next();
			String temp = actFileName.substring(actFileName.lastIndexOf("\\")+40);
			temp = temp.substring(0,temp.indexOf("-"));
			if (!dataproviders.contains(temp))
				dataproviders.add(temp);
		}
		/*
		System.out.println("dataprovider:");
		for (Iterator<String> iterator = dataproviders.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}
		*/
		
		//next list of all files per provider
		HashMap<String, ArrayList<String>> filesNameByDataproviderHashMap = new HashMap<String, ArrayList<String>>();
		for (Iterator<String> iteratorDataprovider = dataproviders.iterator(); iteratorDataprovider.hasNext();) {
			String actDataProvider = (String) iteratorDataprovider.next();
			ArrayList<String> fileNameByDataProvider = new ArrayList<String>();
			for (Iterator<String> iteratorFileNames = fileNames.iterator(); iteratorFileNames.hasNext();) {
				String actFileName = (String) iteratorFileNames.next();
				if (!actFileName.contains("enrichment") && !actFileName.contains("done-transform")) {
					if (actFileName.contains(actDataProvider))
						fileNameByDataProvider.add(actFileName);
				}
			}
			filesNameByDataproviderHashMap.put(actDataProvider, fileNameByDataProvider);
/*
			System.out.println("files for dataprovider:" + actDataProvider);
			for (Iterator<String> iterator = fileNameByDataProvider.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				System.out.println(string);
			}
	*/		
		}
		Iterator<Entry<String, ArrayList<String>>> filesNameByDataproviderHashMapIterator = filesNameByDataproviderHashMap.entrySet().iterator();
	    while (filesNameByDataproviderHashMapIterator.hasNext()) {
	        Entry<String, ArrayList<String>> entry = filesNameByDataproviderHashMapIterator.next();
	        String actDataprovider = entry.getKey();
	        ArrayList<String> actProviderFileList = entry.getValue();
			Qc_base qcBase = createProviderQC(actDataprovider);
//			System.out.println("dataprovider:"+actDataprovider+" "+qcBase.getRecordSeparator());
			
			// calc all XPaths for all fields in all file from a data provider
			ArrayList<String> fieldXPaths = new ArrayList<String>();
			for (String actFileName : actProviderFileList) {
				//System.out.println(actFileName);
				qcBase.setXmlFileName(actFileName);
				NodeList nodes = qcBase.getNodesListByXPath(qcBase.getRecordSeparator() + qcBase.getXpathsToFieldsFromRecordSeparator());
				for (int countRecords = 0; countRecords < nodes.getLength(); countRecords++) {
					//iterate over records
					if (nodes.item(countRecords).hasChildNodes())
					{
						for (int countFields = 0; countFields < nodes.item(countRecords).getChildNodes().getLength(); countFields++) {
							Node actNodeField = nodes.item(countRecords).getChildNodes().item(countFields);
							if (actNodeField.getNodeType() == Node.ELEMENT_NODE) {
								String tempFieldXpath = qcBase.getXPath(actNodeField);
								if (!fieldXPaths.contains(tempFieldXpath))
									fieldXPaths.add(tempFieldXpath);
							} else {
								if (actNodeField.hasChildNodes()) {
									for (int countSubFields = 0; countSubFields < actNodeField.getChildNodes().getLength(); countSubFields++) {
										Node actNodeSubField = actNodeField.getChildNodes().item(countSubFields);
										if (actNodeSubField.getNodeType() == Node.ELEMENT_NODE) {
											String tempFieldXpath = qcBase.getXPath(actNodeSubField);
											if (!fieldXPaths.contains(tempFieldXpath))
												fieldXPaths.add(tempFieldXpath);
										}
									}
								}
							}
						}
					}

				}
				
			}
			// now we have a list of all XPaths to fields for this data provider
			HashMap<String, StructureRecResult> actProviderStructurednessResults = new HashMap<String, StructureRecResult>();
			for (int i = 0; i < fieldXPaths.size(); i++) {
				// for earch field
				String actFieldName = fieldXPaths.get(i).substring(fieldXPaths.get(i).lastIndexOf("/")+1);
				ArrayList<ValueSource> values = new ArrayList<ValueSource>();
//				System.out.println(fieldXPaths.get(i));
				for (String actFileName : actProviderFileList) {
					// for each file
					qcBase.setXmlFileName(actFileName);
					NodeList nodes = qcBase.getNodesListByXPath(fieldXPaths.get(i));
					for (int count = 0; count < nodes.getLength(); count++) {
						if (nodes.item(count).getNodeType() == Node.ELEMENT_NODE) {
//							System.out.println(nodes.item(count).getTextContent());
							values.add(new ValueSource(nodes.item(count).getTextContent(),new File(actFileName).getName()));
						}
					}
				}
				StructureRecognizer recognizer = new StructureRecognizer();
				actProviderStructurednessResults.put(actFieldName, recognizer.analyse(values ));
			}

			
			this.structurednessResults.put(actDataprovider, actProviderStructurednessResults);

	    }
	}
	
	public static String DATAPROVIDER_KIMCOLLECT ="KIMCollect";
	public static String DATAPROVIDER_KIMPORTAL ="KIMPortal";
	public static String DATAPROVIDER_EUROPEANA ="Europeana";
	public static String DATAPROVIDER_DDB ="DeutscheDigitaleBibliothek";
	public static String DATAPROVIDER_MENDELEY ="Mendeley";
	public static String DATAPROVIDER_WISSENMEDIA ="Wissenmedia";
	public static String DATAPROVIDER_ZBW ="ZBW";
	protected String htmlReportInputDataStatisticsResults;
	protected String htmlReportInputDataStatisticsDataprovider;
	protected ArrayList<String> inputDirs;
	protected long timestampStart;
	
	public Qc_base createProviderQC(String dataprovider){
		if (dataprovider.equals(DATAPROVIDER_WISSENMEDIA))
			return new Qc_wissenmedia();
		if (dataprovider.equals(DATAPROVIDER_DDB))
			return new Qc_DDB();
		if (dataprovider.equals(DATAPROVIDER_EUROPEANA))
			return new Qc_europeana();
		if (dataprovider.equals(DATAPROVIDER_KIMCOLLECT))
			return new Qc_kimcollect();
		if (dataprovider.equals(DATAPROVIDER_KIMPORTAL))
			return new Qc_cultureWeb();
		if (dataprovider.equals(DATAPROVIDER_MENDELEY))
			return new Qc_mendeley();
		if (dataprovider.equals(DATAPROVIDER_ZBW))
			return new Qc_ZBW();
		if (dataprovider.equals(DATAPROVIDER_WISSENMEDIA))
			return new Qc_wissenmedia();
		
		return null;
	}
	
	private void printReports() {
		new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_IMG_DIR).mkdirs();
		new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR).mkdirs();
		
		String htmlReportJavascriptGeneral = new String();
		htmlReportJavascriptGeneral += "<script>$(document).ready(function(){";
		
		String htmlReportGeneral = "<html xmlns:prov=\"http://www.w3.org/ns/prov#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:daq=\"http://purl.org/eis/vocab/daq#\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:dqv=\"http://www.w3.org/ns/dqv#\" xmlns:eexdaq=\"http://eexcess.eu/ns/dataquality/daq/\" lang=\"en\">";
		htmlReportGeneral += " <head><title>EEXCESS Data Quality Report</title>";
		htmlReportGeneral += "<link rel=\"stylesheet\" type=\"text/css\" href=\"./report.css\">";
		htmlReportGeneral += "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>";
		htmlReportGeneral += " </head>";
		htmlReportGeneral += " <body>";
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		htmlReportGeneral += "<h1>EEXCESS Data Quality Report</h1><br/><h2>generated at:" +dt.format(new Date(System.currentTimeMillis()))+"</h2>";
		String htmlReportGeneralHeader = htmlReportGeneral;
		htmlReportGeneral += "<p>Portals providing access to different data provider more and more face the problem of \"data quality\". Data providers and aggregators are asked to do more quality checks on data they deliver. This clearly calls for more automation of the quality assessment process. In EEXCESS we decided to implement and include easy to handle features and analysis methods to provide feedback to the data provider regarding the data quality. Therefore, questions regarding the metadata provenance, the referencing of terms from relevant online vocabularies or the usage of open multilingual vocabularies and last but not least the very important questions regarding metadata rights that indicate the options for reusing the published resources have been stressed.</p>"; 
		htmlReportGeneral += "<p>We determine measure about the structuredness of values, for example of fields containing dates, names or dimensions of objects. The aim is not only to make a binary decision whether they are structured, but also whether the format the field can be inferred.</p>";
		htmlReportGeneral += "<p>This report gives feedback based on the data providerís datasets. On field level the data provider gets feedback regarding the structuredness, number of data provided and vocabulary accessibility.</p>";
		htmlReportGeneral += "<p>On field level the report provides feedback regarding the field length and field pattern. Having similar field length over the records and having less and similar patterns leads to the conclusion of better data quality and therefore, better recommendations and better visibility.</p>";
		htmlReportGeneral += "<p>To detect patterns in the values of the datasets, we replace all letters with \"a\", all numbers with \"0\" and multiple whitespaces with single whitespaces.</p>";
		htmlReportGeneral += "<p>By clicking on one data field the histogram sections of this report are opened so that the data provider gets the result of the quality checks.</p>";
		/*
		{
			Iterator<Entry<String, HashMap<String, StructureRecResult>>> iteratorDataprovider = structurednessResults.entrySet().iterator();
		    while (iteratorDataprovider.hasNext()) {
		        Entry<String, HashMap<String, StructureRecResult>> entry = iteratorDataprovider.next();
		        //String dataprovider = entry.getKey();
		        HashMap<String, StructureRecResult> resultsByDataprovider = entry.getValue();
	            // System.out.println("Dataprovider:" + dataprovider);
		        
		        Iterator<Entry<String, StructureRecResult>> iteratorByDataprovider = resultsByDataprovider.entrySet().iterator();
		        while (iteratorByDataprovider.hasNext()) {
		            Entry<String, StructureRecResult> fieldResult = (Entry<String, StructureRecResult>) iteratorByDataprovider.next();
		            String field = fieldResult.getKey();
		            StructureRecResult result = fieldResult.getValue();
		            // System.out.println("field:"+field);
		            // System.out.println("result:\n"+result.toString());
		        }
		    }
		}
	    */
		
		printInputDataInfoReport(htmlReportGeneralHeader);

		String htmlReportGeneralDataproviders = "<ul>";
		Iterator<Entry<String, HashMap<String, StructureRecResult>>> iteratorDataprovider = structurednessResults.entrySet().iterator();
	    while (iteratorDataprovider.hasNext()) {
	    	String htmlReportJavascript = new String(htmlReportJavascriptGeneral);
	    	String htmlReport =  new String(htmlReportGeneral);
	        Entry<String, HashMap<String, StructureRecResult>> entry = iteratorDataprovider.next();
	        String dataprovider = entry.getKey();
	        HashMap<String, StructureRecResult> resultsByDataprovider = entry.getValue();
            // System.out.println("Dataprovider:" + dataprovider);
            htmlReport += "<h3>" + dataprovider +"</h3>";
            
            htmlReport += "<h4>Overview (using RegEx Pattners)</h4>";
	        Iterator<Entry<String, StructureRecResult>> iteratorByDataprovider = resultsByDataprovider.entrySet().iterator();
	        htmlReport += "<table>";
	        htmlReport += "<tr><td><b>fieldname</b></td>";
	        htmlReport += "<td><b>distinct values</b></td>";
	        htmlReport += "<td><b>median</b></td>";
	        htmlReport += "<td><b>median by valid samples</b></td>";
	        htmlReport += "<td><b>standard deviation</b></td>";
	        htmlReport += "<td><b>distinct frac complement</b></td>";
	        htmlReport += "<td><b>cdfl 0.5</b></td>";
	        htmlReport += "<td><b>cdfl 0.75</b></td>";
	        htmlReport += "<td><b>lower frac</b></td>";
	        htmlReport += "<td><b>upper frac</b></td>";
	        htmlReport += "<td><b>weighted frac</b></td>";
	        htmlReport += "</tr>";
	        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
	        decimalFormatSymbols.setDecimalSeparator('.');
	        decimalFormatSymbols.setGroupingSeparator(',');
	        DecimalFormat decimalFormat = new DecimalFormat("#,##0.000", decimalFormatSymbols);
	        while (iteratorByDataprovider.hasNext()) {
	            Entry<String, StructureRecResult> fieldResult = (Entry<String, StructureRecResult>) iteratorByDataprovider.next();
	            String field = fieldResult.getKey();
	            StructureRecResult fieldResultValues = fieldResult.getValue();
	            htmlReport += "<tr>";
	            htmlReport += "<td>"+field+"</td>";
	            htmlReport += "<td>"+fieldResultValues.getResultDistinctValues()+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultMedian())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultMedianPerVaildSamples())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultSigma())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultDistinctFracComplement())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultCdfl05())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultCdfl075())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultFracOutLower())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultFracOutUpper())+"</td>";
	            htmlReport += "<td>"+decimalFormat.format(fieldResultValues.getResultFracOutWeighted())+"</td>";
	            htmlReport += "</tr>";
	        }
	        htmlReport += "</table>";
	        
			String filename = Qc_graphs.struturendnessDataproviderResultOverview(dataprovider, CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, resultsByDataprovider);
			htmlReport += "<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>";

			filename = Qc_graphs.struturendnessDataproviderResultOverview2(dataprovider, CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, resultsByDataprovider);
			htmlReport += "<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>";
	        
	        iteratorByDataprovider = resultsByDataprovider.entrySet().iterator();
	        while (iteratorByDataprovider.hasNext()) {
	            Entry<String, StructureRecResult> fieldResult = (Entry<String, StructureRecResult>) iteratorByDataprovider.next();
	            String field = fieldResult.getKey();
	            htmlReport += "<h4 id=\""+dataprovider+field+"Header\" class=\"flip\">Field:" + field +"</h4>";
	            htmlReport +="<div id=\""+dataprovider+field+"Panel\" class=\"panel\">";

	            htmlReportJavascript += "$(\"#"+dataprovider+field+"Header\").click(function(){";
	            htmlReportJavascript += "    $(\"#"+dataprovider+field+"Panel\").slideToggle(\"slow\");";
	            htmlReportJavascript +="});";

	            StructureRecResult result = fieldResult.getValue();
	            // write Histogramm for value length
				try {
					File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field+"-value length histogram.csv");
					BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
					
					for (int i = 0; i < result.getLengthHistogram().length; i++) {
						writerStatisticRecords.write( i + STATISTIC_FILE_FIELD_SEPERATOR);
					}
					writerStatisticRecords.newLine();
					for (int i = 0; i < result.getLengthHistogram().length; i++) {
						writerStatisticRecords.write(result.getLengthHistogram()[i] + STATISTIC_FILE_FIELD_SEPERATOR);
					}
					writerStatisticRecords.newLine();
					writerStatisticRecords.close();

					htmlReport +="<h5>Histogram for value length</h5>";
					htmlReport += "<table><tr><td><b>length:</b></td>";
					htmlReport += "<td><b>number:</b></td></tr>";
					
					for (int i = 0; i < result.getLengthHistogram().length; i++) {
						if (result.getLengthHistogram()[i] > 0) {
							htmlReport += "<tr><td>"+i+"</td>";
							htmlReport += "<td>"+result.getLengthHistogram()[i]+"</td></tr>";
						}
					}
					htmlReport += "</table>";
				} catch (Exception e) {
					e.printStackTrace();
				}
				filename = Qc_graphs.struturendnessDataproviderFieldValueLengthHistrogramm(dataprovider, field, CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReport += "<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>";
				
				//////
				
				// write histogram for values
				try {
					
					File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field+"-value histogram.csv");
					BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
					{					
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getKey() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
						writerStatisticRecords.newLine();
				        iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getValue() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
					}
					writerStatisticRecords.newLine();
					writerStatisticRecords.close();

					{					
						htmlReport +="<h5>Histogram for values</h5>";
						htmlReport += "<table><tr><td><b>values:</b></td>";
						htmlReport += "<td><b>number:</b></td></tr>";
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
							htmlReport += "<td>"+pattern.getValue()+"</td></tr>";
				        }
				    	htmlReport += "</table>";
						htmlReport += "<p><a href=\".\\"+OUTPUT_STRUCT_CSV_DIR+fileStatisticRecords.getName()+"\">data as CSV</a></p>";

					}

				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
//				Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field, CHART_WIDTH_MID, CHART_HEIGHT_MID, result);
				filename = Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field, "values", CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReport += "<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>";
				
				//////
				
				
				// write histogram for pattern
				try {
					
					File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field+"-value pattern histogram.csv");
					BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
					{					
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getKey() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
						writerStatisticRecords.newLine();
				        iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getValue() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
					}
					writerStatisticRecords.newLine();
					writerStatisticRecords.close();

					{					
						htmlReport +="<h5>Histogram for pattern</h5>";
						htmlReport += "<table><tr><td><b>pattern:</b></td>";
						htmlReport += "<td><b>number:</b></td></tr>";
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
							htmlReport += "<td>"+pattern.getValue()+"</td></tr>";
				        }
				    	htmlReport += "</table>";
					}
					{					
						htmlReport +="<h5>Histogram for pattern - Sourcen</h5>";
						htmlReport += "<table><tr><td><b>pattern:</b></td>";
						htmlReport += "<td><b>number:</b></td>";
						htmlReport += "<td><b>Source:</b></td></tr>";
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
				        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesPatternSourceHashMap().entrySet().iterator();
				        int helpCount = 0;
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
							htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
							htmlReport += "<td>"+pattern.getValue()+"</td>";
							htmlReport += "<td>";
							
							
				            htmlReport += "<div id=\""+dataprovider+field+"PatternSource"+helpCount+"Header\" class=\"flip\">show</h4>";
				            htmlReport +="<div id=\""+dataprovider+field+"PatternSource"+helpCount+"Panel\" class=\"panel\">";

				            htmlReportJavascript += "$(\"#"+dataprovider+field+"PatternSource"+helpCount+"Header\").click(function(){";
				            htmlReportJavascript += "    $(\"#"+dataprovider+field+"PatternSource"+helpCount+"Panel\").slideToggle(\"slow\");";
				            htmlReportJavascript +="});";

							
							
				            htmlReport += "<ul>";
							ArrayList<PatternSource> sources = patternSource.getValue();
							for (int i = 0; i < sources.size(); i++) {
								htmlReport += "<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>";
							}
							htmlReport += "</ul></div></div></td></tr>";
							helpCount++;
				        }
				    	htmlReport += "</table>";
						htmlReport += "<p><a href=\".\\"+OUTPUT_STRUCT_CSV_DIR+fileStatisticRecords.getName()+"\">data as CSV</a></p>";

					}

				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
//				Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field, CHART_WIDTH_MID, CHART_HEIGHT_MID, result);
				filename = Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field, "pattern", CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReport += "<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>";
				
				
				// write histogram for pattern RegEx
				try {
					
					File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field+"-value pattern regex histogram.csv");
					BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
					{					
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getKey() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
						writerStatisticRecords.newLine();
				        iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getValue() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
					}
					writerStatisticRecords.newLine();
					writerStatisticRecords.close();

					{					
						htmlReport +="<h5>Histogram for pattern(RegEx)</h5>";
						htmlReport += "<table><tr><td><b>pattern:</b></td>";
						htmlReport += "<td><b>number:</b></td></tr>";
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
							htmlReport += "<td>"+pattern.getValue()+"</td></tr>";
				        }
				    	htmlReport += "</table>";
					}
					{					
						htmlReport +="<h5>Histogram for pattern(RegEx) - Sourcen</h5>";
						htmlReport += "<table><tr><td><b>pattern:</b></td>";
						htmlReport += "<td><b>number:</b></td>";
						htmlReport += "<td><b>Source:</b></td></tr>";
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
				        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesPatternRegExSourceHashMap().entrySet().iterator();
				        int helpCount = 0;
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
							htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
							htmlReport += "<td>"+pattern.getValue()+"</td>";
							htmlReport += "<td>";
							
							
				            htmlReport += "<div id=\""+dataprovider+field+"PatternRegExSource"+helpCount+"Header\" class=\"flip\">show</h4>";
				            htmlReport +="<div id=\""+dataprovider+field+"PatternRegExSource"+helpCount+"Panel\" class=\"panel\">";

				            htmlReportJavascript += "$(\"#"+dataprovider+field+"PatternRegExSource"+helpCount+"Header\").click(function(){";
				            htmlReportJavascript += "    $(\"#"+dataprovider+field+"PatternRegExSource"+helpCount+"Panel\").slideToggle(\"slow\");";
				            htmlReportJavascript +="});";

							
							
				            htmlReport += "<ul>";
							ArrayList<PatternSource> sources = patternSource.getValue();
							for (int i = 0; i < sources.size(); i++) {
								htmlReport += "<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>";
							}
							htmlReport += "</ul></div></div></td></tr>";
							helpCount++;
				        }
				    	htmlReport += "</table>";
						htmlReport += "<p><a href=\".\\"+OUTPUT_STRUCT_CSV_DIR+fileStatisticRecords.getName()+"\">data as CSV</a></p>";
					}

				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				filename = Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field,"pattern RegEx", CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReport += "<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>";
				

				
				// write histogram for date pattern
				htmlReport +="<h5>date patterns</h5>";
				htmlReport +="<p>date patterns detected:<b>"+result.getValuesDateformatHashMap().size()+"</b></p>";

				try {
					
					File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field+"-value date pattern histogram.csv");
					BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
					{					
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesDateformatHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getKey() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
						writerStatisticRecords.newLine();
				        iteratorPatternHashMap = result.getValuesDateformatHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				             if ( ! pattern.getKey().isEmpty())
				            	 writerStatisticRecords.write(pattern.getValue() + STATISTIC_FILE_FIELD_SEPERATOR);
				        }
					}
					writerStatisticRecords.newLine();
					writerStatisticRecords.close();
					if (result.getValuesDateformatHashMap().size() > 0) {
						{					
							htmlReport +="<h4>Histogram for date patterns</h4>";
							htmlReport += "<table><tr><td><b>pattern:</b></td>";
							htmlReport += "<td><b>number:</b></td></tr>";
					        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesDateformatHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
								htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
								htmlReport += "<td>"+pattern.getValue()+"</td></tr>";
					        }
					    	htmlReport += "</table>";
						}
						{					
							htmlReport +="<h4>Histogram for date pattern - Sourcen</h4>";
							htmlReport += "<table><tr><td><b>date pattern:</b></td>";
							htmlReport += "<td><b>number:</b></td>";
							htmlReport += "<td><b>Source:</b></td></tr>";
					        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesDateformatHashMap().entrySet().iterator();
					        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesDateformatSourceHashMap().entrySet().iterator();
					        int helpCount = 0;
					        while (iteratorPatternHashMap.hasNext()) {
					            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
								htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
								htmlReport += "<td>"+pattern.getValue()+"</td>";
								htmlReport += "<td>";
								
								
					            htmlReport += "<div id=\""+dataprovider+field+"DateFormatSource"+helpCount+"Header\" class=\"flip\">show</h4>";
					            htmlReport +="<div id=\""+dataprovider+field+"DateFormatSource"+helpCount+"Panel\" class=\"panel\">";
	
					            htmlReportJavascript += "$(\"#"+dataprovider+field+"DateFormatSource"+helpCount+"Header\").click(function(){";
					            htmlReportJavascript += "    $(\"#"+dataprovider+field+"DateFormatSource"+helpCount+"Panel\").slideToggle(\"slow\");";
					            htmlReportJavascript +="});";
	
								
								
					            htmlReport += "<ul>";
								ArrayList<PatternSource> sources = patternSource.getValue();
								for (int i = 0; i < sources.size(); i++) {
									htmlReport += "<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>";
								}
								htmlReport += "</ul></div></div></td></tr>";
								helpCount++;
					        }
					    	htmlReport += "</table>";
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				htmlReport +="<h5>URL patterns</h5>";
				htmlReport +="<p>URL patterns detected:<b>"+result.getValuesUrlformatHashMap().size()+"</b></p>";
				if (result.getValuesUrlformatHashMap().size() > 0)
				{
					{					
						htmlReport +="<h4>Histogram for URL patterns</h4>";
						htmlReport += "<table><tr><td><b>pattern:</b></td>";
						htmlReport += "<td><b>number:</b></td></tr>";
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesUrlformatHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
							htmlReport += "<td>"+pattern.getValue()+"</td></tr>";
				        }
				    	htmlReport += "</table>";
					}
					{					
						htmlReport +="<h4>Histogram for URL pattern - Sourcen</h4>";
						htmlReport += "<table><tr><td><b>URL pattern:</b></td>";
						htmlReport += "<td><b>number:</b></td>";
						htmlReport += "<td><b>Source:</b></td></tr>";
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesUrlformatHashMap().entrySet().iterator();
				        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesUrlformatSourceHashMap().entrySet().iterator();
				        int helpCount = 0;
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
							htmlReport += "<tr><td>"+pattern.getKey()+"</td>";
							htmlReport += "<td>"+pattern.getValue()+"</td>";
							htmlReport += "<td>";
							
							
				            htmlReport += "<div id=\""+dataprovider+field+"UrlFormatSource"+helpCount+"Header\" class=\"flip\">show</h4>";
				            htmlReport +="<div id=\""+dataprovider+field+"UrlFormatSource"+helpCount+"Panel\" class=\"panel\">";

				            htmlReportJavascript += "$(\"#"+dataprovider+field+"UrlFormatSource"+helpCount+"Header\").click(function(){";
				            htmlReportJavascript += "    $(\"#"+dataprovider+field+"UrlFormatSource"+helpCount+"Panel\").slideToggle(\"slow\");";
				            htmlReportJavascript +="});";

							
							
				            htmlReport += "<ul>";
							ArrayList<PatternSource> sources = patternSource.getValue();
							for (int i = 0; i < sources.size(); i++) {
								htmlReport += "<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>";
							}
							htmlReport += "</ul></div></div></td></tr>";
							helpCount++;
				        }
				    	htmlReport += "</table>";
					}
				}
				
				htmlReport += "</div>";


	        }
	        htmlReportJavascript += "});</script>";
	        htmlReport += htmlReportJavascript;
	        htmlReport += "</body></html>";
			try {
				File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "dataquality-report-"+dataprovider+".html");
				BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
				
				writerStatisticRecords.write(htmlReport);
				writerStatisticRecords.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			htmlReportGeneralDataproviders +="<li> <a href=\"dataquality-report-"+dataprovider+".html\">" +           dataprovider +"</a></li>";


	    }
	    htmlReportGeneralDataproviders += "</ul>";
        htmlReportJavascriptGeneral += "});</script>";
        htmlReportGeneral += htmlReportJavascriptGeneral;
        
        htmlReportGeneral += "<h3>Input Data</h3>";
        htmlReportGeneral += "This report was generated using the files located at:<ul>" ;
        for (int i = 0; i < this.inputDirs.size(); i++) {
            htmlReportGeneral += "<li>"+this.inputDirs.get(i)+"</li>" ;
		}
        htmlReportGeneral += "</ul>";
        htmlReportGeneral += "More details on the input data is provided in the <a href=\"dataquality-report-inputdata.html\">Inputdata report</a>";
        
        htmlReportGeneral += "<h3>All datafields</h3>";
        htmlReportGeneral += "<p>The chart shows how many datafields are provided by dataprovider.</p>";
        htmlReportGeneral += "<img src=\"all_datafields_bar_chart_1600x1200.png\" style=\"width:1000px;\"/>"; 
        htmlReportGeneral += "<h3>non empty datafields</h3>";
        htmlReportGeneral += "<p>The chart shows the non empty datafields.</p>";
        htmlReportGeneral += "<img src=\"non_empty_datafields_barchart_1600x1200.png\" style=\"width:1000px;\"/>"; 
        htmlReportGeneral += "<h3>non empty datafields per datafields</h3>";
        htmlReportGeneral += "<p>The chart shows the non empty datafields in respect to the number of datafields.</p>";
        htmlReportGeneral += "<img src=\"non_empty_datafields_perdatafields_barchart_1600x1200.png\" style=\"width:1000px;\"/>";
        htmlReportGeneral += "<h3>links in datafields</h3>";
        htmlReportGeneral += "<p>The chart shows how many links are in the datafields.</p>";
        htmlReportGeneral += "<img src=\"links_barchart_1600x1200.png\" style=\"width:1000px;\"/>";
        htmlReportGeneral += "<h3>links accessible</h3>";
        htmlReportGeneral += "<p>The chart shows how many links are accessible.</p>";
        htmlReportGeneral += "<img src=\"links_accessible_barchart_1600x1200.png\" style=\"width:1000px;\"/>";
        
        htmlReportGeneral += "<h3>Summary</h3>";
        htmlReportGeneral += "<ul>";
        htmlReportGeneral += "<li><a target=\"_blank\" href=\""+DATAQUALITY_REPORT_PLOT_HTML_FILENAME+"\">Data Quality Report with JQPlot </a></li>";
        htmlReportGeneral += "<li><a target=\"_blank\" href=\""+DataQualityVocabularyRDFWriter.STATISTICS_DATAPROVIDER_XML_FILENAME+"\">Data Quality Report (RDF/XML) </a></li>";
        htmlReportGeneral += "</ul>";
        
        htmlReportGeneral += "<h3>Reports for single Dataproviders</h3>";
        htmlReportGeneral += htmlReportGeneralDataproviders;
        
        htmlReportGeneral += "<h3>Stats of the report generation</h3>";
		long timestampEnd = System.currentTimeMillis();
		long timespanMS = timestampEnd - timestampStart;
		double timespanS = timespanMS / 1000;
		double timespanM = timespanS / 60;
        htmlReportGeneral += "<p>Report generation </br>"+
        					"started  at:"+dt.format(new Date(timestampStart))+"</br>" +
        					"finished at:"+dt.format(new Date(System.currentTimeMillis()))+"</br>";
        
        htmlReportGeneral += "Elapsed time for processing: " + (timestampEnd - timestampStart) + "ms. ("+timespanS+"s or "+timespanM+"m)</p>";

        htmlReportGeneral += "</body></html>";
		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "dataquality-report.html");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			
			writerStatisticRecords.write(htmlReportGeneral);
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	}

	private void printInputDataInfoReport(String htmlReportGeneral) {
		
		String htmlReport = htmlReportGeneral;
		htmlReport += "<h2>Inputdata</h2>";
		htmlReport += "This report was generated using the files located at:<ul>" ;
        for (int i = 0; i < this.inputDirs.size(); i++) {
        	htmlReport += "<li>"+this.inputDirs.get(i)+"</li>" ;
		}
        htmlReport += "</ul>";
		htmlReport += "<h3>Statistics by dataprovider</h3>";
		htmlReport += this.htmlReportInputDataStatisticsDataprovider;
		
		htmlReport += "<h3>Statistics by input file</h3>";
		htmlReport += this.htmlReportInputDataStatisticsResults;
		
		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "dataquality-report-inputdata.html");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			
			writerStatisticRecords.write(htmlReport);
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void printRDFXMLVisWithJQPlot() {
        try {
        	TransformerFactory factory = TransformerFactory.newInstance();
        	Templates template = factory.newTemplates(new StreamSource(new FileInputStream("./src/resources/dqv2html.xsl")));
        	Transformer xformer = template.newTransformer();
        	Source source = new StreamSource(new FileInputStream(Qc_dataprovider.outputDir + DataQualityVocabularyRDFWriter.STATISTICS_DATAPROVIDER_XML_FILENAME));
        	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        	Document doc = builder.newDocument();
        	DOMResult result = new DOMResult(doc);
        	xformer.transform(source, result);
        	
        			
			File filePlot = new File(Qc_dataprovider.outputDir+ DATAQUALITY_REPORT_PLOT_HTML_FILENAME);
			BufferedWriter writerPlot = new BufferedWriter(new FileWriter(filePlot));
			
			writerPlot.write(getStringFromDocument((Document)result.getNode()));
			writerPlot.close();

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void copyResources() {
		copyResourcesCSS();
	    copyResourcesJQplot();
	    copyResourcesInputXML();
	}

	private void copyResourcesCSS() {
		InputStream inStream = null;
		OutputStream outStream = null;
	    try{
	      try {
	    	  File file = new File("./resources/report.css");
	    	  inStream = new FileInputStream(file); 
	        byte[] bucket = new byte[32*1024];
	        outStream = new BufferedOutputStream(new FileOutputStream(Qc_dataprovider.outputDir + "report.css"));
	        int bytesRead = 0;
	        while(bytesRead != -1){
	          bytesRead = inStream.read(bucket); //-1, 0, or more
	          if(bytesRead > 0){
	            outStream.write(bucket, 0, bytesRead);
	          }
	        }
	      }
	      finally {
	        if (inStream != null) inStream.close();
	        if (outStream != null) outStream.close();
	      }
	    }
	    catch (FileNotFoundException ex){
	      System.out.println("File not found: " + ex);
	    }
	    catch (IOException ex){
	    	System.out.println(ex);
	    }
	}
	
	private void copyResourcesJQplot() {
	    try{
	    	File destDir = new File(Qc_dataprovider.outputDir + "jqplot/");
            File srcDir = new File("./resources/jqplot");
	    	FileUtils.copyDirectory(srcDir, destDir);
	    }
	    catch (FileNotFoundException ex){
	      System.out.println("File not found: " + ex);
	    }
	    catch (IOException ex){
	    	System.out.println(ex);
	    }
	}
	
	private void copyResourcesInputXML() {
	    try{
	    	File destDir = new File(Qc_dataprovider.outputDir + "input/");
            File srcDir = new File(this.inputDirs.get(0));
	    	FileUtils.copyDirectory(srcDir, destDir);
	    }
	    catch (FileNotFoundException ex){
	      System.out.println("File not found: " + ex);
	    }
	    catch (IOException ex){
	    	System.out.println(ex);
	    }
	}
	
	public static String getStringFromDocument(Document doc)
    {
        try
        {
           DOMSource domSource = new DOMSource(doc);
           StringWriter writer = new StringWriter();
           StreamResult result = new StreamResult(writer);
           TransformerFactory tf = TransformerFactory.newInstance();
           javax.xml.transform.Transformer transformer = tf.newTransformer();
           transformer.transform(domSource, result);
           return writer.toString();
        }
        catch(TransformerException ex)
        {
           ex.printStackTrace();
           return null;
        }
    }

}
