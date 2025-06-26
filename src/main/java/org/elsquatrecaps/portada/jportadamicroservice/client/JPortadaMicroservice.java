package org.elsquatrecaps.portada.jportadamicroservice.client;

import org.elsquatrecaps.portada.jportadamicroservice.client.services.ProgressInfo;
import javax.swing.SwingUtilities;
import org.elsquatrecaps.portada.jportadamicroservice.client.gui.ImageFilesSelector;
import org.elsquatrecaps.utilities.console.Print;

/**
 *
 * @author josepcanellas
 */
public class JPortadaMicroservice {
    private static final String VERSION = "1.0.11";
    private final PortadaApi papìInstance = new PortadaApi((ProgressInfo t) -> {
        if(t.getStatus().equals(ProgressInfo.KEY_ALREADY_EXIST_STATUS)){
            t.setName("If you want to replace the existing key, repeat the command adding the forceKeyGeneration (-f) attribute: requestForAccessPermission -tm [TEAM] -m [E-MAIL] -f");
        }else if(t.getStatus().equals(ProgressInfo.SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST_STATUS)){
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
                f.setTitle(String.format("PAPI - CLI (%s)", VERSION));
                f.setSize(740, 385);
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
    
    public static void execute(Configuration config, PortadaApi papiInstance){
        papiInstance.init(config);     
        switch (config.getCommand()) {
            case "jtest":
                papiInstance.jtest(config);
                break;
            case "pytest":
                papiInstance.pytest(config);
                break;
            case "rtest":
                papiInstance.rtest(config);
                break;
            case "accept_key":
                papiInstance.acceptKey(config);
                break;
            case "delete_key":
                papiInstance.deleteKey(config);
                break;
            case "verifyCode":
                papiInstance.verifyCodeSentByMail(config);
                break;
            case "requestAccess":
                papiInstance.requestAccesPermission(config);
                break;
            case "autocorrect":
                papiInstance.autoCorrectAllImages(config);
                break;
            case "deskew":
            case "deskewImageFile":
                papiInstance.deskewImageFile(config);
                break;
            case "dewarp":
            case "dewarpImageFile":
                papiInstance.dewarpImageFile(config);
                break;
            case "fixBackTransparency":
            case "fixBackTransparencyImageFile":
            case "fixTransparency":
            case "fixTransparencyImageFile":
                papiInstance.fixTransparencyImageFile(config);
                break;
            case "fixAll":
            case "fixall":
            case "fix":
                papiInstance.fixAllImages(config);
                break;
            case "ocrAll_documentAI":
                papiInstance.allImagesToTextWithDocumentAI(config);
                break;
            case "ocrAll_qwenAI":
                papiInstance.allImagesToTextQwenAI(config);
                break;
            case "ocrJsonAll":
                papiInstance.allImagesToJson(config);
                break;
            case "reorderAll":
                papiInstance.allImagesToFixOrder(config);
                break;
            case "toParagraphsAll":
                papiInstance.allImagesToParagraphs(config);
                break;
            case "toColumnsAll":
                papiInstance.allImagesToColumns(config);
                break;
            case "toBlocksAll":
                papiInstance.allImagesToBlocks(config);
                break;
            case "extract":
                papiInstance.extractAllDataFromDir(config);
                break;
            case "fixOcrAll":
                papiInstance.fixAllOcr(config);
                break;
            default:
                throw new RuntimeException("Unkown command named: ".concat(config.getCommand()));
        }
    }
    
    private void print(ProgressInfo pinfo){
        switch (pinfo.getType()) {
            case ProgressInfo.ERROR_INFO_TYPE:
            case ProgressInfo.INFO_TYPE:
            case ProgressInfo.STATUS_INFO_TYPE:
                Print.printPercentage(pinfo.getMessage(), 1, 1, Print.ToPrintTypes.MESSAGE);
                break;
            case ProgressInfo.PROGRESS_ERROR_TYPE:
            case ProgressInfo.PROGRESS_INFO_TYPE:
                String message = String.format("%s (%s) - %s", pinfo.getName(), pinfo.getProcess(), pinfo.getErrorState()==0?"OK":"ERROR");
                Print.printPercentage(message , pinfo.getProgress(), pinfo.getMaxProgress());
                break;
        }
    }
    
}
