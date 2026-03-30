package org.elsquatrecaps.portada.jportadamicroservice.client.services.imagefile;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elsquatrecaps.portada.jportadamicroservice.client.PortadaApi;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.PublisherService;
import org.json.JSONObject;

/**
 *
 * @author josep
 */
public class ImageQualityFilterService extends PublisherService {
    
    public static ImageQualityFilterService getInstance(){
        return new ImageQualityFilterService();
    }

    public ImageQualityFilterService() {
        super();
    }
    
    public JSONObject processBinaryFilter(String inputFile){
        return processBinaryFilter(inputFile, null);
    }
    
    public JSONObject processBinaryFilter(String inputFile, Double threshold){
        JSONObject ret;
        String q = "";
        if (threshold != null){
            q = "?threshold=".concat(String.valueOf(threshold.floatValue()));
        }
        ret = new JSONObject(sedFileAndParamsAndGetJson("binary/predict".concat(q), inputFile, null,  "docker"));
        return ret;        
    }
    
    public JSONObject processMulticlassFilter(String inputFile){
        return processMulticlassFilter(inputFile, null);
    }
    
    public JSONObject processMulticlassFilter(String inputFile, Double threshold){
        JSONObject ret;
        String q = "";
        if (threshold != null){
            q = "?threshold=".concat(String.valueOf(threshold.floatValue()));
        }
        ret = new JSONObject(sedFileAndParamsAndGetJson("multiclass/predict".concat(q), inputFile, null,  "docker"));
        return ret;        
    }
    
    private String sedFileAndParamsAndGetJson(String command, String inputFile, HashMap<String, String> paramData, String context){
        String ret = null;
        try{  
            boolean exit;
            HttpURLConnection con = flushMultipartRequest(command, "file", inputFile, paramData, context);
            int times=0;
            do{
                exit = true;
                int responseCode = con.getResponseCode();
                if ((responseCode >= 200) && (responseCode < 400)) {  
                    JSONObject json = new JSONObject(copyStreamToString(con.getInputStream()));
                    ret = "{\"status\":0, \"result\":".concat(json.toString()).concat("}");
                } else if(responseCode==401) {
                    SignedData signedData = signChallengeOfConnection(con, paramData.getOrDefault("team", null));
                    con.disconnect();
                    con = flushMultipartRequest(command, "image", inputFile, paramData, signedData, context);
                    times++;
                    exit = times>1;
                } else {
                    //error
                    ret = "{\"status\":-1, \"message\":\"Error. Something was wrong\"}";
                }
            }while(!exit);
            con.disconnect();
            if(times>1){
                Object[] par = new Object[0];
                Logger.getLogger(PortadaApi.class.getName()).log(Level.WARNING, "Error. You need generate a security key access", par);
                ret = "{\"status\":-2, \"message\":\"Error. You need generate a security key access\"}";
            }
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            ret = "{\"status\":-3, \"message\":\"Error: ".concat(ex.getMessage()).concat("\"}");
        }
        return ret;
        
    }
}
