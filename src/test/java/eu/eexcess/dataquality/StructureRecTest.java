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
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(18, result.getRecords());
    }
    
    public void testStructureYearWithWhiteSpacesValues()
    {
		String path = ".\\src\\test\\resources\\values-years-with-whitespaces.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(18, result.getRecords());
    }
    
    public void testStructureEntstehungszeitValues()
    {
		String path = ".\\src\\test\\resources\\values-entstehungszeit-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureEntstehungszeitVonValues()
    {
		String path = ".\\src\\test\\resources\\values-entstehungszeit-von-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureEntstehungszeitBisValues()
    {
		String path = ".\\src\\test\\resources\\values-entstehungszeit-bis-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureHoeheValues()
    {
		String path = ".\\src\\test\\resources\\values-hoehe-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureBreiteValues()
    {
		String path = ".\\src\\test\\resources\\values-breite-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }
    
    public void testStructureTitelValues()
    {
		String path = ".\\src\\test\\resources\\values-titel-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureSachgruppeValues()
    {
		String path = ".\\src\\test\\resources\\values-sachgruppe-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureZustandValues()
    {
		String path = ".\\src\\test\\resources\\values-zustand-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureTechnikValues()
    {
		String path = ".\\src\\test\\resources\\values-technik-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    public void testStructureTypeValues()
    {
		String path = ".\\src\\test\\resources\\values-type-original-values.txt";
		List<String> values = loadValues(path);
		StructureRecognizer recognizer = new StructureRecognizer();
		StructureRecResult result = recognizer.analyse(values);
		System.out.println("Results:\n"+path+"\n" + result.toString());
		assertEquals(1886, result.getRecords());
    }

    private List<String> loadValues(String path) {
		try {
			return Files.readAllLines(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

}
