package org.elsquatrecaps.portada.jportadamicroservice.client;

import javax.swing.SwingUtilities;
import org.elsquatrecaps.portada.jportadamicroservice.client.gui.ImageFilesSelector;
import org.elsquatrecaps.utilities.console.Print;

/**
 *
 * @author josepcanellas
 */
public class JPortadaMicroservice {
    private final PortadaApi papìInstance = new PortadaApi((PortadaApi.ProgressInfo t) -> {
        print(t);
        return null;
    });
    
    public static void main(String[] args) {
        if(args.length>0){
            JPortadaMicroservice prg = new JPortadaMicroservice();
            prg.run(args);
        }else{
            SwingUtilities.invokeLater(()->{
                ImageFilesSelector f = new ImageFilesSelector();
                f.setSize(675, 350);
                f.setVisible(true);
            });
        }
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
            case "ocrAll":
                papìInstance.allImagesToText(config);
                break;
            case "reorderAll":
                papìInstance.allImagesToFixOrder(config);
                break;
            default:
                throw new RuntimeException("Unkown command named: ".concat(config.getCommand()));
        }
    }
    
    private void print(PortadaApi.ProgressInfo pinfo){
        Print.printPercentage(String.format("%s (%s)", pinfo.getName(), pinfo.getProcess()), pinfo.getProgress(), pinfo.getMaxProgress());
    }
    
}
