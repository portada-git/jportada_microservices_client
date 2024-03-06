package org.elsquatrecaps.portada.jportadamicroservice.client;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    @Arg(dest="input_file")  //-i
    private String inputFile;
    @Arg(dest="output_file")  //-o
    private String outputFile;
    
    private final Set<String> attrs = new HashSet<>();

    private void setDefaultArg(String dest, Object val){
        if(!this.getAttrs().contains(dest)){
            switch(dest){
                case "input_file":
                    inputFile = (String) val;
                    this.getAttrs().add(dest);
                    break;
                case "output_file":
                    outputFile = (String) val;
                    this.getAttrs().add(dest);
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
            if(Files.exists(Paths.get("app.properties"))){
                properties.load(new FileReader("app.properties"));
                properties.forEach((Object k, Object v) -> {
                    args.setDefaultArg(String.valueOf(k), v);
                });
            }else{
                if(this.outputFile==null){
                    this.outputFile=inputFile;
                }
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
        parser.addArgument("-i", "--input_file").nargs("?").help("Image file to fix skewed problem");
        parser.addArgument("-o", "--output_file").nargs("?").help("Path where fixed image will be saved");
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
        }
        if(this.outputFile!=null){
            this.attrs.add("output_file");
        }
    }
    
    public Set<String> getAttrs() {
        return attrs;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String originDir) {
        this.inputFile = originDir;
        this.attrs.add("input_file");
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
        this.attrs.add("output_file");
    }
}
