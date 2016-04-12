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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;

public class Qc_params {

	Qc_trustedLinks trustedLinks = new Qc_trustedLinks();
	
	public Qc_params(DataProvider provider) {
		this.provider = provider;
	}
	
	public void addTrustedLink(String sLink)
	{
		trustedLinks.addTrustedLink(sLink);
	}

	public HashMap<String,Integer> getTrustedLinksCount()
	{
		return trustedLinks.getTrustedLinksCount();
	}
	
	// selected data provider
	DataProvider provider = DataProvider.unknown;

	public DataProvider getProvider() {
		return provider;
	}

	public void setProvider(DataProvider provider) {
		this.provider = provider;
	}

	String recordSeparator = "";

	public String getRecordSeparator() {
		return recordSeparator;
	}

	public void setRecordSeparator(String recordSeparator) {
		this.recordSeparator = recordSeparator;
	}

	// - - - - - - - - - number of records per XML file - - - - - - - - -
	int recordCount = 0;

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	// - - - - - - - - - XML filename - - - - - - - - -
	String xmlFileName = "";

	public String getXmlPath() {
		return xmlFileName;
	}

	public String getXmlFileName() {
		File f = new File(xmlFileName);
		if (f.isFile() == true) {
			return f.getName();
		}
		return "";
	}

	public void setXmlPath(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	// - - - - - - - - - - data fields per record - - - - - - - - - - -
	List<Integer> dataFieldsPerRecord = new ArrayList<Integer>();

	public void addDataFieldsPerRecord(int nCount) {
		dataFieldsPerRecord.add(nCount);
	}

	// - - - - - - - - - - not empty data fields per record - - - - - - - - - -
	// -
	List<Integer> dataNonEmptyFieldsPerRecord = new ArrayList<Integer>();

	public void addNonEmptyDataFieldsPerRecord(int nCount) {
		dataNonEmptyFieldsPerRecord.add(nCount);
	}

	// - - - - - - - - - - data fields with link per record - - - - - - - - - -
	// -
	List<Integer> dataLinkFieldsPerRecord = new ArrayList<Integer>();

	public void addLinkDataFieldsPerRecord(int nCount) {
		dataLinkFieldsPerRecord.add(nCount);
	}
	
	// - - - - - - - - - - data fields with reachable link per record - - - - - - - - - -
	// -
	List<Integer> dataLinkFieldsAccessiblePerRecord = new ArrayList<Integer>();

	public void addAccessibleLinksDataFieldsPerRecord(int nCount) {
		dataLinkFieldsAccessiblePerRecord.add(nCount);
	}
	
	

	// - - - - - - - - - - - data fields per record - - - - - - - - -
	public double getDataFieldsPerRecord() {
		double fReturn = 0;
		if (recordCount > 0) {
			for (int i = 0; i < dataFieldsPerRecord.size(); i++) {
				fReturn += dataFieldsPerRecord.get(i);
			}
			fReturn = fReturn / (double) recordCount;
		}
		return fReturn;
	}

	// - - - - - - - - - - - - - non empty data fields per record - - - - - - -
	// - - -
	public double getNonEmptyDataFieldsPerRecord() {
		double fReturn = 0;
		if (recordCount > 0) {
			for (int i = 0; i < dataNonEmptyFieldsPerRecord.size(); i++) {
				fReturn += dataNonEmptyFieldsPerRecord.get(i);
			}
			fReturn = fReturn / (double) recordCount;
//			fReturn = Math.round(fReturn * 100) / 100;
		}
		return fReturn;
	}

	public double getNonEmptyDataFieldsPerRecordPerDatafields() {
		double datafields = getDataFieldsPerRecord();
		if (datafields != 0 )
			return getNonEmptyDataFieldsPerRecord() / datafields;
		else  
			return 0;
	}

	public int getMinDataFieldsPerRecord() {
		int nReturn = -1;
		for (int i = 0; i < dataFieldsPerRecord.size(); i++) {
			if (nReturn == -1 && dataFieldsPerRecord.get(i) > 0) {
				nReturn = dataFieldsPerRecord.get(i);
			} else if (dataFieldsPerRecord.get(i) < nReturn && dataFieldsPerRecord.get(i) > 0) {
				nReturn = dataFieldsPerRecord.get(i);
			}
		}
		if (nReturn == -1) {
			nReturn = 0;
		}
		return nReturn;
	}

	public int getMaxDataFieldsPerRecord() {
		int nReturn = 0;
		for (int i = 0; i < dataFieldsPerRecord.size(); i++) {
			if (dataFieldsPerRecord.get(i) > nReturn) {
				nReturn = dataFieldsPerRecord.get(i);
			}
		}
		return nReturn;
	}

	public double getEmptyDataFieldsPerRecord() {
		return getDataFieldsPerRecord() - getNonEmptyDataFieldsPerRecord();
	}

	public double getEmptyDataFieldsPerRecordPerDatafields() {
		double datafields = getDataFieldsPerRecord();
		if (datafields != 0 )
			return (getDataFieldsPerRecord() - getNonEmptyDataFieldsPerRecord()) / getDataFieldsPerRecord();
		else  
			return 0;
	}

	// - - - - - - - - - - - - - links per record - - - - - - -
	// - - -
	public double getLinkDataFieldsPerRecord() {
		double fReturn = 0;
		if (recordCount > 0) {
			for (int i = 0; i < dataLinkFieldsPerRecord.size(); i++) {
				fReturn += dataLinkFieldsPerRecord.get(i);
			}
			fReturn = fReturn / (double) recordCount;
//			fReturn = Math.round(fReturn * 100) / 100;
		}
		return fReturn;
	}
	
	// - - - - - - - - - - - - - number of all links - - - - - - -
	// - - -
	public double getNumberOfAllLinkDataFields() {
		double fReturn = 0;
		if (recordCount > 0) {
			for (int i = 0; i < dataLinkFieldsPerRecord.size(); i++) {
				fReturn += dataLinkFieldsPerRecord.get(i);
			}
		}
		return fReturn;
	}

	// - - - - - - - - - - - - - number of accessible links - - - - - - -
	// - - -
	public double getNumberOfAllAccessibleLinks() {
		double fReturn = 0;
		if (recordCount > 0) {
			for (int i = 0; i < dataLinkFieldsAccessiblePerRecord.size(); i++) {
				fReturn += dataLinkFieldsAccessiblePerRecord.get(i);
			}
//			fReturn = fReturn / (double) recordCount;
//			fReturn = Math.round(fReturn * 100) / 100;
		}
		return fReturn;
	}

	// - - - - - - - - - - - - - accessible non empty data fields per record - - - - - - -
	// - - -
	public double getAccessibleLinksDataFieldsPerRecord() {
		double fReturn = 0;
		if (recordCount > 0) {
			for (int i = 0; i < dataLinkFieldsAccessiblePerRecord.size(); i++) {
				fReturn += dataLinkFieldsAccessiblePerRecord.get(i);
			}
			fReturn = fReturn / (double) recordCount;
//			fReturn = Math.round(fReturn * 100) / 100;
		}
		return fReturn;
	}
	

	
	
}
