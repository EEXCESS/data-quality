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
package eu.eexcess.dataquality;

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

    public void testAppDebugKIMCollectSimpleExample()
    {
    	String[] args = new String[]{ ".\\..\\resources\\debug\\kim-collect\\simple-example"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppDebugKIMCollectOneRecordWithImages()
    {
    	String[] args = new String[]{ ".\\..\\resources\\debug\\kim-collect\\one-record-with-images"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testAppDebugMendeleySimpleExample()
    {
    	String[] args = new String[]{ ".\\..\\resources\\debug\\mendeley\\simple-example"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputTestbedServiceResponse()
    {
    	String[] args = new String[]{ ".\\..\\resources\\input-testbed\\service-response"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }
    
    public void testAppInputTestbedRandom100()
    {
    	String[] args = new String[]{ ".\\..\\resources\\input-testbed\\random-100"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputKultur20150302()
    {
    	String[] args = new String[]{ ".\\..\\resources\\kultur-2015-03-02"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputEnrichedManuallyServiceResponse()
    {
    	String[] args = new String[]{ ".\\..\\resources\\input-enriched-manually\\service-response"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

    public void testAppInputEnrichedManuallyServiceResponseEnriched()
    {
    	String[] args = new String[]{ ".\\..\\resources\\input-enriched-manually\\service-response-enriched"};
		DataQualityApp.main(args );
    	assertTrue( true );
    }

}
