# data-quality

This package provides a library to analyze the data from the data provider.
This library inspects the data flow at three stages at the ParternRecommender:
* input data - data received from the API call of the data provider
* transformed data - transformed the data in the EEXCESS data provider
* enriched data - data enriched from the EEXCESS enrichment

The library creates output in different format:
* csv files with detailed data and summarized per data provider
* bar charts 
* in future: data in RDF/XML with the Data Quality Vocabulary (http://www.w3.org/TR/vocab-dqv/)

The library processes the data as provided by the partners data stores as XML ( JSON is transformed to XML before)

The actual implementation analyze the input data and make provides basic checks:
* fields/record (min, max, average)
* empty fields/record
* controlled terms / record
