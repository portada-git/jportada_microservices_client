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
        if(t.getStatus().equals(PortadaApi.ProgressInfo.KEY_ALREADY_EXIST_STATUS)){
            t.setName("If you want to replace the existing key, repeat the command adding the forceKeyGeneration (-f) attribute: requestForAccessPermission -tm [TEAM] -m [E-MAIL] -f");
        }else if(t.getStatus().equals(PortadaApi.ProgressInfo.SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST_STATUS)){
            t.setName(". Before 10 minutes pass, in the console execute the command: verifyCode -tm [YOUR TEAM] -m [YOUR_E_MAIL] -c [THE CODE RECEIVED]");
        }
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
        execute(config, papìInstance);
    }
    
    public static void execute(Configuration config, PortadaApi papìInstance){
        papìInstance.init(config);     
        switch (config.getCommand()) {
            case "jtest":
                papìInstance.jtest(config);
                break;
            case "pytest":
                papìInstance.pytest(config);
                break;
            case "rtest":
                papìInstance.rtest(config);
                break;
            case "verifyCode":
                papìInstance.verifyCodeSentByMail(config);
                break;
            case "requestAccess":
                papìInstance.requestAccesPermission(config);
                break;
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
            case "ocrDocumentAll":
                papìInstance.allImagesToDocument(config);
                break;
            case "ocrJsonAll":
                papìInstance.allImagesToJson(config);
                break;
            case "reorderAll":
                papìInstance.allImagesToFixOrder(config);
                break;
            default:
                throw new RuntimeException("Unkown command named: ".concat(config.getCommand()));
        }
    }
    
    private void print(PortadaApi.ProgressInfo pinfo){
        switch (pinfo.getType()) {
            case PortadaApi.ProgressInfo.ERROR_INFO_TYPE:
            case PortadaApi.ProgressInfo.INFO_TYPE:
            case PortadaApi.ProgressInfo.STATUS_INFO_TYPE:
                Print.printPercentage(pinfo.getMessage(), 1, 1, Print.ToPrintTypes.MESSAGE);
                break;
            case PortadaApi.ProgressInfo.PROGRESS_ERROR_TYPE:
            case PortadaApi.ProgressInfo.PROGRESS_INFO_TYPE:
                String message = String.format("%s (%s) - %s", pinfo.getName(), pinfo.getProcess(), pinfo.errorState==0?"OK":"ERROR");
                Print.printPercentage(message , pinfo.getProgress(), pinfo.getMaxProgress());
                break;
        }
    }
    
}
