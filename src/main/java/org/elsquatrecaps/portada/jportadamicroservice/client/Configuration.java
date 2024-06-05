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
    @Arg(dest="fixTrans")
    private Boolean fixTransparency;
    @Arg(dest="fixSkew")
    private Boolean fixSkew;
    @Arg(dest="fixWarp")
    private Boolean fixWarp;
    @Arg(dest="team")  //-e
    private String team;
    
    private int  commandArgumentsSize=0;    
    private final Set<String> attrs = new HashSet<>();

    private void setDefaultArg(String dest, Object val){
        if(!this.getAttrs().contains(dest)){
            String preDest = DEFAULT;
            if(dest.endsWith(".port") 
                    || dest.endsWith(".host")
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
            }
        }
    }
    
    public void parseArgumentsAndConfigure(String[] args){
        parseArguments(args);
        configure();        
    }
    
    public void configure(){
        Configuration args = this;
        Properties properties = new Properties();
        try {
            if(Files.exists(Paths.get("init.properties"))){
                properties.load(new FileReader("init.properties"));
                properties.forEach((Object k, Object v) -> {
                    args.setDefaultArg(String.valueOf(k), v);
                });
            }else if(Files.exists(Paths.get("config/init.properties"))){
                properties.load(new FileReader("config/init.properties"));
                properties.forEach((Object k, Object v) -> {
                    args.setDefaultArg(String.valueOf(k), v);
                });
            }
            if(this.outputFile==null){
                this.outputFile=inputFile;
            }
            if(!(fixSkew || fixTransparency || fixWarp)){
                fixSkew = fixWarp = fixTransparency = Boolean.TRUE;
            }
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
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
        //parser.addArgument("-p", "--port").nargs("?").help("microservice port");
        //parser.addArgument("-ht", "--host").nargs("?").help("microservice host");
        //parser.addArgument("-pf", "--pref").nargs("?").help("microservice prefix path");
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
        if(this.team!=null){
            this.attrs.add("team");
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
//        if(this.getHost()!=null){
//            this.attrs.add("host");
//        }
//        if(this.getPort()!=null){
//            this.attrs.add("port");
//        }
//        if(this.getPref()!=null){
//            this.attrs.add("pref");
//        }
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
            this.prefs.put(key, val);
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

}
