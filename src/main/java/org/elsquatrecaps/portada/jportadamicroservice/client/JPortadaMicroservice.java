package org.elsquatrecaps.portada.jportadamicroservice.client;

/**
 *
 * @author josepcanellas
 */
public class JPortadaMicroservice {
    private PortadaApi papìInstance = new PortadaApi();
    
    public static void main(String[] args) {
        JPortadaMicroservice prg = new JPortadaMicroservice();
        prg.run(args);
    }
    
    public void run(String[] args) {
        Configuration config = new Configuration();
        config.parseArgumentsAndConfigure(args);
        execute(config);
    }        
    
    public void execute(Configuration config){
        papìInstance.init(config);     
        switch (config.getCommand()) {
            case "deskew":
            case "deskewImageFile":
                papìInstance.deskewImageFile(config);
                break;
            case "dewarp":
            case "dewarpImageFile":
                papìInstance.dewarpImageFile(config);
                break;
            case "fixBackTransparency":
            case "fixBackTransparencyImageFile":
            case "fixTransparency":
            case "fixTransparencyImageFile":
                papìInstance.fixTransparencyImageFile(config);
                break;
            case "fixAll":
            case "fixall":
            case "fix":
                papìInstance.fixAllImages(config);
                break;
            default:
                throw new RuntimeException("Unkown command named: ".concat(config.getCommand()));
        }
    }
}
