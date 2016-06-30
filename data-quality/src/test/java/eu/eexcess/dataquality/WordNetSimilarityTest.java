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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import eu.eexcess.dataquality.wordnet.WordNetSimilarity;
import eu.eexcess.dataquality.wordnet.WordNetSimilarityResultProxyObject;
import eu.eexcess.dataquality.wordnet.WordNetSimilarityResultTwoWords;

public class WordNetSimilarityTest extends TestCase {
	
	
    public WordNetSimilarityTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( WordNetSimilarityTest.class );
    }

    public void testTwoWords()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
		WordNetSimilarityResultTwoWords result = sim.compute("prosciutto", "asparagus");
		System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
    }
    
    public void testTwoWordsNotInWordNet()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
		WordNetSimilarityResultTwoWords result = sim.compute("BerettaXX", "BerettaYYY");
		System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
    }
    public void testTwoWordsSame()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
		WordNetSimilarityResultTwoWords result = sim.compute("prosciutto", "prosciutto");
		System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
    }


    public void testSentencesWordsInWordNet()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
//		String words = "White square dish with prosciutto wrapped asparagus stalks on a green tablemat credit: Ilva Beretta / thePictureKitchen / TopFoto";
		String words = "prosciutto asparagus";
		WordNetSimilarityResultProxyObject result = sim.compute(words);
		System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
    }

    
    public void testSentencesWordsNotInWordNet()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
//		String words = "White square dish with prosciutto wrapped asparagus stalks on a green tablemat credit: Ilva Beretta / thePictureKitchen / TopFoto";
		String words = "BerettaYY BerettaXX";
		WordNetSimilarityResultProxyObject result = sim.compute(words);
		System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
    }

    public void testSentencesProxy()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
//		String words = "White square dish with prosciutto wrapped asparagus stalks on a green tablemat credit: Ilva Beretta / thePictureKitchen / TopFoto";
		String words = "White square dish prosciutto asparagus green";
		WordNetSimilarityResultProxyObject result = sim.compute(words);
		System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
    }
    
    // title europeana:
    //White square dish with prosciutto wrapped asparagus stalks on a green tablemat credit: Ilva Beretta / thePictureKitchen / TopFoto
    
    
    // enriched:
    // concepts:
    // prosciutto asparagus Beretta White
    
    public void testSentencesEnrichedProxy()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
//		String words = "White square dish with prosciutto wrapped asparagus stalks on a green tablemat credit: Ilva Beretta / thePictureKitchen / TopFoto";
		String words = "White square dish prosciutto asparagus green prosciutto asparagus Beretta White";
		WordNetSimilarityResultProxyObject result = sim.compute(words);
		// System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
    }
    
    public void testExampleFromReport()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
		String words = "Culture Make Slant";
		WordNetSimilarityResultProxyObject result = sim.compute(words);
		System.out.println("Results:\n"+result+"\n" + result.toString());
//		assertEquals(18, result.getRecords());
		System.out.println("\n\n");
		words = "Bacteria Culture Make Slant";
		result = sim.compute(words);
		System.out.println("Results:\n"+result+"\n" + result.toString());
    }
    

    
    public void testSentencesProxyEnrichedProxy()
    {
		WordNetSimilarity sim = new WordNetSimilarity();
		sim.setTraceOn();
		String words = "White square dish with prosciutto wrapped asparagus stalks on a green tablemat credit: Ilva Beretta / thePictureKitchen / TopFoto";
		// System.out.println("\ninput:\n"+words+"\n");
		WordNetSimilarityResultProxyObject result = sim.compute(words);
		// System.out.println("Results:\n"+result+"\n" + result.toString());
		words = "White square dish with prosciutto wrapped asparagus stalks on a green tablemat credit: Ilva Beretta / thePictureKitchen / TopFoto prosciutto asparagus Beretta White";
		// System.out.println("\ninput:\n"+words+"\n");
		result = sim.compute(words);
		// System.out.println("Results:\n"+result+"\n" + result.toString());
    }
    
}
