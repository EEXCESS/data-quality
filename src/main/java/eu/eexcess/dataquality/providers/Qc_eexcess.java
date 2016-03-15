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
package eu.eexcess.dataquality.providers;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;

public class Qc_eexcess extends Qc_base {
	public Qc_eexcess()
	{
		recordSeparator = "/*[local-name()='RDF']/*[local-name()='Proxy']";
		dataProvider = DataProvider.unknown;
		notProviderCondition = "<eexcess:Agent rdf:about";
		
		// Blacklist entries:
		lsBlackList.add("ore:proxyFor");
		lsBlackList.add("ore:proxyIn");
	}
	
	ArrayList<String> lsBlackList = new ArrayList<String>();
	
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
		if (cNode != null)
		{
			NodeList nodeChilds = cNode.getChildNodes();
			if (searchType == SearchType.allDataFields || searchType == SearchType.notEmptyDataFields)
			{
				for (int i = 0; i < nodeChilds.getLength(); i++) {
					if (nodeChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
						if (lsBlackList.contains(nodeChilds.item(i).getNodeName()) == false)
						{
							nReturn++;
						}
						else if (nodeChilds.item(i).getNodeName() == "ore:proxyIn")
						{
							if (nodeChilds.item(i).hasAttributes() && nodeChilds.item(i).getAttributes().getNamedItem("rdf:resource") != null)
							{
								// System.out.println(nodeChilds.item(i).getAttributes().getNamedItem("rdf:resource").getNodeValue());
								String linkAggr = nodeChilds.item(i).getAttributes().getNamedItem("rdf:resource").getNodeValue();
								String oreAggregation = "/*[local-name()='RDF']/*[local-name()='Aggregation']";
								NodeList nodeAggr = this.getNodesListByXPath(oreAggregation);
								if (nodeAggr.getLength() > 0)
								{
									for (int j=0; j<nodeAggr.getLength(); j++)
									{
										if (nodeToString(nodeAggr.item(j),linkAggr) == true)
										{
											nReturn += countDataFieldsNode(nodeAggr.item(j), searchType);
											break;
										}
									}
								}
							}
						}
					}
				}
			}
			else if (searchType == SearchType.linkDataFields) {
				for (int i = 0; i < nodeChilds.getLength(); i++) {
					nReturn = countLinksInNodes(nReturn, nodeChilds.item(i), false);
				}
			}
			else if (searchType == SearchType.uriDataFields) {
				for (int i = 0; i < nodeChilds.getLength(); i++) {
					nReturn = countAccessibleLinks(nReturn, nodeChilds.item(i), false);
				}
			}
		}
		return nReturn;
	}
	
}
