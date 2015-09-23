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
package eu.eexcess.dataquality.providers;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;
import eu.eexcess.dataquality.providers.Qc_base.SearchType;

public class Qc_ZBW extends Qc_base {
	public Qc_ZBW() {
		recordSeparator = "/doc/hits/hit";
		dataProvider = DataProvider.ZBW;
	}

	// Counts dataFields
	public void countDataFields(SearchType searchType) {
		for (int i = 0; i < nodelistRecords.getLength(); i++) {
			if (searchType == SearchType.allDataFields) {
				param.addDataFieldsPerRecord(countDataFieldsNode(
						nodelistRecords.item(i), searchType));
			} else if (searchType == SearchType.notEmptyDataFields) {
				param.addNonEmptyDataFieldsPerRecord(countDataFieldsNode(
						nodelistRecords.item(i), searchType));
			} else if (searchType == SearchType.linkDataFields) {
				param.addLinkDataFieldsPerRecord(countDataFieldsNode(
						nodelistRecords.item(i), searchType));
			} //uriCheck
			  else if (searchType == SearchType.uriDataFields){
				param.addAccessibleLinksDataFieldsPerRecord(countDataFieldsNode(
						nodelistRecords.item(i), searchType));				
			}			
		}
	}

	private int countDataFieldsNode(Node cNode, SearchType searchType) {
		int nReturn = 0;
		List<String> sNodeNames = new ArrayList<String>();

		if (searchType == SearchType.allDataFields) {
			NodeList listFirstChild = cNode.getChildNodes();
			if (listFirstChild.getLength() > 0) {
				for (int j = 0; j < listFirstChild.getLength(); j++) {
					NodeList listDataChilds = listFirstChild.item(j)
							.getChildNodes();

					if (listFirstChild.item(j).getNodeType() == Node.ELEMENT_NODE) {
						for (int i = 0; i < listDataChilds.getLength(); i++) {
							if (sNodeNames.contains(listDataChilds.item(i)
									.getNodeName()) == false) {

								sNodeNames.add(listDataChilds.item(i)
										.getNodeName());
								nReturn++;
							}
						}
					}
				}
			}

		} else if (searchType == SearchType.notEmptyDataFields) {
			NodeList listFirstChild = cNode.getChildNodes();
			if (listFirstChild.getLength() > 0) {
				for (int j = 0; j < listFirstChild.getLength(); j++) {
					NodeList listDataChilds = listFirstChild.item(j)
							.getChildNodes();
					for (int i = 0; i < listDataChilds.getLength(); i++) {
						if (sNodeNames.contains(listDataChilds.item(i)
								.getNodeName()) == false) {
							if (listDataChilds.item(i).getTextContent()
									.length() > 0) {
								sNodeNames.add(listDataChilds.item(i)
										.getNodeName());
								nReturn++;
							}
						}
					}
				}
			}
		}
		return nReturn;
	}
}
