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
package eu.eexcess.dataquality.graphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import eu.eexcess.dataquality.Qc_dataprovider;
import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;
import eu.eexcess.dataquality.Qc_paramDataList;
import eu.eexcess.dataquality.structure.StructureRecResult;
import eu.eexcess.dataquality.structure.StructureRecResultAnalysisResultData;
import eu.eexcess.dataquality.structure.StructureRecResult.StructureResultTyp;

public final class Qc_graphs {

	
	private static void setupFonts(JFreeChart chart, final CategoryPlot plot) {
		Font fontTitle = new Font("Tahoma", Font.BOLD, 32); 
        chart.getTitle().setFont(fontTitle);
        chart.removeLegend();
        LegendTitle legend = new LegendTitle(plot.getRenderer());
		Font fontLegend = new Font("Tahoma", Font.PLAIN, 29);// 29 or 34
        legend.setItemFont(fontLegend); 
        legend.setPosition(RectangleEdge.BOTTOM); 
        legend.setItemLabelPadding(new RectangleInsets(2, 2, 2, 50));
//        legend.setLegendItemGraphicPadding(new RectangleInsets(10, 1, 1, 10));
        legend.setLegendItemGraphicLocation(RectangleAnchor.CENTER);
        legend.setLegendItemGraphicAnchor(RectangleAnchor.CENTER);
        legend.setLegendItemGraphicEdge(RectangleEdge.LEFT);
        legend.setMargin(0, 10, 0, 20);
        chart.addLegend(legend);
		Font fontRangeAxis = new Font("Tahoma", Font.PLAIN, 29); 
        plot.getRangeAxis().setTickLabelFont(fontRangeAxis);
        plot.getRangeAxis().setLabelFont(fontRangeAxis);
		Font fontDomainAxis = new Font("Tahoma", Font.PLAIN, 29); 
        plot.getDomainAxis().setLabelFont(fontDomainAxis);
        plot.getDomainAxis().setTickLabelFont(fontDomainAxis);
	}

	private static void setupFonts(JFreeChart chart, final PiePlot3D plot) {
		Font fontTitle = new Font("Tahoma", Font.BOLD, 32); 
        chart.getTitle().setFont(fontTitle);
		Font fontLabel = new Font("Tahoma", Font.PLAIN, 29);// 29 or 34
        plot.setLabelFont(fontLabel);
        chart.removeLegend();
        LegendTitle legend = new LegendTitle(plot.getRootPlot());
		Font fontLegend = new Font("Tahoma", Font.PLAIN, 29);// 29 or 34
        legend.setItemFont(fontLegend); 
        legend.setPosition(RectangleEdge.BOTTOM); 
        legend.setItemLabelPadding(new RectangleInsets(2, 2, 2, 50));
//        legend.setLegendItemGraphicPadding(new RectangleInsets(10, 1, 1, 10));
        legend.setLegendItemGraphicLocation(RectangleAnchor.CENTER);
        legend.setLegendItemGraphicAnchor(RectangleAnchor.CENTER);
        legend.setLegendItemGraphicEdge(RectangleEdge.LEFT);
        legend.setMargin(0, 10, 0, 20);
        chart.addLegend(legend);
        plot.setDarkerSides(true);
        plot.setDepthFactor(0.15);
        plot.setCircular(false);
        plot.setLabelGap(0.02);

//        legend.setPosition(RectangleEdge.BOTTOM); 
//        legend.setItemLabelPadding(new RectangleInsets(2, 2, 2, 50));
////        legend.setLegendItemGraphicPadding(new RectangleInsets(10, 1, 1, 10));
//        legend.setLegendItemGraphicLocation(RectangleAnchor.CENTER);
//        legend.setLegendItemGraphicAnchor(RectangleAnchor.CENTER);
//        legend.setLegendItemGraphicEdge(RectangleEdge.LEFT);
//        legend.setMargin(0, 10, 0, 20);
//        chart.addLegend(legend);
//		Font fontRangeAxis = new Font("Tahoma", Font.PLAIN, 29); 
//        plot.getRangeAxis().setTickLabelFont(fontRangeAxis);
//        plot.getRangeAxis().setLabelFont(fontRangeAxis);
//		Font fontDomainAxis = new Font("Tahoma", Font.PLAIN, 29); 
//        plot.getDomainAxis().setLabelFont(fontDomainAxis);
//        plot.getDomainAxis().setTickLabelFont(fontDomainAxis);
	}

/*
	public static void allProviderRecordsPie (int nWidth, int nHeight, Qc_paramDataList paramList) {

		DefaultPieDataset piechart = new DefaultPieDataset();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			piechart.setValue(DataProvider.values()[i].toString() + " = " + (int)paramList.getRecordsPerProvider(DataProvider.values()[i]), paramList.getRecordsPerProvider(DataProvider.values()[i]));
		}
		
		JFreeChart chart = ChartFactory.createPieChart3D("records / provider", piechart);
		
		chart.setAntiAlias(true);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setIgnoreZeroValues(true);
        plot.setCircular(false);
        plot.setLabelGap(0.02);
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File("record_provider.png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
*/
	/*
	public static void allProviderDataFieldsPerRecordsPie (int nWidth, int nHeight, Qc_paramDataList paramList) {
		DefaultPieDataset piechart = new DefaultPieDataset();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			piechart.setValue(DataProvider.values()[i].toString() + " = " + (int)paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]));
		}
		
		JFreeChart chart = ChartFactory.createPieChart3D("data fields / record / provider", piechart);
		
		chart.setAntiAlias(true);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setIgnoreZeroValues(true);
        plot.setCircular(false);
        plot.setLabelGap(0.02);
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File("all_datafields.png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
*/
	@SuppressWarnings("incomplete-switch")
	public static void allProviderDataFieldsPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				switch (DataProvider.values()[i])
				{
					case KIMCollect:
					case ZBW:
					case Wissenmedia:
					case Mendeley:
					case DDB:
					case cultureWeb:
					case Europeana:
						dataset.addValue(paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
						break;
					case unknown:
						if (paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) > 0) {
							dataset.addValue(paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), Qc_dataprovider.cmdParameterDataprovider, "");
						}
						break;
				}
		}
		
		JFreeChart chart = ChartFactory.createBarChart("mean data fields / record / provider", "provider", "mean data fields / record ", dataset);
        
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation... 
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
//        Plot plot = chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		setupFonts(chart, plot); 

        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+ "all_datafields_bar_chart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@SuppressWarnings("incomplete-switch")
	public static void allProviderLinksPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				switch (DataProvider.values()[i])
				{
					case KIMCollect:
					case ZBW:
					case Wissenmedia:
					case Mendeley:
					case DDB:
					case cultureWeb:
					case Europeana:
						dataset.addValue(paramList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
						break;
					case unknown:
						if (paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) > 0) {
							dataset.addValue(paramList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), Qc_dataprovider.cmdParameterDataprovider, "");
						}
						break;
				}
		}
		
		JFreeChart chart = ChartFactory.createBarChart("mean links / record / provider", "provider", "mean links / record ", dataset);
		
		chart.setAntiAlias(true);
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());

