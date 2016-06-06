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

public class Qc_kimcollect extends Qc_base {
	public Qc_kimcollect() {
		recordSeparator = "/response/result/doc";
		dataProvider = DataProvider.KIMCollect;
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
			} else if (searchType == SearchType.uriDataFields){
				param.addAccessibleLinksDataFieldsPerRecord(countDataFieldsNode(
						nodelistRecords.item(i), searchType));				
			}
		}
	}

	private int countDataFieldsNode(Node cNode, SearchType searchType) {
		int nReturn = 0;
		List<String> sNodeNames = new ArrayList<String>();
		NodeList nodeChilds = cNode.getChildNodes();
		if (searchType == SearchType.allDataFields) {
			for (int i = 0; i < nodeChilds.getLength(); i++) {
				if (nodeChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					nReturn++;
				}
			}
		} else if (searchType == SearchType.notEmptyDataFields) {
			for (int i = 0; i < nodeChilds.getLength(); i++) {
				if (nodeChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if (sNodeNames.contains(nodeChilds.item(i).getNodeName()) == false) {
						sNodeNames.add(cNode.getChildNodes().item(i).getNodeName());
						if (cNode.getChildNodes().item(i).getTextContent().trim().length() > 0) {
							nReturn++;
						}
					}
				}
			}
		} else if (searchType == SearchType.linkDataFields) {
			for (int i = 0; i < nodeChilds.getLength(); i++) {
				Node actNode = nodeChilds.item(i);
				nReturn = countLinksInNodes(nReturn, actNode, true);
			}
		}//uriCheck
		else if (searchType == SearchType.uriDataFields) {
			for (int i = 0; i < nodeChilds.getLength(); i++) {
				Node actNode = nodeChilds.item(i);
				nReturn = countAccessibleLinks(nReturn, actNode, true);
			}
		}
		return nReturn;
	}
}
