/*
Copyright (C) 2014 
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
import java.util.Locale;

import eu.eexcess.dataquality.graphs.Qc_graphs;
import eu.eexcess.dataquality.providers.Qc_DDB;
import eu.eexcess.dataquality.providers.Qc_ZBW;
import eu.eexcess.dataquality.providers.Qc_base;
import eu.eexcess.dataquality.providers.Qc_base.SearchType;
import eu.eexcess.dataquality.providers.Qc_eexcess;
import eu.eexcess.dataquality.providers.Qc_europeana;
import eu.eexcess.dataquality.providers.Qc_kimcollect;
import eu.eexcess.dataquality.providers.Qc_mendeley;
import eu.eexcess.dataquality.providers.Qc_wissenmedia;

// Check for data provider
public class Qc_dataprovider {

	private static final String STATISTIC_FILE_FIELD_SEPERATOR = ";";
	private static final String STATISTIC_SYSTEMOUT_FIELD_SEPERATOR = "\t";
	private static final int CHART_WIDTH_HIGH = 1600;
	private static final int CHART_HEIGHT_HIGH = 1200;
	private static final int CHART_WIDTH_MID = 800;
	private static final int CHART_HEIGHT_MID = 600;
	private static final int CHART_WIDTH_LOW = 400;
	private static final int CHART_HEIGHT_LOW = 350;
	Qc_base currentProvider = null;
	Qc_paramDataList paramDataList = new Qc_paramDataList();

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
		PrintStatistics();
	}

	NumberFormat numberFormater = NumberFormat.getNumberInstance( new Locale.Builder().setLanguage("de").setRegion("DE").build());

	private String formatNumber(double number) {
		return numberFormater.format(number);
	}
	
	private void PrintStatistics() {
		try {
			File fileStatisticRecords = new File("statistics-results.csv");
			BufferedWriter writerStatisticRecords = new BufferedWriter(new FileWriter(fileStatisticRecords));
			writerStatisticRecords.write("file"+STATISTIC_FILE_FIELD_SEPERATOR+"provider"+STATISTIC_FILE_FIELD_SEPERATOR+"#records"+STATISTIC_FILE_FIELD_SEPERATOR+"mean fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+"min fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+"max fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean non empty fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean non empty fields per datafields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean empty fields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"mean empty fields per datafields/record"+STATISTIC_FILE_FIELD_SEPERATOR+
					"links/record");
			writerStatisticRecords.newLine();
			System.out.println("file"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"provider"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"#records"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"mean fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"min fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+"max fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean non empty fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean non empty fields per datafields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean empty fields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean empty fields per datafields/record"+STATISTIC_SYSTEMOUT_FIELD_SEPERATOR+
					"mean links/record");
			
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
						+ formatNumber(param.getLinkDataFieldsPerRecord()));
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
						+ formatNumber(param.getLinkDataFieldsPerRecord()));
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
						"mean links/record");
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
						+ formatNumber(paramDataList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i])));
				writerStatisticRecords.newLine();
			}
			writerStatisticRecords.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Qc_graphs.allProviderDataFieldsPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderDataFieldsPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderDataFieldsPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);

		Qc_graphs.allProviderNonEmptyDataFieldsPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);

		Qc_graphs.allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);

		Qc_graphs.allProviderLinksPerRecordsBarChart(CHART_WIDTH_LOW, CHART_HEIGHT_LOW, paramDataList);
		Qc_graphs.allProviderLinksPerRecordsBarChart(CHART_WIDTH_MID, CHART_HEIGHT_MID, paramDataList);
		Qc_graphs.allProviderLinksPerRecordsBarChart(CHART_WIDTH_HIGH, CHART_HEIGHT_HIGH, paramDataList);

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
					if (currentProvider.IsProviderRecord() == true) {
						currentProvider.getDataProvider();
						currentProvider.countDataFields(SearchType.allDataFields);
						currentProvider.countDataFields(SearchType.notEmptyDataFields);
						currentProvider.countDataFields(SearchType.linkDataFields);
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

}
