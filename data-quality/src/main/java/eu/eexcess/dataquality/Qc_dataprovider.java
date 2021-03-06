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
import java.util.StringTokenizer;

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
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.rank.Median;
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
import eu.eexcess.dataquality.structure.StructureRecResultAnalysisResultData;
import eu.eexcess.dataquality.structure.StructureRecognizer;
import eu.eexcess.dataquality.structure.ValueSource;
import eu.eexcess.dataquality.wordnet.WordNetSimilarity;
import eu.eexcess.dataquality.wordnet.WordNetSimilarityResultObject;
import eu.eexcess.dataquality.wordnet.WordNetSimilarityResultProxyObject;

// Check for data provider
public class Qc_dataprovider {

	private static final String SIMILARTY_DISTANCE_REPORT_LABEL = "distance(enriched - original)[+ means increased similarty/- decreased similarty]";
	private static final String SIMILARTY_ORGINAL_REPORT_LABEL = "similarity score of original meta data[0(different)..1(the same)]";
	private static final String SIMILARTY_ENRICHED_REPORT_LABEL = "similarity score of original and enriched meta data[0(different)..1(the same)]";
	
	public static final String OUTPUT_STRUCT = "struct";
	public static final String OUTPUT_STRUCT_IMG = "img";
	public static final String OUTPUT_STRUCT_IMG_DIR = OUTPUT_STRUCT + "/" + OUTPUT_STRUCT_IMG + "/";
	public static final String OUTPUT_IMG = "img";
	public static final String OUTPUT_IMG_DIR = OUTPUT_IMG + "/";
	public static final String OUTPUT_STRUCT_CSV = "csv";
	public static final String OUTPUT_STRUCT_CSV_DIR = OUTPUT_STRUCT + "/" + OUTPUT_STRUCT_CSV + "/";
	
	private static final String DATAQUALITY_REPORT_PLOT_HTML_FILENAME = "dataquality-report-plot.html";
	private static final String STATISTIC_FILE_FIELD_SEPERATOR = ";";
//	private static final String STATISTIC_SYSTEMOUT_FIELD_SEPERATOR = "\t";
	private static final int CHART_WIDTH_HIGH = 1600;
	private static final int CHART_HEIGHT_HIGH = 1200;
	private static final int CHART_WIDTH_MID = 800;
	private static final int CHART_HEIGHT_MID = 600;
	private static final String DATAQUALITY_REPORT_ENRICHMENT_HTML_FILENAME = "dataquality-report-enrichment.html";
//	private static final int CHART_WIDTH_LOW = 400;
//	private static final int CHART_HEIGHT_LOW = 350;
	Qc_base currentProvider = null;
	Qc_paramDataList paramDataList = new Qc_paramDataList();
	
	public static HashMap<String,Boolean> URLCheckingResultsAccessible = new HashMap<String,Boolean>();
	
	public static HashMap<String,Boolean> URLCheckingResultsHostsBlacklist = new HashMap<String,Boolean>();

	public static String outputDir ="./output/";
	
	public static String resourcesDir ="./resources/";

	private boolean copyInput = true;
	
	public static long countCheckedURLs = 0;
	public static long countCheckedURLsOnline = 0;
	public static long countCheckedURLsViaCache = 0;
	public static long countCheckedURLsViaBlackList = 0;
	
	public boolean writeHistogrammCSV = true;
	
	HashMap<String,HashMap<String, StructureRecResult>> structurednessResults = new HashMap<String, HashMap<String, StructureRecResult>>();
	HashMap<String,HashMap<String,WordNetSimilarityResultObject>> similarityResults = new HashMap<String,HashMap<String, WordNetSimilarityResultObject>>();
	HashMap<String,EnrichmentResultDataprovider> enrichmenResults= new HashMap<String,EnrichmentResultDataprovider>();

	NumberFormat numberFormater = null;
	NumberFormat numberFormaterXML = null;

	public Qc_dataprovider()
	{
		numberFormater = NumberFormat.getNumberInstance( new Locale.Builder().setLanguage("en").setRegion("GB").build());
		numberFormaterXML = NumberFormat.getNumberInstance( new Locale.Builder().setLanguage("en").setRegion("GB").build());
		numberFormaterXML.setGroupingUsed(false);
	}
	
	// XML files of known partners
	public enum DataProvider {
		KIMCollect, KIMCollect_EEXCESS, KIMCollect_enriched,
		ZBW, ZBW_EEXCESS, ZBW_enriched,
		Wissenmedia, Wissenmedia_EEXCESS, Wissenmedia_enriched,
		Mendeley, Mendeley_EEXCESS, Mendeley_enriched,
		DDB, DDB_EEXCESS, DDB_enriched,
		cultureWeb, cultureWeb_EEXCESS, cultureWeb_enriched,
		Europeana, Europeana_EEXCESS, Europeana_enriched,
		unknown
	}

	protected String cmdParameterXpathRecordSeperator = "";
	protected String cmdParameterXpathFieldsToRecordSeperator = "";
	public static String cmdParameterDataprovider = "";
	
	public static boolean checkLinksOnline = true;

