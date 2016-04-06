package eu.eexcess.dataquality;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
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
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
        
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
		
		
		JFreeChart chart = ChartFactory.createLineChart("check enrichment: datafields / record", "transformation", "non empty datafields / record", dataset);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customization... 
		CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
		
		// CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();  
	    // domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/2));
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
		
		
		chart = ChartFactory.createLineChart("check enrichment: links / record", "transformation", "links / record", dataset);
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
}