//        Plot plot = chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		
		setupFonts(chart, plot); 

		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+ "links_barchart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	
	@SuppressWarnings("incomplete-switch")
	public static void allProviderLinksNotAccessiblePerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				switch (DataProvider.values()[i])
				{
					case KIMCollect:
					case ZBW:
					case Wissenmedia:
					case Mendeley:
					case DDB:
					case cultureWeb:
					case Europeana:
						dataset.addValue(paramList.getAccesibleLinksPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
						break;
					case unknown:
						if (paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) > 0) {
							dataset.addValue(paramList.getAccesibleLinksPerRecordsPerProvider(DataProvider.values()[i]), Qc_dataprovider.cmdParameterDataprovider, "");
						}
						break;
				}
		}
		
		JFreeChart chart = ChartFactory.createBarChart("mean links accessible / record / provider", "provider", "mean links accessible / record ", dataset);
		
		chart.setAntiAlias(true);
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());

//        Plot plot = chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		
		setupFonts(chart, plot); 

		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+ "links_accessible_barchart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	
	
	public static void allProviderNonEmptyDataFieldsPerRecordsPie (int nWidth, int nHeight, Qc_paramDataList paramList) {
		DefaultPieDataset piechart = new DefaultPieDataset();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			piechart.setValue(DataProvider.values()[i].toString() + " = " + (int)paramList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), paramList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i]));
		}
		
		JFreeChart chart = ChartFactory.createPieChart3D("non empty data fields / record / provider", piechart);
		
		chart.setAntiAlias(true);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setIgnoreZeroValues(true);
        plot.setCircular(false);
        plot.setLabelGap(0.02);
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+ "non_empty_datafields_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	public static void allProviderNonEmptyDataFieldsPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				switch (DataProvider.values()[i])
				{
					case KIMCollect:
					case ZBW:
					case Wissenmedia:
					case Mendeley:
					case DDB:
					case cultureWeb:
					case Europeana:
						dataset.addValue(paramList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
						break;
					case unknown:
						if (paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) > 0) {
							dataset.addValue(paramList.getAccesibleLinksPerRecordsPerProvider(DataProvider.values()[i]), Qc_dataprovider.cmdParameterDataprovider, "");
						}
						break;
				}
		}
		
		JFreeChart chart = ChartFactory.createBarChart("mean non empty data fields / record / provider","provider", "mean non empty data fields / record", dataset);
		
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
		setupFonts(chart, plot); 

