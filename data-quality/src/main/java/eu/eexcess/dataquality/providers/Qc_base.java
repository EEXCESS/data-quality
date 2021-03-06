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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
import eu.eexcess.dataquality.Qc_dataprovider;
import eu.eexcess.dataquality.Qc_interface;
import eu.eexcess.dataquality.Qc_params;

public class Qc_base implements Qc_interface {

	public enum SearchType {
		allDataFields, notEmptyDataFields, linkDataFields
		//uriCheck
		, uriDataFields
	}

	boolean useAttributesInXPath = false; 
	
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
	
	// - - - - - - - - - - - - Additional Provider Condition - - - - - - - - - - - - -
	// Condition which should occur for a specified data provider
	String additionalProviderCondition = "";
	
	// - - - - - - - - - - - - - Not Provider Condition - - - - - - - - - - - - - -
	// Condition which should not occur for a specified data provider
	String notProviderCondition = "";

	// XML node list of records
	NodeList nodelistRecords = null;

	public NodeList getNodelistRecords() {
		return nodelistRecords;
	}

	public void setNodelistRecords(NodeList nodelistRecords) {
		this.nodelistRecords = nodelistRecords;
	}
	
	protected boolean isProviderRecordCheckAdditional(Document doc)
	{
		StringWriter sw = new StringWriter();
		
		if (additionalProviderCondition.length() > 0 || notProviderCondition.length() > 0)
		{
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer;
			try {
				transformer = tf.newTransformer();

	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		if (additionalProviderCondition.length() > 0)
		{
			if (sw.toString().contains(notProviderCondition) == false)
			{
				return false;
			}
		}
		if (notProviderCondition.length() > 0)
		{
			
			if (sw.toString().contains(notProviderCondition) == true)
			{
				return false;
			}
		}
		return true;
	}

	// check if XML file is from current data provider
	public boolean isProviderRecord() {
		boolean bReturn = false;
		if (xmlFileName.length() > 0) {
			try {
				File f = new File(xmlFileName);
				
				if (xmlFileName.contains("KIMPortalTransformer-done") == true)
				{
					dataProvider = DataProvider.KIMCollect_EEXCESS;
				}
				else if (xmlFileName.contains("EuropeanaTransformer-done") == true)
				{
					dataProvider = DataProvider.Europeana_EEXCESS;
				}
				else if (xmlFileName.contains("ZBWTransformer-done") == true)
				{
					dataProvider = DataProvider.ZBW_EEXCESS;
				}
				else if (xmlFileName.contains("DDBTransformer-done") == true)
				{
					dataProvider = DataProvider.DDB_EEXCESS;
				}
				else if (xmlFileName.contains("MendeleyTransformer-done") == true)
				{
					dataProvider = DataProvider.Mendeley_EEXCESS;
				}
				
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
		if (cNode != null)
		{
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
					nReturn = countLinksInNodes(nReturn, actNode, true);
				}
			} //uriCheck 
			  else if (searchType == SearchType.uriDataFields) {
				for (int i = 0; i < nodeChilds.getLength(); i++) {
					Node actNode = nodeChilds.item(i);
					nReturn = countAccessibleLinks(nReturn, actNode, true);
				}
			}
		}
		return nReturn;
	}
	
	protected String getAllParentNodes (Node actNode)
	{
		String sReturn = actNode.getNodeName();
		if (actNode.getParentNode() != null)
		{
			sReturn = getAllParentNodes(actNode.getParentNode()) + "/" + sReturn;
		}
		return sReturn;
	}
	
	ArrayList<String> lUniqueLinks = new ArrayList<String>();
	
	/*
	 * count Links in nodes
	 * */
	protected int countLinksInNodes(int nReturn, Node actNode, boolean bSearchAttributes) {
		
		if (actNode.getNodeType() == Node.ELEMENT_NODE) {
			String actNodeTextContent = actNode.getTextContent();
			if (actNodeTextContent != null &&
					(
					actNodeTextContent.toLowerCase().trim().startsWith("http://") || 
					//uriCheck additional uri-schemes
					actNodeTextContent.toLowerCase().trim().startsWith("https://") ||
					actNodeTextContent.toLowerCase().trim().startsWith("ftp://")
//					actNodeTextContent.toLowerCase().startsWith("mailto://") ||
//					actNodeTextContent.toLowerCase().startsWith("file://") ||
//					actNodeTextContent.toLowerCase().startsWith("data://")
					)
				){
				if (lUniqueLinks.contains(actNodeTextContent.toLowerCase().trim()) == false)
				{
					lUniqueLinks.add(actNodeTextContent.toLowerCase().trim());
					countTrustedLinks(actNodeTextContent.toLowerCase().trim(), getAllParentNodes(actNode));
					nReturn++;
				}
			} 
			
			if (actNode.getAttributes() != null && bSearchAttributes == true && actNode.getAttributes().getLength() > 0)
			{
				NamedNodeMap attributes = actNode.getAttributes();
				for (int attributesIndex = 0; attributesIndex < actNode.getAttributes().getLength() ; attributesIndex++) {
					Node attribute = attributes.item(attributesIndex);
					String value = attribute.getNodeValue();
					String nodeName = attribute.getNodeName();
					
					if (value != null && !value.isEmpty() && !nodeName.startsWith("xmlns:") &&
							(value.toLowerCase().trim().startsWith("http://") || 
							 value.toLowerCase().trim().startsWith("https://") || 
							 value.toLowerCase().trim().startsWith("ftp://")
//							 value.toLowerCase().startsWith("mailto://") ||
//							 value.toLowerCase().startsWith("file://") ||
//							 value.toLowerCase().startsWith("data://")									
							)
						){
						if (lUniqueLinks.contains(actNodeTextContent.toLowerCase().trim()) == false &&
								!value.toLowerCase().trim().endsWith("/enrichedproxy/") &&
								!value.toLowerCase().trim().endsWith("/aggregation/") &&
								!value.toLowerCase().trim().endsWith("/proxy/"))
						{
							lUniqueLinks.add(value.toLowerCase().trim());
							countTrustedLinks(value.toLowerCase().trim(), getAllParentNodes(actNode));
							nReturn++;
						}
					}
				}
			}

			if (actNode.hasChildNodes())
			{
				for (int i = 0; i < actNode.getChildNodes().getLength(); i++) {
					Node actNodeChild = actNode.getChildNodes().item(i);
					nReturn = countLinksInNodes(nReturn, actNodeChild, bSearchAttributes);
				}
			}
		}
		return nReturn;
	}

	protected void countTrustedLinks(String sLink, String xPath)
	{
		param.addTrustedLink(sLink, xPath);
	}
	
	public HashMap<String,Integer> getTrustedLinksCount()
	{
		return param.getTrustedLinksCount();
	}
	
	public ArrayList<String> getAllUnknownLinks()
	{
		return param.getAllUnknownLinks();
	}
	
	protected int countAccessibleLinks(int nReturn, Node actNode, boolean bSearchAttributes) {
		
		if (actNode.getNodeType() == Node.ELEMENT_NODE) {
			String textContent = actNode.getTextContent();
			
			nReturn = checkURLAccessible(nReturn, textContent); 
			
			if (actNode.getAttributes() != null && bSearchAttributes == true && actNode.getAttributes().getLength() > 0)
			{
				NamedNodeMap attributes = actNode.getAttributes();
				for (int attributesIndex = 0; attributesIndex < actNode.getAttributes().getLength() ; attributesIndex++) {
					Node attribute = attributes.item(attributesIndex);
					String value = attribute.getNodeValue();
					if (attribute.getNodeName() != null && !attribute.getNodeName().isEmpty() && !attribute.getNodeName().startsWith("xmlns:"))
						nReturn = checkURLAccessible(nReturn, value); 
				}
			}

			if (actNode.hasChildNodes())
			{
				for (int i = 0; i < actNode.getChildNodes().getLength(); i++) {
					Node actNodeChild = actNode.getChildNodes().item(i);
					nReturn = countAccessibleLinks(nReturn, actNodeChild, bSearchAttributes);
				}
			}
		}
		return nReturn;
	}

	private String getHost(String url){
	    if(url == null || url.length() == 0)
	        return "";

	    int doubleslash = url.indexOf("//");
	    if(doubleslash == -1)
	        doubleslash = 0;
	    else
	        doubleslash += 2;

	    int end = url.indexOf('/', doubleslash);
	    end = end >= 0 ? end : url.length();

	    int port = url.indexOf(':', doubleslash);
	    end = (port > 0 && port < end) ? port : end;

	    return url.substring(doubleslash, end);
	}
	
	private int checkURLAccessible(int nReturn, String urlToCheck) {
		
		if (Qc_dataprovider.checkLinksOnline && urlToCheck != null &&
				(
				urlToCheck.toLowerCase().trim().startsWith("http://") || 
				//uriCheck additional uri-schemes
				urlToCheck.toLowerCase().trim().startsWith("https://") ||
				urlToCheck.toLowerCase().trim().startsWith("ftp://")
//					textContent.toLowerCase().startsWith("mailto://") ||
//					textContent.toLowerCase().startsWith("file://") ||
//					textContent.toLowerCase().startsWith("data://")
				)
			){
//			System.out.println(urlToCheck);
			String host = this.getHost(urlToCheck);
			if (Qc_dataprovider.URLCheckingResultsHostsBlacklist.containsKey(host)) {
				Qc_dataprovider.countCheckedURLsViaBlackList++;
				return nReturn;
			}
			
			Qc_dataprovider.countCheckedURLs++;
			if (Qc_dataprovider.URLCheckingResultsAccessible.containsKey(urlToCheck)) {
				Qc_dataprovider.countCheckedURLsViaCache++;
				if (Qc_dataprovider.URLCheckingResultsAccessible.get(urlToCheck))
					return nReturn++;
				else 
					return nReturn;
			}
			Qc_dataprovider.countCheckedURLsOnline++;
			nReturn++;
			boolean accessible = false;
			try{
			    final URLConnection connection = new URL(urlToCheck).openConnection();
			    connection.setConnectTimeout(500);
			    connection.setReadTimeout(2000);
			    connection.setUseCaches(false);
			    connection.connect();
			    ((HttpURLConnection) connection).disconnect();
			    //System.out.println("Ressource " + textContent + " is available. ");
			    accessible = true;
			} 
			catch (UnknownHostException e){
				nReturn--;
			    System.out.println("Ressource " + urlToCheck + ": UnknownHostException\n"+ e.getMessage());	
				Qc_dataprovider.URLCheckingResultsHostsBlacklist.put(host, false);
			}
			catch(final MalformedURLException e){
				nReturn--;
			    System.out.println("Ressource " + urlToCheck + ": MalformedURLException\n"+ e.getMessage());
			} catch(final IOException e){
				nReturn--;
				//Log.info("Ressource " + textContent + " NOT available. ", e);				    
				System.out.println("Ressource " + urlToCheck + " is NOT available. \nIOException\n" + e.getMessage());
				if (e.getMessage().contains("No buffer space available (maximum connections reached?)")) {
			        Date date = new Date(System.currentTimeMillis());
			        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
					System.out.println(simpleDateFormat.format(date)+": max connections reached...waiting...");
					try {
						Thread.sleep(1000*120);
				        Date dateEnd = new Date(System.currentTimeMillis());
						System.out.println(simpleDateFormat.format(dateEnd) + ": now trying again...");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			} catch (RuntimeException e) {
				nReturn--;
				System.out.println("Ressource " + urlToCheck + " is NOT available. \nRuntimeException\n" + e.getMessage());				    
			}
			Qc_dataprovider.URLCheckingResultsAccessible.put(urlToCheck, accessible);
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
	/*
	protected void writeToTempFile(String textToAppend){
		File tempFile = new File(Qc_dataprovider.outputDir + "tempFileJR.csv");
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
	*/
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
	        if (this.useAttributesInXPath && xpath.isEmpty()){
	        	NamedNodeMap attributes = node.getAttributes();
	 			for (int i=0; i<attributes.getLength();i++)
				{
					elementName += "[@"+attributes.item(i).getNodeName()+"='"+attributes.item(i).getNodeValue()+"']";
				}
	        }
	    }
	    Node parent = node.getParentNode();
	    if (parent == null) {
	        return xpath;
	    }
	    return getXPath(parent, "/" + elementName + xpath);
	}
	
	public boolean nodeToString(Node node, String sAttribute) {
		boolean bFound = false;
		if (node != null)
		{
			NamedNodeMap attr = node.getAttributes();
			for (int i=0; i<attr.getLength();i++)
			{
				if (attr.item(i).getNodeValue().equals(sAttribute))
				{
					bFound = true;
					break;
				}
			}
		}
	    return bFound;
	}
}
