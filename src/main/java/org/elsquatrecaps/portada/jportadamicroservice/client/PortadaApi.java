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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josepcanellas
 */
public class PortadaApi {
    public static String[] msContext = {"java", "python", "r"};
    public static String[] imagesExtensions = {".jpg", ".jpeg", ".png", "gif", ".tif", "tiff"};
    private final Map<String, ConnectionMs> conDataList = new HashMap<>();
    private Function<Integer, Void> progress;
    private Function<ProgressInfo, Void> publish;
    
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
        
    }
    public PortadaApi(Function<ProgressInfo, Void> publish) {
        this.publish = publish;
    }

    public PortadaApi(Function<ProgressInfo, Void> publish, Function<Integer, Void> progress) {
        this.publish = publish;
        this.progress = progress;
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
    
    private void publishInitInfo(String message, String process){
        if(publish!=null){
            publish.apply(new ProgressInfo(message, process));
        }
    }

    private void publishInfo(String message, String process, int fet, int all){
        if(publish!=null){
            int percent = 100*fet/all;
            publish.apply(new ProgressInfo(message, process, percent));
        }
    }
    
    private void publishError(String message, String process, int fet, int all, int errorState){
        if(publish!=null){
            publish.apply(new ProgressInfo(PortadaApi.ProgressInfo.ERROR, message, "", process, fet, all, errorState));
        }
    }
    
    private void publishProgress(String messagePrevious, String name, String process, int fet, int all, int errorState){
        if(progress!=null){
            progress.apply(fet);
        }
        if(publish!=null){
            publish.apply(new ProgressInfo(messagePrevious, name, process, fet, all, errorState));
        }
    }
    
    public void allImagesToFixOrder(Configuration config){
        switch (config.getCommandArgumentsSize()) {
            case 1:
                allImagesToFixOrder(config.getInputDir(), config.getInputDir(), config.getTeam());
                break;
            case 2:
                allImagesToFixOrder(config.getInputFile(), config.getOutputFile(), config.getTeam());
                break;
            case 3:
                allImagesToFixOrder(config.getInputFile(), config.getOutputFile(), config.getErrorFile(), config.getTeam());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for fixBackTransparencyImageFile command");             
         }        
    }
    
    private void allImagesToFixOrder(String inputDir, String outputDir, String team) {
        allImagesToFixOrder(inputDir, outputDir, "errors.txt",  team);
    }

    private void allImagesToFixOrder(String inputDir, String outputDir, String errorFileName, String team) {
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
            int all = lf.length;
            int fet=0;
            publishInfo("Starting process", "Redraw in correct order", fet++, all);
            HashMap p = new HashMap();
            p.put("team", team);
            int errorState=0;
            for(File inputImageFile: lf){
                File outputImageFile = new File(outputDirFile, inputImageFile.getName());
                String m = "image: ";
                String n = inputImageFile.getName();
                if(transformImageFile("redrawOrderedImageFile", inputImageFile.getAbsolutePath(), 
                        outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath(), p, "python")){
                    errorState=0;
                }else{
                    errorState=-1;
                }
                publishProgress(m, n, "Redraw in correct order", fet++, all, errorState);
            } 
//            System.out.println();
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
            publishError(message, "Redraw in correct order", -1, -1, -1);
        }        
    }

    
    public void allImagesToText(Configuration config){
         switch (config.getCommandArgumentsSize()) {
            case 1:
                allImagesToText(config.getInputDir(), config.getInputDir(), config.getTeam());
                break;
            case 2:
                allImagesToText(config.getInputFile(), config.getOutputFile(), config.getTeam());
                break;
            case 3:
                allImagesToText(config.getInputFile(), config.getOutputFile(), config.getErrorFile(), config.getTeam());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for fixBackTransparencyImageFile command");             
         }        
    }
    
    private void allImagesToText(String inputDir, String outputDir, String team) {
        allImagesToText(inputDir, outputDir, "errors.txt",  team);
    }

    private void allImagesToText(String inputDir, String outputDir, String errorFileName, String team) {
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
            int all = lf.length;
            int fet=0;
            publishInfo("Starting process", "OCR", fet++, all);
            HashMap p = new HashMap();
            p.put("team", team);
            int errorState=0;
            for(File inputImageFile: lf){
                File outputImageFile = new File(outputDirFile, inputImageFile.getName()
                        .substring(0, inputImageFile.getName().lastIndexOf(".")).concat(".txt"));
                String m = "image: ";
                String n = inputImageFile.getName();
                if(transformImageFile("ocr", inputImageFile.getAbsolutePath(), 
                        outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath(), p, "java")){
                    errorState=0;
                }else{
                    errorState=-1;
                }
                publishProgress(m, n, "OCR", fet++, all, errorState);
            } 
//            System.out.println();
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
            publishError(message, "OCR", -1, -1, -1);
        }        
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
        String p;
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
            int processTofix=0;
            if(FixActions.isActionIn(FixActions.FIX_TANSPARENCY, actions)){
                processTofix++;
            }
            if(FixActions.isActionIn(FixActions.FIX_WARP, actions)){                
                processTofix++;
            }
            if(FixActions.isActionIn(FixActions.FIX_SKEW, actions)){                
                processTofix++;
            }

            int all = lf.length*processTofix;
            int fet=0;
            int errorState=0;
            publishInfo("Starting process", "fix image", fet++, all);
            for(File inputImageFile: lf){
                boolean processOk;
                File outputImageFile = new File(outputDirFile, inputImageFile.getName());
                String m = "image: ";
                String n = inputImageFile.getName();
                String iif = inputImageFile.getAbsolutePath();
                if(FixActions.isActionIn(FixActions.FIX_TANSPARENCY, actions)){
                    processOk = fixBackTransparencyImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                    if(processOk){
                        iif = outputImageFile.getAbsolutePath();                        
                        errorState=0;
                    }else{
                        errorState=-1;
                    }
                    p = "fix transparency";
                    publishProgress(m, n, p, fet++, all, errorState);
                }                
                if(FixActions.isActionIn(FixActions.FIX_WARP, actions)){                
                    processOk = dewarpImageFile(iif, outputImageFile.getAbsolutePath(),errorFile.getAbsolutePath());
                    if(processOk){
                        iif = outputImageFile.getAbsolutePath();
                        errorState=0;
                    }else{
                        errorState=-1;
                    }
                    p = "dewarp";
                    publishProgress(m, n, p, fet++, all, errorState);
                }
                if(FixActions.isActionIn(FixActions.FIX_SKEW, actions)){                
                    processOk = deskewImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                    if(processOk){
                        errorState=0;
                    }else{
                        errorState=-1;
                    }
                    p = "deskew";
                    publishProgress(m, n, p, fet++, all, errorState);
                }
            } 
            //System.out.println();
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
            publishError(message, "fix image", -1, -1, -1);
        }        
    }
    
    
    
    public boolean fixTransparencyImageFile(Configuration config){   
        boolean ret;
        switch (config.getCommandArgumentsSize()) {
            case 1:
                ret = fixBackTransparencyImageFile(config.getInputFile());
                break;
            case 2:
                ret = fixBackTransparencyImageFile(config.getInputFile(), config.getOutputFile());
                break;
            case 3:
                ret = fixBackTransparencyImageFile(config.getInputFile(), config.getOutputFile(), config.getErrorFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for fixBackTransparencyImageFile command");
        }
        return ret;
    }
    
    public boolean fixBackTransparencyImageFile(String inputFile){
        return fixBackTransparencyImageFile(inputFile, inputFile, inputFile);
    }
    
    public boolean fixBackTransparencyImageFile(String inputFile, String outputFile){
        return fixBackTransparencyImageFile(inputFile, outputFile, outputFile);
    }
    
    public boolean fixBackTransparencyImageFile(String inputFile, String outputFile, String errorFile){
        return transformImageFile("fixBackTransparency", inputFile, outputFile, errorFile, "java");
    }
    
    public boolean deskewImageFile(Configuration config){
        boolean ret;
        switch (config.getCommandArgumentsSize()) {
            case 1:
                ret = deskewImageFile(config.getInputFile());
                break;
            case 2:
                ret = deskewImageFile(config.getInputFile(), config.getOutputFile());
                break;
            case 3:
                ret = deskewImageFile(config.getInputFile(), config.getOutputFile(), config.getErrorFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for deskewImageFile command");
        }
        return ret;
    }
    
    public boolean deskewImageFile(String inputFile){
        return deskewImageFile(inputFile, inputFile, inputFile);
    }
    
    public boolean deskewImageFile(String inputFile, String outputFile){
        return deskewImageFile(inputFile, outputFile, outputFile);
    }
    
    public boolean deskewImageFile(String inputFile, String outputFile, String errorFile){
        return transformImageFile("deskewImageFile", inputFile, outputFile, errorFile, "python");
    }
    
    public boolean dewarpImageFile(Configuration config){
        boolean ret;
        switch (config.getCommandArgumentsSize()) {
            case 1:
                ret = dewarpImageFile(config.getInputFile());
                break;
            case 2:
                ret = dewarpImageFile(config.getInputFile(), config.getOutputFile());
                break;
            case 3:
                ret = dewarpImageFile(config.getInputFile(), config.getOutputFile(), config.getErrorFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for deskewImageFile command");
        }
        return ret;        
    }
    
    public boolean dewarpImageFile(String inputFile){
        return dewarpImageFile(inputFile, inputFile, inputFile);
    }
    
    public boolean dewarpImageFile(String inputFile, String outputFile){
        return dewarpImageFile(inputFile, outputFile, outputFile);
    }
    
    public boolean dewarpImageFile(String inputFile, String outputFile, String errorFile){
        return transformImageFile("dewarpImageFile", inputFile, outputFile, errorFile, "python");
    }
    
    private boolean transformImageFile(String command, String inputFile, String outputFile, String errorFile, String context){
        return transformImageFile(command, inputFile, outputFile, errorFile, null, context);
    }
    
    private boolean transformImageFile(String command, String inputFile, String outputFile, String errorFile, HashMap<String, String> paramData, String context){
        boolean ret=true;
        String strUrl = String.format("http://%s:%s%s%s", getHost(context), getPort(context),getPref(context), command);
//        Logger.getLogger(PortadaApi.class.getName()).log(Level.INFO, strUrl);
        try{  
            File inFile = new File(inputFile);
            StringBuilder hwriter = new StringBuilder();
            if(paramData!=null){
                paramData.forEach((key, val) -> {
                    hwriter.append("--*****\r\n");
                    hwriter.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n");
                    hwriter.append("Content-Type: ").append("text/plain").append("\r\n");
                    hwriter.append("\r\n"); 
                    hwriter.append(val).append("\r\n");
                });
            }
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
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");
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
                ret = false;
            }
            con.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        } catch (IOException ex) {
            ret = false;
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
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
    
    public static final class ProgressInfo{
        public static final int ERROR=-1;
        public static final int PROCESS=0;
        public static final int MESSAGE=1;
        private int type=0;  
        private String pre;
        private String name;
        private String process;
        private int progress;
        private int maxProgress;
        int errorState=0;

        public ProgressInfo(String message, String process) {
            this(MESSAGE, message, "", process, 0, 100, 0);
        }
        
        public ProgressInfo(String message, String process, int percent) {
            this(MESSAGE, message, "", process, percent, 100, 0);
        }
        
        public ProgressInfo(String name, String process, int progress, int maxProgress) {
            this(PROCESS, "", name, process, progress, maxProgress, 0);
        }
        
        public ProgressInfo(String preNameOrMessage, String name, String process, int progress, int maxProgress) {
            this(PROCESS, preNameOrMessage, name, process, progress, maxProgress, 0);
        }
        
        public ProgressInfo(String name, String process, int progress, int maxProgress, int errorState) {
            this(PROCESS, "", name, process, progress, maxProgress, errorState);
        }
        
        public ProgressInfo(String preNameOrMessage, String name, String process, int progress, int maxProgress, int errorState) {
            this(PROCESS, preNameOrMessage, name, process, progress, maxProgress, errorState);
        }
        
        public ProgressInfo(int type, String preNameOrMessage, String name, String process, int progress, int maxProgress, int errorState) {
            this.type=type;
            this.pre= preNameOrMessage;
            this.name = name;
            this.process = process;
            this.progress = progress;
            this.maxProgress = maxProgress;
            this.errorState = errorState;
        }

        public String getMessage(){
            return this.pre.concat(this.name);
        }
        
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the process
         */
        public String getProcess() {
            return process;
        }

        /**
         * @return the progress
         */
        public int getProgress() {
            return progress;
        }

        /**
         * @return the maxProgress
         */
        public int getMaxProgress() {
            return maxProgress;
        }
        
        public int getErrorState(){
            return errorState;
        }
        
        public int getType(){
            return type;
        }
        
    }
}
