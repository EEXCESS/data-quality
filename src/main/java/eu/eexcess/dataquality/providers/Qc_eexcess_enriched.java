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

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;
import eu.eexcess.dataquality.Qc_params;

public class Qc_eexcess_enriched extends Qc_base {
	
	public Qc_eexcess_enriched()
	{
		recordSeparator = "/*[local-name()='RDF']/*[local-name()='Proxy']";
		dataProvider = DataProvider.unknown;
		additionalProviderCondition = "<eexcess:Agent rdf:about"; 
		
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
						System.out.println(nodeChilds.item(i).getNodeName());
						if (lsBlackList.contains(nodeChilds.item(i).getNodeName()) == false)
						{
							nReturn++;
						}
						else if (nodeChilds.item(i).getNodeName() == "ore:proxyIn")
						{
							if (nodeChilds.item(i).hasChildNodes())
							{
								NodeList nodeListAggr = nodeChilds.item(i).getChildNodes();
								for (int j=0; j<nodeListAggr.getLength(); j++)
								{
									if (nodeListAggr.item(j).getNodeType() == Node.ELEMENT_NODE)
									{
										if (nodeListAggr.item(j).getNodeName() == "ore:Aggregation")
										{
											System.out.println(nodeListAggr.item(j).getNodeName());
											nReturn += countDataFieldsNode(nodeListAggr.item(j), searchType);
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
	
	// check if XML file is from current data provider
	public boolean isProviderRecord() {
		boolean bReturn = false;
		if (xmlFileName.length() > 0) {
			try {
				if (xmlFileName.contains("KIMPortal-Enrichment-done") == true)
				{
					dataProvider = DataProvider.KIMCollect_enriched;
				}
				else if (xmlFileName.contains("Europeana-Enrichment-done") == true)
				{
					dataProvider = DataProvider.Europeana_enriched;
				}
				else if (xmlFileName.contains("ZBW-Enrichment-done") == true)
				{
					dataProvider = DataProvider.ZBW_enriched;
				}
				else if (xmlFileName.contains("Deutsche Digitale Bibliothek-Enrichment-done") == true)
				{
					dataProvider = DataProvider.DDB_enriched;
				}
				else if (xmlFileName.contains("Mendeley-Enrichment-done") == true)
				{
					dataProvider = DataProvider.Mendeley_enriched;
				}
				else
				{
					System.out.println(xmlFileName);
				}
				
				File f = new File(xmlFileName);
				if (f.exists() == true && f.isDirectory() == false) {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(f);

					doc.getDocumentElement().normalize();
					if (isProviderRecordCheckAdditional(doc) == true)
					{
						XPathFactory xPathfactory = XPathFactory.newInstance();
						XPath xpath = xPathfactory.newXPath();
						XPathExpression expr = xpath.compile(recordSeparator);
						nodelistRecords = (NodeList) expr.evaluate(doc,
								XPathConstants.NODESET);
						if (nodelistRecords.getLength() > 0) {
							param = new Qc_params(dataProvider);
							param.setRecordCount(GetRecordCountProxy(nodelistRecords));
							param.setXmlPath(xmlFileName);
							bReturn = true;
						} else {
							if (xmlFileName.contains(dataProvider.toString()))
							{
								param = new Qc_params(dataProvider);
								param.setRecordCount(0);
								param.setXmlPath(xmlFileName);
								bReturn = true;
							}
						}
					}
				}
			} catch (SAXParseException err) {
				System.out.println("** Parsing error" + ", line "
						+ err.getLineNumber() + ", uri " + err.getSystemId());
				System.out.println(" " + err.getMessage());
			} catch (SAXException e) {
				Exception x = e.getException();
				((x == null) ? e : x).printStackTrace();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
		return bReturn;
	}

	int GetRecordCountProxy (NodeList nodeList)
	{
		int nReturn = 0;
		for (int i=0;i<nodeList.getLength();i++)
		{
			if (nodeList.item(i).hasAttributes() == true )
			{
				NamedNodeMap nodesAttr = nodeList.item(i).getAttributes();
				for (int j=0; j<nodesAttr.getLength(); j++)
				{
					if (nodesAttr.item(j).getNodeName() == "rdf:about" && nodesAttr.item(j).getNodeValue().endsWith("/enrichedProxy/"))
					{
						nReturn++;
					}
				}
			}
		}
		return nReturn;
	}
}
