package org.elsquatrecaps.portada.jportadamicroservice.client;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

/**
 *
 * @author josep
 */
public class Configuration{

    /**
     * @return the extractConfigMode
     */
    public String getExtractConfigMode() {
        return extractConfigMode;
    }

    /**
     * @param extractConfigMode the extractConfigMode to set
     */
    protected void setExtractConfigMode(String extractConfigMode) {
        this.extractConfigMode = extractConfigMode;
        if(extractConfigMode!=null && !extractConfigMode.isEmpty()){
            this.attrs.add("extractConfigMode");        
        }
    }

    /**
     * @return the extractConfigProtertiesFile
     */
    public String getExtractConfigProtertiesFile() {
        return extractConfigProtertiesFile;
    }

    /**
     * @param extractConfigProtertiesFile the extractConfigProtertiesFile to set
     */
    protected void setExtractConfigProtertiesFile(String extractConfigProtertiesFile) {
        this.extractConfigProtertiesFile = extractConfigProtertiesFile;
        if(extractConfigProtertiesFile!=null && !extractConfigProtertiesFile.isEmpty()){
            this.attrs.add("extractConfigProtertiesFile");        
        }
    }

    /**
     * @return the extractJsonConfigParsersFile
     */
    public String getExtractJsonConfigParsersFile() {
        return extractJsonConfigParsersFile;
    }

    /**
     * @param extractJsonConfigParsersFile the extractJsonConfigParsersFile to set
     */
    protected void setExtractJsonConfigParsersFile(String extractJsonConfigParsersFile) {
        this.extractJsonConfigParsersFile = extractJsonConfigParsersFile;
        if(extractJsonConfigParsersFile!=null && !extractJsonConfigParsersFile.isEmpty()){
            this.attrs.add("extractJsonConfigParsersFile");        
        }
    }
    
    /**
     * @return the extractJsonConfigParsersFile
     */
    public String getExtractExtensionFile() {
        return extractExtensionFile;
    }

    /**
     * @param extractExtensionFile the extractJsonConfigParsersFile to set
     */
    protected void setExtractExtensionFile(String extractExtensionFile) {
        this.extractExtensionFile = extractExtensionFile;
        if(extractExtensionFile!=null && !extractExtensionFile.isEmpty()){
            this.attrs.add("extractExtensionFile");        
        }
    }
    
    private final static String DEFAULT = "default";

    /**
     * @return the commandArgumentsSize
     */
    public int getCommandArgumentsSize() {
        return commandArgumentsSize;
    }
    @Arg(dest="input_file")  //-i
    private String inputFile;
    @Arg(dest="output_file")  //-o
    private String outputFile;
    @Arg(dest="error_file")  //-e
    private String errorFile;
    @Arg(dest="command")
    private ArrayList<String> positionalArgs;
    private String command;
    private Map<String, Integer> ports = new HashMap<>();
    private Map<String, String> hosts = new HashMap<>();
    private Map<String, String> prefs = new HashMap<>();
    private Map<String, String> protocols = new HashMap<>();
    @Arg(dest="discard_folder")
    private String discardFolder; //-df
    @Arg(dest="autoDiscard")
    private Boolean autoDiscard;  //-ad
    @Arg(dest="fixTrans")
    private Boolean fixTransparency;
    @Arg(dest="fixSkew")
    private Boolean fixSkew;
    @Arg(dest="fixWarp")
    private Boolean fixWarp;
    @Arg(dest="team")  //-tm
    private String team;
    @Arg(dest="verificationCode")  //-c
    private String verificationCode;
    @Arg(dest="email")  //-m
    private String email;
    @Arg(dest="forceKeyGeneration")  //-f
    private Boolean forceKeyGeneration;
    private String[] teamsForSelecting;
    @Arg(dest="k")  //-k
    private String pk;
    @Arg(dest="u")  //-u
    private String adminUser;
    @Arg(dest="p")  //-p
    private String adminPass;
    @Arg(dest="extractConfigMode")
    private String extractConfigMode;
    @Arg(dest="extractConfigProtertiesFile")
    private String extractConfigProtertiesFile;
    @Arg(dest="extractJsonConfigParsersFile")
    private String extractJsonConfigParsersFile;
    @Arg(dest="extractExtensionFile")
    private String extractExtensionFile;
    
