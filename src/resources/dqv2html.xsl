<!-- ##################################################################### 

	DQV to HTML
	     
	 ##################################################################### -->
	 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:daq="http://purl.org/eis/vocab/daq#" xmlns:dcat="http://www.w3.org/ns/dcat#" xmlns:dct="http://purl.org/dc/terms/" xmlns:dqv="http://www.w3.org/ns/dqv#" xmlns:eexdaq="http://eexcess.eu/ns/dataquality/daq/" xmlns:prov="http://www.w3.org/ns/prov#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			>

<xsl:output method = "html" />
<xsl:template match="/">
<html lang="en"><head>
<script language="javascript" type="text/javascript" src="jqplot/jquery.min.js"></script>
<script language="javascript" type="text/javascript" src="jqplot/jquery.jqplot.min.js"></script>
<script type="text/javascript" src="jqplot/jqplot.canvasTextRenderer.min.js"></script>
<script type="text/javascript" src="jqplot/jqplot.canvasAxisLabelRenderer.min.js"></script>

<link rel="stylesheet" type="text/css" href="jqplot/jquery.jqplot.min.css" />
<title>Data Quality Result Visualisation</title>
<script class="include" language="javascript" type="text/javascript" src="jqplot/plugins/jqplot.barRenderer.min.js"></script>
<script class="include" language="javascript" type="text/javascript" src="jqplot/plugins/jqplot.categoryAxisRenderer.min.js"></script>
<script class="include" language="javascript" type="text/javascript" src="jqplot/plugins/jqplot.pointLabels.min.js"></script>

 <style type="text/css">
    .note {
        font-size: 0.8em;
    }
    .jqplot-yaxis-tick {
      white-space: nowrap;
    }
</style>


<script class="code" type="text/javascript">
$(document).ready(function(){


    var measurements = [ 
	
	<xsl:call-template name="collectMeasurements"/>
	
	];

	 
   // Can specify a custom tick Array.
    // Ticks should match up one for each y value (category) in the series.
    var ticks = [
	
	<xsl:apply-templates select="//dcat:Distribution" />
    
	];
	
    var plot1 = $.jqplot('chart', measurements, {
        // The "seriesDefaults" option is an options object that will
        // be applied to all series in the chart.
        seriesDefaults:{
            renderer:$.jqplot.BarRenderer,
            rendererOptions: {fillToZero: true}
        }, 
        axes: {
            // Use a category axis on the x axis and use our custom ticks.
            xaxis: {
                renderer: $.jqplot.CategoryAxisRenderer,
                ticks: ticks,
				label: 'data providers'
            },
	
           // Pad the y axis just a little so bars can get close to, but
            // not touch, the grid boundaries.  1.2 is the default padding.
            yaxis: {
                pad: 1.05,
                tickOptions: {formatString: '%f'},
				label: 'score',
				min: 0
            }
        },
		legend: {
            show: true,
			location: 'ne',
            placement: 'inside',
			labels: [ <xsl:apply-templates select="//daq:Metric" /> ]
        },	
	});


	
});
</script>

</head><body>

	<h2>Data Quality Measurements</h2>
	<div id="chart" style="width:document.body.offsetWidth; height:500px;"></div>

	
</body>
</html>
</xsl:template>

<xsl:template match = "//daq:Metric" > 
	<xsl:text>'</xsl:text><xsl:value-of select="substring-after(@rdf:about,'#')" /><xsl:text>'</xsl:text>
	<xsl:if test="not(position()=last())">
		<xsl:text>,</xsl:text>
	</xsl:if>
</xsl:template> 

<xsl:template match = "//dcat:Distribution" > 
	<xsl:text>'</xsl:text><xsl:value-of select="dct:title" /><xsl:text>'</xsl:text>
	<xsl:if test="not(position()=last())">
		<xsl:text>,</xsl:text>
	</xsl:if>
</xsl:template> 

<xsl:template name="collectMeasurements" > 
	<xsl:for-each select="//daq:Metric">
		<xsl:variable name="cMetric"><xsl:value-of select="@rdf:about" /></xsl:variable>
		<xsl:text>[</xsl:text>
		<xsl:for-each select="//dcat:Distribution">
			<xsl:variable name="cDataDistribution"><xsl:value-of select="@rdf:about" /></xsl:variable>
						
			<xsl:apply-templates select="//dqv:QualityMeasure[daq:computedOn/@rdf:resource=$cDataDistribution and daq:metric/@rdf:resource=$cMetric]" />
			
			<xsl:if test="not(position()=last())">
				<xsl:text>,</xsl:text>
			</xsl:if>
			
		</xsl:for-each>
		<xsl:text>]</xsl:text>
		
		<xsl:if test="not(position()=last())">
			<xsl:text>,</xsl:text>
		</xsl:if>
	</xsl:for-each>
	
</xsl:template> 

<xsl:template match = "//dqv:QualityMeasure" > 
	<xsl:value-of select="daq:value" />
	<xsl:if test="not(position()=last())">
		<xsl:text>,</xsl:text>
	</xsl:if>
</xsl:template> 

<xsl:template match = "node()|@*" > 
	<xsl:apply-templates/>
</xsl:template> 

</xsl:stylesheet>

