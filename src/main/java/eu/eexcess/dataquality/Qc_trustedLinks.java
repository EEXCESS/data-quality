package eu.eexcess.dataquality;

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
		while (it.hasNext())
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
					int nCount = hcountTrustedLinks.get(entry.getKey());
					nCount++;
					hcountTrustedLinks.put(entry.getKey(), nCount);
				}
			}
		}
		
		if (bLinkKnown == false)
		{
			System.out.println(sLink);
		}
	}

	public HashMap<String,Integer> getTrustedLinksCount()
	{
		return hcountTrustedLinks;
	}
}
