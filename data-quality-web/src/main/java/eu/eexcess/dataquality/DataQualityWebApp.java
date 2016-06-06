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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.primefaces.model.UploadedFile;

@ManagedBean
public class DataQualityWebApp 
{
	public static final String CMD_PARAM_DONT_COPY_INPUT = "--dontCopyInput";
	public static final String CMD_PARAM_LOG = "--log";
	
	public static final String CMD_PARAM_XPATH_RECORD_SEPERATOR = "--XpathRecordSeparator=";
	public static final String CMD_PARAM_XPATH_FIELDS_TO_RECORD_SEPERATOR = "--XpathsToFieldsFromRecordSeparator";
	public static final String CMD_PARAM_DATAPROVIDER = "--dataprovider=";
	 
	private UploadedFile file;
	 
    public UploadedFile getFile() {
        return file;
    }
 
    public void setFile(UploadedFile file) {
        this.file = file;
    }
    
    public void upload() {
        if(file != null) {
            FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            
            
        }
    }
    
}
