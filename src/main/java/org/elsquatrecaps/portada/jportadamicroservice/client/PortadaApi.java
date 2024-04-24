package org.elsquatrecaps.portada.jportadamicroservice.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elsquatrecaps.utilities.console.Print;

/**
 *
 * @author josepcanellas
 */
public class PortadaApi {
    public static String[] msContext = {"java", "python", "r"};
    public static String[] imagesExtensions = {".jpg", ".jpeg", ".png", "gif", ".tif", "tiff"};
    private final Map<String, ConnectionMs> conDataList = new HashMap<>();
    
    /**
     * @return the host
     */
    public String getHost(String key) {
        return conDataList.get(key).getHost();
    }

    /**
     * @return the port
     */
    public String getPort(String key) {
        return conDataList.get(key).getPort();
    }

    /**
     * @return the pref
     */
    public String getPref(String key) {
        return conDataList.get(key).getPref();
    }

    public PortadaApi() {
//        init();
    }

    public final void init(Configuration config) {
        for(String ctx: msContext){
            this.conDataList.put(ctx, new ConnectionMs(config.getPort(ctx), config.getHosts(ctx), config.getPrefs(ctx)));
        }
    }
    
    public final void init(){
        Configuration cfg = new Configuration();
        cfg.configure();
        init(cfg);
    }
    
    public void fixAllImages(Configuration config){
         switch (config.getCommandArgumentsSize()) {
            case 1:
                fixAllImages(config.getInputDir(), config.getInputDir(), 
                        FixActions.getActions(config.getFixTransparency(), config.getFixSkew(), config.getFixWarp()));
                break;
            case 2:
                fixAllImages(config.getInputFile(), config.getOutputFile(),
                        FixActions.getActions(config.getFixTransparency(), config.getFixSkew(), config.getFixWarp()));
                break;
            case 3:
                fixAllImages(config.getInputFile(), config.getOutputFile(), config.getErrorFile(), 
                        FixActions.getActions(config.getFixTransparency(), config.getFixSkew(), config.getFixWarp()));
                break;
            default:
                throw new RuntimeException("Bad number of parametres for fixBackTransparencyImageFile command");             
         }        
    }
    
    public void fixAllImages(String inputDir, String outputDir, int actions){
        fixAllImages(inputDir, outputDir, "errors.txt",  actions);
    }
    
    public void fixAllImages(String inputDir, String outputDir, String errorFileName, int actions){
        File errorFile = new File(errorFileName);
        File inputDirFile = new File(inputDir);
        File outputDirFile = new File(outputDir);
        if(errorFile.exists()){
            errorFile.delete();
        }
        if(!outputDirFile.exists()){
            outputDirFile.mkdirs();
        }
        if(inputDirFile.isDirectory() && outputDirFile.isDirectory()){
            File[] lf = inputDirFile.listFiles(new FileFilter(){
                @Override
                public boolean accept(File file) {
                    boolean ret = false;
                    for(int i=0; !ret && i<imagesExtensions.length; i++){
                        ret = file.isFile() && file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase().equals(imagesExtensions[i]);
                    }
                    return ret;
                }
            });
            int all = lf.length*3;
            int fet=0;
            Print.printPercentage("Starting process", fet++, all);
            for(File inputImageFile: lf){
                File outputImageFile = new File(outputDirFile, inputImageFile.getName());
                String m = String.format("image: '%s'", inputImageFile.getName());
                String iif = inputImageFile.getAbsolutePath();
                if(FixActions.isActionIn(FixActions.FIX_TANSPARENCY, actions)){
                    fixBackTransparencyImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                    iif = outputImageFile.getAbsolutePath();
                }
                Print.printPercentage(m, fet++, all);    
                if(FixActions.isActionIn(FixActions.FIX_SKEW, actions)){                
                    deskewImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                    iif = outputImageFile.getAbsolutePath();
                }
                Print.printPercentage(m, fet++, all);
                if(FixActions.isActionIn(FixActions.FIX_WARP, actions)){                
                    dewarpImageFile(iif, outputImageFile.getAbsolutePath(),errorFile.getAbsolutePath());
                }
                Print.printPercentage(m, fet++, all);   
            } 
            System.out.println();
        }else{
            //ERROR NO ES DIRECTORIO
            String dName;
            if(!inputDirFile.isDirectory()){
                dName = inputDir;
            }else{
                dName = outputDir;
            }
            String message = String.format("Error! %s is not a directory. this command need an input difectory and an output directory" , dName);
            try(FileWriter err = new FileWriter(errorFile, true)){
                err.write(message);
                Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, message);
            } catch (IOException ex) {
                Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }
    
    
    
    public void fixTransparencyImageFile(Configuration config){   
        switch (config.getCommandArgumentsSize()) {
            case 1:
                fixBackTransparencyImageFile(config.getInputFile());
                break;
            case 2:
                fixBackTransparencyImageFile(config.getInputFile(), config.getOutputFile());
                break;
            case 3:
                fixBackTransparencyImageFile(config.getInputFile(), config.getOutputFile(), config.getErrorFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for fixBackTransparencyImageFile command");
        }
    }
    
    public void fixBackTransparencyImageFile(String inputFile){
        fixBackTransparencyImageFile(inputFile, inputFile, inputFile);
    }
    
    public void fixBackTransparencyImageFile(String inputFile, String outputFile){
        fixBackTransparencyImageFile(inputFile, outputFile, outputFile);
    }
    
    public void fixBackTransparencyImageFile(String inputFile, String outputFile, String errorFile){
        transformImageFile("fixBackTransparency", inputFile, outputFile, errorFile, "java");
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
        transformImageFile("deskewImageFile", inputFile, outputFile, errorFile, "python");
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
        transformImageFile("dewarpImageFile", inputFile, outputFile, errorFile, "python");
    }
    
    private void transformImageFile(String command, String inputFile, String outputFile, String errorFile, String context){
        String strUrl = String.format("http://%s:%s%s%s", getHost(context), getPort(context),getPref(context), command);
//        Logger.getLogger(PortadaApi.class.getName()).log(Level.INFO, strUrl);
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
}
