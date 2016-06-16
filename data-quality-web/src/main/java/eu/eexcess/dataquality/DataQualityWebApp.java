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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@ManagedBean
@SessionScoped
public class DataQualityWebApp 
{
	public static final String CMD_PARAM_DONT_COPY_INPUT = "--dontCopyInput";
	public static final String CMD_PARAM_LOG = "--log";
	
	public static final String CMD_PARAM_XPATH_RECORD_SEPERATOR = "--XpathRecordSeparator=";
	public static final String CMD_PARAM_XPATH_FIELDS_TO_RECORD_SEPERATOR = "--XpathsToFieldsFromRecordSeparator";
	public static final String CMD_PARAM_DATAPROVIDER = "--dataprovider=";
	 
	public DataQualityWebApp(){
//		this.dataproviderName="The European Library";
//		this.xpathLoop="/*[local-name()='BibliographicResourceCollection']/*[local-name()='BibliographicResource']";
		this.reportGenerated = false;
		this.sessionCode = System.currentTimeMillis() + "";
		{
			File newSessionDir = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath("") +"\\"+this.sessionCode+"\\");
			newSessionDir.mkdir();
		}
		{
			File newSessionDir = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath("") +"\\"+this.sessionCode+"\\uploaded\\");
			newSessionDir.mkdir();
		}
		{
			File newSessionDir = new File(FacesContext.getCurrentInstance().getExternalContext().getRealPath("") +"\\"+this.sessionCode+"\\report\\");
			newSessionDir.mkdir();
		}
	}
	
	private UploadedFile file;
	
	protected String xpathLoop;
	protected String dataproviderName;
		
	protected String sessionCode;
	
	protected boolean reportGenerated;
	 
    public String getDataproviderName() {
		return dataproviderName;
	}

	public String getSessionCode() {
		return sessionCode;
	}

	public void setSessionCode(String sessionCode) {
		this.sessionCode = sessionCode;
	}

	public void setDataproviderName(String dataproviderName) {
		this.dataproviderName = dataproviderName;
	}

	public String getXpathLoop() {
		return xpathLoop;
	}

	public void setXpathLoop(String xpathLoop) {
		this.xpathLoop = xpathLoop;
	}

	public UploadedFile getFile() {
        return file;
    }
 
    public void setFile(UploadedFile file) {
        this.file = file;
    }
    
    public void uploadPhoto(FileUploadEvent e) throws IOException{
    	file=e.getFile();
    }
    
    public void upload() {
        if(file != null) {

            if (file.getSize()> 220000)
            {
                FacesMessage messageFileToBig = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "The file "+file.getFileName() + " is to big("+file.getSize()+" Bytes). Please use a file smaler than 200kB.");
                FacesContext.getCurrentInstance().addMessage(null, messageFileToBig);
                return;
            }
//            File tempFile = new File(file.getFileName());
            try {
                copyFile(file.getFileName(), file.getInputstream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Qc_dataprovider provider = new Qc_dataprovider();
        	String[] args = new String[]{ getRealPathforSession()+"\\uploaded\\", 
        			"--outputDir="+getRealPathforSession()+"\\report\\",
        			"--XpathRecordSeparator="+this.xpathLoop,
        			"--dataprovider="+this.dataproviderName,
        			"--resourcesDir="+this.getRealPath()+"\\resources\\"
        			};

        	boolean errorFlag = false;
        	try {
        		provider.process(args);
        	} catch (RuntimeException e) {
        		errorFlag = true;
                FacesMessage errorMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, errorMessage);
        	}
        	if (!errorFlag){
                FacesMessage successMessage = new FacesMessage("Succesful", "report is finished");
                FacesContext.getCurrentInstance().addMessage(null, successMessage);
                this.reportGenerated = true;
        	}
        }
    }
    
    
    public boolean isReportGenerated() {
		return reportGenerated;
	}

	public void setReportGenerated(boolean reportGenerated) {
		this.reportGenerated = reportGenerated;
	}

	public void copyFile(String fileName, InputStream in) {
        try {
           
             // write the inputStream to a FileOutputStream
        	File outFile = new File( getRealPathforSession()+"\\uploaded\\" + fileName);
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

	private String getRealPathforSession() {
		String realPath = getRealPath() +"\\"+this.sessionCode+"\\";
		return realPath;
	}
  
	private String getRealPath() {
		String realPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("");
		return realPath;
	}

}
