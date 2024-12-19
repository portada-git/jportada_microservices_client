/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client.services.imagefile;

import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elsquatrecaps.portada.jportadamicroservice.client.ConnectionMs;
import org.elsquatrecaps.portada.jportadamicroservice.client.PortadaApi;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.PublisherService;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author josep
 */
public class ImageFileService extends PublisherService {

    /**
     *
     * @return
     */
    public static ImageFileService getInstance(){
        return new ImageFileService();
    }

    public ImageFileService() {
        super();
    }
    
    public String transformImageFileToJsonImages(String command, String inputFile, String outputDir, String context){
        return transformImageFileToJsonImages(command, inputFile, outputDir, null, context);
    }
    
    public String transformImageFileToJsonImages(String command, String inputFile, String outputDir, HashMap<String, String> paramData, String context){
        String ret = null;
        try{  
            boolean exit;
            HttpURLConnection con = flushMultipartRequest(command, "image", inputFile, paramData, context);
            do{
                exit = true;
                int responseCode = con.getResponseCode();
                if ((responseCode >= 200) && (responseCode < 400)) {  
                    JSONObject json = new JSONObject(copyStreamToString(con.getInputStream()));
                    if(json.has("status") && json.getInt("status")!=0){
                        ret = json.toString();
                    }else if(json.has("error") && json.getBoolean("error")){
                        ret = "{\"status\":-1, \"message\":\"".concat(json.getString("message")).concat("\"}");
                    }else{
                        //copy all files
                        JSONArray ar = json.getJSONArray("images");
                        for(int i=0; i< ar.length(); i++){
                            byte[] bytes = Base64.getDecoder().decode(ar.getJSONObject(i).getString("image"));
                            Files.write(
                                    Paths.get(String.format("%s_%03d%s", 
                                            outputDir, 
                                            ar.getJSONObject(i).getInt("count"), 
                                            ar.getJSONObject(i).getString("extension"))), 
                                    bytes);
                        }
                        //if OK
                            ret = "{\"status\":0, \"message\":\"".concat(json.has("message")?json.getString("message"):"OK").concat("\"}");
                        //else
                            //ret = "{\"status\":-2, \"message\":\"".concat(json.has("message")?json.getString("message"):"OK").concat("\"}");
                    }                    
                } else if(responseCode==401) {
                    SignedData signedData = signChallengeOfConnection(con, paramData.getOrDefault("team", null));
                    con.disconnect();
                    con = flushMultipartRequest(command, "image", inputFile, paramData, signedData, context);
                    exit = false;
                } else {
                    //error
                    ret = "{\"status\":-3, \"message\":\"".concat(copyStreamToString(con.getErrorStream())).concat("\"}");
                }
            }while(!exit);
            con.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            ret = "{\"status\":-4, \"message\":\"".concat(ex.getMessage()).concat("\"}");
        }
        return ret;
        
    }
    
    public boolean transformImageFile(String command, String inputFile, String outputFile, String errorFile, String context){
        return transformImageFile(command, inputFile, outputFile, errorFile, null, context);
    }
    
    public boolean transformImageFile(String command, String inputFile, String outputFile, String errorFile, HashMap<String, String> paramData, String context){
        boolean ret=true;
        try{  
            boolean exit;
            HttpURLConnection con = flushMultipartRequest(command, "image", inputFile, paramData, context);
            do{
                exit = true;
                int responseCode = con.getResponseCode();
                if ((responseCode >= 200) && (responseCode < 400)) {  
                    try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
                        copyStreams(con.getInputStream(), outputStream);
                    }
                } else if(responseCode==401) {
                    SignedData signedData = signChallengeOfConnection(con, paramData.getOrDefault("team", null));
                    con.disconnect();
                    con = flushMultipartRequest(command, "image", inputFile, paramData, signedData, context);
                    exit = false;
                } else {
                    //error
                    try(FileOutputStream outputStream = new FileOutputStream(errorFile)){
                        copyStreams(con.getErrorStream(), outputStream);
                    }
                    ret = false;
                }
            }while(!exit);
            con.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        }
        return ret;
    }

}
