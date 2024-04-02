package org.elsquatrecaps.portada.jportadamicroservice.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josepcanellas
 */
public class PortadaApi {

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the pref
     */
    public String getPref() {
        return pref;
    }

    /**
     * @param pref the pref to set
     */
    public void setPref(String pref) {
        setPrefField(pref);        
    }
    
    private String host;
    private String port;
    private String pref;

    public PortadaApi() {
        init();
    }

    public PortadaApi(String host, String port, String pref) {
        this.host = host;
        this.port = port;
        this.setPrefField(pref);
    }
    
    
    public final void init(Configuration config) {
        this.host = config.getHost();
        this.port = config.getPort();
        this.setPrefField(config.getPref());
    }
    
    public final void init(String host, String port, String pref) {
        this.host = host;
        this.port = port;
        this.setPrefField(pref);
    }
    
    public final void init(){
        Properties prop = new Properties();
        try {
            FileReader freader = new FileReader("config/init.properties");
            prop.load(freader);
            setHost(prop.getProperty("host"));
            setPort(prop.getProperty("port"));
            setPref(prop.getProperty("pref"));
        } catch (IOException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void deskewImageFile(Configuration config){
        switch (config.getCommandArgumentsSize()) {
            case 1:
                deskewImageFile(config.getInputFile());
                break;
            case 2:
                deskewImageFile(config.getInputFile(), config.getOutputFile());
                break;
            case 3:
                deskewImageFile(config.getInputFile(), config.getOutputFile(), config.getErrorFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for deskewImageFile command");
        }
        
    }
    
    public void deskewImageFile(String inputFile){
        deskewImageFile(inputFile, inputFile, inputFile);
    }
    
    public void deskewImageFile(String inputFile, String outputFile){
        deskewImageFile(inputFile, outputFile, outputFile);
    }
    
    public void deskewImageFile(String inputFile, String outputFile, String errorFile){
        transformImageFile("deskewImageFile", inputFile, outputFile, errorFile);
    }
    
    public void dewarpImageFile(Configuration config){
        switch (config.getCommandArgumentsSize()) {
            case 1:
                dewarpImageFile(config.getInputFile());
                break;
            case 2:
                dewarpImageFile(config.getInputFile(), config.getOutputFile());
                break;
            case 3:
                dewarpImageFile(config.getInputFile(), config.getOutputFile(), config.getErrorFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for deskewImageFile command");
        }
        
    }
    
    public void dewarpImageFile(String inputFile){
        dewarpImageFile(inputFile, inputFile, inputFile);
    }
    
    public void dewarpImageFile(String inputFile, String outputFile){
        dewarpImageFile(inputFile, outputFile, outputFile);
    }
    
    public void dewarpImageFile(String inputFile, String outputFile, String errorFile){
        transformImageFile("dewarpImageFile", inputFile, outputFile, errorFile);
    }
    
    private void transformImageFile(String command, String inputFile, String outputFile, String errorFile){
        String strUrl = String.format("http://%s:%s%s%s", getHost(), getPort(),getPref(), command);
        try{  
            File inFile = new File(inputFile);
            StringBuilder hwriter = new StringBuilder();
            hwriter.append("--*****\r\n");
            hwriter.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(inFile.getName()).append("\"\r\n");
            hwriter.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(inFile.getName())).append("\r\n");
            hwriter.append("Content-Length: ").append(String.valueOf(inFile.length())).append("\r\n");
            hwriter.append("\r\n"); 
            StringBuilder fwriter = new StringBuilder();
            fwriter.append("\r\n");
            fwriter.append("--*****--\r\n");
            
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");
            con.setRequestMethod("POST");
            con.connect();
            try (OutputStream outputStream = con.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) { 
                writer.append(hwriter.toString());   
                writer.flush();
                try(FileInputStream in = new FileInputStream(inFile)){
                    copyStreams(in, outputStream);
                    outputStream.flush();
                }
                writer.append(fwriter.toString());
                writer.flush();
            }
            int responseCode = con.getResponseCode();
            if ((responseCode >= 200) && (responseCode < 400)) {  
                try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
                    copyStreams(con.getInputStream(), outputStream);
                }
            } else {
                //error
                try(FileOutputStream outputStream = new FileOutputStream(errorFile)){
                    copyStreams(con.getErrorStream(), outputStream);
                }
            }
            con.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private long copyStreams(InputStream in, OutputStream out) throws IOException{
        BufferedOutputStream bos = new BufferedOutputStream(out);
        BufferedInputStream bis = new BufferedInputStream(in);
        byte[] buffer = new byte[12288]; // 12K
        long count = 0L;
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    
    private final void setPrefField(String pref) {
        if(pref==null || pref.isEmpty() || pref.isBlank()){
            pref="/";
        }else{
            if(!pref.startsWith("/")){
                pref="/".concat(pref);
            }
            if(!pref.endsWith("/")){
                pref=pref.concat("/");
            }
        }
        this.pref = pref;       
    }
}
