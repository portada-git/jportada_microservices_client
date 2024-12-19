package org.elsquatrecaps.portada.jportadamicroservice.client.gui;

import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.elsquatrecaps.portada.jportadamicroservice.client.Configuration;
import org.elsquatrecaps.portada.jportadamicroservice.client.JPortadaMicroservice;
import org.elsquatrecaps.portada.jportadamicroservice.client.PortadaApi;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.ProgressInfo;

/**
 *
 * @author josep
 */
public class PortadaWorker extends SwingWorker<Void, ProgressInfo>{
    PortadaApi papìInstance;
    private Configuration config;
    InfoProcessFrame processFrame;

    public PortadaWorker() {
        processFrame= new ProcessFrame();
        this.papìInstance = new PortadaApi((ProgressInfo t) -> {
            publish(t);
            return null;
        });
    }
    
    public PortadaWorker(InfoProcessFrame processFrame) {
        this.processFrame = processFrame;
        this.papìInstance = new PortadaApi((ProgressInfo t) -> {
            publish(t);
            return null;
        });
    }
    
    public void init(Configuration config) {
        this.config = config;
    }

    public void init(Configuration config, InfoProcessFrame processFrame) {
        this.config = config;
        this.processFrame = processFrame;
    }

    
    @Override
    protected Void doInBackground() throws Exception {
        SwingUtilities.invokeLater(() -> {
            processFrame.pack();
            processFrame.setVisible(true);
        });
        JPortadaMicroservice.execute(config, papìInstance);
        SwingUtilities.invokeLater(() -> {
            processFrame.setProgressBarPanelVisible(false);
        });
        return null;
    }

    @Override
    protected void process(List<ProgressInfo> chunks) {
        for(ProgressInfo info: chunks){
            switch (info.getType()) {
                case ProgressInfo.INFO_TYPE:
                    processFrame.updateInfo(info.getMessage(), info.getProcess());
                    break;
                case ProgressInfo.ERROR_INFO_TYPE:
                    processFrame.updateErrorInfo(info.getMessage(), info.getProcess(), info.getErrorState());
                    break;
                case ProgressInfo.STATUS_INFO_TYPE:
                    processFrame.updateStatusInfo(info.getStatus(), info.getMessage(), info.getProcess());
                    break;
                default:
                    double percent = 100*info.getProgress()/(double)info.getMaxProgress();
                    processFrame.updateProgress(info.getName(), info.getProcess(), percent);
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
