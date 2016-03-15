package eu.eexcess.dataquality;

import java.awt.Color;
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
import org.jfree.data.category.DefaultCategoryDataset;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;
import eu.eexcess.dataquality.graphs.ChartDrawingSupplier;
import eu.eexcess.dataquality.structure.StructureRecResult;

public class CheckEnrichment {

	ArrayList<DataProvider> lprovider = new ArrayList<DataProvider>();
	ArrayList<Integer> lrecordCount = new ArrayList<Integer>();
	ArrayList<Integer> ldatafieldCount = new ArrayList<Integer>();
	
	public void CalcEnrichment(Qc_paramDataList paramDataList, int nWidth, int nHeight)
	{
		for (int i=0; i<paramDataList.size();i++)
		{
			DataProvider provider = paramDataList.get(i).provider;
			int recordCount = paramDataList.get(i).recordCount;
			if (recordCount == 0)
			{
				recordCount=1;
			}
			int datafieldCount = (int) paramDataList.get(i).getDataFieldsPerRecord();
			
			if (lprovider.contains(provider) == false)
			{
				lprovider.add(provider);
				lrecordCount.add(recordCount);
				ldatafieldCount.add(datafieldCount);
			}
			else
			{
				int nIndex = lprovider.indexOf(provider);
				lrecordCount.set(nIndex, lrecordCount.get(nIndex)+recordCount);
				ldatafieldCount.set(nIndex, ldatafieldCount.get(nIndex)+datafieldCount);
			}
		}
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0; i<lprovider.size();i++) {
			
           	
        }
        
		if (lprovider.indexOf(DataProvider.KIMCollect)>=0)
		{
			int i = lprovider.indexOf(DataProvider.KIMCollect);
			dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"before transform");
			if (lprovider.indexOf(DataProvider.KIMCollect_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.KIMCollect_EEXCESS);
				dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.KIMCollect_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.KIMCollect_enriched);
				dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.KIMCollect.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.Europeana)>=0)
		{
			int i = lprovider.indexOf(DataProvider.Europeana);
			dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"before transform");
			if (lprovider.indexOf(DataProvider.Europeana_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.Europeana_EEXCESS);
				dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.Europeana_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.Europeana_enriched);
				dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.Europeana.name(),"enriched");
			}
		}
		
		if (lprovider.indexOf(DataProvider.ZBW)>=0)
		{
			int i = lprovider.indexOf(DataProvider.ZBW);
			dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"before transform");
			if (lprovider.indexOf(DataProvider.ZBW_EEXCESS)>=0)
			{
				i = lprovider.indexOf(DataProvider.ZBW_EEXCESS);
				dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"done transform");
			}
			if (lprovider.indexOf(DataProvider.ZBW_enriched)>=0)
			{
				i = lprovider.indexOf(DataProvider.ZBW_enriched);
				dataset.addValue(ldatafieldCount.get(i)/lrecordCount.get(i), DataProvider.ZBW.name(),"enriched");
			}
		}
		
		
		JFreeChart chart = ChartFactory.createLineChart("Check Enrichment", "transformation", "datafields", dataset);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customization... 
		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());
		// CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();  
	    // domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/2));
	    
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File(Qc_dataprovider.outputDir+Qc_dataprovider.OUTPUT_STRUCT_IMG_DIR+ "enrichment" +"-overview-"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