    private int  commandArgumentsSize=0;    
    private final Set<String> attrs = new HashSet<>();

    private void setDefaultArg(String dest, Object val){
        if(!this.getAttrs().contains(dest)){
            String preDest = DEFAULT;
            if(dest.endsWith(".port") 
                    || dest.endsWith(".host")
                    || dest.endsWith(".protocol")
                    || dest.endsWith(".pref")){
                preDest = dest.substring(0, dest.lastIndexOf("."));
                dest = dest.substring(dest.lastIndexOf(".")+1);
            }
            switch(dest){
                case "command":
                    this.setCommand((String) val);
                    break;
                case "input_file":
                    setInputFile((String) val);
                    break;
                case "output_file":
                    setOutputFile((String) val);
                    break;                    
                case "error_file":
                    setErrorFile((String) val);
                    break;                    
                case "protocol":
                    setProtocols(preDest, (String) val);
                    break;
                case "port":
                    setPorts(preDest, (String) val);
                    break;
                case "host":
                    setHosts(preDest, (String) val);
                    break;
                case "pref":
                    setPrefs(preDest, (String) val);
                    break;
                case "fixTrans":
                    this.setFixTrans((String) val);
                    break;
                case "fixSkew":
                    this.setFixSkew((String) val);
                    break;
                case "fixWarp":
                    this.setFixWarp((String) val);
                    break;
                case "team":
                    this.setTeam((String) val);
                    break;
                case "email":
                    this.setEmail((String) val);
                    break;
                case "verificationCode":
                    this.setVerificationCode((String) val);
                    break;
                case "forceKeyGeneration":
                    this.setForceKeyGeneration((String) val);
                    break;
                case "teamsForSelecting":
                    this.setTeamsForSelecting((String) val);
                    break;
                case "pk":
                    this.setPk((String) val);
                    break;
                case "user":
                    this.setUser((String) val);
                    break;
                case "pass":
                    this.setPass((String) val);
                    break;
                case "extractConfigMode":
                    this.setExtractConfigMode((String) val);
                    break;
                case "extractConfigProtertiesFile":
                    this.setExtractConfigProtertiesFile((String) val);
                    break;
                case "extractJsonConfigParsersFile":
                    this.setExtractJsonConfigParsersFile((String) val);
                    break;
                case "extractExtensionFile":
                    this.setExtractExtensionFile((String) val);
                    break;
                case "discardFolder":
                    this.setDiscardFolder((String) val);
                    break;
                case "autoDiscard":
                    this.setAutoDiscard((String) val);
                    break;
            }
        }
    }
    
    public void parseArgumentsAndConfigure(String[] args){
        parseArguments(args);
        configure();        
    }
    
