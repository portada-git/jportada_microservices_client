package org.elsquatrecaps.portada.jportadamicroservice.client.services;

import org.elsquatrecaps.portada.jportadamscaller.PortadaMicroservicesCaller;
import java.util.function.Function;

/**
 *
 * @author josep
 */
public class PublisherService extends PortadaMicroservicesCaller {
    private Function<ProgressInfo, Void> publish;
    
    
    public PublisherService() {
    }
    
    public<P extends PublisherService> P init(Function<ProgressInfo, Void> publish) {
        this.publish = publish;
        return (P) this;
    }  
    
    public void publishInfo(String message, String process){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.INFO_TYPE, message, "", process, 1,1,0));
        }
    }
    
    public void publishStatus(String status, String message, String process){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.STATUS_INFO_TYPE, status, message, "", process, 1,1,0));
        }
    }
    
    public void publishProgress(String name, String process, int fet, int all){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_INFO_TYPE, "", name, process, fet,all,0));
        }
    }
    
    public void publishProgress(String pre, String name, String process, int fet, int all){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_INFO_TYPE, pre, name, process, fet,all,0));
        }
    }
    
    public void publishErrorProgress(String name, String process, int fet, int all, int errorState){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_ERROR_TYPE, "", name, process, fet,all, errorState));
        }
    }
    
    public void publishErrorProgress(String pre, String name, String process, int fet, int all, int errorState){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.PROGRESS_ERROR_TYPE, pre, name, process, fet,all, errorState));
        }
    }
    
    public void publishErrorInfo(String message, String process, int errorState){
        if(publish!=null){
            publish.apply(new ProgressInfo(ProgressInfo.ERROR_INFO_TYPE, message, "", process, 1, 1, errorState));
        }
    }    
}
