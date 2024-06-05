/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client.gui;

import java.util.List;
import java.util.function.Function;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.elsquatrecaps.portada.jportadamicroservice.client.Configuration;
import org.elsquatrecaps.portada.jportadamicroservice.client.PortadaApi;

/**
 *
 * @author josep
 */
public class PortadaWorker extends SwingWorker<Void, PortadaApi.ProgressInfo>{
    PortadaApi papìInstance;
    private Configuration config;
    ProcessFrame processFrame= new ProcessFrame();

    public PortadaWorker() {
        this.papìInstance = new PortadaApi((PortadaApi.ProgressInfo t) -> {
            publish(t);
            return null;
        });
    }
    
    public void init(Configuration config) {
        this.config = config;
    }

    
    @Override
    protected Void doInBackground() throws Exception {
        SwingUtilities.invokeLater(() -> {
            processFrame.pack();
            processFrame.setVisible(true);
        });
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
        SwingUtilities.invokeLater(() -> {
            processFrame.setProgressBarPanelVisible(false);
        });
        return null;
    }

    @Override
    protected void process(List<PortadaApi.ProgressInfo> chunks) {
        for(PortadaApi.ProgressInfo info: chunks){
            double percent = 100*info.getProgress()/(double)info.getMaxProgress();
            processFrame.updateProgress(info.getName(), info.getProcess(), percent);
            if(info.getType()==PortadaApi.ProgressInfo.PROCESS){
                processFrame.updateList(info.getName(), info.getProcess(), percent, info.getErrorState()==0);
            }
        }
    }
    
    /**
     * @return the config
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Configuration config) {
        this.config = config;
    }
}