	public void process(String[] sParams) {
		timestampStart = System.currentTimeMillis();
		timestampLastTime = System.currentTimeMillis();
		inputDirs = new ArrayList<String>();
		for (int i = 0; i < sParams.length; i++) {
			if (sParams[i].equalsIgnoreCase(DataQualityApp.CMD_PARAM_DONT_COPY_INPUT))
			{
				this.copyInput = false;
			} else {
				if (sParams[i].toLowerCase().startsWith(DataQualityApp.CMD_PARAM_XPATH_RECORD_SEPERATOR.toLowerCase()))
				{
					this.cmdParameterXpathRecordSeperator = sParams[i].substring(DataQualityApp.CMD_PARAM_XPATH_RECORD_SEPERATOR.length());;
				} else {
					if (sParams[i].toLowerCase().startsWith(DataQualityApp.CMD_PARAM_XPATH_FIELDS_TO_RECORD_SEPERATOR.toLowerCase()))
					{
						this.cmdParameterXpathFieldsToRecordSeperator = sParams[i].substring(DataQualityApp.CMD_PARAM_XPATH_FIELDS_TO_RECORD_SEPERATOR.length());;
					} else {
						if (sParams[i].toLowerCase().startsWith(DataQualityApp.CMD_PARAM_DATAPROVIDER.toLowerCase()))
						{
							cmdParameterDataprovider = sParams[i].substring(DataQualityApp.CMD_PARAM_DATAPROVIDER.length());;
						} else {
							if (sParams[i].toLowerCase().startsWith(DataQualityApp.CMD_PARAM_OUTPUTDIR.toLowerCase()))
							{
								this.outputDir = sParams[i].substring(DataQualityApp.CMD_PARAM_OUTPUTDIR.length());;
							} else {
								if (sParams[i].toLowerCase().startsWith(DataQualityApp.CMD_PARAM_RESOURCESDIR.toLowerCase()))
								{
									this.resourcesDir = sParams[i].substring(DataQualityApp.CMD_PARAM_RESOURCESDIR.length());;
								} else {

								}	
							}
						}
					}
				}
			}
		}
		//exists output dir?
		File outputDir = new File(this.outputDir);
		if (!outputDir.exists()) {
			outputDir.mkdir();
		}
		
		for (int i = 0; i < sParams.length; i++) {
			if (sParams[i].equalsIgnoreCase(DataQualityApp.CMD_PARAM_DONT_COPY_INPUT) || 
					sParams[i].toLowerCase().startsWith(DataQualityApp.CMD_PARAM_XPATH_RECORD_SEPERATOR.toLowerCase()) ||
					sParams[i].toLowerCase().startsWith(DataQualityApp.CMD_PARAM_XPATH_FIELDS_TO_RECORD_SEPERATOR.toLowerCase())
				)
			{
			} else {
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
		}
		
		// System.out.println("size of HashMap:"+Qc_dataprovider.URLCheckingResultsAccessible.size());
		// System.out.println("countCheckedURLs:"+Qc_dataprovider.countCheckedURLs);
		// System.out.println("countCheckedURLsOnline:"+Qc_dataprovider.countCheckedURLsOnline);
		// System.out.println("countCheckedURLsViaCache:"+Qc_dataprovider.countCheckedURLsViaCache);
		// System.out.println("countCheckedURLsViaBlackList:"+Qc_dataprovider.countCheckedURLsViaBlackList);
		// System.out.println("size of Blacklist for Hosts:"+Qc_dataprovider.URLCheckingResultsHostsBlacklist.size());
		Qc_dataprovider.URLCheckingResultsAccessible.clear();
		Qc_dataprovider.URLCheckingResultsHostsBlacklist.clear();
		System.gc();
		printDebugTime("checking");
		// check enrichment
		checkEnrichment();
		printDebugTime("checkEnrichment");
		// check structuredness
		checkStructuredness(sParams);
		printDebugTime("checkStructuredness");
		// output results
		copyResources();
		printDebugTime("copyResources");
		printStatisticsCharts();
		printDebugTime("printStatisticsCharts");
		printRDFXMLVisWithJQPlot();
		printDebugTime("printRDFXMLVisWithJQPlot");
		printReports();
		printDebugTime("printReports");

	}

	private void printDebugTime(String info) {
		{
			long timestampAct = System.currentTimeMillis();
			long timespanMS = timestampAct - timestampStart;
			double timespanS = timespanMS / 1000;
			double timespanM = timespanS / 60;
			long timespanSinceLastTimeMS = timestampAct - timestampLastTime;
			double timespanSinceLastTimeS = timespanSinceLastTimeMS / 1000;
			double timespanSinceLastTimeM = timespanSinceLastTimeS / 60;
			System.out.println("\nElapsed time for processing(finished "+info+"):"+
					"\n\t\t" + (timespanSinceLastTimeMS) + "ms. \t("+timespanSinceLastTimeS+"s or "+timespanSinceLastTimeM+"m) for "+info+
					"\n\t\t" + (timespanMS) + "ms. \t("+timespanS+"s or "+timespanM+"m)"
					);
			this.timestampLastTime = timestampAct;
		}
	}
	
	CheckEnrichment enrichment = null;
	
	// check enrichment
	@SuppressWarnings("incomplete-switch")
	private void checkEnrichment()
	{
		enrichment = new CheckEnrichment();
		enrichment.CalcEnrichment(paramDataList, CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, null);
		for (DataProvider provider : DataProvider.values())
		{
			switch (provider)
			{
				case ZBW:
				case Wissenmedia:
				case Mendeley:
				case DDB:
				case cultureWeb:
				case Europeana:
				case KIMCollect:
					enrichment.CalcEnrichment(paramDataList, CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH,provider);
					break;
			}
		}
	}


	private String formatNumber(double number) {
		if (Double.isNaN(number))
			return "";
		return numberFormater.format(number);
	}
	

	private String formatNumberXML(double number) {
		return numberFormaterXML.format(number);
	}
	
	private void printStatisticsCharts() {
		htmlReportInputDataStatisticsResults = new StringBuffer("<table>");
		
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
//			System.out.println("file"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"provider"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"#records"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"mean fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"min fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"max fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//					"mean non empty fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//					"mean non empty fields per datafields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//					"mean empty fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//					"mean empty fields per datafields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//uriCheck
//					"mean links/record");
//					"links/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//					"accessible links/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//					"#links"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
//					"#accessible links");					
			htmlReportInputDataStatisticsResults.append("<tr><th>file</th><th>provider</th><th>#records</th><th>mean fields/record</th><th>min fields/record</th>"+
					"<th>max fields/record</th><th>mean non empty fields/record</th><th>mean non empty fields per data fields/record</th><th>mean empty fields/record</th>"+
					"<th>mean empty fields per data fields/record</th><th>links/record</th><th>accessible links/record</th><th>#links</th><th>#accessible links</th></tr>");
			
			for (int i = 0; i < paramDataList.size(); i++) {
				Qc_params param = paramDataList.get(i);
//				System.out.println(param.getXmlFileName() + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ param.getProvider().toString() + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ param.getRecordCount() + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getMinDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getMaxDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getNonEmptyDataFieldsPerRecord()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getNonEmptyDataFieldsPerRecordPerDatafields()) + STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getEmptyDataFieldsPerRecord())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getEmptyDataFieldsPerRecordPerDatafields())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
////uriCheck						
////						+ formatNumber(param.getLinkDataFieldsPerRecord()));
//						+ formatNumber(param.getLinkDataFieldsPerRecord())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getAccessibleLinksDataFieldsPerRecord())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getNumberOfAllLinkDataFields())+ STATISTIC_SYSTEMOUT_FIELD_SEPERATOR
//						+ formatNumber(param.getNumberOfAllAccessibleLinks()));		
				htmlReportInputDataStatisticsResults.append("<tr><td><a href=\".\\input\\"+param.getXmlFileName()+"\">" + param.getXmlFileName() + "</a></td><td>");
				
				
				if (param.getProvider() == DataProvider.unknown)
					htmlReportInputDataStatisticsResults.append(Qc_dataprovider.cmdParameterDataprovider);
				else 
					htmlReportInputDataStatisticsResults.append(param.getProvider().toString());
				htmlReportInputDataStatisticsResults.append("</td><td>"
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
								+ "</td></tr>");
						
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
		htmlReportInputDataStatisticsResults.append("</table><p><a href=\".\\statistics-results.csv\">data as CSV</a></p>");
		htmlReportInputDataStatisticsDataprovider = new StringBuffer("<table>");

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
			htmlReportInputDataStatisticsDataprovider.append("<tr><th>"+"provider"+"</th><th>"+"#records"+"</th><th>"+"mean fields/record"+"</th><th>"+"min fields/record"+"</th><th>"+"max fields/record"+"</th><th>"+
					"mean non empty fields/record"+"</th><th>"+
					"mean non empty fields per data fields/record"+"</th><th>"+
					"mean empty fields/record"+"</th><th>"+
					"mean empty fields per data fields/record"+"</th><th>"+
					//uriCheck
					//"mean links/record");
					"mean links/record"+"</th><th>"+
					"mean accessible links/record"+"</th><th>"+
					"#links"+"</th><th>"+
					"#accessible links</th></tr>");
			
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
				htmlReportInputDataStatisticsDataprovider.append("<tr><td>");
				if (DataProvider.values()[i] == DataProvider.unknown && !Qc_dataprovider.cmdParameterDataprovider.isEmpty())
					htmlReportInputDataStatisticsDataprovider.append(Qc_dataprovider.cmdParameterDataprovider);
				else 
					htmlReportInputDataStatisticsDataprovider.append(DataProvider.values()[i].toString());
				htmlReportInputDataStatisticsDataprovider.append("</td><td>"
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
						+ "</td></tr>");

			}
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		htmlReportInputDataStatisticsDataprovider.append("</table><p><a href=\".\\statistics-dataprovider.csv\">data as CSV</a></p>");

		DataQualityVocabularyRDFWriter dQVRDFWriter = new DataQualityVocabularyRDFWriter();
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramDataList.getRecordsPerProvider(DataProvider.values()[i]) == 0 ) {
				continue;
			}
			dQVRDFWriter.addQualityMeasure_numberOfRecords(DataProvider.values()[i].toString(), formatNumberXML(paramDataList.getRecordsPerProvider(DataProvider.values()[i])));
			dQVRDFWriter.addQualityMeasure_meanFieldsPerRecord(DataProvider.values()[i].toString(), formatNumberXML(paramDataList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
			dQVRDFWriter.addQualityMeasure_minFieldsPerRecord(DataProvider.values()[i].toString(), ""+paramDataList.getMinDataFieldsPerRecordsPerProvider(DataProvider.values()[i]));
			dQVRDFWriter.addQualityMeasure_maxFieldsPerRecord(DataProvider.values()[i].toString(), ""+paramDataList.getMaxDataFieldsPerRecordsPerProvider(DataProvider.values()[i]));
			
			dQVRDFWriter.addQualityMeasure_meanNonEmptyFieldsPerRecord(DataProvider.values()[i].toString(), formatNumberXML(paramDataList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
			dQVRDFWriter.addQualityMeasure_meanNonEmptyFieldsPerDatafieldsPerRecord(DataProvider.values()[i].toString(), formatNumberXML(paramDataList.getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i])));
			
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
					
				case cultureWeb_enriched:
				case KIMCollect_enriched:
				case Wissenmedia_enriched:
				case Mendeley_enriched:
				case Europeana_enriched:
				case DDB_enriched:
				case ZBW_enriched:
					currentProvider = new Qc_eexcess_enriched();
					break;

				case cultureWeb_EEXCESS:
				case KIMCollect_EEXCESS:
				case Wissenmedia_EEXCESS:
				case Mendeley_EEXCESS:
				case Europeana_EEXCESS:
				case DDB_EEXCESS:
				case ZBW_EEXCESS:
					currentProvider = new Qc_eexcess();
					break;

				case ZBW:
					currentProvider = new Qc_ZBW();
					break;

				default:
					currentProvider = new Qc_base();
					currentProvider.setRecordSeparator(this.cmdParameterXpathRecordSeperator);
					currentProvider.setXpathsToFieldsFromRecordSeparator(this.cmdParameterXpathFieldsToRecordSeperator);
					break;
				}

				if (currentProvider != null) {
					currentProvider.setXmlFileName(xmlFile);
					if (currentProvider.isProviderRecord() == true) {
						currentProvider.getDataProvider();
						
						// System.out.println(currentProvider + " " + currentProvider.getDataProvider().name());
						
						currentProvider.countDataFields(SearchType.allDataFields);
						currentProvider.countDataFields(SearchType.notEmptyDataFields);
						currentProvider.countDataFields(SearchType.linkDataFields);
						//uriCheck
						currentProvider.countDataFields(SearchType.uriDataFields);
						Qc_params param = currentProvider.getParam();
						paramDataList.addParam(param);
						
						// System.out.println(currentProvider + " " + param.provider.name());
						
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
			if (actFileName.lastIndexOf("\\")+40 < actFileName.length()) {
				String temp = actFileName.substring(actFileName.lastIndexOf("\\")+40);
				temp = temp.substring(0,temp.indexOf("-"));
				if (!dataproviders.contains(temp))
					dataproviders.add(temp);
			} else {
				dataproviders.add(DATAPROVIDER_UNKNOWN);
			}
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
		HashMap<String, ArrayList<String>> filesNameByDataproviderTransformedHashMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> filesNameByDataproviderEnrichedHashMap = new HashMap<String, ArrayList<String>>();
		for (Iterator<String> iteratorDataprovider = dataproviders.iterator(); iteratorDataprovider.hasNext();) {
			String actDataProvider = (String) iteratorDataprovider.next();
			ArrayList<String> fileNameByDataProvider = new ArrayList<String>();
			ArrayList<String> fileNameByDataProviderTransformed = new ArrayList<String>();
			ArrayList<String> fileNameByDataProviderEnriched = new ArrayList<String>();
			for (Iterator<String> iteratorFileNames = fileNames.iterator(); iteratorFileNames.hasNext();) {
				String actFileName = (String) iteratorFileNames.next();
				if (actDataProvider.equals(DATAPROVIDER_UNKNOWN)) {
						fileNameByDataProvider.add(actFileName);
				} else {
					if (!actFileName.contains("enrichment") && !actFileName.contains("done-transform")) {
						if (actFileName.contains(actDataProvider))
							fileNameByDataProvider.add(actFileName);
					} else {
						if (actFileName.contains("done-transform")) {
							if (actFileName.contains(actDataProvider))
								fileNameByDataProviderTransformed.add(actFileName);
						}else {
							if (actFileName.contains("enrichment")) {
								if (actFileName.contains(actDataProvider))
									fileNameByDataProviderEnriched.add(actFileName);
							}
						}
					}
				}
			}
			filesNameByDataproviderHashMap.put(actDataProvider, fileNameByDataProvider);
			filesNameByDataproviderTransformedHashMap.put(actDataProvider, fileNameByDataProviderTransformed);
			filesNameByDataproviderEnrichedHashMap.put(actDataProvider, fileNameByDataProviderEnriched);
/*
			System.out.println("files for dataprovider:" + actDataProvider);
			for (Iterator<String> iterator = fileNameByDataProvider.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				System.out.println(string);
			}
*/
		}
		///////////////////////////////
		//
		// process Service-Responses 
		//
		///////////////////////////////
		Iterator<Entry<String, ArrayList<String>>> filesNameByDataproviderHashMapIterator = filesNameByDataproviderHashMap.entrySet().iterator();
	    while (filesNameByDataproviderHashMapIterator.hasNext()) {
	        Entry<String, ArrayList<String>> entry = filesNameByDataproviderHashMapIterator.next();
	        String actDataprovider = entry.getKey();
	        ArrayList<String> actProviderFileList = entry.getValue();
			Qc_base qcBase = createProviderQC(actDataprovider);
			if (actDataprovider.equalsIgnoreCase(DATAPROVIDER_KIMPORTAL))
				System.out.println("dataprovider:"+actDataprovider+" "+qcBase.getRecordSeparator());
//			System.out.println("dataprovider:"+actDataprovider+" "+qcBase.getRecordSeparator());
			
			// calc all XPaths for all fields in all file from a data provider
			ArrayList<String> fieldXPaths = new ArrayList<String>();
			if (qcBase != null)
			{
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
			}
			// now we have a list of all XPaths to fields for this data provider
			HashMap<String, StructureRecResult> actProviderStructurednessResults = new HashMap<String, StructureRecResult>();
			for (int i = 0; i < fieldXPaths.size(); i++) {
				// for earch field
				String tempXPath = fieldXPaths.get(i);
				String actFieldName = tempXPath.substring(fieldXPaths.get(i).lastIndexOf("/")+1);
				if (actDataprovider == DATAPROVIDER_UNKNOWN){
					if (actFieldName.contains(":")) {
						tempXPath = tempXPath.replace(actFieldName, "*[local-name()='"+actFieldName.substring(actFieldName.indexOf(":")+1)+"']");
					}
					StringTokenizer tokenizer = new StringTokenizer(tempXPath,"/");
					String newXpath = "";
					while (tokenizer.hasMoreElements()) {
						String tempToken = tokenizer.nextToken();
						if (tempToken.contains(":"))
							newXpath += "/*[local-name()='" + tempToken.substring(tempToken.indexOf(":")+1)+"']";
						else 
							newXpath += "/" +  tempToken;
					}
					tempXPath = newXpath;
				}
				ArrayList<ValueSource> values = new ArrayList<ValueSource>();
				//System.out.println(fieldXPaths.get(i));
				for (String actFileName : actProviderFileList) {
					// for each file
					qcBase.setXmlFileName(actFileName);
					NodeList nodes = qcBase.getNodesListByXPath(tempXPath);
					for (int count = 0; count < nodes.getLength(); count++) {
						if (nodes.item(count).getNodeType() == Node.ELEMENT_NODE) {
							//System.out.println(nodes.item(count).getTextContent());
							values.add(new ValueSource(nodes.item(count).getTextContent(),new File(actFileName).getName()));
						}
					}
				}
				StructureRecognizer recognizer = new StructureRecognizer();
				//System.out.println(actDataprovider + ": structure - analyse field:" + actFieldName + " \nwith XPATH:" +tempXPath);
				actProviderStructurednessResults.put(actFieldName, recognizer.analyse(values ));
			}

			
			this.structurednessResults.put(actDataprovider, actProviderStructurednessResults);

	    }
		///////////////////////////////
		//
		// process Enriched 
		//
		///////////////////////////////
	    
		Iterator<Entry<String, ArrayList<String>>> filesNameByDataproviderEnrichedHashMapIterator = filesNameByDataproviderEnrichedHashMap.entrySet().iterator();
	    while (filesNameByDataproviderEnrichedHashMapIterator.hasNext()) {
	        Entry<String, ArrayList<String>> entry = filesNameByDataproviderEnrichedHashMapIterator.next();
	        String actDataprovider = entry.getKey();
	        ArrayList<String> actProviderFileList = entry.getValue();
			Qc_base qcBase = new Qc_eexcess_enriched();
//			System.out.println("dataprovider:"+actDataprovider+" "+qcBase.getRecordSeparator());
			
			// calc all XPaths for all fields in all file from a data provider
			ArrayList<String> fieldXPathsProxy = new ArrayList<String>();
			ArrayList<String> fieldXPathsEnrichedProxy = new ArrayList<String>();
			fieldXPathsProxy.add(		 "/*[local-name()='RDF']/*[local-name()='Proxy'][contains(@about,'/proxy/')]/*[local-name()='title']");
//			fieldXPathsProxy.add(		 "/*[local-name()='RDF']/*[local-name()='Proxy'][contains(@about,'/proxy/')]/*[local-name()='description']");
			fieldXPathsProxy.add(		 "/*[local-name()='RDF']/*[local-name()='Proxy'][contains(@about,'/proxy/')]/*[local-name()='creator']");
			
			fieldXPathsEnrichedProxy.add("/*[local-name()='RDF']/*[local-name()='Proxy'][contains(@about,'/enrichedProxy/')]/*[local-name()='subject']/*/*[local-name()='label']");

			HashMap<String, WordNetSimilarityResultObject> mySimilarityResultHashMap = new HashMap<String, WordNetSimilarityResultObject>();
		    EnrichmentResultDataprovider enrichmentResultDataprovider = new EnrichmentResultDataprovider();

			if (qcBase != null)
			{
				for (String actFileName : actProviderFileList) {
			    	enrichmentResultDataprovider.increaseRecordsEnriched();
//					System.out.println("//////////////////////////////////////////////////////\n"+actFileName);
					qcBase.setXmlFileName(actFileName);
					ArrayList<String> valuesProxy = new ArrayList<String>();
					ArrayList<String> valuesEnrichedProxy = new ArrayList<String>();
//					System.out.println("fieldXPathsProxy:");
					for (int i = 0; i < fieldXPathsProxy.size(); i++) {
						String xpath = 	fieldXPathsProxy.get(i);
//						System.out.println("xPath:"+xpath);
						NodeList nodes = qcBase.getNodesListByXPath(xpath);
						for (int count = 0; count < nodes.getLength(); count++) {
							if (nodes.item(count).getNodeType() == Node.ELEMENT_NODE) {
								String temp = nodes.item(count).getTextContent().trim();
//								System.out.println(temp);
								valuesProxy.add(temp);
							}
						}
//						System.out.println("-------------------------------------------------------");
					}
//					System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//					System.out.println("fieldXPathsEnrichedProxy:");
					for (int i = 0; i < fieldXPathsEnrichedProxy.size(); i++) {
						String xpath = 	fieldXPathsEnrichedProxy.get(i);
//						System.out.println("xPath:"+xpath);
						NodeList nodes = qcBase.getNodesListByXPath(xpath);
						for (int count = 0; count < nodes.getLength(); count++) {
							if (nodes.item(count).getNodeType() == Node.ELEMENT_NODE) {
								String temp = nodes.item(count).getTextContent().trim();
//								System.out.println(temp);
								valuesEnrichedProxy.add(temp);
							}
						}
//						System.out.println("-------------------------------------------------------");
					}
//					for (int i = 0; i < values.size(); i++) {
//						System.out.println(values.get(i));
//					}
					if (valuesEnrichedProxy.size() > 0 ) {
						enrichmentResultDataprovider.increaseRecordsEnrichedWithEnrichedMetadata();
						valuesEnrichedProxy.addAll(valuesProxy);
						if (valuesProxy.size() > 0 ) {
							WordNetSimilarity wordnetSimilarity = new WordNetSimilarity();
							String valuesProxyString ="";
							for (int i = 0; i < valuesProxy.size(); i++) {
								valuesProxyString += valuesProxy.get(i)+" ";
							}
							WordNetSimilarityResultProxyObject valueProxy = wordnetSimilarity.compute(valuesProxyString);
							String valuesEnrichedProxyString ="";
							for (int i = 0; i < valuesEnrichedProxy.size(); i++) {
								valuesEnrichedProxyString += valuesEnrichedProxy.get(i)+" ";
							}
							WordNetSimilarityResultProxyObject valueEnrichedProxy = wordnetSimilarity.compute(valuesEnrichedProxyString);
							WordNetSimilarityResultObject mySimilarityResult = new WordNetSimilarityResultObject(valueProxy, valueEnrichedProxy,actFileName);
							mySimilarityResultHashMap.put(actFileName, mySimilarityResult);
						}
					}

				}
			}
			this.similarityResults.put(actDataprovider, mySimilarityResultHashMap);
			this.enrichmenResults.put(actDataprovider, enrichmentResultDataprovider);
	    }
	}
	
	public static String DATAPROVIDER_CULTUREWEB ="KIMCollect";
	public static String DATAPROVIDER_KIMPORTAL ="KIMPortal";
	public static String DATAPROVIDER_EUROPEANA ="Europeana";
	public static String DATAPROVIDER_DDB ="DeutscheDigitaleBibliothek";
	public static String DATAPROVIDER_D_D_B ="Deutsche Digitale Bibliothek";

	public static String DATAPROVIDER_MENDELEY ="Mendeley";
	public static String DATAPROVIDER_WISSENMEDIA ="Wissenmedia";
	public static String DATAPROVIDER_ZBW ="ZBW";
	public static String DATAPROVIDER_UNKNOWN ="unknown";
	
	protected StringBuffer htmlReportInputDataStatisticsResults;
	protected StringBuffer htmlReportInputDataStatisticsDataprovider;
	protected ArrayList<String> inputDirs;
	protected long timestampStart;
	protected long timestampLastTime;
	
	public Qc_base createProviderQC(String dataprovider){
		if (dataprovider.equals(DATAPROVIDER_WISSENMEDIA))
			return new Qc_wissenmedia();
		if (dataprovider.equals(DATAPROVIDER_DDB) || dataprovider.equals(DATAPROVIDER_D_D_B))
			return new Qc_DDB();
		if (dataprovider.equals(DATAPROVIDER_EUROPEANA))
			return new Qc_europeana();
		if (dataprovider.equals(DATAPROVIDER_CULTUREWEB))
			return new Qc_cultureWeb();
		if (dataprovider.equals(DATAPROVIDER_KIMPORTAL))
			return new Qc_kimcollect();
		if (dataprovider.equals(DATAPROVIDER_MENDELEY))
			return new Qc_mendeley();
		if (dataprovider.equals(DATAPROVIDER_ZBW))
			return new Qc_ZBW();
		if (dataprovider.equals(DATAPROVIDER_WISSENMEDIA))
			return new Qc_wissenmedia();
		if (dataprovider.equals(DATAPROVIDER_UNKNOWN)) {
			Qc_base ret = new Qc_base();
			ret.setRecordSeparator(this.cmdParameterXpathRecordSeperator);
			ret.setXpathsToFieldsFromRecordSeparator(this.cmdParameterXpathFieldsToRecordSeperator);
			return ret;
		}
		return null;
	}
	
	private void printReports() {
		new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_IMG_DIR).mkdirs();
		new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR).mkdirs();
		new File(Qc_dataprovider.outputDir+OUTPUT_IMG_DIR).mkdirs();
		
		String htmlReportJavascriptGeneral = new String();
		htmlReportJavascriptGeneral += "<script>$(document).ready(function(){";
		
		String htmlReportGeneral = "<html xmlns:prov=\"http://www.w3.org/ns/prov#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:daq=\"http://purl.org/eis/vocab/daq#\" xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:dqv=\"http://www.w3.org/ns/dqv#\" xmlns:eexdaq=\"http://eexcess.eu/ns/dataquality/daq/\" lang=\"en\">";
		htmlReportGeneral += " <head><title>EEXCESS Data Quality Report</title>";
		htmlReportGeneral += "<link rel=\"icon\" href=\"./eexcess.ico\" type=\"image/x-icon\" />";
		htmlReportGeneral += "<link rel=\"stylesheet\" type=\"text/css\" href=\"./report.css\">";
		htmlReportGeneral += "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>";
		
		
		htmlReportGeneral += "<link rel=\"stylesheet\" type=\"text/css\" href=\"datatables.min.css\"/>";
		htmlReportGeneral += "<script type=\"text/javascript\" src=\"datatables.min.js\"></script>";
		
		htmlReportGeneral += " </head>";
		htmlReportGeneral += " <body>";
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		htmlReportGeneral += "<h1><img src=\"./eexcess_Logo.jpg\"> Data Quality Report</h1><br/><h2>generated at: " +dt.format(new Date(System.currentTimeMillis()))+"</h2>";
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
	    	StringBuffer htmlReport =  new StringBuffer(htmlReportGeneral);
	        Entry<String, HashMap<String, StructureRecResult>> entry = iteratorDataprovider.next();
	        String dataprovider = entry.getKey();
	        HashMap<String, StructureRecResult> resultsByDataprovider = entry.getValue();
            // System.out.println("Dataprovider:" + dataprovider);
	        if (dataprovider.equals(DATAPROVIDER_UNKNOWN))
	        	dataprovider = this.cmdParameterDataprovider;
	        htmlReport.append("<h3>" + dataprovider +"</h3>");
            {
        		htmlReport.append("<h4>Overview (using RegEx Pattners)</h4>");
        		printStructureOverview(htmlReport, dataprovider,resultsByDataprovider,StructureRecResult.StructureResultTyp.REGEX );
        		htmlReport.append("<h4>Overview (using Values)</h4>");
        		printStructureOverview(htmlReport, dataprovider,resultsByDataprovider,StructureRecResult.StructureResultTyp.VALUE );
            }
            
			if ( dataprovider.equals(DATAPROVIDER_DDB) || dataprovider.equals(DATAPROVIDER_D_D_B))
			{
				htmlReport.append("<img src=\"./enrichment-" + DataProvider.DDB.toString() + "-"+CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH + ".png\" style=\"width:1000px;\">") ;
				htmlReport.append("<img src=\"./enrichment-link-" + DataProvider.DDB.toString() + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">") ;
				htmlReport.append("<img src=\"./vocabulary-" + DataProvider.DDB.toString() + "-" + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">") ;
			}
			else if ( dataprovider.equals(DATAPROVIDER_EUROPEANA))
			{
				htmlReport.append("<img src=\"./enrichment-" + DataProvider.Europeana.toString() + "-"+CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH + ".png\" style=\"width:1000px;\">") ;
				htmlReport.append("<img src=\"./enrichment-link-" + DataProvider.Europeana.toString() + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">") ;
				htmlReport.append("<img src=\"./vocabulary-" + DataProvider.Europeana.toString() + "-" + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">") ;
			}
			else if ( dataprovider.equals(DATAPROVIDER_MENDELEY))
			{
				htmlReport.append("<img src=\"./enrichment-" + DataProvider.Mendeley.toString() + "-"+CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH + ".png\" style=\"width:1000px;\">") ;
				htmlReport.append("<img src=\"./enrichment-link-" + DataProvider.Mendeley.toString() + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">" );
				htmlReport.append("<img src=\"./vocabulary-" + DataProvider.Mendeley.toString() + "-" + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">" );
			}
			else if ( dataprovider.equals(DATAPROVIDER_ZBW))
			{
				htmlReport.append("<img src=\"./enrichment-" + DataProvider.ZBW.toString() + "-"+CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH + ".png\" style=\"width:1000px;\">" );
				htmlReport.append("<img src=\"./enrichment-link-" + DataProvider.ZBW.toString() + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">") ;
				htmlReport.append("<img src=\"./vocabulary-" + DataProvider.ZBW.toString() + "-" + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">") ;
			}
			else if ( dataprovider.equals(DATAPROVIDER_KIMPORTAL))
			{
				htmlReport.append("<img src=\"./enrichment-" + DataProvider.KIMCollect.toString() + "-"+CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH + ".png\" style=\"width:1000px;\">") ;
				htmlReport.append("<img src=\"./enrichment-link-" + DataProvider.KIMCollect.toString() + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">" );
				htmlReport.append("<img src=\"./vocabulary-" + DataProvider.KIMCollect.toString() + "-" + CHART_WIDTH_HIGH+"x"+CHART_HEIGHT_HIGH+".png\" style=\"width:1000px;\">") ;
			}
			
			htmlReport.append("<p><h4>Fields:</h4>");
	        Iterator<Entry<String, StructureRecResult>> iteratorByDataprovider = resultsByDataprovider.entrySet().iterator();
	        while (iteratorByDataprovider.hasNext()) {
	        	StringBuffer htmlReportPerField = new StringBuffer(htmlReportGeneralHeader);
		    	String htmlReportPerFieldJavascript = new String(htmlReportJavascriptGeneral);

	            Entry<String, StructureRecResult> fieldResult = (Entry<String, StructureRecResult>) iteratorByDataprovider.next();
	            String field = fieldResult.getKey();
	            htmlReportPerField.append("<h3>" + dataprovider +"</h3>");
//	            htmlReportPerField.append("<h4 id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"Header\" class=\"flip\">Field:" + field +"</h4>");
	            htmlReportPerField.append("<h4>Field:" + field +"</h4>");
//	            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"Panel\" class=\"panel\">");
	            htmlReportPerField.append("<div >");

	            htmlReportPerFieldJavascript += "$(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"Header\").click(function(){";
	            htmlReportPerFieldJavascript += "    $(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"Panel\").slideToggle(\"slow\");";
	            htmlReportPerFieldJavascript +="});";

	            StructureRecResult result = fieldResult.getValue();
	            // write Histogramm for value length
				try {
					
					if (this.writeHistogrammCSV) {
						if (result.getLengthHistogram() != null)
						{
							File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field.replace(":", " ")+"-value length histogram.csv");
							StringBuffer writerStatisticRecordsBuffer = new StringBuffer();
							for (int i = 0; i < result.getLengthHistogram().length; i++) {
								writerStatisticRecordsBuffer.append( i + STATISTIC_FILE_FIELD_SEPERATOR);
							}
							writerStatisticRecordsBuffer.append("\n");
							for (int i = 0; i < result.getLengthHistogram().length; i++) {
								writerStatisticRecordsBuffer.append(result.getLengthHistogram()[i] + STATISTIC_FILE_FIELD_SEPERATOR);
							}
							writerStatisticRecordsBuffer.append("\n");
							BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
							writerStatisticRecords.append(writerStatisticRecordsBuffer);
							writerStatisticRecords.close();
						}
					}					
					htmlReportPerField.append("<h5>Histogram for value length</h5>");
					String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"valuelength";
		            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
					htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>length:</b></td>");
					htmlReportPerField.append("<td><b>number:</b></td></tr></thead>");
					
					if (result.getLengthHistogram() != null)
					{
						for (int i = 0; i < result.getLengthHistogram().length; i++) {
							if (result.getLengthHistogram()[i] > 0) {
								htmlReportPerField.append("<tr><td>"+i+"</td>");
								htmlReportPerField.append("<td>"+result.getLengthHistogram()[i]+"</td></tr>");
							}
						}
					}
					htmlReportPerField.append("</table>");
				} catch (Exception e) {
					e.printStackTrace();
				}
				String filename = Qc_graphs.struturendnessDataproviderFieldValueLengthHistrogramm(dataprovider, field.replace(":", " "), CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReportPerField.append("<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>");
				
				//////
				
				// write histogram for values
				try {
					String filenameHistogramCSV = "";
					if (writeHistogrammCSV) {
						File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field.replace(":", " ")+"-value histogram.csv");
						StringBuffer writerStatisticRecordsBuffer = new StringBuffer();
						{					
					        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					             if ( ! pattern.getKey().isEmpty())
					            	 writerStatisticRecordsBuffer.append(pattern.getKey() + STATISTIC_FILE_FIELD_SEPERATOR);
					        }
					        writerStatisticRecordsBuffer.append("\n");
					        iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					             if ( ! pattern.getKey().isEmpty())
					            	 writerStatisticRecordsBuffer.append(pattern.getValue() + STATISTIC_FILE_FIELD_SEPERATOR);
					        }
					        writerStatisticRecordsBuffer.append("\n");
						}
						BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
						writerStatisticRecords.append(writerStatisticRecordsBuffer);
						writerStatisticRecords.close();
						filenameHistogramCSV = fileStatisticRecords.getName();
					}
					{					
						htmlReportPerField.append("<h5>Histogram for values</h5>");
						String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"valuehisto";
			            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
						htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>values:</b></td>");
						htmlReportPerField.append("<td><b>number:</b></td></tr></thead>");
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
							htmlReportPerField.append("<td>"+pattern.getValue()+"</td></tr>");
				        }
				    	htmlReportPerField.append("</table>");
				    	if (this.writeHistogrammCSV)
				    		htmlReportPerField.append("<p><a href=\".\\"+OUTPUT_STRUCT_CSV_DIR+filenameHistogramCSV+"\">data as CSV</a></p>");

					}

				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
//				Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field, CHART_WIDTH_MID, CHART_HEIGHT_MID, result);
				filename = Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field.replace(":", " "), "values", CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReportPerField.append("<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>");
				
