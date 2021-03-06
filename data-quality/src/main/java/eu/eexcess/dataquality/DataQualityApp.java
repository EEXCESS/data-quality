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
package eu.eexcess.dataquality;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class DataQualityApp 
{
    public static void main( String[] args )
    {
    	for (int i = 0; i < args.length; i++) {
			if (0==args[i].compareToIgnoreCase(CMD_PARAM_LOG) ){
		    	try {
					System.setOut(new PrintStream(new FileOutputStream("DataQualityApp.log",false)));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
        Qc_dataprovider provider = new Qc_dataprovider();
        provider.process(args);
    }

	public static final String CMD_PARAM_DONT_COPY_INPUT = "--dontCopyInput";
	public static final String CMD_PARAM_LOG = "--log";
	
	public static final String CMD_PARAM_XPATH_RECORD_SEPERATOR = "--XpathRecordSeparator=";
	public static final String CMD_PARAM_XPATH_FIELDS_TO_RECORD_SEPERATOR = "--XpathsToFieldsFromRecordSeparator";
	public static final String CMD_PARAM_DATAPROVIDER = "--dataprovider=";
	public static final String CMD_PARAM_OUTPUTDIR = "--outputDir=";
	public static final String CMD_PARAM_RESOURCESDIR = "--resourcesDir=";
	
}
