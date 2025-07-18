package org.elsquatrecaps.portada.jportadamicroservice.client;

import org.elsquatrecaps.portada.jportadamicroservice.client.services.ProgressInfo;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.elsquatrecaps.autonewsextractor.tools.ReaderTools;
import org.elsquatrecaps.autonewsextractor.tools.configuration.AutoNewsExtractorConfiguration;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.PublisherService;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.extractor.FileExtractorSevice;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.imagefile.ImageFileService;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.imagefile.ImageQualityFilterService;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.imagefile.QwenOcrService;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.publickey.PublicKeyService;
import org.elsquatrecaps.portada.jportadamscaller.ConnectionMs;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author josepcanellas
 */
public class PortadaApi {
//    public static String[] msContext = {"java", "python", "r"};
    public static String[] imagesExtensions = {".jpg", ".jpeg", ".png", "gif", ".tif", "tiff"};
//    private final Map<String, ConnectionMs> conDataList = new HashMap<>();
    private Function<ProgressInfo, Void> publish;
    PublisherService publisherService=new PublisherService();
    ImageFileService imageFileService=new ImageFileService();
    ImageQualityFilterService imageQualityFilterService = new ImageQualityFilterService();
    PublicKeyService publicKeyService=new PublicKeyService();
    FileExtractorSevice fileExtractorSevice=new FileExtractorSevice();
    QwenOcrService qwenOcrService = new QwenOcrService();
    
    public PortadaApi() {
        
    }
    public PortadaApi(Function<ProgressInfo, Void> publish) {
        this.publish = publish;
    }

    public final void init(Configuration config) {
        Map<String, ConnectionMs> conDataList = new HashMap<>();
        for(String ctx: PublisherService.msContext){
            conDataList.put(ctx, new ConnectionMs(config.getProtocols(ctx), config.getPort(ctx), config.getHosts(ctx), config.getPrefs(ctx)));
        }
//        ((PublisherService)publicKeyService.init(conDataList)).init(publish);
//        ((PublisherService)imageFileService.init(conDataList)).init(publish);
//        ((PublisherService)publisherService.init(conDataList)).init(publish);
//        ((PublisherService)fileExtractorSevice.init(conDataList)).init(publish);
//        ((PublisherService)imageQualityFilterService.init(conDataList)).init(publish);
        publicKeyService.init(publish).init(conDataList);
        imageFileService.init(publish).init(conDataList);
        publisherService.init(publish).init(conDataList);
        fileExtractorSevice.init(publish).init(conDataList);
        imageQualityFilterService.init(publish).init(conDataList);
        qwenOcrService.init(publish).init(conDataList);
    }
    
    public final void init(){
        Configuration cfg = new Configuration();
        cfg.configure();
        init(cfg);
    }
    
    public void jtest(Configuration configuration) {
        test(configuration.getTeam(), "java", "jtest");
    }
    
    public void pytest(Configuration configuration) {
        test(configuration.getTeam(), "python", "pytest");
    }
    
    public void rtest(Configuration configuration) {
        test(configuration.getTeam(), "r", "rtest");
    }
    
