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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.eexcess.dataquality.graphs.Qc_graphs;
import eu.eexcess.dataquality.output.dataqualityvocabulary.DataQualityVocabularyRDFWriter;
import eu.eexcess.dataquality.providers.Qc_DDB;
import eu.eexcess.dataquality.providers.Qc_ZBW;
import eu.eexcess.dataquality.providers.Qc_base;
import eu.eexcess.dataquality.providers.Qc_base.SearchType;
import eu.eexcess.dataquality.providers.Qc_eexcess;
import eu.eexcess.dataquality.providers.Qc_europeana;
import eu.eexcess.dataquality.providers.Qc_kimcollect;
import eu.eexcess.dataquality.providers.Qc_mendeley;
import eu.eexcess.dataquality.providers.Qc_wissenmedia;
import eu.eexcess.dataquality.structure.StructureRecResult;
import eu.eexcess.dataquality.structure.StructureRecognizer;

// Check for data provider
public class Qc_dataprovider {

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
	
	HashMap<String,HashMap<String, StructureRecResult>> structurednessResults = new HashMap<String, HashMap<String, StructureRecResult>>();

	// XML files of known partners
	public enum DataProvider {
		KIMCollect, ZBW, Europeana, Wissenmedia, Mendeley, EEXCESS, DDB, unknown
	}

	public void InputParams(String[] sParams) {
		for (int i = 0; i < sParams.length; i++) {
			File f = new File(sParams[i]);
			if (f.isFile() == true && f.isDirectory() == false) {
				checkDataProviderFile(sParams[i]);
			} else if (f.isDirectory() == true) {
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
		printStatistics();
		printStructuredness();
	}


	


	NumberFormat numberFormater = NumberFormat.getNumberInstance( new Locale.Builder().setLanguage("en").setRegion("GB").build());

	private String formatNumber(double number) {
		return numberFormater.format(number);
	}
	
	private void printStatistics() {
		try {
			File fileStatisticRecords = new File("statistics-results.csv");
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
		try {
			File fileStatisticRecords = new File("statistics-dataprovider.csv");
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
			}
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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

				case KIMCollect:
					currentProvider = new Qc_kimcollect();
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
						currentProvider.countDataFields(SearchType.allDataFields);
						currentProvider.countDataFields(SearchType.notEmptyDataFields);
						currentProvider.countDataFields(SearchType.linkDataFields);
						//uriCheck
						currentProvider.countDataFields(SearchType.uriDataFields);
						Qc_params param = currentProvider.getParam();
						paramDataList.addParam(param);
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
				if (actFileName.contains(actDataProvider))
					fileNameByDataProvider.add(actFileName);
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
				String actFieldName = fieldXPaths.get(i).substring(fieldXPaths.get(i).lastIndexOf("/"));
				ArrayList<String> values = new ArrayList<String>();
//				System.out.println(fieldXPaths.get(i));
				for (String actFileName : actProviderFileList) {
					// for each file
					qcBase.setXmlFileName(actFileName);
					NodeList nodes = qcBase.getNodesListByXPath(fieldXPaths.get(i));
					for (int count = 0; count < nodes.getLength(); count++) {
						if (nodes.item(count).getNodeType() == Node.ELEMENT_NODE) {
//							System.out.println(nodes.item(count).getTextContent());
							values.add(nodes.item(count).getTextContent());
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
	public static String DATAPROVIDER_EUROPEANA ="Europeana";
	public static String DATAPROVIDER_DDB ="DeutscheDigitaleBibliothek";
	public static String DATAPROVIDER_MENDELEY ="Mendeley";
	public static String DATAPROVIDER_WISSENMEDIA ="Wissenmedia";
	public static String DATAPROVIDER_ZBW ="ZBW";
	
	public Qc_base createProviderQC(String dataprovider){
		if (dataprovider.equals(DATAPROVIDER_WISSENMEDIA))
			return new Qc_wissenmedia();
		if (dataprovider.equals(DATAPROVIDER_DDB))
			return new Qc_DDB();
		if (dataprovider.equals(DATAPROVIDER_EUROPEANA))
			return new Qc_europeana();
		if (dataprovider.equals(DATAPROVIDER_KIMCOLLECT))
			return new Qc_kimcollect();
		if (dataprovider.equals(DATAPROVIDER_MENDELEY))
			return new Qc_mendeley();
		if (dataprovider.equals(DATAPROVIDER_ZBW))
			return new Qc_ZBW();
		if (dataprovider.equals(DATAPROVIDER_WISSENMEDIA))
			return new Qc_wissenmedia();
		
		return null;
	}
	
	private void printStructuredness() {
		Iterator<Entry<String, HashMap<String, StructureRecResult>>> iteratorDataprovider = structurednessResults.entrySet().iterator();
	    while (iteratorDataprovider.hasNext()) {
	        Entry<String, HashMap<String, StructureRecResult>> entry = iteratorDataprovider.next();
	        String dataprovider = entry.getKey();
	        HashMap<String, StructureRecResult> resultsByDataprovider = entry.getValue();
            System.out.println("Dataprovider:" + dataprovider);
	        
	        
	        Iterator iteratorByDataprovider = resultsByDataprovider.entrySet().iterator();
	        while (iteratorByDataprovider.hasNext()) {
	            Entry<String, StructureRecResult> fieldResult = (Entry<String, StructureRecResult>) iteratorByDataprovider.next();
	            String field = fieldResult.getKey();
	            StructureRecResult result = fieldResult.getValue();
	            System.out.println("field:"+field);
	            System.out.println("result:\n"+result.toString());
	        }
	    }
	}
}
