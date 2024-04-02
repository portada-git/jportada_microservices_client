package org.elsquatrecaps.portada.jportadamicroservice.client;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

/**
 *
 * @author josep
 */
public class Configuration{

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
    @Arg(dest="port")
    private Integer port;
    @Arg(dest="host")
    private String host;
    @Arg(dest="pref")
    private String pref;
    
    private int  commandArgumentsSize=0;    
    private final Set<String> attrs = new HashSet<>();

    private void setDefaultArg(String dest, Object val){
        if(!this.getAttrs().contains(dest)){
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
                    setPort((String) val);
                    break;
                case "host":
                    setHost((String) val);
                    break;
                case "pref":
                    setPref((String) val);
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
        parser.addArgument("-p", "--port").nargs("?").help("microservice port");
        parser.addArgument("-ht", "--host").nargs("?").help("microservice host");
        parser.addArgument("-pf", "--pref").nargs("?").help("microservice prefix path");
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
        if(this.getHost()!=null){
            this.attrs.add("host");
        }
        if(this.getPort()!=null){
            this.attrs.add("port");
        }
        if(this.getPref()!=null){
            this.attrs.add("pref");
        }
    }
    
    public Set<String> getAttrs() {
        return attrs;
    }

    public String getInputFile() {
        return inputFile;
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

    /**
     * @return the port
     */
    public String getPort() {
        String ret;
        if (port==null){
            ret = null;
        }else{
            ret = String.valueOf(port);
        }
        return ret;
    }

    protected void setPort(Integer val) {
        this.port = val;
        this.attrs.add("port");      
    }
    
    protected void setPort(String val) {
        if(val!=null && !val.isEmpty()){
            this.setPort(Integer.valueOf(val));
        }else{
            this.port = null;
        }
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    protected void setHost(String val) {
        this.host = val;
        if(host!=null && !host.isEmpty()){
            this.attrs.add("host");        
        }
    }

    /**
     * @return the pref
     */
    public String getPref() {
        return pref;
    }

    protected void setPref(String val) {
        this.pref = val;
        if(pref!=null && !pref.isEmpty()){
            this.attrs.add("pref");        
        }
    }
}