    public void test(String team, String contex, String process) {
        String ret;
        try {
            HashMap p = new HashMap();
            p.put("team", team);
            JSONObject jsonresponse = new JSONObject(publisherService.sendData("test", p, contex));
            ret = jsonresponse.getString("message");
            publishInfo(ret, process);
        }catch (Exception ex){
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), process, -1);            
        }
    }

    public void acceptKey(Configuration configuration){
        publicKeyService.acceptKey(configuration.getTeam(), configuration.getPk(), configuration.getUser(), configuration.getPass());
    }
    
    public void deleteKey(Configuration configuration){
        publicKeyService.deleteKey(configuration.getTeam(), configuration.getPk(), configuration.getUser(), configuration.getPass());
    }
    
    public void verifyCodeSentByMail(Configuration configuration){
        publicKeyService.verifyCodeSentByMail(configuration.getTeam(), configuration.getEmail(), configuration.getVerificationCode());
    }
    
    public void requestAccesPermission(Configuration configuration){
        publicKeyService.requestAccesPermission(configuration.getTeam(), configuration.getEmail(), configuration.getForceKeyGeneration());
    }
    

    public void extractAllDataFromDir(Configuration config){
        boolean remoteConfig = config.getExtractConfigMode().startsWith("R");
        try {
            if(remoteConfig){
                fileExtractorSevice.init(config.getExtractConfigProtertiesFile());
                if(config.getExtractJsonConfigParsersFile()!=null){
                    fileExtractorSevice.init(new File(config.getExtractJsonConfigParsersFile()));
                }
            }else{
                    AutoNewsExtractorConfiguration extractorConf = new AutoNewsExtractorConfiguration();
                    String[] args = {"-d", config.getInputDir(),
                        "-x", config.getExtractExtensionFile()};
                    extractorConf.parseArgumentsAndConfigure(args, config.getExtractConfigProtertiesFile());
                    fileExtractorSevice.init(extractorConf);
                    if(config.getExtractJsonConfigParsersFile()!=null){
                        fileExtractorSevice.init(new File(config.getExtractJsonConfigParsersFile()));
                    }else{
                        fileExtractorSevice.init(new File(extractorConf.getParserConfigJsonFile()));
                    }
            }
            fileExtractorSevice.processFiles(config.getTeam(), config.getOutputFile(), config.getInputDir(), config.getExtractExtensionFile());
        } catch (IOException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
//    public void extractAllDataFromDir(Configuration config){ //REACTIVE
//        String configPath;
//        JSONObject cfgJsonParsers=null;
//        AutoNewsExtractorConfiguration extractorConf=null;
//        boolean remoteConfig = config.getExtractConfigMode().startsWith("R");
//        if(config.getExtractConfigProtertiesFile()==null){
//            //error
//            throw new UnsupportedOperationException();
//        }else{
//            configPath = config.getExtractConfigProtertiesFile();
//        }
//        if(config.getExtractJsonConfigParsersFile()!=null){
//            try {
//                String jsc = new String(Files.readAllBytes(Paths.get(config.getExtractJsonConfigParsersFile())));   
//                cfgJsonParsers = new JSONObject(jsc);
//            } catch (IOException ex) {
//                Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
//                //ERROR
//                throw new UnsupportedOperationException();
//            }
//        }
//        if(!remoteConfig){
//            try {
//                extractorConf = new AutoNewsExtractorConfiguration();
//                String[] args = {"-i", config.getInputDir(), 
//                                 "-ext", config.getExtractExtensionFile()};
//                extractorConf.parseArgumentsAndConfigure(args, configPath);
//            } catch (IOException ex) {
//                //TODO: ERROR extractor configFile not found
//                throw new UnsupportedOperationException(ex);
//            }
//        }
//        
//        BoatFactVersionUpdater.BoatFactVersionUpdaterResponse r;
//        FileExtractorSevice service = fileExtractorSevice;
//        if(remoteConfig){
//            if(cfgJsonParsers==null){
//                //TODO: crida remota passant configPath com a newspaper
//                service.init(configPath);
//                //TODO: El config és al servidor. Cal comprovar la seva versió de forma remota.
//                r = service.processUpdate();
//            }else{
//                //TODO: El config és local. Cal comprovar la seva versió en local i fer canvis si cal.
//                r = BoatFactVersionUpdater.tryToUpdate(cfgJsonParsers);
//                //TODO: crida remota passant configPath i el contingut del cfgJsonParsers
//                service.init(configPath).init(cfgJsonParsers);
//            }
//        }else if(extractorConf!=null && cfgJsonParsers!=null){
//            //TODO: El config és local. Cal comprovar la seva versió al servidor i fer canvis si cal.
//            r = BoatFactVersionUpdater.tryToUpdate(cfgJsonParsers);
//            //TODO: crida romota passant el contingut de les dues configuracions
//            service.init(extractorConf).init(cfgJsonParsers);
//        }else{
//            //[TODO: Impossible si la configuració ambdós fitxers de configuració han de ser local
//            //generar ERROR
//            throw new UnsupportedOperationException();
//        }
        
//        if(!r.isError()){
//            Map<String, List<ExtractedData>> responseMap = new HashMap<>();
//            final JSONObject parsersCfg = cfgJsonParsers;
//            service.processFiles(config.getInputDir(), config.getExtractExtensionFile()).subscribe(item-> {
//                    JSONObject resp = new JSONObject(item);
//                    if(resp.getInt("statusCode")==0){
//                        if(!responseMap.containsKey(resp.getString("parser_name"))){
//                            responseMap.put(resp.getString("parser_name"), new ArrayList<>());
//                        }
//                        JSONArray dataList = resp.getJSONArray("data_list");
//                        for(int i=0; i<dataList.length(); i++){
//                            responseMap.get(resp.getString("parser_name")).add(new MutableNewsExtractedData(dataList.getJSONObject(i)));
//                        }
//                        publishProgress(resp.getString("information_unit_name"), "extraction process", resp.getInt("percent"), 100);
//                    }else{
//                        //ERROR
//                        //generar ERROR
//                        throw new UnsupportedOperationException();
//                    }
//                },
//                error -> {
//                    //TODO: ON ERROR
//                    error.printStackTrace();
//                    publishErrorInfo(error.getMessage(), "extraction process", -100);
//                }, new Runnable() {
//                @Override
//                public void run() {
//                    BoatFactCsvFormatter csvFormatter = new BoatFactCsvFormatter();
//                    for(String parser: responseMap.keySet()){
//                        String fn = String.format("%s_%s",config.getOutputFile(), parser);
//                        if(parsersCfg.optJSONObject(parser)!=null && parsersCfg.optJSONObject(parser).has("csv_view")){
//                            JSONObject csvParams = parsersCfg.getJSONObject(parser).getJSONObject("csv_view");
//                            csvFormatter.configHeaderFields(csvParams).format(responseMap.get(parser)).toFile(fn);
//                        }
//                        JsonFileFormatterForExtractedData<ExtractedData> jsonFormatter = new JsonFileFormatterForExtractedData<>(responseMap.get(parser));
//                        jsonFormatter.toFile(fn);
//                    }
//                }
//            });
//        }
//    }
    
    public void testAllImagesToParagraphs(Configuration config){
        String processName = "Testing to marck columns in correct order";
        String command = "testParagraphImageFile";
        String context = "python";
        allImagesToGetImagesList(config, command, context, processName);        
    }
    
    public void allImagesToParagraphs(Configuration config){
        String processName = "Cutting paragraphs in correct order";
        String command = "redrawParagraphImageFile";
        String context = "python";
        allImagesToGetImagesList(config, command, context, processName);
    }

    public void allImagesToColumns(Configuration config){
        String processName = "Cutting columns in correct order";
        String command = "redrawColumnImageFile";
        String context = "python";
        allImagesToGetImagesList(config, command, context, processName);
    }
    
    public void allImagesToBlocks(Configuration config){
        String processName = "Cutting blocks in correct order";
        String command = "redrawBlockImageFile";
        String context = "python";
        allImagesToGetImagesList(config, command, context, processName);
    }
    
    public void allImagesToFixOrder(Configuration config){
        String processName = "Redraw in correct order";
        String command = "pr/redrawOrderedImageFile";
        String context = "python";
        allImagesToGetImagesList(config, command, context, processName);
    }
    
    public void allImagesToGetImagesList(Configuration config, String cmd, String context, String processName){
        switch (config.getCommandArgumentsSize()) {
            case 1:
                allImagesToGetImagesList(cmd, context, config.getInputDir(), config.getInputDir(), config.getTeam(), processName);
                break;
            case 2:
                allImagesToGetImagesList(cmd, context,config.getInputFile(), config.getOutputFile(), config.getTeam(), processName);
                break;
            case 3:
                allImagesToGetImagesList(cmd, context,config.getInputFile(), config.getOutputFile(), config.getErrorFile(), config.getTeam(), processName);
                break;
            default:
                throw new RuntimeException("Bad number of parametres for allImagesToGetImagesList command");             
         }        
    }
    
    private void allImagesToGetImagesList(String cmd, String context,  String inputDir, String outputDir, String team, String processName) {
        allImagesToGetImagesList(cmd, context, inputDir, outputDir, "errors.txt",  team, processName);
    }

    private void allImagesToGetImagesList(String command, String context, String inputDir, String outputDir, String errorFileName, String team, String processName) {
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
            Arrays.sort(lf);
            int all = lf.length;
            int fet=0;
            publishInfo("Starting process", processName );
            HashMap p = new HashMap();
            if (team !=null){
                p.put("team", team);
            }
            for(File inputImageFile: lf){
                String m = "image: ";
                String n = inputImageFile.getName();
                String iname = n.split("\\.")[0];
                File outName = new File(new File(outputDir), iname);
                JSONObject jsonresponse = new JSONObject(
                        imageFileService.transformImageFileToJsonImages(command, 
                                inputImageFile.getAbsolutePath(), 
                                outName.getAbsolutePath(), p, context));
                if(jsonresponse.getInt("status")==0){
                    publishProgress(m, n, processName, ++fet, all);
                }else{
                    publishErrorProgress(m, n, processName, ++fet, all, -1);
                }
            } 
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
            publishErrorInfo(message, processName,-1);
        }        
    }
   
    public void allImagesToJson(Configuration config){
         switch (config.getCommandArgumentsSize()) {
            case 1:
                allImagesToJson(config.getInputDir(), config.getInputDir(), config.getTeam());
                break;
            case 2:
                allImagesToJson(config.getInputFile(), config.getOutputFile(), config.getTeam());
                break;
            case 3:
                allImagesToJson(config.getInputFile(), config.getOutputFile(), config.getErrorFile(), config.getTeam());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for allImagesToJson command");             
         }                
    }
    
    private void allImagesToJson(String inputDir, String outputDir, String team) {
        allImagesToJson(inputDir, outputDir, "errors.txt",  team);
    }

    private void allImagesToJson(String inputDir, String outputDir, String errorFileName, String team) {
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
            Arrays.sort(lf);
            int all = lf.length;
            int fet=0;
            publishInfo("Starting process", "OCR");
            HashMap p = new HashMap();
            p.put("team", team);
            int errorState=0;
            for(File inputImageFile: lf){
                File outputImageFile = new File(outputDirFile, inputImageFile.getName()
                        .substring(0, inputImageFile.getName().lastIndexOf(".")).concat(".json"));
                String m = "image: ";
                String n = inputImageFile.getName();
                if(imageFileService.transformImageFile("pr/ocrJson", inputImageFile.getAbsolutePath(), 
                        outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath(), p, "java")){
                    publishProgress(m, n, "OCR", ++fet, all);
                }else{
                    publishErrorProgress(m, n, "OCR", ++fet, all, errorState);
                }
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
            publishErrorInfo(message, "OCR", -1);
        }        
    }
    
    public void fixAllOcr(Configuration config){
         switch (config.getCommandArgumentsSize()) {
            case 2:
                fixAllOcr(config.getTeam(), config.getInputDir(), config.getExtraInputDir(), config.getInputDir(), "errors.txt", null);
                break;
            case 3:
                fixAllOcr(config.getTeam(), config.getInputDir(), config.getExtraInputDir(), config.getOutputFile(), "errors.txt", null);
                break;
            case 4:
                if(config.getErrorFile()==null){
                    fixAllOcr(config.getTeam(), config.getInputDir(), config.getExtraInputDir(), config.getOutputFile(), "errors.txt", config.getConfigFile());
                }else{
                    fixAllOcr(config.getTeam(), config.getInputDir(), config.getExtraInputDir(), config.getOutputFile(), config.getErrorFile(), null);
                }
                break;
            case 5:
                fixAllOcr(config.getTeam(), config.getInputDir(), config.getExtraInputDir(), config.getOutputFile(), config.getErrorFile(), config.getConfigFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for allImagesToText command");             
         }        
    }
    
    private void fixAllOcr(String team, String textDir, String imagesDir, String outputDir, String errorFileName, String jsonConfigPath){
        Map<String, List<File>> textFilesToFix = new TreeMap<>();
        Map<String, List<File>> imagesFilesForFixing = new HashMap<>();
        File errorFile = new File(errorFileName);
        File textDirFile = new File(textDir);
        File imagesDirFile = new File(imagesDir);
        File outputDirFile = new File(outputDir);
        File jsonConfigFile = jsonConfigPath==null?null:new File(jsonConfigPath);
        if(errorFile.exists()){
            errorFile.delete();
        }
        if(!outputDirFile.exists()){
            outputDirFile.mkdirs();
        }
        if(textDirFile.isDirectory() && imagesDirFile.isDirectory() && outputDirFile.isDirectory()){
            File[] ltf = textDirFile.listFiles();
            File[] lif = imagesDirFile.listFiles();
            Arrays.sort(ltf);
            Arrays.sort(lif);
            String page = "";
            for(File f:ltf){
                if(f.getName().matches("\\d{4}_\\d{2}_\\d{2}_[A-Z]{3}_[A-Z]{2}_[A-Z]_\\d{2}.*?\\.txt")){
                    String name = f.getName().substring(0, 20);
                    if(!textFilesToFix.containsKey(name)){
                        textFilesToFix.put(name, new ArrayList<>());
                    }
                    textFilesToFix.get(name).add(f);
                }
            }            
            for(File f:lif){
                if(f.getName().matches("\\d{4}_\\d{2}_\\d{2}_[A-Z]{3}_[A-Z]{2}_[A-Z]_\\d{2}.*?((\\.jpg)|(\\.jpeg)|(\\.png)|(\\.gif))")){
                    String name = f.getName().substring(0, 20);
                    if(!imagesFilesForFixing.containsKey(name)){
                        imagesFilesForFixing.put(name, new ArrayList<>());
                    }
                    imagesFilesForFixing.get(name).add(f);
                }
            }
            publishInfo("Starting process", "FixingOCR");
            int all = textFilesToFix.size();
            int fet=0;
            for(String k: textFilesToFix.keySet()){
                fet++;
                if(imagesFilesForFixing.containsKey(k)){
                    if(!textFilesToFix.get(k).isEmpty()){
                         page = textFilesToFix.get(k).get(0).getName().substring(20, 22);
                    }
                    JSONObject resp = qwenOcrService.fixOcr(team, textFilesToFix.get(k), imagesFilesForFixing.get(k), jsonConfigFile);                   
                    if(resp.getInt("status")==0){
                        String ocrText = ReaderTools.doubleLf2SingleLf(resp.getString("text"));
                        try {
                            //save ocr file
                            FileUtils.writeStringToFile(new File(new File(outputDir), k.concat(page).concat(".txt")), ocrText, Charset.defaultCharset());
                            publishProgress("Information Unit:", k, "FIXING OCR", fet, all);
                        } catch (IOException ex) {
                            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
                            publishErrorProgress(String.format("Error! %s, for Information Unit:",ex.getMessage()), k, "ERROR FIXING OCR", fet, all, -1);
                        }
                    }else{
                     publishErrorProgress(String.format("Error! %s, for Information Unit:",resp.getString("error_message")), k, "ERROR FIXING OCR", fet, all, -1);
                    }
                }else{
                    publishErrorProgress("Information Unit:", k, "ERROR FIXING OCR. Image doesn't exist.", fet, all, -1);
                }
            }
        }else{
            //ERROR NO ES DIRECTORIO
            String dName;
            if(!textDirFile.isDirectory()){
                dName = textDir;
            }else if(!imagesDirFile.isDirectory()){
                dName = imagesDir;
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
            publishErrorInfo(message, "FIXING OCR", -1);
        }        
    }

    public void allImagesToTextQwenAI(Configuration config){
         switch (config.getCommandArgumentsSize()) {
            case 1:
                allImagesToTextQwenAI(config.getTeam(), config.getInputDir(), config.getInputDir(), "error.txt",
                        config.getAutoDiscard(), config.getDiscardFolder(), config.getConfigFile());
                break;
            case 2:
                allImagesToTextQwenAI(config.getTeam(), config.getInputDir(), config.getOutputFile(), "error.txt",
                        config.getAutoDiscard(), config.getDiscardFolder(), config.getConfigFile());
                break;
            case 3:
                allImagesToTextQwenAI(config.getTeam(), config.getInputDir(), config.getOutputFile(), config.getErrorFile(), 
                        config.getAutoDiscard(), config.getDiscardFolder(),config.getConfigFile());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for allImagesToText command");             
         }        
    }
    
    public void allImagesToTextQwenAI(String team, String imagesDir, String outputDir, String errorFileName, 
                                        boolean autoDiscard, String discardFolder, String jsonConfigPath){
        Map<String, List<File>> imagesFilesForFixing = new HashMap<>();
        double thresholdToDiscard = 0.6;
        File errorFile = new File(errorFileName);
        File inputDirFile = new File(imagesDir);
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
            Arrays.sort(lf);
            int all = lf.length;
            int fet=0;
            publishInfo("Starting process", "OCR");
            HashMap p = new HashMap();
            p.put("team", team);
            String page="";
            for(File inputImageFile: lf){
                String m = "image: ";
                String n = inputImageFile.getName();
                boolean discardImage = false;
                if(autoDiscard){
                    JSONObject resp = imageQualityFilterService.processBinaryFilter(
                            inputImageFile.getAbsolutePath(), thresholdToDiscard);
                    if(resp.getInt("status")==0){
                        discardImage = resp.getJSONObject("result").getDouble("score")>thresholdToDiscard;
                    }else{
                         publishErrorProgress(m, n, "DISCARDING IMAGE", ++fet, all, -1);
                    }
                }
                if(discardImage){
                    try {
                        File outputImageFile = new File(discardFolder, inputImageFile.getName());
                        Files.copy(inputImageFile.toPath(), outputImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        publishProgress(m, n, "OCR DISCARDED", ++fet, all);
                    } catch (IOException ex) {
                        publishErrorProgress(m, n, "OCR DISCARDED", ++fet, all, -1);
                    }
                }else{
                    if(inputImageFile.getName().matches("\\d{4}_\\d{2}_\\d{2}_[A-Z]{3}_[A-Z]{2}_[A-Z]_\\d{2}.*?((\\.jpg)|(\\.jpeg)|(\\.png)|(\\.gif))")){
                        String name = inputImageFile.getName().substring(0, 20);
                        if(!imagesFilesForFixing.containsKey(name)){
                            imagesFilesForFixing.put(name, new ArrayList<>());
                        }
                        imagesFilesForFixing.get(name).add(inputImageFile);
                    }
                }
            }
            for(String k: imagesFilesForFixing.keySet()){
                fet++;
                File jsonConfigFile = null;
                if(jsonConfigPath!=null){
                    jsonConfigFile = new File(jsonConfigPath);
                }
                if(!imagesFilesForFixing.get(k).isEmpty()){
                    page=imagesFilesForFixing.get(k).get(0).getName().substring(20, 22);
                }
                JSONObject resp = qwenOcrService.getOcr(team, imagesFilesForFixing.get(k), jsonConfigFile);                   
                if(resp.getInt("status")==0){
                    String ocrText = ReaderTools.doubleLf2SingleLf(resp.getString("text"));
                    //save ocr file
                    try {
                        //save ocr file
                        FileUtils.writeStringToFile(new File(new File(outputDir), k.concat(page).concat(".txt")), ocrText, Charset.defaultCharset());
                        publishProgress("Information Unit:", k, "QWEN OCR", fet, all);
                    } catch (IOException ex) {
                        Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
                        publishErrorProgress(String.format("Error! %s, for Information Unit:",ex.getMessage()), k, "ERROR QWEN OCR", fet, all, -1);
                    }
                }else{
                     publishErrorProgress(String.format("Error! %s, for Information Unit:",resp.getString("error_message")), k, "ERROR QWEN OCR", fet, all, -1);
                }
            }                    
        }else{
            //ERROR NO ES DIRECTORIO
            String dName;
            if(!inputDirFile.isDirectory()){
                dName = imagesDir;
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
            publishErrorInfo(message, "OCR", -1);
        }            
    }

    public void allImagesToTextWithDocumentAI(Configuration config){
         switch (config.getCommandArgumentsSize()) {
            case 1:
                allImagesToTextWithDocumentAI(config.getInputDir(), config.getInputDir(), 
                        config.getTeam(), config.getAutoDiscard(), 
                        config.getDiscardFolder(), config.getOcrtxt(), 
                        config.getOcrJson());
                break;
            case 2:
                allImagesToTextWithDocumentAI(config.getInputDir(), config.getOutputFile(), 
                        config.getTeam(), config.getAutoDiscard(), 
                        config.getDiscardFolder(), config.getOcrtxt(), 
                        config.getOcrJson());
                break;
            case 3:
                allImagesToTextWithDocumentAI(config.getInputDir(), config.getOutputFile(), config.getErrorFile(), 
                        config.getTeam(), config.getAutoDiscard(), config.getDiscardFolder(),
                        config.getOcrtxt(), config.getOcrJson());
                break;
            default:
                throw new RuntimeException("Bad number of parametres for allImagesToText command");             
         }        
    }
    
    private void allImagesToTextWithDocumentAI(String inputDir, String outputDir, String team, boolean autoDiscard, String discardFolder, boolean outpu_txt, boolean output_json) {
        allImagesToTextWithDocumentAI(inputDir, outputDir, "errors.txt",  team, autoDiscard, discardFolder, outpu_txt, output_json);
    }

    private void allImagesToTextWithDocumentAI(String inputDir, String outputDir, String errorFileName, String team, boolean autoDiscard, String discardFolder, boolean outpu_txt, boolean output_json) {
        double thresholdToDiscard = 0.6;
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
            Arrays.sort(lf);
            int all = lf.length;
            int fet=0;
            publishInfo("Starting process", "OCR");
            HashMap p = new HashMap();
            p.put("team", team);
            for(File inputImageFile: lf){
                String m = "image: ";
                String n = inputImageFile.getName();
                boolean discardImage = false;
                if(autoDiscard){
                    JSONObject resp = imageQualityFilterService.processBinaryFilter(
                            inputImageFile.getAbsolutePath(), thresholdToDiscard);
                    if(resp.getInt("status")==0){
                        discardImage = resp.getJSONObject("result").getDouble("score")>thresholdToDiscard;
                    }else{
                         publishErrorProgress(m, n, "DISCARDING IMAGE", ++fet, all, -1);
                    }
                }
                if(discardImage){
                    try {
                        File outputImageFile = new File(discardFolder, inputImageFile.getName());
                        Files.copy(inputImageFile.toPath(), outputImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        publishProgress(m, n, "OCR DISCARDED", ++fet, all);
                    } catch (IOException ex) {
                        publishErrorProgress(m, n, "OCR DISCARDED", ++fet, all, -1);
                    }
                }else{
                    if(outpu_txt && output_json){
                        File outputImageFile = new File(outputDirFile, inputImageFile.getName()
                                .substring(0, inputImageFile.getName().lastIndexOf(".")));    
                        JSONObject resp = new JSONObject(imageFileService.transformImageFileToOcrData("pr/ocr_txt_and_json", 
                                inputImageFile.getAbsolutePath(), outputImageFile.getAbsolutePath(), p, 
                                "java"));
                        if(resp.getInt("status")==0){
                           publishProgress(m, n, "OCR", ++fet, all);
                        }else{
                           publishErrorProgress(m, n, "OCR", ++fet, all, -1); 
                        }                        
                    }else{
                        File outputImageFile = new File(outputDirFile, inputImageFile.getName()
                                .substring(0, inputImageFile.getName().lastIndexOf(".")).concat(outpu_txt?".txt":"json"));
                        if(imageFileService.transformImageFile(outpu_txt?"pr/ocr":"pr/ocrJson", inputImageFile.getAbsolutePath(), 
                                outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath(), p, "java")){
                            publishProgress(m, n, "OCR", ++fet, all);
                        }else{
                            publishErrorProgress(m, n, "OCR", ++fet, all, -1);
                        }
                    }
                }
            } 
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
            publishErrorInfo(message, "OCR", -1);
        }        
    }

    public void autoCorrectAllImages(Configuration config){
         switch (config.getCommandArgumentsSize()) {
            case 1:
                autoCorrectAllImages(config.getInputDir(), config.getInputDir(),
                        FixActions.getActions(config.getFixTransparency(), config.getFixSkew(), config.getFixWarp()));
                break;
            case 2:
                autoCorrectAllImages(config.getInputFile(), config.getOutputFile(),
                        FixActions.getActions(config.getFixTransparency(), config.getFixSkew(), config.getFixWarp()));
                break;
            case 3:
                autoCorrectAllImages(config.getInputFile(), config.getOutputFile(), config.getErrorFile(),
                        FixActions.getActions(config.getFixTransparency(), config.getFixSkew(), config.getFixWarp()));
                break;
            default:
                throw new RuntimeException("Bad number of parametres for autoCorrectAllImages command");             
         }        
    }
    
    public void autoCorrectAllImages(String inputDir, String outputDir, int noActions){
        autoCorrectAllImages(inputDir, outputDir, "errors.txt", noActions);
    }
    
    public void autoCorrectAllImages(String inputDir, String outputDir, String errorFileName, int noActions){
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
            Arrays.sort(lf);
            int fet=0;
            int all = lf.length;
            int actions = 0;
            publishInfo("Starting process", "fix image");
            for(File inputImageFile: lf){
                boolean processOk;
                File outputImageFile = new File(outputDirFile, inputImageFile.getName());
                String m = "image: ";
                String n = inputImageFile.getName();
                String iif = inputImageFile.getAbsolutePath();
                
                JSONObject multiclassFilter = imageQualityFilterService.processMulticlassFilter(iif, 0.6);
                if(multiclassFilter.getInt("status")==0){
                    JSONArray transfomationIndex = multiclassFilter.getJSONObject("result").getJSONArray("suggested_transformation_indexes");
                    actions = 0;
                    int cActions=0;
                    for(int i= 0; i < transfomationIndex.length(); i++){
                        switch (transfomationIndex.getInt(i)) {
                            case 0:
                                if(!FixActions.isActionIn(FixActions.FIX_WARP, noActions)){
                                    actions += FixActions.FIX_WARP.getId();
                                    cActions++;
                                }
                                break;
                            case 1:
                                if(!FixActions.isActionIn(FixActions.FIX_SKEW, noActions)){
                                    actions += FixActions.FIX_SKEW.getId();
                                    cActions++;
                                }
                                break;
                            case 3:
                                if(!FixActions.isActionIn(FixActions.FIX_TANSPARENCY, noActions)){
                                    actions += FixActions.FIX_TANSPARENCY.getId();
                                    cActions++;
                                }
                                break;
                        }
                    }
                    if(cActions>1){
                        all += cActions-1;
                    }
                    if(actions==0){
                        try {
                            Files.copy(inputImageFile.toPath(), outputImageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            processOk = true;
                        } catch (IOException ex) {
                            processOk = false;
                        }
                        p = "copying good image";
                        if(processOk){
                            publishProgress(m, n, p, ++fet, all);
                        }else{
                            publishErrorProgress(m, n, p, ++fet, all, -1);                    
                        }
                    }else{
                        if(FixActions.isActionIn(FixActions.FIX_TANSPARENCY, actions)){
                            processOk = fixBackTransparencyImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                            p = "fix transparency";
                            if(processOk){
                                iif = outputImageFile.getAbsolutePath();                        
                                publishProgress(m, n, p, ++fet, all);
                            }else{
                                publishErrorProgress(m, n, p, ++fet, all, -1);                    
                            }
                        }                
                        if(FixActions.isActionIn(FixActions.FIX_WARP, actions)){                
                            processOk = dewarpImageFile(iif, outputImageFile.getAbsolutePath(),errorFile.getAbsolutePath());
                            p = "dewarp";
                            if(processOk){
                                iif = outputImageFile.getAbsolutePath();                        
                                publishProgress(m, n, p, ++fet, all);
                            }else{
                                publishErrorProgress(m, n, p, ++fet, all, -1);                    
                            }                    
                        }
                        if(FixActions.isActionIn(FixActions.FIX_SKEW, actions)){                
                            processOk = deskewImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                            p = "deskew";
                            if(processOk){
                                publishProgress(m, n, p, ++fet, all);
                            }else{
                                publishErrorProgress(m, n, p, ++fet, all, multiclassFilter.getInt("status"));                    
                            }                    
                        }
                    }
                }else{
                    publishErrorInfo(multiclassFilter.getString("message"), "fix image", -1);
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
            publishErrorInfo(message, "fix image", -1);
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
                throw new RuntimeException("Bad number of parametres for fixAllImages command");             
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
            Arrays.sort(lf);
            int all = lf.length*processTofix;
            int fet=0;
            publishInfo("Starting process", "fix image");
            for(File inputImageFile: lf){
                boolean processOk;
                File outputImageFile = new File(outputDirFile, inputImageFile.getName());
                String m = "image: ";
                String n = inputImageFile.getName();
                String iif = inputImageFile.getAbsolutePath();
                if(FixActions.isActionIn(FixActions.FIX_TANSPARENCY, actions)){
                    processOk = fixBackTransparencyImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                    p = "fix transparency";
                    if(processOk){
                        iif = outputImageFile.getAbsolutePath();                        
                        publishProgress(m, n, p, ++fet, all);
                    }else{
                        publishErrorProgress(m, n, p, ++fet, all, -1);                    
                    }
                }                
                if(FixActions.isActionIn(FixActions.FIX_WARP, actions)){                
                    processOk = dewarpImageFile(iif, outputImageFile.getAbsolutePath(),errorFile.getAbsolutePath());
                    p = "dewarp";
                    if(processOk){
                        iif = outputImageFile.getAbsolutePath();                        
                        publishProgress(m, n, p, ++fet, all);
                    }else{
                        publishErrorProgress(m, n, p, ++fet, all, -1);                    
                    }                    
                }
                if(FixActions.isActionIn(FixActions.FIX_SKEW, actions)){                
                    processOk = deskewImageFile(iif, outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath());
                    p = "deskew";
                    if(processOk){
                        publishProgress(m, n, p, ++fet, all);
                    }else{
                        publishErrorProgress(m, n, p, ++fet, all, -1);                    
                    }                    
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
            publishErrorInfo(message, "fix image", -1);
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
    
    public boolean fixBackTransparencyImageFile(String inputFile, String outputFile, String errorFile){
        return imageFileService.transformImageFile("fixBackTransparency", inputFile, outputFile, errorFile, "java");
    }
    
    public boolean fixBackTransparencyImageFile(String inputFile){
        return fixBackTransparencyImageFile(inputFile, inputFile, inputFile);
    }
    
    public boolean fixBackTransparencyImageFile(String inputFile, String outputFile){
        return fixBackTransparencyImageFile(inputFile, outputFile, outputFile);
    }
    
//    public boolean fixBackTransparencyImageFile(String inputFile, String outputFile, String errorFile){
//        return imageFileService.transformImageFile("fixBackTransparency", inputFile, outputFile, errorFile, "java");
//    }
    
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
        return imageFileService.transformImageFile("deskewImageFile", inputFile, outputFile, errorFile, "python");
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
                throw new RuntimeException("Bad number of parametres for dewarpImageFile command");
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
        return imageFileService.transformImageFile("dewarpImageFile", inputFile, outputFile, errorFile, "python");
    }

    public void publishInfo(String message, String process){
        publisherService.publishInfo(message, process);
    }
    
    public void publishStatus(String status, String message, String process){
        publisherService.publishStatus(status, message, process);
    }
    
    public void publishProgress(String name, String process, int fet, int all){
        publisherService.publishProgress(name, process, fet, all);
    }
    
    public void publishProgress(String pre, String name, String process, int fet, int all){
        publisherService.publishProgress(pre, name, process, fet, all);
    }
    
    public void publishErrorProgress(String name, String process, int fet, int all, int errorState){
        publisherService.publishErrorProgress(name, process, fet, all, errorState);
    }
    
    public void publishErrorProgress(String pre, String name, String process, int fet, int all, int errorState){
        publisherService.publishErrorProgress(pre, name, process, fet, all, errorState);
    }
    
    public void publishErrorInfo(String message, String process, int errorState){
        publisherService.publishErrorInfo(message, process, errorState);
    }   
}
