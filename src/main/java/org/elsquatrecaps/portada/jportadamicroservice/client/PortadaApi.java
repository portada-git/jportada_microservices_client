package org.elsquatrecaps.portada.jportadamicroservice.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.elsquatrecaps.portada.jportadamicroservice.client.exceptions.IOPapiCliException;
import org.json.JSONObject;

/**
 *
 * @author josepcanellas
 */
public class PortadaApi {
    private static final String SECURITY_PATH = "security";
    public static String[] msContext = {"java", "python", "r"};
    public static String[] imagesExtensions = {".jpg", ".jpeg", ".png", "gif", ".tif", "tiff"};
    private final Map<String, ConnectionMs> conDataList = new HashMap<>();
//    private Function<Integer, Void> progress;
    private Function<ProgressInfo, Void> publish;
    
    /**
     * @param key
     * @return the host
     */
    public String getHost(String key) {
        return conDataList.get(key).getHost();
    }

    /**
     * @param key
     * @return the protocol
     */
    public String getProtocol(String key) {
        return conDataList.get(key).getProtocol();
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

//    public PortadaApi(Function<ProgressInfo, Void> publish, Function<Integer, Void> progress) {
//        this.publish = publish;
//        this.progress = progress;
//    }

    public final void init(Configuration config) {
        for(String ctx: msContext){
            this.conDataList.put(ctx, new ConnectionMs(config.getProtocols(ctx), config.getPort(ctx), config.getHosts(ctx), config.getPrefs(ctx)));
        }
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
            JSONObject jsonresponse = new JSONObject(sendData("test", p, contex));
            ret = jsonresponse.getString("message");
            publishInfo(ret, process);
        }catch (Exception ex){
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), process, -1);            
        }
    }
        
    public void acceptKey(Configuration configuration){
        acceptKey(configuration.getTeam(), configuration.getPk(), configuration.getUser(), configuration.getPass());
    }
    
    public void acceptKey(String team, String pkname, String user, String pass){
        String ret;
        try {
            HashMap p = new HashMap();
            p.put("pkname", pkname);
            p.put("team", team);
            p.put("u", user);
            p.put("p", DigestUtils.md5Hex(pass).toUpperCase());
            JSONObject jsonresponse = new JSONObject(sendData("pr/acceptKey", p, "java"));
            if(jsonresponse.getInt("statusCode")==0){
                ret = jsonresponse.getString("message");
                publishInfo(ret, "accept key");
            }else{
                ret = jsonresponse.getString("message");
                publishErrorInfo(ret, "accept key", jsonresponse.getInt("statusCode"));
            }
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "accept key", -1);

        }
    }

    public void deleteKey(Configuration configuration){
        acceptKey(configuration.getTeam(), configuration.getPk(), configuration.getUser(), configuration.getPass());
    }
    
    public void deleteKey(String team, String pkname, String user, String pass){
        String ret;
        try {
            HashMap p = new HashMap();
            p.put("pkname", pkname);
            p.put("team", team);
            p.put("u", user);
            p.put("p", pass);
            JSONObject jsonresponse = new JSONObject(sendData("pr/deleteKey", p, "java"));
            if(jsonresponse.getInt("statusCode")==0){
                ret = jsonresponse.getString("message");
                publishInfo(ret, "delete key");
            }else{
                ret = jsonresponse.getString("message");
                publishErrorInfo(ret, "delete key", jsonresponse.getInt("statusCode"));
            }
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "delete key", -1);
        }
    }

    
    public void verifyCodeSentByMail(Configuration configuration){
        verifyCodeSentByMail(configuration.getTeam(), configuration.getEmail(), configuration.getVerificationCode());
    }
    
    public void verifyCodeSentByMail(String team, String email, String code){
        String ret;
        try {
            HashMap p = new HashMap();
            p.put("code", code);
            p.put("team", team);
            p.put("email", email);
            JSONObject jsonresponse = new JSONObject(sendData("verifyRequestedAcessPermission", p, "java"));
            if(jsonresponse.getInt("statusCode")==0){
                ret = jsonresponse.getString("message");
                publishInfo(ret, "verification process");
                //rename public key
                String publicKeyFilename = "public.pem";
                String newPublicKeyFilename = String.format("%s_%s_%s" , email.replaceAll("@", "__AT_SIGN__"), code, publicKeyFilename);
                File publicKeyFile = new File(new File(SECURITY_PATH, team), publicKeyFilename);
                publicKeyFile.renameTo(new File(new File(SECURITY_PATH, team), newPublicKeyFilename));
            }else{
                //error
                if(jsonresponse.getInt("statusCode")==3){
                    //eliminar claus
                    File keyFile = new File(new File(SECURITY_PATH, team), "public.pem");
                    keyFile.delete();
                    keyFile = new File(new File(SECURITY_PATH, team), "private.pem");
                    keyFile.delete();
                }
                ret = jsonresponse.getString("message");
                publishErrorInfo(ret, "verification process", jsonresponse.getInt("statusCode"));
            }
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "verification process", -1);

        }
    }

    public void requestAccesPermission(Configuration configuration){
        requestAccesPermission(configuration.getTeam(), configuration.getEmail(), configuration.getForceKeyGeneration());
    }
    
    public void requestAccesPermission(String team, String email, boolean forceGeneration){
        String ret;
        try {
            File oldKey = null;
            File privateKeyFile = new File(new File(SECURITY_PATH, team), "private.pem").getCanonicalFile().getAbsoluteFile();
            File publicKeyFile = new File(new File(SECURITY_PATH, team), "public.pem").getCanonicalFile().getAbsoluteFile();
            if(privateKeyFile.exists() && !forceGeneration){
                publishStatus("KEY_ALREADY_EXIST", "An access key already exists on this computer. A new key was not created.", "request for accessing");
                return;
            }
            if(forceGeneration){
                File oldKeyDir = publicKeyFile.getParentFile();
                if(oldKeyDir.exists() && oldKeyDir.isDirectory()){
                     for(File n : oldKeyDir.listFiles()){
                         if(n.getName().matches(".*__AT_SIGN__.*_public.pem")){
                            oldKey = n;
                         }
                     }
                }
            }
            KeyPair keyPair = generateKeyPair();
//            String randomName = RandomStringGenerator.builder().get().generate(30);
//            saveKey(keyPair.getPrivate().getEncoded(), new File(SECURITY_PATH, String.format("private%s.pem", randomName)).getCanonicalFile().getAbsoluteFile(), "PRIVATE");
            saveKey(keyPair.getPrivate().getEncoded(), privateKeyFile, "PRIVATE");
            saveKey(keyPair.getPublic().getEncoded(), publicKeyFile, "PUBLIC");
            HashMap p = new HashMap();
            p.put("team", team);
            p.put("email", email);
            if(forceGeneration && oldKey!=null){
                p.put("oldKeyName", oldKey.getName());
            }
            try{
                JSONObject jsonresponse = new JSONObject(sendPublicKeyFile("requestAccessPermission", publicKeyFile.getCanonicalFile().getAbsolutePath(), p, "java"));
                if(jsonresponse.getInt("statusCode")==0){
                    //ret = jsonresponse.getString("message").concat(". Before 10 minutes pass, in the console execute the command: verifyCode -tm [YOUR TEAM] -m [YOUR_E_MAIL] -c [THE CODE RECEIVED]");
                    ret = jsonresponse.getString("message");
                    publishStatus(PortadaApi.ProgressInfo.SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST_STATUS, ret, "request for accessing");
                    if(oldKey!=null){
                        oldKey.delete();
                    }
                }else{
                    //error
                    publicKeyFile.delete();
                    privateKeyFile.delete();
                    ret = jsonresponse.getString("message");
                    publishErrorInfo(ret, "request for accessing", jsonresponse.getInt("statusCode"));
                }
            }catch(IOException ex){
                publicKeyFile.delete();
                privateKeyFile.delete();
                Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
                publishErrorInfo(ex.getMessage(), "request for accessing", -1);
            }
        } catch (NoSuchAlgorithmException | IOException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "request for accessing", -1);
        }
    }
    
    private void saveKey(byte[] key, File fileName, String type) throws FileNotFoundException, IOException{
        if(!fileName.getParentFile().exists()){
            fileName.getParentFile().mkdirs();
        }
        String pemKey = String.format("-----BEGIN %s KEY-----\n%s\n-----END %s KEY-----", type,  Base64.getEncoder().encodeToString(key), type);
         try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(pemKey.getBytes());
        }
    }
    
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair pair = keyPairGen.generateKeyPair();
        return pair;
    }
    
    
    
    private void publishInfo(String message, String process){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.INFO_TYPE, message, "", process, 1,1,0));
        }
    }
    
    private void publishStatus(String status, String message, String process){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.STATUS_INFO_TYPE, status, message, "", process, 1,1,0));
        }
    }
    
    private void publishProgress(String name, String process, int fet, int all){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_INFO_TYPE, "", name, process, fet,all,0));
        }
    }
    
    private void publishProgress(String pre, String name, String process, int fet, int all){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_INFO_TYPE, pre, name, process, fet,all,0));
        }
    }
    
    private void publishErrorProgress(String name, String process, int fet, int all, int errorState){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_ERROR_TYPE, "", name, process, fet,all, errorState));
        }
    }
    
    private void publishErrorProgress(String pre, String name, String process, int fet, int all, int errorState){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_ERROR_TYPE, pre, name, process, fet,all, errorState));
        }
    }
    
    private void publishErrorInfo(String message, String process, int errorState){
        if(publish!=null){
            publish.apply(new ProgressInfo(PortadaApi.ProgressInfo.ERROR_INFO_TYPE, message, "", process, 1, 1, errorState));
        }
    }
    
