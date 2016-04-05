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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class DataQualityAppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DataQualityAppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DataQualityAppTest.class );
    }
    
    public void testAppTransformedEnrichedKIMPortal()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-KIMPortal"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppTransformedEnrichedKIMPortalII()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-KIMPortal-II"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppTransformedEnrichedZBW()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-ZBW"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppTransformedEnrichedEuropeana()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-Europeana"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppDebugKIMCollectSimpleExample()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\debug\\kim-collect\\simple-example"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppDebugKIMCollectOneRecordWithImages()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\debug\\kim-collect\\one-record-with-images"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testAppDebugMendeleySimpleExample()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\debug\\mendeley\\simple-example"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputTestbedServiceResponse()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-testbed\\service-response"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testAppInputTestbedRandom100()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-testbed\\random-100"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedRandom100()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\structuredness\\random-100"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedRandom100Small()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\structuredness\\random-100-small"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedRandom100Small4Development()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\structuredness\\random-100-small4Dev"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedRandom100Small4DevelopmentOutliers()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\structuredness\\random-100-small4Dev-outliers"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedRandom100Small4DevelopmentDatePatterns()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\structuredness\\random-100-small4Dev-date-patterns"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedRandom100Small4DevelopmentWithCR()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\structuredness\\random-100-small4Dev-title-with-CR"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    
    public void testAppInputStructurednessTestbedSomeDataprovidersSmall()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-some-dataproviders-small"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testAppInputStructurednessTestbedSomeDataprovidersSmallV2()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-some-dataproviders-small-v2"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedSomeDataprovidersBigD44()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-some-dataproviders-big-d44"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessTestbedSomeDataprovidersBigD44V2()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-transformed-enriched-some-dataproviders-big-d44-v2"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputStructurednessKIMCollect()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\structuredness\\kimcollect"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputKultur20150302()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\kultur-2015-03-02"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    

    public void testAppInputEnrichedManuallyServiceResponse()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-enriched-manually\\service-response"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputEnrichedManuallyServiceResponseEnriched()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\input-enriched-manually\\service-response-enriched"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testContentNegotiation() throws IOException{
    	URL myUrl= new URL("http://digv525.joanneum.at/kim-kgs/rest/masterdata/collection/5");
    	URLConnection urlConnection = myUrl.openConnection();
    	
    	 Map<String, List<String>> headers = urlConnection.getHeaderFields();
    	 Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
         for (Map.Entry<String, List<String>> entry : entrySet) {
             String headerName = entry.getKey();
             System.out.println("Header Name:" + headerName);
             List<String> headerValues = entry.getValue();
             for (String value : headerValues) {
                 System.out.print("Header value:" + value);
             }
             System.out.println();
             System.out.println();
  	    	
		};
		
    	System.out.println("JRtest");
    	assertEquals(true, true);
    	
    }

	public void testContentNegotiation3() throws IOException{
		URL myUrl= new URL("http://dbpedia.org/page/Delos");
		URLConnection urlConnection = myUrl.openConnection();
		
		 Map<String, List<String>> headers = urlConnection.getHeaderFields();
		 Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
	     for (Map.Entry<String, List<String>> entry : entrySet) {
	         String headerName = entry.getKey();
	         System.out.println("Header Name:" + headerName);
	         List<String> headerValues = entry.getValue();
	         for (String value : headerValues) {
	             System.out.print("Header value:" + value);
	         }
	         System.out.println();
	         System.out.println();
	    	
		};
		
		System.out.println("JRtest");
		assertEquals(true, true);
		
	}

	public void testContentNegotiation4() throws IOException{
		URL myUrl= new URL("http://dbpedia.org/page/Delos");
		URLConnection urlConnection = myUrl.openConnection();
		
		 Map<String, List<String>> headers = urlConnection.getHeaderFields();
		 Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
	     for (Map.Entry<String, List<String>> entry : entrySet) {
	         String headerName = entry.getKey();
	         System.out.println("Header Name:" + headerName);
	         List<String> headerValues = entry.getValue();
	         for (String value : headerValues) {
	             System.out.print("Header value:" + value);
	         }
	         System.out.println();
	         System.out.println();
	    	
		};
		
		System.out.println("JRtest");
		assertEquals(true, true);
		
	}

    public void testAppZBWOneRecord(){
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\debug\\zbw\\one-record"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testAppInputTestbedRandom100_DDB()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\debug\\ddb"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testAppInputTestbedRandom100_ZBW()
    {
    	String[] args = new String[]{ ".\\..\\..\\eexcess\\trunk\\src\\wp4\\dataquality\\resources\\debug\\zbw\\testbed-records"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    
	
}
