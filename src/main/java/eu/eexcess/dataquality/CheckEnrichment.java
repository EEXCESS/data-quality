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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;
import eu.eexcess.dataquality.graphs.ChartDrawingSupplier;
import eu.eexcess.dataquality.structure.StructureRecResult;

public class CheckEnrichment {

	ArrayList<DataProvider> lprovider = new ArrayList<DataProvider>();
	ArrayList<Integer> lrecordCount = new ArrayList<Integer>();
	ArrayList<Integer> ldatafieldNonEmptyCount = new ArrayList<Integer>();
	ArrayList<Integer> ldatafieldLinkCount = new ArrayList<Integer>();
	
	public void CalcEnrichment(Qc_paramDataList paramDataList, int nWidth, int nHeight, DataProvider providerSelected)
	{
		for (int i=0; i<paramDataList.size();i++)
		{
			DataProvider provider = paramDataList.get(i).provider;
			int recordCount = paramDataList.get(i).recordCount;
			int recordLinkCount = (int)paramDataList.get(i).getLinkDataFieldsPerRecord();
			if (recordCount == 0)
			{
				recordCount = 1;
			}
			int datafieldNonEmptyCount = (int) paramDataList.get(i).getNonEmptyDataFieldsPerRecord();
			
			if (lprovider.contains(provider) == false)
			{
				lprovider.add(provider);
				lrecordCount.add(recordCount);
				ldatafieldNonEmptyCount.add(datafieldNonEmptyCount);
				ldatafieldLinkCount.add(recordLinkCount);
			}
			else
			{
				int nIndex = lprovider.indexOf(provider);
				lrecordCount.set(nIndex, lrecordCount.get(nIndex)+recordCount);
				ldatafieldNonEmptyCount.set(nIndex, ldatafieldNonEmptyCount.get(nIndex) + datafieldNonEmptyCount);
				ldatafieldLinkCount.set(nIndex, ldatafieldLinkCount.get(nIndex)+recordLinkCount);
			}
		}
		
		// - - - - - - - - - - - - - - - - - - - - - - enrichment overview - - - - - - - - - - - - - - - - - - - - - - - - - -
		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();
        
		if (lprovider.indexOf(DataProvider.KIMCollect)>=0 && (providerSelected == null || providerSelected == DataProvider.KIMCollect))
		{
			int i = lprovider.indexOf(DataProvider.KIMCollect);
			dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"before transform");
			if (lprovider.indexOf(DataProvider.KIMCollect_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.KIMCollect_EEXCESS);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.KIMCollect_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.KIMCollect_enriched);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.Europeana)>=0 && (providerSelected == null || providerSelected == DataProvider.Europeana))
		{
			int i = lprovider.indexOf(DataProvider.Europeana);
			dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"before transform");
			if (lprovider.indexOf(DataProvider.Europeana_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.Europeana_EEXCESS);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.Europeana_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.Europeana_enriched);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.ZBW)>=0 && (providerSelected == null || providerSelected == DataProvider.ZBW))
		{
			int i = lprovider.indexOf(DataProvider.ZBW);
			dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"before transform");
			if (lprovider.indexOf(DataProvider.ZBW_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.ZBW_EEXCESS);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.ZBW_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.ZBW_enriched);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.DDB)>=0 && (providerSelected == null || providerSelected == DataProvider.DDB))
		{
			int i = lprovider.indexOf(DataProvider.DDB);
			dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.DDB.name(),"before transform");
			if (lprovider.indexOf(DataProvider.DDB_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.DDB_EEXCESS);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.DDB.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.DDB_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.DDB_enriched);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.DDB.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.Mendeley)>=0 && (providerSelected == null || providerSelected == DataProvider.Mendeley))
		{
			int i = lprovider.indexOf(DataProvider.Mendeley);
			dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.Mendeley.name(),"before transform");
			if (lprovider.indexOf(DataProvider.Mendeley_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.Mendeley_EEXCESS);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.Mendeley.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.Mendeley_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.Mendeley_enriched);
				dataset.addValue(ldatafieldNonEmptyCount.get(i)/lrecordCount.get(i), DataProvider.Mendeley.name(),"enriched");
			}
		}
		
		
		JFreeChart chart = ChartFactory.createLineChart("check enrichment: data fields / record", " ", "non empty data fields / record", dataset);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customization... 
		CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
		
		setupFonts(chart, plot);
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
        File outputfile = null;
        if (providerSelected == null)
        {
        	outputfile = new File(Qc_dataprovider.outputDir+ "enrichment" +"-overview-"+nWidth+"x"+nHeight+".png");
        }
        else
        {
        	outputfile = new File(Qc_dataprovider.outputDir+ "enrichment" + "-" + providerSelected.toString() + "-"+nWidth+"x"+nHeight+".png");
        }
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		// - - - - - - - - - - - - - - - - - - - - - - enrichment links - - - - - - - - - - - - - - - - - - - - - - - - - -
		dataset = new DefaultCategoryDataset ();
        
		if (lprovider.indexOf(DataProvider.KIMCollect)>=0 && (providerSelected == null || providerSelected == DataProvider.KIMCollect))
		{
			int i = lprovider.indexOf(DataProvider.KIMCollect);
			dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"before transform");
			if (lprovider.indexOf(DataProvider.KIMCollect_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.KIMCollect_EEXCESS);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.KIMCollect_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.KIMCollect_enriched);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.Europeana)>=0 && (providerSelected == null || providerSelected == DataProvider.Europeana))
		{
			int i = lprovider.indexOf(DataProvider.Europeana);
			dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"before transform");
			if (lprovider.indexOf(DataProvider.Europeana_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.Europeana_EEXCESS);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.Europeana_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.Europeana_enriched);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.ZBW)>=0 && (providerSelected == null || providerSelected == DataProvider.ZBW))
		{
			int i = lprovider.indexOf(DataProvider.ZBW);
			dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"before transform");
			if (lprovider.indexOf(DataProvider.ZBW_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.ZBW_EEXCESS);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.ZBW_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.ZBW_enriched);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.DDB)>=0 && (providerSelected == null || providerSelected == DataProvider.DDB))
		{
			int i = lprovider.indexOf(DataProvider.DDB);
			dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.DDB.name(),"before transform");
			if (lprovider.indexOf(DataProvider.DDB_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.DDB_EEXCESS);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.DDB.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.DDB_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.DDB_enriched);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.DDB.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.Mendeley)>=0 && (providerSelected == null || providerSelected == DataProvider.Mendeley))
		{
			int i = lprovider.indexOf(DataProvider.Mendeley);
			dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.Mendeley.name(),"before transform");
			if (lprovider.indexOf(DataProvider.Mendeley_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.Mendeley_EEXCESS);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.Mendeley.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.Mendeley_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.Mendeley_enriched);
				dataset.addValue(ldatafieldLinkCount.get(i)/lrecordCount.get(i), DataProvider.Mendeley.name(),"enriched");
			}
		}
		
		
		chart = ChartFactory.createLineChart("check enrichment: links / record", " ", "links / record", dataset);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customization... 
		plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
		
		// CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();  
	    // domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/2));
		setupFonts(chart, plot);
        img_graph = chart.createBufferedImage(nWidth, nHeight);
		
        if (providerSelected == null)
        {
        	outputfile = new File(Qc_dataprovider.outputDir + "enrichment" +"-link-" +nWidth+"x"+nHeight+".png");
        }
        else
        {
        	outputfile = new File(Qc_dataprovider.outputDir + "enrichment" +"-link-" + providerSelected.toString() + nWidth+"x"+nHeight+".png");
        }
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		CalcTrustedLinks(paramDataList,nWidth,nHeight,providerSelected);
	}
	
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
        
        int i=0;
        try
        {
        	while (i<100)
	        {
	        	plot.getRenderer().setSeriesStroke(i, new BasicStroke(5.0f));
	        	i++;
	        }
        }
        catch (Exception e)
        {
        	
        }
	}
	
	//Show chart for trusted links
	void CalcTrustedLinks(Qc_paramDataList paramDataList, int nWidth, int nHeight, DataProvider providerSelected)
	{
		int nRecords = 0;
		
		HashMap<String,Integer> hcountAllTrustedLinks = new HashMap<String,Integer>();
		
		for (int i=0; i<paramDataList.size();i++)
		{
			if (providerSelected != null)
			{
				if (!paramDataList.get(i).provider.toString().startsWith(providerSelected.toString()))
				{
					continue;
				}
			}
			nRecords++;
			String columnKey = "", rowKey = "";
			
			if (paramDataList.get(i).provider.toString().endsWith("enriched"))
			{
				columnKey = "enriched";
			}
			else if (paramDataList.get(i).provider.toString().endsWith("EEXCESS"))
			{
				columnKey = "done transform";
			}
			else
			{
				columnKey = "before transform";
			}
			
			HashMap<String,Integer> hTrustedLinkCount = paramDataList.get(i).getTrustedLinksCount();
			Iterator<Entry<String, Integer>> it = hTrustedLinkCount.entrySet().iterator();
			while (it.hasNext())
			{
				Entry<String, Integer> entry = it.next();
				rowKey = entry.getKey();
				
				Integer nCount = 0;
				if (hcountAllTrustedLinks.containsKey(columnKey + "\t" + rowKey))
				{
					nCount = hcountAllTrustedLinks.get(columnKey + "\t" + rowKey);
				}
				nCount += (Integer)entry.getValue();
				hcountAllTrustedLinks.put(columnKey + "\t" + rowKey, nCount);
			}
		}
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();
		
		Iterator<Entry<String, Integer>> it = hcountAllTrustedLinks.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Integer> entry = it.next();
			String[] sParts = entry.getKey().split("\t");
			dataset.addValue(((double)entry.getValue() * 3) / nRecords, sParts[1], sParts[0]);
			// System.out.println(sParts[1] + " # " + sParts[0] + " # " + entry.getValue());
		}
		
		JFreeChart chart = null;
		
		if (providerSelected == null)
		{
			chart = ChartFactory.createLineChart("vocabulary links / record", " ", "vocabulary / record", dataset);
		}
		else
		{
			chart = ChartFactory.createLineChart( providerSelected.toString() + ": vocabulary links / record", " ", "vocabulary / record", dataset);
		}
		
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customization... 
		CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
		
		setupFonts(chart, plot);
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
        File outputfile = null;
        if (providerSelected == null)
        {
        	outputfile = new File(Qc_dataprovider.outputDir + "vocabulary" +"-" + nWidth+"x"+nHeight+".png");
        }
        else
        {
        	outputfile = new File(Qc_dataprovider.outputDir + "vocabulary-" + providerSelected +"-" + nWidth+"x"+nHeight+".png");
        }
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
