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
package eu.eexcess.dataquality.providers;

import java.io.File;

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

import eu.eexcess.dataquality.Qc_interface;
import eu.eexcess.dataquality.Qc_params;
import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;

public class Qc_base implements Qc_interface {

	public enum SearchType {
		allDataFields, notEmptyDataFields, linkDataFields
	}

	String xmlFileName = "";
	DataProvider dataProvider = DataProvider.unknown;
	Qc_params param = null;

	public DataProvider getDataProvider() {
		return dataProvider;
	}

	public String getXmlFileName() {
		return xmlFileName;
	}

	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	// - - - - - - - - - recordSeparator - - - - - - - - - -
	String recordSeparator = "";

	public String getRecordSeparator() {
		return recordSeparator;
	}

	public void setRecordSeparator(String recordSeparator) {
		this.recordSeparator = recordSeparator;
	}

	// XML node list of records
	NodeList nodelistRecords = null;

	// check if XML file is from current data provider
	public boolean IsProviderRecord() {
		boolean bReturn = false;
		if (xmlFileName.length() > 0) {
			try {
				File f = new File(xmlFileName);
				if (f.exists() == true && f.isDirectory() == false) {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(f);

					doc.getDocumentElement().normalize();

					XPathFactory xPathfactory = XPathFactory.newInstance();
					XPath xpath = xPathfactory.newXPath();
					XPathExpression expr = xpath.compile(recordSeparator);
					nodelistRecords = (NodeList) expr.evaluate(doc,
							XPathConstants.NODESET);
					if (nodelistRecords.getLength() > 0) {
						param = new Qc_params(dataProvider);
						param.setRecordCount(nodelistRecords.getLength());
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
			}
		}
	}

	private int countDataFieldsNode(Node cNode, SearchType searchType) {
		int nReturn = 0;
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
					if (nodeChilds.item(i).getTextContent().trim().length() > 0)
					{
						nReturn++;
					}
				}
			}
		} else if (searchType == SearchType.linkDataFields) {
			for (int i = 0; i < nodeChilds.getLength(); i++) {
				Node actNode = nodeChilds.item(i);
				nReturn = countLinksInNodes(nReturn, actNode);
			}
		}
		return nReturn;
	}

	protected int countLinksInNodes(int nReturn, Node actNode) {
		if (actNode.getNodeType() == Node.ELEMENT_NODE) {
			if (actNode.getTextContent() != null &&
					(
					actNode.getTextContent().toLowerCase().startsWith("http://") || 
					actNode.getTextContent().toLowerCase().startsWith("https://")
					)
					){
				nReturn++;
			} else {
				
			}
			if (actNode.getAttributes() != null && actNode.getAttributes().getLength() > 0)
			{
				NamedNodeMap attributes = actNode.getAttributes();
				for (int attributesIndex = 0; attributesIndex < actNode.getAttributes().getLength() ; attributesIndex++) {
					Node attribute = attributes.item(attributesIndex);
					String value = attribute.getNodeValue();
					if (value != null && !value.isEmpty() && (value.toLowerCase().startsWith("http://") || value.toLowerCase().startsWith("https://")))
					{
						nReturn++;
					}
				}
			}
			if (actNode.hasChildNodes())
			{
				for (int i = 0; i < actNode.getChildNodes().getLength(); i++) {
					Node actNodeChild = actNode.getChildNodes().item(i);
					nReturn = countLinksInNodes(nReturn, actNodeChild);
				}
			}
		}
		return nReturn;
	}

	@Override
	public int getRecordsCount() {
		if (param != null) {
			return param.getRecordCount();
		}
		return 0;
	}

	@Override
	public Qc_params getParam() {
		return param;
	}
}