//        PiePlot plot = (PiePlot) chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+ "non_empty_datafields_barchart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	public static void allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				switch (DataProvider.values()[i])
				{
					case KIMCollect:
					case ZBW:
					case Wissenmedia:
					case Mendeley:
					case DDB:
					case cultureWeb:
					case Europeana:
						dataset.addValue(paramList.getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
						break;
					case unknown:
						if (paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]) > 0) {
							dataset.addValue(paramList.getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i]), Qc_dataprovider.cmdParameterDataprovider, "");
						}
						break;
				}
		}
		
		JFreeChart chart = ChartFactory.createBarChart("mean non empty data fields / record / provider","provider", "mean non empty data fields / record", dataset);
		
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
		setupFonts(chart, plot); 
		NumberAxis domain = (NumberAxis) plot.getRangeAxis();
        domain.setRange(0.00, 1.00);
        domain.setTickUnit(new NumberTickUnit(0.1));
        
//        PiePlot plot = (PiePlot) chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+ "non_empty_datafields_perdatafields_barchart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}


	public static String struturendnessDataproviderFieldValueLengthHistrogramm(String dataprovider, String fieldname, int nWidth, int nHeight, StructureRecResult result) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		if (result.getLengthHistogram() != null)
		{
			for (int i=0;i<result.getLengthHistogram().length; i++)
			{
				if (result.getLengthHistogram()[i] > 0)
				{
					dataset.addValue(result.getLengthHistogram()[i], dataprovider +" "+fieldname,i+"");
				}
			}
		}
		
		JFreeChart chart = ChartFactory.createBarChart(dataprovider +" "+ fieldname + " value length histogram", "length", "number ", dataset);
        
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation... 
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
//        Plot plot = chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		setupFonts(chart, plot);

		chart.removeLegend();
		CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();  
	    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/2));
	    if (result.getLengthHistogram() != null)
	    {
		    if (result.getLengthHistogram().length > 20 )
		    	domainAxis.setVisible(false);
	    }
	      
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+ dataprovider +"-"+ fieldname + "-value length histogram"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return outputfile.getName();
	}

	public static String struturendnessDataproviderFieldValuePatternHistrogramm(String dataprovider, String fieldname, String diagrammType, int nWidth, int nHeight, StructureRecResult result) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
        Iterator<Entry<String, Integer>> iteratorPatternHashMap;
        if (diagrammType.contains("RegEx"))
        	iteratorPatternHashMap = result.getValuesPatternRegExHashMap().entrySet().iterator();
        else 
        	iteratorPatternHashMap = result.getValuesPatternHashMap().entrySet().iterator();
        while (iteratorPatternHashMap.hasNext()) {
            Entry<String, Integer> pattern = iteratorPatternHashMap.next();
            String patternString = pattern.getKey();
            // old
            //if (patternString.isEmpty()) patternString = "[empty]";
 			//dataset.addValue(pattern.getValue(), dataprovider +" "+fieldname,patternString);
            if (! patternString.isEmpty()) 
            	dataset.addValue(pattern.getValue(), dataprovider +" "+fieldname,patternString);
        }
		
		JFreeChart chart = ChartFactory.createBarChart(dataprovider +" "+ fieldname + " value "+diagrammType+" histogram", diagrammType, "number ", dataset);
        
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation... 
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
//        Plot plot = chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		setupFonts(chart, plot); 
		chart.removeLegend();
		CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();  
	    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/2));
	    
	    if (result.getValuesPatternHashMap().size() > 10 )
	    	domainAxis.setVisible(false);
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+ dataprovider +"-"+ fieldname + "-value "+diagrammType+" histogram"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return outputfile.getName();
	}
	
	public static String struturendnessDataproviderResultStructuredness(String dataprovider, int nWidth, int nHeight, HashMap<String, StructureRecResult> result,StructureResultTyp resultType) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
        Iterator<Entry<String, StructureRecResult>> iteratorFieldsHashMap = result.entrySet().iterator();
        while (iteratorFieldsHashMap.hasNext()) {
            Entry<String, StructureRecResult> fieldResult = iteratorFieldsHashMap.next();
		    StructureRecResultAnalysisResultData resultData = null;
		    if (resultType == StructureRecResult.StructureResultTyp.REGEX)
		    	resultData = fieldResult.getValue().getResultRegEx();
		    if (resultType == StructureRecResult.StructureResultTyp.VALUE)
		    	resultData = fieldResult.getValue().getResultValue();

           	dataset.addValue(resultData.getResultMedianPerVaildSamples(), "Median by valid samples",fieldResult.getKey());
           	dataset.addValue(resultData.getResultDistinctFracComplement(), "DistinctFracComplement",fieldResult.getKey());
           	dataset.addValue(resultData.getResultCdfl05(), "cdfl 0.5",fieldResult.getKey());
           	dataset.addValue(resultData.getResultCdfl075(), "cdfl 0.75",fieldResult.getKey());
        }
		
		JFreeChart chart = ChartFactory.createBarChart(dataprovider +" structuredness", "fields", "score", dataset);
        
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation... 
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
//        Plot plot = chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		setupFonts(chart, plot); 
//		chart.removeLegend();
		CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();  
	    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/2));
	    
