package eu.eexcess.dataquality;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;




public class DataQualityDesktopApp 
{
	
	private static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
	protected String inputDataPath="";
	protected String outputDataPath="";
	protected JLabel inputDataLabel;
	protected JLabel outputDataLabel;
	protected JTextComponent messageConsoleTextArea;
	protected MessageConsole messageConsole;
	private JLabel outputDataReportLabel;
	protected JFrame mainWindow;
	private JTextField xpath;
	private JTextField namedataprovider;
	
    public static void main( String[] args )
    {
    	new DataQualityDesktopApp();
    }

    public DataQualityDesktopApp()
    {
    	setupGUI();
    }

	protected void setupGUI() {
		this.inputDataPath = "/home";
		messageConsoleTextArea = new JTextArea();
		messageConsoleTextArea.setSize(800, 800);
		messageConsoleTextArea.setAutoscrolls(true);
		messageConsole = new MessageConsole(messageConsoleTextArea, true);
    	this.messageConsole.redirectErr();
    	this.messageConsole.redirectOut();

		Color eexcess = new Color(29,144,78);
		mainWindow = new JFrame("EEXCESS DataQuality");
    	mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	mainWindow.setVisible(true);
        
        JPanel mainframe=new JPanel();
        mainframe.setBackground(BACKGROUND_COLOR);
        //mainframe.setLayout(new FlowLayout());
        mainframe.setLayout(new BoxLayout(mainframe, BoxLayout.PAGE_AXIS));
        mainframe.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel headerLabel = new JLabel();
        headerLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerLabel.setForeground(eexcess);
        headerLabel.setText("EEXCESS DataQuality");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 20f));
        mainframe.add(headerLabel);
        JTextArea descLabel = new JTextArea();
        descLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        descLabel.setForeground(Color.BLACK);
        descLabel.setEditable(false);
        descLabel.setText("With this application you can analyse your data.\nFor more general information about the EEXCESS project please visit:\nhttp://eexcess.eu/ or \nhttps://github.com/EEXCESS.");
        descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN, 14f));
        descLabel.setBackground(BACKGROUND_COLOR);
        mainframe.add(descLabel);

        JButton buttonInputData=new JButton();
        buttonInputData.setText(" open data ");
        //buttonInputData.setBorder(new EmptyBorder(10, 10, 10, 10));
        
//        buttonInputData.setBorder(BorderFactory.createLineBorder(eexcess, 5, false));
        buttonInputData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
					openData();
            }
        });
        mainframe.add(buttonInputData);
        inputDataLabel = new JLabel();
        inputDataLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputDataLabel.setForeground(eexcess);
        mainframe.add(inputDataLabel);
        
        JLabel namedataproviderLabel = new JLabel();
        namedataproviderLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        namedataproviderLabel.setText("name of the dataprovider");
        mainframe.add(namedataproviderLabel);
        namedataprovider = new JTextField();
        namedataprovider.setToolTipText("name of the dataprovider");
//        namedataprovider.setText("The European Library");
        mainframe.add(namedataprovider);

        JLabel xpathLabel = new JLabel();
        xpathLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        xpathLabel.setText("xpath for the loop");
        mainframe.add(xpathLabel);
        xpath = new JTextField();
        xpath.setToolTipText("xpath for the loop");
//        xpath.setText("/*[local-name()='BibliographicResourceCollection']/*[local-name()='BibliographicResource']");
        mainframe.add(xpath);

        JButton buttonCalcReport=new JButton();
        buttonCalcReport.setText(" analyse data ");
        buttonCalcReport.setMargin(new Insets(5,5,5,5));
        buttonCalcReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                	analyseData();
            }
        });
        mainframe.add(buttonCalcReport);
        outputDataLabel = new JLabel();
        outputDataLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        outputDataLabel.setForeground(eexcess);
        mainframe.add(outputDataLabel);
        JButton buttonOpenReport=new JButton();
        buttonOpenReport.setText(" show report ");
        buttonOpenReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
					openReport();
            }
        });
        mainframe.add(this.messageConsoleTextArea);
        outputDataReportLabel = new JLabel();
        outputDataReportLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        outputDataReportLabel.setForeground(eexcess);
        mainframe.add(outputDataReportLabel);
        mainframe.add(buttonOpenReport);
        
        
        JScrollPane sp = new JScrollPane(mainframe);
        mainWindow.getContentPane().add(sp);
        mainWindow.pack();
        mainWindow.setVisible(true);
        mainWindow.setSize(800, 600);
        
        
        this.setInputDataPath("");

	}
    
    
    protected void openData() {
        final JFileChooser chooser = new JFileChooser("Verzeichnis wählen"); 
        chooser.setDialogType(JFileChooser.OPEN_DIALOG); 
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        final File file = new File(this.inputDataPath); 

        chooser.setCurrentDirectory(file); 

        chooser.addPropertyChangeListener(new PropertyChangeListener() { 
            public void propertyChange(PropertyChangeEvent e) { 
                if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY) 
                        || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) { 
                    //final File f = (File) e.getNewValue(); 
                } 
            } 
        }); 

        chooser.setVisible(true); 
        final int result = chooser.showOpenDialog(null); 

        if (result == JFileChooser.APPROVE_OPTION) { 
            File inputVerzFile = chooser.getSelectedFile(); 
            String inputVerzStr = inputVerzFile.getPath(); 
            this.setInputDataPath(inputVerzStr);
            this.setOutputDataPath(inputVerzFile.getParent()+"\\report\\");
        } 
        chooser.setVisible(false); 		
	}

    
	protected void analyseData() {
        this.mainWindow.setCursor(new Cursor(Cursor.WAIT_CURSOR));

		File output = new File(getOutputDataPath());
		output.delete();
        Qc_dataprovider provider = new Qc_dataprovider();
    	String[] args = new String[]{ getInputDataPath(), 
    			"--outputDir="+getOutputDataPath(),
    			"--XpathRecordSeparator="+this.xpath.getText(),
    			"--dataprovider="+this.namedataprovider.getText(),
    			"--resourcesDir=.\\resources\\"
    	};
    	try {
    		provider.process(args);
    	} catch (RuntimeException e) {
    	}
    	this.mainWindow.setCursor(Cursor.getDefaultCursor());
	}


	public String getInputDataPath() {
		return this.inputDataPath;
	}

	public void setInputDataPath(String path) {
		this.inputDataPath = path;
		this.inputDataLabel.setText("Path for input data:\n " + path);
	}

	public String getOutputDataPath() {
		return this.outputDataPath;
	}

	public void setOutputDataPath(String path) {
		this.outputDataPath = path;
		this.outputDataLabel.setText("Path for output data:\n " + path);
		this.outputDataReportLabel.setText("Path for Report:\n " + getReportFilename() );
	}


	public void openReport() {
		URI uri;
		uri = new File(getReportFilename()).toURI();
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
		    try {
		        desktop.browse(uri);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
    }

	protected String getReportFilename() {
		return getOutputDataPath() + "dataquality-report.html";
	}

}
