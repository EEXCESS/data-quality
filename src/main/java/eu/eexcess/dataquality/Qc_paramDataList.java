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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;

public class Qc_paramDataList {

	List<Qc_params> paramList = new ArrayList<Qc_params>();

	public void addParam(Qc_params param) {

		paramList.add(param);
	}

	public Qc_params get(int i) {
		if (i < paramList.size()) {
			return paramList.get(i);
		}
		return null;
	}

	public int size() {
		return paramList.size();
	}

	public int getRecordsPerProvider(DataProvider provider) {
		int nReturn = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (provider == null || paramList.get(i).getProvider() == provider) {
				nReturn += paramList.get(i).getRecordCount();
			}
		}
		return nReturn;
	}

	public double getDataFieldsPerRecordsPerProvider(DataProvider provider) {
		double nReturn = 0, nFiles = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				nReturn += paramList.get(i).getDataFieldsPerRecord();
				nFiles++;
			}
		}

		if (nFiles > 0) {
			nReturn = nReturn / (double) nFiles;
		}
		return nReturn;
	}
	
	
	public int getMinDataFieldsPerRecordsPerProvider(DataProvider provider) {
		int nReturn = -1;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider) {
				if (nReturn == -1 && paramList.get(i).getMinDataFieldsPerRecord() > 0) {
					nReturn = paramList.get(i).getMinDataFieldsPerRecord();
				} else if (paramList.get(i).getMinDataFieldsPerRecord() < nReturn && paramList.get(i).getMinDataFieldsPerRecord() > 0) {
					nReturn = paramList.get(i).getMinDataFieldsPerRecord();
				}
			}
		}
		if (nReturn == -1) {
			nReturn = 0;
		}
		return nReturn;
	}

	public int getMaxDataFieldsPerRecordsPerProvider(DataProvider provider) {
		int nReturn = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider) {
				if (paramList.get(i).getMaxDataFieldsPerRecord() > nReturn) {
					nReturn = paramList.get(i).getMaxDataFieldsPerRecord();
				}
			}
		}
		return nReturn;
	}

	public boolean hasProviderData(
			DataProvider provider) {
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider) {
				return true;
			}
		}
		return false;
	}

	public double getNonEmptyDataFieldsPerRecordsPerProvider(
			DataProvider provider) {
		double nReturn = 0, nFiles = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				nReturn += paramList.get(i).getNonEmptyDataFieldsPerRecord();
				nFiles++;
			}
		}

		if (nFiles > 0) {
			nReturn = nReturn / (double) nFiles;
		}
		return nReturn;
	}
	
	public double getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(
			DataProvider provider) {
		double nReturn = 0, nFiles = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				nReturn += paramList.get(i).getNonEmptyDataFieldsPerRecordPerDatafields();
				nFiles++;
			}
		}

		if (nFiles > 0) {
			nReturn = nReturn / (double) nFiles;
		}
		return nReturn;
	}

	public double getEmptyDataFieldsPerRecordsPerProvider(
			DataProvider provider) {
		double nReturn = 0, nFiles = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				nReturn += paramList.get(i).getEmptyDataFieldsPerRecord();
				nFiles++;
			}
		}

		if (nFiles > 0) {
			nReturn = nReturn / (double) nFiles;
		}
		return nReturn;
	}

	public double getEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(
			DataProvider provider) {
		double nReturn = 0, nFiles = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				nReturn += paramList.get(i).getEmptyDataFieldsPerRecordPerDatafields();
				nFiles++;
			}
		}

		if (nFiles > 0) {
			nReturn = nReturn / (double) nFiles;
		}
		return nReturn;
	}

	public double getLinkDataFieldsPerRecordsPerProvider(
			DataProvider provider) {
		double nReturn = 0, nFiles = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				nReturn += paramList.get(i).getLinkDataFieldsPerRecord();
				nFiles++;
			}
		}

		if (nFiles > 0) {
			nReturn = nReturn / (double) nFiles;
		}
		return nReturn;
	}
	
	public double getNumberOfLinkDataFieldsPerProvider(
			DataProvider provider) {
		double nReturn = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				//nReturn += paramList.get(i).getLinkDataFieldsPerRecord();
				nReturn += paramList.get(i).getNumberOfAllLinkDataFields();
			}			
		}

//		if (nFiles > 0) {
//			nReturn = nReturn / (double) nFiles;
//		}
		return nReturn;
	}

	
	
	public double getAccesibleLinksPerRecordsPerProvider(
			DataProvider provider) {
		double nReturn = 0, nFiles = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				nReturn += paramList.get(i).getAccessibleLinksDataFieldsPerRecord();
				nFiles++;
			}
		}

		if (nFiles > 0) {
			nReturn = nReturn / (double) nFiles;
		}
		return nReturn;
	}
	
	public double getNumberOfAccesibleLinksPerProvider(
			DataProvider provider) {
		double nReturn = 0;
		for (int i = 0; i < paramList.size(); i++) {
			if (paramList.get(i).getProvider() == provider && paramList.get(i).getRecordCount() > 0) {
				//nReturn += paramList.get(i).getAccessibleLinksDataFieldsPerRecord();
				nReturn += paramList.get(i).getNumberOfAllAccessibleLinks();
			}
		}

//		if (nFiles > 0) {
//			nReturn = nReturn / (double) nFiles;
//		}
		return nReturn;
	}

	public ArrayList<String> getAllTrustedLinks()
	{
		ArrayList<String> saLinks = new ArrayList<String>();
		for (int i = 0; i < paramList.size(); i++)
		{
			for (String sLink : paramList.get(i).getTrustedLinks())
			{
				if (!saLinks.contains(sLink))
				{
					saLinks.add(sLink);
				}
			}
		}
		Collections.sort(saLinks);
		return saLinks;
	}
	
	public ArrayList<String> getAllUnknownLinks()
	{
		ArrayList<String> saLinks = new ArrayList<String>();
		for (int i = 0; i < paramList.size(); i++)
		{
			for (String sLink : paramList.get(i).getAllUnknownLinks())
			{
				if (!saLinks.contains(sLink))
				{
					saLinks.add(sLink);
				}
			}
		}
		Collections.sort(saLinks);
		return saLinks;
	}
	
	public String getTrustedLinkCountPerLinkAndProvider(DataProvider provider, String sLink)
	{
		String sCount = "";
		if (provider != null)
		{
			double nCount=0;
			for (int i = 0; i < paramList.size(); i++) {
				if (paramList.get(i).getProvider() == provider)
				{
					nCount += paramList.get(i).getTrustedLinkCountPerLink(sLink);
				}
			}
			if (nCount > 0)
			{
				nCount = nCount / getRecordsPerProvider(provider);
				sCount = String.valueOf(nCount);
			}
		}
		return sCount;
	}
	
	public String getAllUnknownLinkCountPerLinkAndProvider(DataProvider provider, String sLink)
	{
		String sCount = "";
		if (provider != null)
		{
			double nCount=0;
			for (int i = 0; i < paramList.size(); i++) {
				if (paramList.get(i).getProvider() == provider)
				{
					nCount += paramList.get(i).getAllUnknownLinkCountPerLink(sLink);
				}
			}
			if (nCount > 0)
			{
				nCount = nCount / getRecordsPerProvider(provider);
				sCount = String.valueOf(nCount);
			}
		}
		return sCount;
	}
}
