package org.elsquatrecaps.portada.jportadamicroservice.client.services.imagefile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.elsquatrecaps.autonewsextractor.error.AutoNewsRuntimeException;
import org.elsquatrecaps.autonewsextractor.informationunitbuilder.reader.InformationUnitBuilderFromSdlFiles;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.PublisherService;
import org.elsquatrecaps.portada.jportadamscaller.exceptions.PortadaMicroserviceCallException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author josep
 */
public class FixOcrService extends PublisherService{
    
    public static FixOcrService getInstance(){
        return new FixOcrService();
    }
    
    public JSONObject fixOcr(String aiPlatform, String team, List<File> textFiles, List<File> imageFiles){        
        return fixOcr(aiPlatform, team, textFiles, imageFiles, null);
    }

    public JSONObject fixOcr(String aiPlatform, String team, List<File> textFiles, List<File> imageFiles, File configFile){
        JSONObject ret=null;
        JSONObject params = new JSONObject();
        params.put("team", team).put("text", readFileAndGetText(textFiles))
                .put("images", readImageAndGetBytesAndMime(imageFiles)).put("ai_platform", aiPlatform);
        if(configFile!=null){
            try {
                JSONObject content = new JSONObject(FileUtils.readFileToString(configFile, Charset.defaultCharset()));
                params.put("config_json", content);
            } catch (IOException ex) {
                Logger.getLogger(FixOcrService.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        try{
            ret = new JSONObject(sendPostAsFormatParams("/pr/fix_ocr_from_text_and_images", "python", params, String.class));
        } catch (PortadaMicroserviceCallException ex) {
            Logger.getLogger(FixOcrService.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return ret;
    }
    
    
    public JSONObject getOcr(String team, List<File> imageFiles){
        return getOcr(team, imageFiles, null);

    }

    public JSONObject getOcr(String team, List<File> imageFiles, File configFile){
        JSONObject ret=null;
        JSONObject params = new JSONObject();
        params.put("team", team).put("images", readImageAndGetBytesAndMime(imageFiles));
        if(configFile!=null){
            try {
                JSONObject content = new JSONObject(FileUtils.readFileToString(configFile, Charset.defaultCharset()));
                params.put("config_json", content);
            } catch (IOException ex) {
                Logger.getLogger(FixOcrService.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        try{
            ret = new JSONObject(sendPostAsFormatParams("/pr/get_text_from_images", "python", params, String.class));
        } catch (PortadaMicroserviceCallException ex) {
            Logger.getLogger(FixOcrService.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return ret;
    }
    
    private String readFileAndGetText(List<File> files) {
        InformationUnitBuilderFromSdlFiles iub = new InformationUnitBuilderFromSdlFiles();
        StringBuilder bonText = new StringBuilder();
        for(File f: files){
            iub.appendText(bonText, iub.file2Text(f.getName(), f.getParentFile().getAbsolutePath()));
        }
        return bonText.toString();     
    }
    
    private JSONArray readImageAndGetBytesAndMime(List<File> files){
        JSONArray ret = new JSONArray();
        for(File f: files){
            try{
                JSONObject jobject = new JSONObject();
                byte[] fileContent = FileUtils.readFileToByteArray(f);
                String encodedString = Base64.encodeBase64String(fileContent);
                jobject.put("mime_type", Files.probeContentType(f.toPath()));
                jobject.put("image", encodedString);
                ret.put(jobject);
            }catch(IOException ex){
                throw new AutoNewsRuntimeException(String.format("The file %s doesn't exist or can't be read", f), ex);
            }        
        }
        return ret;
    }    
    
}
