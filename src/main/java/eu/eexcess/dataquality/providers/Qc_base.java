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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;
import eu.eexcess.dataquality.Qc_interface;
import eu.eexcess.dataquality.Qc_params;

public class Qc_base implements Qc_interface {

	public enum SearchType {
		allDataFields, notEmptyDataFields, linkDataFields
		//uriCheck
		, uriDataFields
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

	String xpathsToFieldsFromRecordSeparator = "";
	
	public String getXpathsToFieldsFromRecordSeparator() {
		return xpathsToFieldsFromRecordSeparator;
	}

	public void setXpathsToFieldsFromRecordSeparator(
			String xpathsToFieldsFromRecordSeparator) {
		this.xpathsToFieldsFromRecordSeparator = xpathsToFieldsFromRecordSeparator;
	}

	public String getRecordSeparator() {
		return recordSeparator;
	}

	public void setRecordSeparator(String recordSeparator) {
		this.recordSeparator = recordSeparator;
	}

	// XML node list of records
	NodeList nodelistRecords = null;

	public NodeList getNodelistRecords() {
		return nodelistRecords;
	}

	public void setNodelistRecords(NodeList nodelistRecords) {
		this.nodelistRecords = nodelistRecords;
	}

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
			}//uriCheck
			  else if (searchType == SearchType.uriDataFields) {
					param.addAccessibleLinksDataFieldsPerRecord(countDataFieldsNode(
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
		} //uriCheck 
		  else if (searchType == SearchType.uriDataFields) {
			for (int i = 0; i < nodeChilds.getLength(); i++) {
				Node actNode = nodeChilds.item(i);
				nReturn = countAccessibleLinks(nReturn, actNode);
			}
		}
		return nReturn;
	}

	/*
	 * count Links in nodes
	 * */
	protected int countLinksInNodes(int nReturn, Node actNode) {
		
		if (actNode.getNodeType() == Node.ELEMENT_NODE) {
			String actNodeTextContent = actNode.getTextContent();
			if (actNodeTextContent != null &&
					(
					actNodeTextContent.toLowerCase().startsWith("http://") || 
					//uriCheck additional uri-schemes
					actNodeTextContent.toLowerCase().startsWith("https://") ||
					actNodeTextContent.toLowerCase().startsWith("ftp://")
//					actNodeTextContent.toLowerCase().startsWith("mailto://") ||
//					actNodeTextContent.toLowerCase().startsWith("file://") ||
//					actNodeTextContent.toLowerCase().startsWith("data://")
					)
				){
				nReturn++;
			} 
			
			if (actNode.getAttributes() != null && actNode.getAttributes().getLength() > 0)
			{
				NamedNodeMap attributes = actNode.getAttributes();
				for (int attributesIndex = 0; attributesIndex < actNode.getAttributes().getLength() ; attributesIndex++) {
					Node attribute = attributes.item(attributesIndex);
					String value = attribute.getNodeValue();
					if (value != null && !value.isEmpty() && 
							(value.toLowerCase().startsWith("http://") || 
							 value.toLowerCase().startsWith("https://") || 
							 value.toLowerCase().startsWith("ftp://")
//							 value.toLowerCase().startsWith("mailto://") ||
//							 value.toLowerCase().startsWith("file://") ||
//							 value.toLowerCase().startsWith("data://")									
							)
						){
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

	
	protected int countAccessibleLinks(int nReturn, Node actNode) {
		
		if (actNode.getNodeType() == Node.ELEMENT_NODE) {
			String textContent = actNode.getTextContent();
			if (textContent != null &&
					(
					textContent.toLowerCase().startsWith("http://") || 
					//uriCheck additional uri-schemes
					textContent.toLowerCase().startsWith("https://") ||
					textContent.toLowerCase().startsWith("ftp://")
//					textContent.toLowerCase().startsWith("mailto://") ||
//					textContent.toLowerCase().startsWith("file://") ||
//					textContent.toLowerCase().startsWith("data://")
					)
				){
//				Socket socket = null;
//				boolean reachable = false;
//				try {
//				    socket = new Socket(textContent);
//				    reachable = true;
//				} finally {            
//				    if (socket != null) try { socket.close(); } catch(IOException e) {}
				nReturn++;
				try{
				    final URLConnection connection = new URL(textContent).openConnection();
				    connection.setConnectTimeout(1000);
				    connection.setReadTimeout(2000);
				    connection.setUseCaches(false);
				    connection.connect();

				    //System.out.println("Ressource " + textContent + " is available. ");
				    //available = true;
				    
				    
				} 
				catch (UnknownHostException e){
					nReturn--;
				    System.out.println("Ressource " + textContent + ": UnknownHostException");					
				}
				catch(final MalformedURLException e){
					nReturn--;
				    System.out.println("Ressource " + textContent + ": MalformedURLException");
				    throw new IllegalStateException("Bad URL: " + textContent, e);
//				}   catch(final SocketTimeoutException e){
//					Log.info("Ressource " + textContent + " NOT available (Timeout exceeded).", e);
				} catch(final IOException e){
					nReturn--;
					//Log.info("Ressource " + textContent + " NOT available. ", e);				    
					System.out.println("Ressource " + textContent + " is NOT available. " + e.getMessage());
				}
			} 
//				else {
//				
//			}
			
			if (actNode.getAttributes() != null && actNode.getAttributes().getLength() > 0)
			{
				NamedNodeMap attributes = actNode.getAttributes();
				for (int attributesIndex = 0; attributesIndex < actNode.getAttributes().getLength() ; attributesIndex++) {
					Node attribute = attributes.item(attributesIndex);
					String value = attribute.getNodeValue();
					if (value != null && !value.isEmpty() && 
							(value.toLowerCase().startsWith("http://") || 
									 value.toLowerCase().startsWith("https://") || 
									 value.toLowerCase().startsWith("ftp://")
//									 value.toLowerCase().startsWith("mailto://") ||
//									 value.toLowerCase().startsWith("file://") ||
//									 value.toLowerCase().startsWith("data://")									
							)
					   ){
						writeToTempFile(this.dataProvider + " | " + value);
						nReturn++;
						try{
						    final URLConnection connection = new URL(value).openConnection();
						    connection.setConnectTimeout(1000);
						    connection.setReadTimeout(2000);
						    connection.setUseCaches(false);
						    connection.connect();
						    //System.out.println("Ressource " + value + " is available. ");
						}						    
						catch (UnknownHostException e){
								nReturn--;
							    System.out.println("Ressource " + textContent + ": UnknownHostException");					
						}						    
						catch(final MalformedURLException e){
							nReturn--;
						    System.out.println("Ressource " + value + ": MalformedURLException");							
						    throw new IllegalStateException("Bad URL: " + value, e);
//						}   catch(final SocketTimeoutException e){
//							Log.info("Ressource " + value + " NOT available (Timeout exceeded).", e);
						} catch(final IOException e){
							nReturn--;
							System.out.println("Ressource " + value + " is NOT available. " + e.getMessage());				    
							//System.out.println("Ressource " + value + " NOT available. " + e.getMessage());
						}						
					}
				}
			}

			if (actNode.hasChildNodes())
			{
				for (int i = 0; i < actNode.getChildNodes().getLength(); i++) {
					Node actNodeChild = actNode.getChildNodes().item(i);
					nReturn = countAccessibleLinks(nReturn, actNodeChild);
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
	
	protected void writeToTempFile(String textToAppend){
		File tempFile = new File("tempFileJR.csv");
		BufferedWriter writeBuffer;
		try {
			writeBuffer = new BufferedWriter(new FileWriter(tempFile));
			writeBuffer.append(textToAppend);
			writeBuffer.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//writeBuffer.close();
	}
	
	public NodeList getNodesListByXPath(String xpathValues){
		if (xmlFileName.length() > 0) {
			try {
				File f = new File(xmlFileName);
				if (f.exists() == true && f.isDirectory() == false) {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder;
						dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(f);

					doc.getDocumentElement().normalize();

					XPathFactory xPathfactory = XPathFactory.newInstance();
					XPath xpath = xPathfactory.newXPath();
					XPathExpression expr = xpath.compile(xpathValues);
					NodeList ret = (NodeList) expr.evaluate(doc,
							XPathConstants.NODESET);
					return ret;
				}
			} catch (ParserConfigurationException | XPathExpressionException | SAXException | IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}
	
	public String getXPath(Node node) {
	    return getXPath(node, "");
	}

	public String getXPath(Node node, String xpath) {
	    if (node == null) {
	        return "";
	    }
	    String elementName = "";
	    if (node instanceof Element) {
	        elementName = ((Element) node).getNodeName();
	    }
	    Node parent = node.getParentNode();
	    if (parent == null) {
	        return xpath;
	    }
	    return getXPath(parent, "/" + elementName + xpath);
	}
}