				//////
				
				
				// write histogram for pattern
				try {
					String filenameHistogramCSV = "";
					if (writeHistogrammCSV) {
						File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field.replace(":", " ")+"-value pattern histogram.csv");
						StringBuffer writerStatisticRecordsBuffer = new StringBuffer();
						{					
					        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					             if ( ! pattern.getKey().isEmpty())
					            	 writerStatisticRecordsBuffer.append(pattern.getKey() + STATISTIC_FILE_FIELD_SEPERATOR);
					        }
					        writerStatisticRecordsBuffer.append("\n");
					        iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					             if ( ! pattern.getKey().isEmpty())
					            	 writerStatisticRecordsBuffer.append(pattern.getValue() + STATISTIC_FILE_FIELD_SEPERATOR);
					        }
					        writerStatisticRecordsBuffer.append("\n");
						}
						BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
						writerStatisticRecords.append(writerStatisticRecordsBuffer);
						writerStatisticRecords.close();
						filenameHistogramCSV = fileStatisticRecords.getName();
					}
					{					
						htmlReportPerField.append("<h5>Histogram for pattern</h5>");
						String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"patternhisto";
			            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
						htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>pattern:</b></td>");
						htmlReportPerField.append("<td><b>number:</b></td></tr></thead>");
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
							htmlReportPerField.append("<td>"+pattern.getValue()+"</td></tr>");
				        }
				    	htmlReportPerField.append("</table>");
					}
					{					
						htmlReportPerField.append("<h5>Histogram for pattern - Sourcen</h5>");
						String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"sourcehisto";
			            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
						htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>pattern:</b></td>");
						htmlReportPerField.append("<td><b>number:</b></td>");
						htmlReportPerField.append("<td><b>Source:</b></td></tr></thead>");
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
				        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesPatternSourceHashMap().entrySet().iterator();
				        int helpCount = 0;
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
							htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
							htmlReportPerField.append("<td>"+pattern.getValue()+"</td>");
							htmlReportPerField.append("<td>");
							
							
				            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternSource"+helpCount+"Header\" class=\"flip\">show");
				            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternSource"+helpCount+"Panel\" class=\"panel\">");

				            htmlReportPerFieldJavascript += "$(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternSource"+helpCount+"Header\").click(function(){";
				            htmlReportPerFieldJavascript += "    $(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternSource"+helpCount+"Panel\").slideToggle(\"slow\");";
				            htmlReportPerFieldJavascript +="});";

							
							
				            htmlReportPerField.append("<ul>");
							ArrayList<PatternSource> sources = patternSource.getValue();
							for (int i = 0; i < sources.size(); i++) {
								htmlReportPerField.append("<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>");
							}
							htmlReportPerField.append("</ul></div></div></td></tr>");
							helpCount++;
				        }
				    	htmlReportPerField.append("</table>");
						htmlReportPerField.append("<p><a href=\".\\"+OUTPUT_STRUCT_CSV_DIR+filenameHistogramCSV+"\">data as CSV</a></p>");

					}

				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
