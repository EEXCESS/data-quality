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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
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

            File tempFile = new File(file.getFileName());
         // Do what you want with the file        
            try {
                copyFile(file.getFileName(), file.getInputstream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Qc_dataprovider provider = new Qc_dataprovider();
        	String[] args = new String[]{ getRealPath()+"\\uploaded\\", "--outputDir="+getRealPath()+"\\report\\"};
//
        	boolean errorFlag = false;
        	try {
            provider.process(args);
        	}
        	catch (RuntimeException e) {
        		errorFlag = true;
                FacesMessage errorMessage = new FacesMessage("Error", e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, errorMessage);

        	}
        	if (!errorFlag){
                FacesMessage successMessage = new FacesMessage("Succesful", "report is finished");
                FacesContext.getCurrentInstance().addMessage(null, successMessage);
        	}
//            String realPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("");
//            System.out.println("real path:"+realPath);
        }
    }
    
    
    public void copyFile(String fileName, InputStream in) {
        try {
           
           
             // write the inputStream to a FileOutputStream
        	File outFile = new File( getRealPath()+"\\uploaded\\" + fileName);
        	FileOutputStream out = new FileOutputStream(outFile);
           
             int read = 0;
             byte[] bytes = new byte[1024];
           
             while ((read = in.read(bytes)) != -1) {
                 out.write(bytes, 0, read);
             }
             
           
             in.close();
             out.flush();
             out.close();
           
             System.out.println("New file created!\n"+outFile.getAbsolutePath());
             } catch (IOException e) {
             System.out.println(e.getMessage());
             }
 }

	private String getRealPath() {
		String realPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("");
		return realPath;
	}
  
}