    public static Properties loadAndGetConfigProperties(){
        Properties properties = new Properties();
        try {
            if(Files.exists(Paths.get("init.properties"))){
                properties.load(new FileReader("init.properties"));
            }else if(Files.exists(Paths.get("config/init.properties"))){
                properties.load(new FileReader("config/init.properties"));
            }  
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return properties;
    }
    
    public void configure(){
        Configuration args = this;
        Properties properties = loadAndGetConfigProperties();
        properties.forEach((Object k, Object v) -> {
            args.setDefaultArg(String.valueOf(k), v);
        });
        if(this.outputFile==null){
            this.outputFile=inputFile;
        }
        if(fixSkew==null){
            fixSkew = false;
        }
        if(fixTransparency==null){
            fixTransparency = false;
        }
        if(fixWarp==null){
            fixWarp = false;
        }
        if(!(fixSkew || fixTransparency || fixWarp)){
            fixSkew = fixWarp = fixTransparency = Boolean.TRUE;
        }
    }
    
    public void parseArguments(String[] args){
        ArgumentParser parser = ArgumentParsers.newFor("AppReader").build()
                .defaultHelp(true)
                .description("Client of portADa microservice for processing images");
        parser.addArgument("command").metavar("<COMMAND>").nargs(1);
        parser.addArgument("-i", "--input_file").nargs("?").help("Image file to fix skewed problem");
        parser.addArgument("-o", "--output_file").nargs("?").help("Path where fixed image will be saved");
        parser.addArgument("-e", "--error_file").nargs("?").help("Path where error will be saved if an error is happened");
        parser.addArgument("-tm", "--team").nargs("?").help("Team from user processes the command");
        parser.addArgument("-m", "--email").nargs("?").help("email to send the veridication code");
        parser.addArgument("-c", "--verificationCode").nargs("?").help("email to send the veridication code");
        parser.addArgument("-f", "--forceKeyGeneration").action(Arguments.storeTrue()).help("Force Key pair generation for requestAccess command.");
        parser.addArgument("-k", "--pk").action(Arguments.storeTrue()).help("Key filename.");
        parser.addArgument("-u", "--user").action(Arguments.storeTrue()).help("User name.");
        parser.addArgument("-p", "--pass").action(Arguments.storeTrue()).help("User password.");
        parser.addArgument("--extractConfigMode").nargs("?").help("The mode to read the configuration data for extractor. Can be: 'R' for remote, or 'L' for local");
        parser.addArgument("--extractConfigProtertiesFile").nargs("?").help("The remote path file if 'extractConfigMode' is remote or the local path file if 'extractConfigMode' is local for the extractor config file");
        parser.addArgument("--extractJsonConfigParsersFile").nargs("?").help("The local path for the parsers JSON configuration file regardless of whether 'extractConfigMode' is local or remote");
        parser.addArgument("-ext","--extractExtensionFile").nargs("?").setDefault("txt").help("Extension to filter files to extract data");
        //parser.addArgument("-p", "--port").nargs("?").help("microservice port");
        //parser.addArgument("-ht", "--host").nargs("?").help("microservice host");
        //parser.addArgument("-pf", "--pref").nargs("?").help("microservice prefix path");
        parser.addArgument("-df", "--discard_folder").nargs("?").help("Folder where discard wrong images");
        parser.addArgument("-ad", "--autoDiscard").action(Arguments.storeTrue()).help("Discard images with wrong quality before of ocr process");
        parser.addArgument("-t", "--fixTrans").action(Arguments.storeTrue()).help("Images to process not need to fix back transparency");
        parser.addArgument("-s", "--fixSkew").action(Arguments.storeTrue()).help("Images to process not need to fix skew lines");
        parser.addArgument("-w", "--fixWarp").action(Arguments.storeTrue()).help("Images to process not need to fix warp lines");
        try {
            parser.parseArgs(args, this);
            this.updateAttrs();
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }
    
    public Boolean getBoolean(String val){
        return val!=null && (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("t") || 
            val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("y") || 
            val.equalsIgnoreCase("si") || val.equalsIgnoreCase("s") || 
            val.equalsIgnoreCase("vertader") || val.equalsIgnoreCase("vertadera") || val.equalsIgnoreCase("v") || 
            val.equalsIgnoreCase("cert") || val.equalsIgnoreCase("certa") ||val.equalsIgnoreCase("c"));
    }
    
    
    private void updateAttrs(){
        if(getExtractConfigMode()!=null){
            this.attrs.add("extractConfigMode");
        }else{
            setExtractConfigMode("L");
        }
        if(getExtractConfigProtertiesFile()!=null){
            this.attrs.add("extractConfigProtertiesFile");
        }
        if(getExtractJsonConfigParsersFile()!=null){
            this.attrs.add("extractJsonConfigParsersFile");
        }
        if(getExtractExtensionFile()!=null){
            this.attrs.add("extractExtensionFile");
        }
        if(this.forceKeyGeneration!=null){
            this.attrs.add("forceKeyGeneration");
        }else{
            this.forceKeyGeneration = false;
        }
        if(this.pk!=null){
            this.attrs.add("pk");
        }
        if(this.adminUser!=null){
            this.attrs.add("user");
        }
        if(this.adminPass!=null){
            this.attrs.add("pass");
        }
        if(this.verificationCode!=null){
            this.attrs.add("verificationCode");
        }
        if(this.email!=null){
            this.attrs.add("email");
        }
        if(this.team!=null){
            this.attrs.add("team");
        }
        if(this.discardFolder!=null){
            this.attrs.add("discard_folder");
        }
        if(this.inputFile!=null){
            this.attrs.add("input_file");
            this.commandArgumentsSize++;
        }
        if(this.outputFile!=null){
            this.attrs.add("output_file");
            this.commandArgumentsSize++;
        }
        if(this.errorFile!=null){
            this.attrs.add("error_file");
            this.commandArgumentsSize++;
        }
        if(this.positionalArgs!=null){
            this.command = positionalArgs.get(0);
            this.attrs.add("command");
        }
        if(autoDiscard!=null){
            this.attrs.add("autoDiscard");            
        }else{
            this.autoDiscard=false;
        }
        if(getFixTransparency()!=null){
            this.attrs.add("fixTrans");            
        }else{
            this.fixTransparency=false;
        }
        if(getFixSkew()!=null){
            this.attrs.add("fixSkew");            
        }else{
            this.fixSkew=false;
        }
        if(getFixWarp()!=null){
            this.attrs.add("fixWarp");            
        }else{
            this.fixWarp=false;
        }
    }
    
    public Set<String> getAttrs() {
        return attrs;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getInputDir() {
        return getInputFile();
    }

    protected void setInputFile(String originDir) {
        this.inputFile = originDir;
        if(inputFile!=null && !inputFile.isEmpty()){
            this.attrs.add("input_file");        
            this.commandArgumentsSize++;
        }
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getOutputDir() {
        return getOutputFile();
    }

    protected void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
        if(outputFile!=null && !outputFile.isEmpty()){
            this.attrs.add("output_file");        
            this.commandArgumentsSize++;
        }
    }

    public String getErrorFile() {
        return errorFile;
    }

    protected void setErrorFile(String errorFile) {
        this.errorFile = errorFile;
        if(errorFile!=null && !errorFile.isEmpty()){
            this.attrs.add("error_file");        
            this.commandArgumentsSize++;
        }
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    protected void setCommand(String val) {
        this.command = val;
        if(command!=null && !command.isEmpty()){
            this.attrs.add("command");        
        }
    }

    public String getPort(String key) {
        String ret;
        if(this.ports.containsKey(key)){
            ret = String.valueOf(this.ports.get(key));
        }else if(ports.containsKey(DEFAULT)){
            ret = String.valueOf(this.ports.get(DEFAULT));
        }else{
            ret = "80";
        }
        return ret;
    }
    
    protected void setPorts(String key, Integer val) {
        this.ports.put(key, val);
    }
    
    protected void setPorts(String key, String val) {
        if(val!=null && !val.isEmpty()){
            this.setPorts(key, Integer.valueOf(val));
        }
    }
    
    /**
     * @return the host
     */
    public String getHosts(String key) {
        String ret;
        if(this.hosts.containsKey(key)){
            ret = hosts.get(key);
        }else{
            ret = hosts.get(DEFAULT);
        }
        return ret;
    }

    protected void setHosts(String key, String val) {
        if(val!=null && !val.isEmpty()){
            this.hosts.put(key, val);
        }
    }

    /**
    /**
     * @return the protocol
     */
    public String getProtocols(String key) {
        String ret;
        if(this.protocols.containsKey(key)){
            ret = protocols.get(key);
        }else if(protocols.containsKey(DEFAULT)){
            ret = protocols.get(DEFAULT);
        }else{
            ret = "http";
        }
        return ret;
    }

    protected void setProtocols(String key, String val) {
        if(val!=null && !val.isEmpty()){
            this.protocols.put(key, val);
        }
    }

    /**
     * @return the pref
     */
    public String getPrefs(String key) {
        String ret;
        if(this.prefs.containsKey(key)){
            ret = prefs.get(key);
        }else{
            ret = prefs.get(DEFAULT);
        }
        return ret;
    }

    protected void setPrefs(String key, String val) {
        if(val!=null && !val.isEmpty()){
            this.prefs.put(key, val.equals("_NONE_")?"":val);
        }
    }

    private void setFixTrans(Boolean val) {
        this.fixTransparency = val;
        this.attrs.add("fixTrans");
    }
    
    private void setFixTrans(String string) {
        setFixTrans(getBoolean(string));
    }

    /**
     * @return the fixTransparency
     */
    public Boolean getFixTransparency() {
        return fixTransparency;
    }

    private void setFixSkew(Boolean v) {
        this.fixSkew = v;
        this.attrs.add("fixSkew");

    }
    
    private void setFixSkew(String string) {
        setFixSkew(getBoolean(string));        
    }

    private void setFixWarp(Boolean v) {
        this.fixWarp = v;
        this.attrs.add("fixWarp");
        
    }
    
    private void setFixWarp(String string) {
        setFixWarp(getBoolean(string));        
    }

    public Boolean getFixSkew() {
        return this.fixSkew;
    }

    public Boolean getFixWarp() {
        return this.fixWarp;
    }

    /**
     * @return the team
     */
    public String getTeam() {
        return team;
    }

    private void setTeam(String string) {
        team=string;        
        this.attrs.add("team");
    }

    /**
     * @return the verificationCode
     */
    public String getVerificationCode() {
        return verificationCode;
    }

    private void setVerificationCode(String string) {
        verificationCode=string;        
        this.attrs.add("verificationCode");
    }

    private void setForceKeyGeneration(String string) {
        setForceKeyGeneration(getBoolean(string));        
    }

    private void setForceKeyGeneration(Boolean val) {
        forceKeyGeneration = val;
    }

    public Boolean getForceKeyGeneration() {
        return forceKeyGeneration;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    private void setEmail(String string) {
        email=string;        
        this.attrs.add("email");
    }

    /**
     * @return the teamsForSelecting
     */
    public String[] getTeamsForSelecting() {
        return teamsForSelecting;
    }

    /**
     * @param teamsForSelecting the teamsForSelecting to set
     */
    public void setTeamsForSelecting(String[] teamsForSelecting) {
        this.teamsForSelecting = teamsForSelecting;
    }

    public void setTeamsForSelecting(String teamsForSelecting) {
        this.teamsForSelecting = teamsForSelecting.split(",");
    }

    private void setPk(String string) {
        this.pk = string;
        this.attrs.add("pk");
    }

    private void setUser(String string) {
        this.adminUser = string;
        this.attrs.add("user");
    }

    private void setPass(String string) {
        this.adminPass = string;
        this.attrs.add("pass");
    }

    public String getPk() {
        return this.pk;
    }

    public String getUser() {
        return this.adminUser;
    }

    public String getPass() {
        return this.adminPass;
    }

    private void setDiscardFolder(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void setAutoDiscard(Boolean par) {
        this.autoDiscard = par;
    }
    
    private void setAutoDiscard(String string) {
        setAutoDiscard(getBoolean(string));   
    }

    public Boolean getAutoDiscard() {
        return autoDiscard==null?false:autoDiscard;
    }

    public String getDiscardFolder() {
        return discardFolder;
    }

}