//				Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field, CHART_WIDTH_MID, CHART_HEIGHT_MID, result);
				filename = Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field.replace(":", " "), "pattern", CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReportPerField.append("<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>");
				
				
				// write histogram for pattern RegEx
				try {
					String filenameHistogramCSV = "";
					if (this.writeHistogrammCSV) {
						File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field.replace(":", " ")+"-value pattern regex histogram.csv");
						BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
						StringBuffer writerStatisticRecordsBuffer = new StringBuffer();
						{					
					        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					             if ( ! pattern.getKey().isEmpty())
					            	 writerStatisticRecordsBuffer.append(pattern.getKey() + STATISTIC_FILE_FIELD_SEPERATOR);
					        }
					        writerStatisticRecordsBuffer.append("\n");
					        iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					             if ( ! pattern.getKey().isEmpty())
					            	 writerStatisticRecordsBuffer.append(pattern.getValue() + STATISTIC_FILE_FIELD_SEPERATOR);
					        }
						}
						writerStatisticRecordsBuffer.append("\n");
						writerStatisticRecords.append(writerStatisticRecordsBuffer);
						writerStatisticRecords.close();
						filenameHistogramCSV = fileStatisticRecords.getName();
					}
					{					
						htmlReportPerField.append("<h5>Histogram for pattern(RegEx)</h5>");
						String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"patternRegExhisto";
			            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
						htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>pattern:</b></td>");
						htmlReportPerField.append("<td><b>number:</b></td></tr></thead>");
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				             Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
							htmlReportPerField.append("<td>"+pattern.getValue()+"</td></tr>");
				        }
				    	htmlReportPerField.append("</table>");
					}
					{					
						htmlReportPerField.append("<h5>Histogram for pattern(RegEx) - Sourcen</h5>");
						String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"patternRegExhistoSource";
			            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
						htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>pattern:</b></td>");
						htmlReportPerField.append("<td><b>number:</b></td>");
						htmlReportPerField.append("<td><b>Source:</b></td></tr></thead>");
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
				        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesPatternRegExSourceHashMap().entrySet().iterator();
				        int helpCount = 0;
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
							htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
							htmlReportPerField.append("<td>"+pattern.getValue()+"</td>");
							htmlReportPerField.append("<td>");
							
							
				            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternRegExSource"+helpCount+"Header\" class=\"flip\">show");
				            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternRegExSource"+helpCount+"Panel\" class=\"panel\">");

				            htmlReportPerFieldJavascript += "$(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternRegExSource"+helpCount+"Header\").click(function(){";
				            htmlReportPerFieldJavascript += "    $(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"PatternRegExSource"+helpCount+"Panel\").slideToggle(\"slow\");";
				            htmlReportPerFieldJavascript +="});";

							
							
				            htmlReportPerField.append("<ul>");
							ArrayList<PatternSource> sources = patternSource.getValue();
							for (int i = 0; i < sources.size(); i++) {
								htmlReportPerField.append("<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>");
							}
							htmlReportPerField.append("</ul></div></div></td></tr>");
							helpCount++;
				        }
				    	htmlReportPerField.append("</table>");
						htmlReportPerField.append("<p><a href=\".\\"+OUTPUT_STRUCT_CSV_DIR+filenameHistogramCSV+"\">data as CSV</a></p>");
					}

				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				filename = Qc_graphs.struturendnessDataproviderFieldValuePatternHistrogramm(dataprovider, field.replace(":", " "),"pattern RegEx", CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, result);
				htmlReportPerField.append("<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>");
				

				
				// write histogram for date pattern
				htmlReportPerField.append("<h5>date patterns</h5>");
				htmlReportPerField.append("<p>date patterns detected:<b>"+result.getValuesDateformatHashMap().size()+"</b></p>");

				try {
					if (this.writeHistogrammCSV) {
						File fileStatisticRecords = new File(Qc_dataprovider.outputDir+OUTPUT_STRUCT_CSV_DIR+ dataprovider+"-"+field.replace(":", " ")+"-value date pattern histogram.csv");
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
					}
					if (result.getValuesDateformatHashMap().size() > 0) {
						{					
							htmlReportPerField.append("<h4>Histogram for date patterns</h4>");
							String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"datepatternhisto";
				            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
							htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>pattern:</b></td>");
							htmlReportPerField.append("<td><b>number:</b></td></tr></thead>");
					        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesDateformatHashMap().entrySet().iterator();
					        while (iteratorPatternHashMap.hasNext()) {
					            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
								htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
								htmlReportPerField.append("<td>"+pattern.getValue()+"</td></tr>");
					        }
					    	htmlReportPerField.append("</table>");
						}
						{					
							htmlReportPerField.append("<h4>Histogram for date pattern - Sourcen</h4>");
							String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"datepatternhistosource";
				            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
							htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>date pattern:</b></td>");
							htmlReportPerField.append("<td><b>number:</b></td>");
							htmlReportPerField.append("<td><b>Source:</b></td></tr></thead>");
					        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesDateformatHashMap().entrySet().iterator();
					        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesDateformatSourceHashMap().entrySet().iterator();
					        int helpCount = 0;
					        while (iteratorPatternHashMap.hasNext()) {
					            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
					            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
								htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
								htmlReportPerField.append("<td>"+pattern.getValue()+"</td>");
								htmlReportPerField.append("<td>");
								
								
					            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"DateFormatSource"+helpCount+"Header\" class=\"flip\">show");
					            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"DateFormatSource"+helpCount+"Panel\" class=\"panel\">");
	
					            htmlReportPerFieldJavascript += "$(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"DateFormatSource"+helpCount+"Header\").click(function(){";
					            htmlReportPerFieldJavascript += "    $(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"DateFormatSource"+helpCount+"Panel\").slideToggle(\"slow\");";
					            htmlReportPerFieldJavascript +="});";
	
								
								
					            htmlReportPerField.append("<ul>");
								ArrayList<PatternSource> sources = patternSource.getValue();
								for (int i = 0; i < sources.size(); i++) {
									htmlReportPerField.append("<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>");
								}
								htmlReportPerField.append("</ul></div></div></td></tr>");
								helpCount++;
					        }
					    	htmlReportPerField.append("</table>");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				htmlReportPerField.append("<h5>URL patterns</h5>");
				htmlReportPerField.append("<p>URL patterns detected:<b>"+result.getValuesUrlformatHashMap().size()+"</b></p>");
				if (result.getValuesUrlformatHashMap().size() > 0)
				{
					{					
						htmlReportPerField.append("<h4>Histogram for URL patterns</h4>");
						String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"urlpatternhisto";
			            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
						htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>pattern:</b></td>");
						htmlReportPerField.append("<td><b>number:</b></td></tr></thead>");
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesUrlformatHashMap().entrySet().iterator();
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
							htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
							htmlReportPerField.append("<td>"+pattern.getValue()+"</td></tr>");
				        }
				    	htmlReportPerField.append("</table>");
					}
					{					
						htmlReportPerField.append("<h4>Histogram for URL pattern - Sourcen</h4>");
						String tempHTMLID = dataprovider.replace(" ", "")+field.replace(":", "")+"urlpatternhistosource";
			            htmlReportPerFieldJavascript +="$('#"+tempHTMLID+"').DataTable();";
						htmlReportPerField.append("<table id =\""+tempHTMLID+"\"><thead><tr><td><b>URL pattern:</b></td>");
						htmlReportPerField.append("<td><b>number:</b></td>");
						htmlReportPerField.append("<td><b>Source:</b></td></tr></thead>");
				        Iterator<Entry<String, Integer>> iteratorPatternHashMap = result.getValuesUrlformatHashMap().entrySet().iterator();
				        Iterator<Entry<String, ArrayList<PatternSource>>> iteratorPatternSourceHashMap = result.getValuesUrlformatSourceHashMap().entrySet().iterator();
				        int helpCount = 0;
				        while (iteratorPatternHashMap.hasNext()) {
				            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
				            Entry<String, ArrayList<PatternSource>> patternSource = iteratorPatternSourceHashMap.next();
							htmlReportPerField.append("<tr><td>"+pattern.getKey()+"</td>");
							htmlReportPerField.append("<td>"+pattern.getValue()+"</td>");
							htmlReportPerField.append("<td>");
							
							
				            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"UrlFormatSource"+helpCount+"Header\" class=\"flip\">show");
				            htmlReportPerField.append("<div id=\""+dataprovider.replace(" ", "")+field.replace(":", "")+"UrlFormatSource"+helpCount+"Panel\" class=\"panel\">");

				            htmlReportPerFieldJavascript += "$(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"UrlFormatSource"+helpCount+"Header\").click(function(){";
				            htmlReportPerFieldJavascript += "    $(\"#"+dataprovider.replace(" ", "")+field.replace(":", "")+"UrlFormatSource"+helpCount+"Panel\").slideToggle(\"slow\");";
				            htmlReportPerFieldJavascript +="});";

							
							
				            htmlReportPerField.append("<ul>");
							ArrayList<PatternSource> sources = patternSource.getValue();
							for (int i = 0; i < sources.size(); i++) {
								htmlReportPerField.append("<li><a href=\".\\input\\"+sources.get(i).getFilename()+"\">" + sources.get(i).getValue() + " " + "</a></li>");
							}
							htmlReportPerField.append("</ul></div></div></td></tr>");
							helpCount++;
				        }
				    	htmlReportPerField.append("</table>");
					}
				}
				
				htmlReportPerField.append("</div>");

				
				htmlReportPerFieldJavascript += "});</script>";
				htmlReportPerField.append(htmlReportPerFieldJavascript);
				htmlReportPerField.append("</body></html>");
		        try {
					File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "dataquality-report-"+dataprovider+"-"+field.replace(":", "")+".html");
					BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
					
					writerStatisticRecords.append(htmlReportPerField);
					writerStatisticRecords.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
	        	
				htmlReport.append("<li> <a href=\"dataquality-report-"+dataprovider+"-"+field.replace(":", "")+".html\">" +           field +"</a></li>");

	        }
	        htmlReportJavascript += "});</script>";
	        htmlReport.append(htmlReportJavascript);
	        htmlReport.append("</body></html>");
			try {
				File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "dataquality-report-"+dataprovider+".html");
				BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
				
				writerStatisticRecords.append(htmlReport);
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
        
        htmlReportGeneral += "<h3>All data fields</h3>";
        htmlReportGeneral += "<p>The chart shows how many data fields are provided by the data providers.</p>";
        htmlReportGeneral += "<img src=\"all_datafields_bar_chart_1600x1200.png\" style=\"width:1000px;\"/>"; 
        htmlReportGeneral += "<h3>non empty data fields</h3>";
        htmlReportGeneral += "<p>The chart shows the non empty data fields.</p>";
        htmlReportGeneral += "<img src=\"non_empty_datafields_barchart_1600x1200.png\" style=\"width:1000px;\"/>"; 
        htmlReportGeneral += "<h3>non empty data fields per record</h3>";
        htmlReportGeneral += "<p>The chart shows the non empty data fields in respect to the number of data fields.</p>";
        htmlReportGeneral += "<img src=\"non_empty_datafields_perdatafields_barchart_1600x1200.png\" style=\"width:1000px;\"/>";
        htmlReportGeneral += "<h3>links per record and per data provider</h3>";
        htmlReportGeneral += "<p>The chart shows the number of links per record.</p>";
        htmlReportGeneral += "<img src=\"links_barchart_1600x1200.png\" style=\"width:1000px;\"/>";
        htmlReportGeneral += "<h3>links accessible</h3>";
        htmlReportGeneral += "<p>The chart shows how many links are accessible.</p>";
        htmlReportGeneral += "<img src=\"links_accessible_barchart_1600x1200.png\" style=\"width:1000px;\"/>";
        
        htmlReportGeneral += "<h3>enrichment statistics</h3>";
        htmlReportGeneral += "<p>The chart shows the number of non empty data fields during the enrichment process.</p>";
        htmlReportGeneral += "<img src=\"enrichment-overview-1600x1200.png\" style=\"width:1000px;\"/>";
        
        htmlReportGeneral += "<p>The chart shows the number of links per record during the enrichment process.</p>";
        htmlReportGeneral += "<img src=\"enrichment-link-1600x1200.png\" style=\"width:1000px;\"/>";
        htmlReportGeneral += "<h3>known vocabulary links</h3>";
        htmlReportGeneral += "<p>The chart shows the number of links of known vocabulary links during the enrichment process.</p>";
        htmlReportGeneral += "<img src=\"vocabulary-1600x1200.png\" style=\"width:1000px;\"/>";
        
        htmlReportGeneral += "<h4>known vocabulary links detailed data</h4>";
        htmlReportGeneral += "<p>More detailed data is provided on <a href=\"dataquality-report-enrichment-vocabulary-links.html\">this page.</a></p>";
		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "dataquality-report-enrichment-vocabulary-links.html");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			String tempReportFile = htmlReportGeneralHeader;
			tempReportFile += "<h3>known vocabulary links detailed data</h3>";
			tempReportFile +=enrichment.calcLinkTable(paramDataList,STATISTIC_FILE_FIELD_SEPERATOR);
			
			for (DataProvider provider : DataProvider.values())
			{
				tempReportFile +=enrichment.calcLinkTablePerDataprovider(paramDataList,STATISTIC_FILE_FIELD_SEPERATOR,provider);
				Qc_graphs.linkTablePerDataproviderPie(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList,provider);
				tempReportFile += "<img src=\""+Qc_dataprovider.OUTPUT_IMG_DIR+"statistics-links-dataprovider"+provider.toString()+".png\" style=\"width:1000px;\"/>";

			}
			
			
			tempReportFile +="</body></html>";
			writerStatisticRecords.write(tempReportFile);
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        

        htmlReportGeneral += "<h3>Summary</h3>";
        htmlReportGeneral += "<ul>";
        htmlReportGeneral += "<li><a target=\"_blank\" href=\""+DATAQUALITY_REPORT_PLOT_HTML_FILENAME+"\">Data Quality Report with JQPlot </a></li>";
        htmlReportGeneral += "<li><a target=\"_blank\" href=\""+DataQualityVocabularyRDFWriter.STATISTICS_DATAPROVIDER_XML_FILENAME+"\">Data Quality Report (RDF/XML) </a></li>";
        htmlReportGeneral += "</ul>";
        
        htmlReportGeneral += "<h3>Reports for single data providers</h3>";
        htmlReportGeneral += htmlReportGeneralDataproviders;
        
        htmlReportGeneral += "<h3>Report for enrichments</h3>";
        htmlReportGeneral +="<a target=\"_blank\" href=\""+DATAQUALITY_REPORT_ENRICHMENT_HTML_FILENAME+"\">Annotation Consistency </a>";        
        
        htmlReportGeneral += "<h3>Stats of the report generation</h3>";
		long timestampEnd = System.currentTimeMillis();
		long timespanMS = timestampEnd - timestampStart;
		double timespanS = timespanMS / 1000;
		double timespanM = timespanS / 60;
        htmlReportGeneral += "<p>Report generation </br>"+
        					"started at: "+dt.format(new Date(timestampStart))+"</br>" +
        					"finished at: "+dt.format(new Date(System.currentTimeMillis()))+"</br>";
        
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
		printEnrichmentsReport(htmlReportGeneralHeader);
	    
	}

	private void printEnrichmentsReport(String htmlReportGeneral)
	{
		String htmlReportJavascript = "<script>$(document).ready(function(){";
        htmlReportGeneral += "<h2>Annotation Consistency</h2>";
        Iterator<Entry<String, EnrichmentResultDataprovider>> enrichmenResultsIterator = this.enrichmenResults.entrySet().iterator();
//        htmlReportGeneral += "<h3>Enrichment Overview</h3>";
//        htmlReportGeneral +="<p><table>";
//        htmlReportGeneral +="<tr><td><b>dataprovider</b></td><td><b>records</b></td><td><b>records with enriched metadata</b></td></tr>";
//	    while (enrichmenResultsIterator.hasNext()) {
//	        Entry<String, EnrichmentResultDataprovider> entry = enrichmenResultsIterator.next();
//	        htmlReportGeneral += "<tr><td>"+entry.getKey()+"</td><td>";
//	        htmlReportGeneral += entry.getValue().getCountRecordsEnriched();
//	        htmlReportGeneral += "</td><td>";
//	        htmlReportGeneral += entry.getValue().getCountRecordsEnrichedWithEnrichedMetadata();
//	        htmlReportGeneral += "</td></tr>";
//	    	
//	    }
//        htmlReportGeneral +="</table></p>";
	    
		Iterator<Entry<String, HashMap<String, WordNetSimilarityResultObject>>> similarityResultsIterator = this.similarityResults.entrySet().iterator();
        SummaryStatistics statsScoreAllProxyMean = new SummaryStatistics();
        SummaryStatistics statsScoreAllEnrichedProxyMean = new SummaryStatistics();
        SummaryStatistics statsScoreAllDistanceMean = new SummaryStatistics();
        ArrayList<Double> statsScoreAllProxyMedian = new ArrayList<Double>();
        ArrayList<Double> statsScoreAllEnrichedProxyMedian = new ArrayList<Double>();
        ArrayList<Double> statsScoreAllDistanceMedian = new ArrayList<Double>();
	    while (similarityResultsIterator.hasNext()) {
	        Entry<String, HashMap<String, WordNetSimilarityResultObject>> entry = similarityResultsIterator.next();
	        String dataprovider = entry.getKey();
	        HashMap<String, WordNetSimilarityResultObject> resultsByDataprovider = entry.getValue();
	        htmlReportGeneral += "<h3>"+dataprovider+"</h3><p>";
	        htmlReportGeneral += "<div id=\""+dataprovider.replace(" ", "")+"Header\" class=\"flip\">show";
	        htmlReportGeneral += "<div id=\""+dataprovider.replace(" ", "")+"Panel\" class=\"panel\">";

            htmlReportJavascript += "$(\"#"+dataprovider.replace(" ", "")+"Header\").click(function(){";
            htmlReportJavascript += "    $(\"#"+dataprovider.replace(" ", "")+"Panel\").slideToggle(\"slow\");";
            htmlReportJavascript +="});";

            SummaryStatistics statsScoreProxyMean = new SummaryStatistics();
            SummaryStatistics statsScoreEnrichedProxyMean = new SummaryStatistics();
            SummaryStatistics statsScoreDistanceMean = new SummaryStatistics();
            ArrayList<Double> statsScoreProxyMedian = new ArrayList<Double>();
            ArrayList<Double> statsScoreEnrichedProxyMedian = new ArrayList<Double>();
            ArrayList<Double> statsScoreDistanceMedian = new ArrayList<Double>();

	        htmlReportGeneral += "<table><tr>"+
	        		"<td> mean "+SIMILARTY_ORGINAL_REPORT_LABEL+"</td><td>mean "+SIMILARTY_ENRICHED_REPORT_LABEL+"</td><td>mean "+SIMILARTY_DISTANCE_REPORT_LABEL+"</td>"+
	        		"<td> median "+SIMILARTY_ORGINAL_REPORT_LABEL+"</td><td>median "+SIMILARTY_ENRICHED_REPORT_LABEL+"</td><td>median "+SIMILARTY_DISTANCE_REPORT_LABEL+"</td>"+
	        		"<td>values original</td><td>values original and enriched</td><td>source</td></tr>";

			Iterator<Entry<String, WordNetSimilarityResultObject>> resultsByDataproviderIterator = resultsByDataprovider.entrySet().iterator();
			boolean hasElements =false;	    
			EnrichmentResultDataprovider actEnrichmentResult=null;
	        enrichmenResultsIterator = this.enrichmenResults.entrySet().iterator();
			while (enrichmenResultsIterator.hasNext()) {
		        Entry<String, EnrichmentResultDataprovider> entryEnrichmentResultDataprovider = enrichmenResultsIterator.next();
		        if (entryEnrichmentResultDataprovider.getKey().equalsIgnoreCase(dataprovider))
		        	actEnrichmentResult = entryEnrichmentResultDataprovider.getValue();
			}
		    while (resultsByDataproviderIterator.hasNext()) {
		        Entry<String, WordNetSimilarityResultObject> entryObject = resultsByDataproviderIterator.next();
		        String filename = entryObject.getKey();
		        WordNetSimilarityResultObject resultsObject = entryObject.getValue();
	        
		        if (resultsObject.getWuPalmerRelatednessOfWordsMeanDist() != Double.MAX_VALUE &&
		        	!Double.isNaN(resultsObject.getWuPalmerRelatednessOfWordsMeanDist())
		        		)
		        {
					actEnrichmentResult.countRecordsEnrichedWithEnrichedMetadataSimilarityChecked++;
		        	hasElements = true;
			        htmlReportGeneral += "<tr>";
		        	htmlReportGeneral += "<td>" + this.formatNumber(resultsObject.getValueProxy().getWuPalmerRelatednessOfWordsMean()) +"</td>";
		        	htmlReportGeneral += "<td>" + this.formatNumber(resultsObject.getValueEnrichedProxy().getWuPalmerRelatednessOfWordsMean()) +"</td>";
		        	htmlReportGeneral += "<td>" + this.formatNumber(resultsObject.getWuPalmerRelatednessOfWordsMeanDist()) +"</td>";

		        	htmlReportGeneral += "<td>" + this.formatNumber(resultsObject.getValueProxy().getWuPalmerRelatednessOfWordsMedian()) +"</td>";
		        	htmlReportGeneral += "<td>" + this.formatNumber(resultsObject.getValueEnrichedProxy().getWuPalmerRelatednessOfWordsMedian()) +"</td>";
		        	htmlReportGeneral += "<td>" + this.formatNumber(resultsObject.getWuPalmerRelatednessOfWordsMedianDist()) +"</td>";
		        	htmlReportGeneral += "<td><ul>";
		        	for (int i = 0; i < resultsObject.getValueProxy().getWordListUsed().size(); i++) {
		        		htmlReportGeneral += "<li>" + resultsObject.getValueProxy().getWordListUsed().get(i)+"</li>";	
					}
		        	htmlReportGeneral += "</ul></td>";
//		        	htmlReportGeneral += "<td>" + resultsObject.getValueProxy().getWordListUsed().toString() +"</td>";
//		        	htmlReportGeneral += "<td>" + resultsObject.getValueEnrichedProxy().getWordListUsed().toString() +"</td>";
		        	htmlReportGeneral += "<td><ul>";
		        	for (int i = 0; i < resultsObject.getValueEnrichedProxy().getWordListUsed().size(); i++) {
		        		htmlReportGeneral += "<li>" + resultsObject.getValueEnrichedProxy().getWordListUsed().get(i)+"</li>";	
					}
		        	htmlReportGeneral += "</ul></td>";
		        	htmlReportGeneral += "<td> <a href=\".\\input\\"+filename.substring(filename.lastIndexOf("\\"))+"\" target=\"_blank\">" + filename.substring(filename.lastIndexOf("\\")+1) + " " + "</a></td>";
		        	htmlReportGeneral += "</tr>";
		        	
		        	statsScoreProxyMean.addValue(resultsObject.getValueProxy().getWuPalmerRelatednessOfWordsMean());
		        	statsScoreEnrichedProxyMean.addValue(resultsObject.getValueEnrichedProxy().getWuPalmerRelatednessOfWordsMean());
		        	statsScoreDistanceMean.addValue(resultsObject.getWuPalmerRelatednessOfWordsMeanDist());
		        	
		        	statsScoreProxyMedian.add(resultsObject.getValueProxy().getWuPalmerRelatednessOfWordsMedian());
		        	statsScoreEnrichedProxyMedian.add(resultsObject.getValueEnrichedProxy().getWuPalmerRelatednessOfWordsMedian());
		        	statsScoreDistanceMedian.add(resultsObject.getWuPalmerRelatednessOfWordsMedianDist());

		        	statsScoreAllProxyMean.addValue(resultsObject.getValueProxy().getWuPalmerRelatednessOfWordsMean());
		        	statsScoreAllEnrichedProxyMean.addValue(resultsObject.getValueEnrichedProxy().getWuPalmerRelatednessOfWordsMean());
		        	statsScoreAllDistanceMean.addValue(resultsObject.getWuPalmerRelatednessOfWordsMeanDist());

		        	statsScoreAllProxyMedian.add(resultsObject.getValueProxy().getWuPalmerRelatednessOfWordsMedian());
		        	statsScoreAllEnrichedProxyMedian.add(resultsObject.getValueEnrichedProxy().getWuPalmerRelatednessOfWordsMedian());
		        	statsScoreAllDistanceMedian.add(resultsObject.getWuPalmerRelatednessOfWordsMedianDist());
		        }
		    }
		    this.enrichmenResults.put(dataprovider, actEnrichmentResult);
		    
	        htmlReportGeneral += "</table>";
	        if (hasElements) {
		        htmlReportGeneral += "<p>" +
		        		"<table><tr><td></td><td>"+SIMILARTY_ORGINAL_REPORT_LABEL+"</td><td>"+SIMILARTY_ENRICHED_REPORT_LABEL+"</td><td>"+SIMILARTY_DISTANCE_REPORT_LABEL+"</td></tr>";
		        htmlReportGeneral += "<tr>";
		        htmlReportGeneral += "<td>mean:</td><td>" + this.formatNumber(statsScoreProxyMean.getMean()) + "</td>";
		        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreEnrichedProxyMean.getMean()) + "</td>";
		        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreDistanceMean.getMean()) + "</td>";
		        htmlReportGeneral += "</tr>";
		        
		        htmlReportGeneral += "<tr>";
		        htmlReportGeneral += "<td>median:</td><td>" + this.formatNumber(getMedian(statsScoreProxyMedian)) + "</td>";
		        htmlReportGeneral += "<td>" + this.formatNumber(getMedian(statsScoreEnrichedProxyMedian)) + "</td>";
		        htmlReportGeneral += "<td>" + this.formatNumber(getMedian(statsScoreDistanceMedian)) + "</td>";
		        htmlReportGeneral += "</tr>";

		        htmlReportGeneral += "<tr>";
		        htmlReportGeneral += "<td>StandardDeviation:</td><td>" + this.formatNumber(statsScoreProxyMean.getStandardDeviation()) + "</td>";
		        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreEnrichedProxyMean.getStandardDeviation()) + "</td>";
		        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreDistanceMean.getStandardDeviation()) + "</td>";
		        htmlReportGeneral += "</tr>";
		        
		        htmlReportGeneral += "</table></p>";
	        }
	        htmlReportGeneral += "</div></div>";
	        htmlReportGeneral += "</p>";
	    }        
        htmlReportGeneral += "<h3>All dataprovider</h3>"; 
        htmlReportGeneral += "<p>" +
        		"<table><tr><td></td><td>"+SIMILARTY_ORGINAL_REPORT_LABEL+"</td><td>"+SIMILARTY_ENRICHED_REPORT_LABEL+"</td><td>"+SIMILARTY_DISTANCE_REPORT_LABEL+"</td></tr>";
        htmlReportGeneral += "<tr>";
        htmlReportGeneral += "<td>mean:</td><td>" + this.formatNumber(statsScoreAllProxyMean.getMean()) + "</td>";
        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreAllEnrichedProxyMean.getMean()) + "</td>";
        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreAllDistanceMean.getMean()) + "</td>";
        htmlReportGeneral += "</tr>";
        
        htmlReportGeneral += "<tr>";
        htmlReportGeneral += "<td>median:</td><td>" + this.formatNumber(getMedian(statsScoreAllProxyMedian)) + "</td>";
        htmlReportGeneral += "<td>" + this.formatNumber(getMedian(statsScoreAllEnrichedProxyMedian)) + "</td>";
        htmlReportGeneral += "<td>" + this.formatNumber(getMedian(statsScoreAllDistanceMedian)) + "</td>";
        htmlReportGeneral += "</tr>";

        htmlReportGeneral += "<tr>";
        htmlReportGeneral += "<td>StandardDeviation:</td><td>" + this.formatNumber(statsScoreAllProxyMean.getStandardDeviation()) + "</td>";
        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreAllEnrichedProxyMean.getStandardDeviation()) + "</td>";
        htmlReportGeneral += "<td>" + this.formatNumber(statsScoreAllDistanceMean.getStandardDeviation()) + "</td>";
        htmlReportGeneral += "</tr>";
        
        htmlReportGeneral += "</table></p>";
        htmlReportGeneral += "<h3>Enrichment Overview</h3>";
        htmlReportGeneral +="<p><table>";
        htmlReportGeneral +="<tr><td><b>dataprovider</b></td><td><b>records</b></td><td><b>records with enriched metadata</b></td><td><b>records with enriched metadata and similarity results</b></td></tr>";
        enrichmenResultsIterator = this.enrichmenResults.entrySet().iterator();
	    while (enrichmenResultsIterator.hasNext()) {
	        Entry<String, EnrichmentResultDataprovider> entry = enrichmenResultsIterator.next();
	        htmlReportGeneral += "<tr><td>"+entry.getKey()+"</td><td>";
	        htmlReportGeneral += entry.getValue().getCountRecordsEnriched();
	        htmlReportGeneral += "</td><td>";
	        htmlReportGeneral += entry.getValue().getCountRecordsEnrichedWithEnrichedMetadata();
	        htmlReportGeneral += "</td><td>";
	        htmlReportGeneral += entry.getValue().getCountRecordsEnrichedWithEnrichedMetadataSimilarityChecked();
	        htmlReportGeneral += "</td></tr>";
	    	
	    }
        htmlReportGeneral +="</table></p>";
        htmlReportGeneral += htmlReportJavascript + "});</script>";
        htmlReportGeneral += "</body></html>";
		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ DATAQUALITY_REPORT_ENRICHMENT_HTML_FILENAME);
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			
			writerStatisticRecords.write(htmlReportGeneral);
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

	protected double getMedian(
			ArrayList<Double> statsScoreProxyMedian) {
		UnivariateStatistic stat = new Median();
		double[] tempValues = new double[statsScoreProxyMedian.size()];
		for (int i = 0; i < tempValues.length; i++) {
			tempValues[i] = statsScoreProxyMedian.get(i);
		}
		double median = stat.evaluate(tempValues);
		return median;
	}
	
	private void printStructureOverview(StringBuffer htmlReport,
			String dataprovider,
			HashMap<String, StructureRecResult> resultsByDataprovider, StructureRecResult.StructureResultTyp structureResultType) {
		Iterator<Entry<String, StructureRecResult>> iteratorByDataprovider = resultsByDataprovider.entrySet().iterator();
		htmlReport.append("<table>");
		htmlReport.append("<tr><td><b>fieldname</b></td>");
		htmlReport.append("<td><b>distinct values</b></td>");
		htmlReport.append("<td><b>median</b></td>");
		htmlReport.append("<td><b>median by valid samples</b></td>");
		htmlReport.append("<td><b>standard deviation</b></td>");
		htmlReport.append("<td><b>distinct frac complement</b></td>");
		htmlReport.append("<td><b>cdfl 0.5</b></td>");
		htmlReport.append("<td><b>cdfl 0.75</b></td>");
		htmlReport.append("<td><b>lower frac</b></td>");
		htmlReport.append("<td><b>upper frac</b></td>");
		htmlReport.append("<td><b>weighted frac</b></td>");
		htmlReport.append("</tr>");
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormatSymbols.setGroupingSeparator(',');
		DecimalFormat decimalFormat = new DecimalFormat("#,##0.000", decimalFormatSymbols);
		while (iteratorByDataprovider.hasNext()) {
		    Entry<String, StructureRecResult> fieldResult = (Entry<String, StructureRecResult>) iteratorByDataprovider.next();
		    String field = fieldResult.getKey();
		    StructureRecResult fieldResultValues = fieldResult.getValue();
		    StructureRecResultAnalysisResultData resultData = null;
		    if (structureResultType == StructureRecResult.StructureResultTyp.REGEX)
		    	resultData = fieldResultValues.getResultRegEx();
		    if (structureResultType == StructureRecResult.StructureResultTyp.VALUE)
		    	resultData = fieldResultValues.getResultValue();
		    
		    htmlReport.append("<tr>");
		    htmlReport.append("<td>"+field+"</td>");
		    htmlReport.append("<td>"+resultData.getResultDistinctValues()+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultMedian())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultMedianPerVaildSamples())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultSigma())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultDistinctFracComplement())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultCdfl05())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultCdfl075())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultFracOutLower())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultFracOutUpper())+"</td>");
		    htmlReport.append("<td>"+decimalFormat.format(resultData.getResultFracOutWeighted())+"</td>");
		    htmlReport.append("</tr>");
		}
		htmlReport.append("</table>");
		htmlReport.append("<p>");
		htmlReport.append("The following chart shows the data from the table above.<br/>");
		String filename = Qc_graphs.struturendnessDataproviderResultStructuredness(dataprovider, CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, resultsByDataprovider,structureResultType);
		htmlReport.append("<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>");

		htmlReport.append("</p><p>");
		htmlReport.append("The following chart shows the data from the table above.<br/>");
		filename = Qc_graphs.struturendnessDataproviderResultOutliners(dataprovider, CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, resultsByDataprovider,structureResultType);
		htmlReport.append("<img src=\""+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+filename+"\" style=\"width:1000px;\"/>");
		
		htmlReport.append("</p>");
	}

	private void printInputDataInfoReport(String htmlReportGeneral) {
		
		StringBuffer htmlReport = new StringBuffer(htmlReportGeneral);
		htmlReport.append("<h2>Input data</h2>");
		htmlReport.append("This report was generated using the files located at:<ul>" );
        for (int i = 0; i < this.inputDirs.size(); i++) {
        	htmlReport.append("<li>"+this.inputDirs.get(i)+"</li>") ;
		}
        htmlReport.append("</ul><p>");
        int numberRecords = 0;
		for (int i=0;i<DataProvider.values().length; i++)
		{
			numberRecords +=paramDataList.getRecordsPerProvider(DataProvider.values()[i]);
		}
        htmlReport.append("The dataset includes <b>"+numberRecords + "</b> records in <b>"+paramDataList.size() + "</b> files.");
        htmlReport.append("</p>");
        
		htmlReport.append("<h3>Statistics by data provider</h3>");
		htmlReport.append(this.htmlReportInputDataStatisticsDataprovider);
		
		htmlReport.append("<h3>Statistics by input file</h3>");
		htmlReport.append(this.htmlReportInputDataStatisticsResults);
		
		try {
			File fileStatisticRecords = new File(Qc_dataprovider.outputDir+ "dataquality-report-inputdata.html");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			writerStatisticRecords.append(htmlReport);
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void printRDFXMLVisWithJQPlot() {
        try {
        	TransformerFactory factory = TransformerFactory.newInstance();
        	Templates template = factory.newTemplates(new StreamSource(new FileInputStream(this.resourcesDir+"xsl/dqv2html.xsl")));
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
	    copyResourcesHTML();
	    if (this.copyInput) copyResourcesInputXML();
	    copyResource("eexcess.ico");
	    copyResource("eexcess_Logo.jpg");
	}
	
	private void copyResource(String sFile)
	{
		InputStream inStream = null;
		OutputStream outStream = null;
	    try{
	      try {
	    	  File file = new File(this.resourcesDir + sFile);
	    	  inStream = new FileInputStream(file); 
	        byte[] bucket = new byte[32*1024];
	        outStream = new BufferedOutputStream(new FileOutputStream(Qc_dataprovider.outputDir + sFile));
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

	private void copyResourcesCSS() {
		InputStream inStream = null;
		OutputStream outStream = null;
	    try{
	      try {
	    	  File file = new File(this.resourcesDir+"report.css");
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
	
	private void copyResourcesHTML() {
	    try{
	    	File destDir = new File(Qc_dataprovider.outputDir + "jqplot/");
            File srcDir = new File(this.resourcesDir+"jqplot");
	    	FileUtils.copyDirectory(srcDir, destDir);
	    	destDir = new File(Qc_dataprovider.outputDir );
            srcDir = new File(this.resourcesDir+"cssjs");
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