//	    if (result.getValuesPatternHashMap().size() > 10 )
//	    	domainAxis.setVisible(false);
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+ dataprovider +"-structuredness-"+resultType+"-"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return outputfile.getName();
	}

	public static String struturendnessDataproviderResultOutliners(String dataprovider, int nWidth, int nHeight, HashMap<String, StructureRecResult> result,StructureResultTyp resultType) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
        Iterator<Entry<String, StructureRecResult>> iteratorFieldsHashMap = result.entrySet().iterator();
        while (iteratorFieldsHashMap.hasNext()) {
            Entry<String, StructureRecResult> fieldResult = iteratorFieldsHashMap.next();
		    StructureRecResultAnalysisResultData resultData = null;
		    if (resultType == StructureRecResult.StructureResultTyp.REGEX)
		    	resultData = fieldResult.getValue().getResultRegEx();
		    if (resultType == StructureRecResult.StructureResultTyp.VALUE)
		    	resultData = fieldResult.getValue().getResultValue();
           	dataset.addValue(resultData.getResultFracOutLower(), "lower frac",fieldResult.getKey());
           	dataset.addValue(resultData.getResultFracOutUpper(), "upper frac",fieldResult.getKey());
           	dataset.addValue(resultData.getResultFracOutWeighted(), "weighted frac",fieldResult.getKey());
        }
		
		JFreeChart chart = ChartFactory.createBarChart(dataprovider +" outliers", "fields", "score", dataset);
        
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation... 
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
//        Plot plot = chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		setupFonts(chart, plot); 
//		chart.removeLegend();
		CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();  
	    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/2));
	    
//	    if (result.getValuesPatternHashMap().size() > 10 )
//	    	domainAxis.setVisible(false);
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+ dataprovider +"-outliers-"+resultType+"-"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return outputfile.getName();
	}

	public static void linkTablePerDataproviderPie (int nWidth, int nHeight, Qc_paramDataList paramList, DataProvider provider) {

		DefaultPieDataset piechart = new DefaultPieDataset();
		
		for (String sLink : paramList.getAllTrustedLinks())
		{
			String temp = paramList.getTrustedLinkCountPerLinkAndProvider(provider, sLink,2);
			if (temp.isEmpty())
				continue;
				switch (provider)
				{
					case cultureWeb:
					case cultureWeb_EEXCESS:
					case cultureWeb_enriched:
					case unknown:
						// Do nothing
						break;
						
					default:
						NumberFormat nf = NumberFormat.getInstance();
						try {
							piechart.setValue(sLink ,  nf.parse(paramList.getTrustedLinkCountPerLinkAndProvider(provider, sLink,2)));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
				}
		}
		
		for (String sLink : paramList.getAllUnknownLinks())
		{
			String temp = paramList.getAllUnknownLinkCountPerLinkAndProvider(provider, sLink,2);
			if (temp.isEmpty())
				continue;
				switch (provider)
				{
					case cultureWeb:
					case cultureWeb_EEXCESS:
					case cultureWeb_enriched:
					case unknown:
						// Do nothing
						break;
						
					default:
						NumberFormat nf = NumberFormat.getInstance();
						try {
							piechart.setValue(sLink ,  nf.parse(paramList.getAllUnknownLinkCountPerLinkAndProvider(provider, sLink,2)));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
				}
		}
		
		
		
		JFreeChart chart = ChartFactory.createPieChart3D(provider.toString().replace("_", " ") +   " vocabulary links / record", piechart);
		
		chart.setAntiAlias(true);
		PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setIgnoreZeroValues(true);
		setupFonts(chart, plot);
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+Qc_dataprovider.OUTPUT_IMG_DIR+"statistics-links-dataprovider"+provider.toString()+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
