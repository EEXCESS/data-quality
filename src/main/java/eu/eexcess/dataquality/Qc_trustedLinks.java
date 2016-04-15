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
package eu.eexcess.dataquality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Qc_trustedLinks {

	HashMap<String,String> hTrustedLinks = new HashMap<String,String>();
	HashMap<String,Integer> hcountTrustedLinks = new HashMap<String,Integer>();
	HashMap<String,Integer> hcountUnknownLinks = new HashMap<String,Integer>();
	
	public Qc_trustedLinks()
	{
		hTrustedLinks.put("Geonames", "geonames.org");
		hTrustedLinks.put("LC Linked Data Service", "id.loc.gov");
		hTrustedLinks.put("DBpedia", "dbpedia.org");
		hTrustedLinks.put("Katalog der deutschen Nationalbibliothek", "d-nb.info");
		
		Iterator<Entry<String, String>> it = hTrustedLinks.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, String> entry = it.next();
			hcountTrustedLinks.put(entry.getKey(), 0);
		}
	}
	
	public void addTrustedLink(String sLink)
	{
		Boolean bLinkKnown = false; 
		Iterator<Entry<String, String>> it = hTrustedLinks.entrySet().iterator();
		while (it.hasNext() && bLinkKnown == false)
		{
			Entry<String, String> entry = it.next();
			if (sLink.contains(entry.getValue()))
			{
				bLinkKnown = true;
				if (hcountTrustedLinks.containsKey(entry.getKey())==false)
				{
					hcountTrustedLinks.put(entry.getKey(), 1);
				}
				else
				{
					Integer nCount = hcountTrustedLinks.get(entry.getKey());
					nCount++;
					hcountTrustedLinks.put(entry.getKey(), nCount);
					// System.out.println(entry.getKey() + " # " + nCount);
				}
			}
		}
		
		if (bLinkKnown == false)
		{
			CountUnknownLinks(sLink);
		}
	}
	
	void CountUnknownLinks(String sLink)
	{
		if (sLink.contains("://"))
		{
			sLink = sLink.substring(sLink.indexOf("://")+3);
			if (sLink.contains("/"))
			{
				sLink = sLink.substring(0,sLink.indexOf("/"));
			}
			
			if (hcountUnknownLinks.containsKey(sLink)){
				int nCount = hcountUnknownLinks.get(sLink);
				nCount++;
				hcountUnknownLinks.put(sLink, nCount);	
			}
			else
			{
				hcountUnknownLinks.put(sLink, 1);
			}
			// System.out.println(sLink + " # " + hcountUnknownLinks.get(sLink));	
		}
	}

	public HashMap<String,Integer> getTrustedLinksCount()
	{
		return hcountTrustedLinks;
	}
	
	public ArrayList<String> getTrustedLinks()
	{
		ArrayList<String> saLinks = new ArrayList<String>();
		Iterator<Entry<String, String>> it = hTrustedLinks.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, String> entry = it.next();
			saLinks.add(entry.getKey());
		}
		return saLinks;
	}
	
	public ArrayList<String> getAllUnknownLinks()
	{
		ArrayList<String> saLinks = new ArrayList<String>();
		Iterator<Entry<String, Integer>> it = hcountUnknownLinks.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = it.next();
			saLinks.add(entry.getKey());
		}
		return saLinks;
	}
	
	public Integer getAllUnknownLinkCountPerLink(String sLink)
	{
		if (hcountUnknownLinks.containsKey(sLink))
		{
			return hcountUnknownLinks.get(sLink);
		}
		return 0;
	}
	
	public Integer getTrustedLinkCountPerLink(String sLink)
	{
		if (hcountTrustedLinks.containsKey(sLink))
		{
			return hcountTrustedLinks.get(sLink);
		}
		return 0;
	}
}