//    private void publishError(String message, String process, int fet, int all, int errorState){
//        if(publish!=null){
//            publish.apply(new ProgressInfo(PortadaApi.ProgressInfo.ERROR, message, "", process, fet, all, errorState));
//        }
//    }
//    
//    private void publishProgress(String messagePrevious, String name, String process, int fet, int all, int errorState){
////        if(progress!=null){
////            progress.apply(fet);
////        }
//        if(publish!=null){
//            publish.apply(new ProgressInfo(messagePrevious, name, process, fet, all, errorState));
//        }
//    }
    
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
            publishInfo("Starting process", "Redraw in correct order");
            HashMap p = new HashMap();
            p.put("team", team);
            for(File inputImageFile: lf){
                File outputImageFile = new File(outputDirFile, inputImageFile.getName());
                String m = "image: ";
                String n = inputImageFile.getName();
                if(transformImageFile("pr/redrawOrderedImageFile", inputImageFile.getAbsolutePath(), 
                        outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath(), p, "python")){
                    publishProgress(m, n, "Redraw in correct order", ++fet, all);
                }else{
                    publishErrorProgress(m, n, "Redraw in correct order", ++fet, all, -1);
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
            publishErrorInfo(message, "Redraw in correct order",-1);
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
                throw new RuntimeException("Bad number of parametres for fixBackTransparencyImageFile command");             
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
                if(transformImageFile("pr/ocrJson", inputImageFile.getAbsolutePath(), 
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
            publishInfo("Starting process", "OCR");
            HashMap p = new HashMap();
            p.put("team", team);
            for(File inputImageFile: lf){
                File outputImageFile = new File(outputDirFile, inputImageFile.getName()
                        .substring(0, inputImageFile.getName().lastIndexOf(".")).concat(".txt"));
                String m = "image: ";
                String n = inputImageFile.getName();
                if(transformImageFile("pr/ocr", inputImageFile.getAbsolutePath(), 
                        outputImageFile.getAbsolutePath(), errorFile.getAbsolutePath(), p, "java")){
                    publishProgress(m, n, "OCR", ++fet, all);
                }else{
                    publishErrorProgress(m, n, "OCR", ++fet, all, -1);
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
        try{  
            boolean exit;
            HttpURLConnection con = flushMultipartRequest(command, "image", inputFile, paramData, context);
            do{
                exit = true;
                int responseCode = con.getResponseCode();
                if ((responseCode >= 200) && (responseCode < 400)) {  
                    try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
                        copyStreams(con.getInputStream(), outputStream);
                    }
                } else if(responseCode==401) {
                    SignedData signedData = signChallengeOfConnection(con, paramData.getOrDefault("team", null));
                    con.disconnect();
                    con = flushMultipartRequest(command, "image", inputFile, paramData, signedData, context);
                    exit = false;
                } else {
                    //error
                    try(FileOutputStream outputStream = new FileOutputStream(errorFile)){
                        copyStreams(con.getErrorStream(), outputStream);
                    }
                    ret = false;
                }
            }while(!exit);
            con.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        }
        return ret;
    }
    
    private static String signChallenge(String challenge, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(challenge.getBytes());

        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }
    
    private String sendData(String command, HashMap<String, String> paramData, String context) throws Exception{
        return sendData(command, paramData, null, context);
    }
    private String sendData(String command, HashMap<String, String> paramData, SignedData signatureData, String context) throws Exception{
        String ret;
        String strUrl = String.format("%s://%s:%s%s%s", getProtocol(context), getHost(context), getPort(context),getPref(context), command);
        HttpPost post = new HttpPost(strUrl);
        if(signatureData!=null){
            post.addHeader("X-Signature", signatureData.getSignedData());
            post.addHeader("Cookie", signatureData.getSessionCookie());
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for(String key: paramData.keySet()){
            params.add(new BasicNameValuePair(key, paramData.get(key)));
        }
        post.setEntity(new UrlEncodedFormEntity(params));
        try (CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = (CloseableHttpResponse) client.execute(post)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if ((responseCode >= 200) && (responseCode < 400)) { 
                ret = EntityUtils.toString(response.getEntity());
            }else if(responseCode == 401){
                SignedData signedData = signChallengeOfConnection(response, paramData.getOrDefault("team", null));
                if(signedData==null){
                    ret = "{\"error\":true, \"message\":\"You need generate a security key access\"}";
                }else{
                    ret = sendData(command, paramData,signedData , context);
                }
            }else{
                ret = "{\"error\":true, \"message\":\"Access to resource forbidden\", \"response\":".concat(EntityUtils.toString(response.getEntity())).concat("}");
            }
        }
        return ret;
    }
    
    private String sendPublicKeyFile(String command, String inputFile, HashMap<String, String> paramData, String context) throws IOException{
        String ret;
            HttpURLConnection con = flushMultipartRequest(command, "pk", inputFile, paramData, context);
            int responseCode = con.getResponseCode();
            if ((responseCode >= 200) && (responseCode < 400)) { 
                try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                    copyStreams(con.getInputStream(), outputStream);
                    ret = new String(outputStream.toByteArray());
                    Logger.getLogger(PortadaApi.class.getName()).log(Level.INFO, ret);
                }
            } else {
                //error
                try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                    copyStreams(con.getErrorStream(), outputStream);
                    ret = new String(outputStream.toByteArray());
                    Logger.getLogger(PortadaApi.class.getName()).log(Level.WARNING, ret);
                }
                throw new IOPapiCliException(String.format("ERROR: %d - %s (%s)", responseCode, con.getResponseMessage(), ret));
            }
            con.disconnect();
        return ret;        
    }
    
    private HttpURLConnection flushMultipartRequest(String command, String fileFieldName, String inputFile, HashMap<String, String> paramData, String context) throws MalformedURLException, IOException{
        return flushMultipartRequest(command, fileFieldName, inputFile, paramData, null, context);
    }
    
    private HttpURLConnection flushMultipartRequest(String command, String fileFieldName, String inputFile, HashMap<String, String> paramData, SignedData signatureData, String context) throws MalformedURLException, IOException{
        HttpURLConnection con;
        String strUrl = String.format("%s://%s:%s%s%s", getProtocol(context), getHost(context), getPort(context),getPref(context), command);
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
        hwriter.append("Content-Disposition: form-data; name=\"").append(fileFieldName).append("\"; filename=\"").append(inFile.getName()).append("\"\r\n");
        hwriter.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(inFile.getName())).append("\r\n");
        hwriter.append("Content-Length: ").append(String.valueOf(inFile.length())).append("\r\n");
        hwriter.append("\r\n"); 
        StringBuilder fwriter = new StringBuilder();
        fwriter.append("\r\n");
        fwriter.append("--*****--\r\n");

        URL url = new URL(strUrl);
        con = (HttpURLConnection)url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestMethod("POST");
        if(signatureData!=null){
            con.setRequestProperty("X-Signature", signatureData.getSignedData());
            con.setRequestProperty("Cookie", signatureData.getSessionCookie());
        }
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
        return con;
    }    
    
    private SignedData signChallengeOfConnection(CloseableHttpResponse response, String team) throws Exception{
        return signChallengeOfConnection(team, response.getEntity().getContent(), response.getFirstHeader("Set-Cookie").getValue());       
    }
    
    private SignedData signChallengeOfConnection(HttpURLConnection con, String team) throws Exception{
        return signChallengeOfConnection(team, con.getErrorStream(), con.getHeaderField("Set-Cookie"));
    }
    
    private SignedData signChallengeOfConnection(String team, InputStream stream, String sessionCookie) throws Exception{
        SignedData ret = null;
        File privateKeyFile = new File(new File(SECURITY_PATH, team), "private.pem").getCanonicalFile().getAbsoluteFile();
        if(privateKeyFile.exists()){
            PrivateKey privateKey = loadPrivateKey(privateKeyFile.getAbsolutePath());
            InputStreamReader reader = new InputStreamReader(stream);
            JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
            String signed = signChallenge(jsonResponse.get("challenge").getAsString(), privateKey);
            ret = new SignedData(signed, sessionCookie);
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
    
    private static PrivateKey loadPrivateKey(String filename) throws Exception {
        String key = new String(Files.readAllBytes(new File(filename).toPath()));
        
        // Eliminar les línies d'encapçalament i peu
        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "")
                 .replaceAll("\\s", ""); // Elimina espais i salts de línia

        byte[] keyBytes = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }
    
    private static final class SignedData{
        private String signedData;
        private String sessionCookie;

        public SignedData(String signedData, String sessionCookie) {
            this.signedData = signedData;
            this.sessionCookie = sessionCookie;
        }

        /**
         * @return the signedData
         */
        public String getSignedData() {
            return signedData;
        }

        /**
         * @return the sessionCookie
         */
        public String getSessionCookie() {
            return sessionCookie;
        }
    }  
    
    public static final class ProgressInfo{
        public static final int ERROR_INFO_TYPE=1;
        public static final int PROGRESS_ERROR_TYPE=201;
        public static final int PROGRESS_INFO_TYPE=200;
        public static final int INFO_TYPE=0;
        public static final int STATUS_INFO_TYPE=100;
        
//        public static final int ERROR=-1;
//        public static final int PROCESS=0;
//        public static final int MESSAGE=1;
//        public static final int INFO=2;
//        public static final int INFO_ERROR=3;
//        public static final int STATUS_INFO=4;
        public static final String KEY_ALREADY_EXIST_STATUS="KEY_ALREADY_EXIST";
        public static final String SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST_STATUS="SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST";
        public static final String OK_STATUS="";
        private int type=0;  
        private String pre;
        private String name;
        private String process;
        private int progress;
        private int maxProgress;
        private String status;
        int errorState=0;

//        public ProgressInfo(String message, String process, String status) {
//            this(STATUS_INFO_TYPE, message, "", process, 1, 1, 0);
//            this.status = status;
//        }
//        
////        public ProgressInfo(String message, String process, boolean infoOnly) {
////            this(infoOnly?INFO:MESSAGE, message, "", process, 0, 100, 0);
////        }
//        
////        public ProgressInfo(String message, String process, int percent) {
////            this(MESSAGE, message, "", process, percent, 100, 0);
////        }
//
//        public ProgressInfo(String message, String process) {
//            this(INFO_TYPE, message, "", process, 1, 1, 0);
//        }
//        
//        public ProgressInfo(String name, String process, int progress, int maxProgress) {
//            this(PROGRESS_INFO_TYPE, "", name, process, progress, maxProgress, 0);
//        }
//        
//        public ProgressInfo(String preNameOrMessage, String name, String process, int progress, int maxProgress) {
//            this(PROGRESS_INFO_TYPE, preNameOrMessage, name, process, progress, maxProgress, 0);
//        }
//        
//        public ProgressInfo(String name, String process, int progress, int maxProgress, int errorState) {
//            this(PROGRESS_ERROR_TYPE, "", name, process, progress, maxProgress, errorState);
//        }
//        
//        public ProgressInfo(String preNameOrMessage, String name, String process, int progress, int maxProgress, int errorState) {
//            this(PROGRESS_ERROR_TYPE, preNameOrMessage, name, process, progress, maxProgress, errorState);
//        }
        
        public ProgressInfo(int type, String status, String preNameOrMessage, String name, String process, int progress, int maxProgress, int errorState) {
            this(type, preNameOrMessage, name, process, progress, maxProgress, errorState);
            this.status = status;
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

        public String getStatus(){
            String ret = "";
            if(getType() == STATUS_INFO_TYPE){
                ret = status;
            }
            return ret;
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
        
        public void setName(String str){
            this.name = str;
        }
        
    }
}
