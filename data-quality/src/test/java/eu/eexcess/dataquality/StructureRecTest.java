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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import eu.eexcess.dataquality.structure.StructureRecResult;
import eu.eexcess.dataquality.structure.StructureRecognizer;
import eu.eexcess.dataquality.structure.ValueSource;

public class StructureRecTest extends TestCase {
	
	
    public StructureRecTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( StructureRecTest.class );
    }

    public void testStructureYearValues()
    {
		String path = ".\\src\\test\\resources\\values-years.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(18, result.getRecords());
    }
    
    public void testStructureYearWithWhiteSpacesValues()
    {
		String path = ".\\src\\test\\resources\\values-years-with-whitespaces.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(23, result.getRecords());
    }
    
    public void testStructureEntstehungszeitValues()
    {
		String path = ".\\src\\test\\resources\\values-entstehungszeit-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureEntstehungszeitVonValues()
    {
		String path = ".\\src\\test\\resources\\values-entstehungszeit-von-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureEntstehungszeitBisValues()
    {
		String path = ".\\src\\test\\resources\\values-entstehungszeit-bis-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureHoeheValues()
    {
		String path = ".\\src\\test\\resources\\values-hoehe-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureBreiteValues()
    {
		String path = ".\\src\\test\\resources\\values-breite-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }
    
    public void testStructureTitelValues()
    {
		String path = ".\\src\\test\\resources\\values-titel-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureSachgruppeValues()
    {
		String path = ".\\src\\test\\resources\\values-sachgruppe-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureZustandValues()
    {
		String path = ".\\src\\test\\resources\\values-zustand-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureTechnikValues()
    {
		String path = ".\\src\\test\\resources\\values-technik-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureTypeValues()
    {
		String path = ".\\src\\test\\resources\\values-type-original-values.txt";
		List<ValueSource> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    private List<ValueSource> loadValues(String path) {
		try {
			List<ValueSource> ret = new ArrayList<ValueSource>();
					
			List<String> temp = Files.readAllLines(Paths.get(path));
			for (int i = 0; i < temp.size(); i++) {
				ret.add(new ValueSource(temp.get(i), path));
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<ValueSource>();
	}

}
