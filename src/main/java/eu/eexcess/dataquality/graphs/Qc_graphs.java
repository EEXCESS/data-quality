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
package eu.eexcess.dataquality.graphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleEdge;

import eu.eexcess.dataquality.Qc_dataprovider.DataProvider;
import eu.eexcess.dataquality.Qc_paramDataList;

public final class Qc_graphs {


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

	public static void allProviderDataFieldsPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				if (DataProvider.values()[i] != DataProvider.unknown)
					dataset.addValue(paramList.getDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
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
		
		/*
		LegendTitle legend = new LegendTitle(plot.getRenderer());
		Font font3 = new Font("Dialog", Font.PLAIN, 20); 
        legend.setItemFont(font3); 
        legend.setPosition(RectangleEdge.BOTTOM); 
        chart.removeLegend();
        chart.addLegend(legend); 
*/
        BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File("all_datafields_bar_chart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void allProviderLinksPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				if (DataProvider.values()[i] != DataProvider.unknown)
					dataset.addValue(paramList.getLinkDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
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
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File("links_barchart_"+nWidth+"x"+nHeight+".png");
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
		
		File outputfile = new File("non_empty_datafields_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void allProviderNonEmptyDataFieldsPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				if (DataProvider.values()[i] != DataProvider.unknown)
					dataset.addValue(paramList.getNonEmptyDataFieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
		}
		
		JFreeChart chart = ChartFactory.createBarChart("mean non empty data fields / record / provider","provider", "mean non empty data fields / record", dataset);
		
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());

//        PiePlot plot = (PiePlot) chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File("non_empty_datafields_barchart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void allProviderNonEmptyDataFieldsPerDatafieldsPerRecordsBarChart (int nWidth, int nHeight, Qc_paramDataList paramList) {
		DefaultCategoryDataset  dataset = new DefaultCategoryDataset ();
		
		for (int i=0;i<DataProvider.values().length; i++)
		{
			if (paramList.hasProviderData(DataProvider.values()[i]))
				if (DataProvider.values()[i] != DataProvider.unknown)
					dataset.addValue(paramList.getNonEmptyDataFieldsPerDatafieldsPerRecordsPerProvider(DataProvider.values()[i]), DataProvider.values()[i].toString(), "");
		}
		
		JFreeChart chart = ChartFactory.createBarChart("mean non empty data fields / record / provider","provider", "mean non empty data fields / record", dataset);
		
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot(); 
		plot.setDrawingSupplier(new ChartDrawingSupplier());

//        PiePlot plot = (PiePlot) chart.getPlot();
//        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
//        plot.setNoDataMessage("No data available");
//        plot.setIgnoreZeroValues(true);
//        plot.setCircular(false);
//        plot.setLabelGap(0.02);
		
		BufferedImage img_graph = chart.createBufferedImage(nWidth, nHeight);
		
		File outputfile = new File("non_empty_datafields_perdatafields_barchart_"+nWidth+"x"+nHeight+".png");
		try {
			ImageIO.write(img_graph, "png", outputfile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
