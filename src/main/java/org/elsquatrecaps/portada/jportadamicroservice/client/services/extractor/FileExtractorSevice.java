package org.elsquatrecaps.portada.jportadamicroservice.client.services.extractor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.elsquatrecaps.autonewsextractor.model.MutableNewsExtractedData;
import org.elsquatrecaps.autonewsextractor.model.NewsExtractedData;
import org.elsquatrecaps.autonewsextractor.model.PublicationInfo;
import org.elsquatrecaps.autonewsextractor.tools.configuration.AutoNewsExtractorConfiguration;
import org.elsquatrecaps.autonewsextractor.tools.formatter.BoatFactCsvFormatter;
import org.elsquatrecaps.autonewsextractor.tools.formatter.JsonFileFormatterForExtractedData;
import org.elsquatrecaps.portada.boatfactextractor.BoatFactReader;
import org.elsquatrecaps.portada.boatfactextractor.BoatFactVersionUpdater;
import static org.elsquatrecaps.portada.boatfactextractor.BoatFactVersionUpdater.BoatFactVersionUpdaterResponse.JSON_IS_NOT_UPDATABLE;
import static org.elsquatrecaps.portada.boatfactextractor.BoatFactVersionUpdater.BoatFactVersionUpdaterResponse.UNKNOWN_VERSION;
import org.elsquatrecaps.portada.boatfactextractor.BoatFactVersionVerifier;
import org.elsquatrecaps.portada.boatfactextractor.TextParserInfo;
import org.elsquatrecaps.portada.jportadamicroservice.client.exceptions.RuntimePapiCliException;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.PublisherService;
import org.elsquatrecaps.portada.jportadamscaller.ConnectionMs;
import org.elsquatrecaps.portada.jportadamscaller.exceptions.PortadaMicroserviceCallException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class FileExtractorSevice extends PublisherService{
    private AutoNewsExtractorConfiguration extractorProp=null;
    private File extractorJsonConfPath=null;
    private String newsPaper=null;
    
    public static FileExtractorSevice getInstance(){
        return new FileExtractorSevice();
    }
    
    public FileExtractorSevice() {
        super();
    }
    
//    public FileExtractorReactiveSevice(Map<String, ConnectionMs> conDataList) {
//        this.conDataList = conDataList;
//        initWebClient();
//    }
//    
//    public FileExtractorReactiveSevice(Configuration config) {
//        this();
//        init(config);
//    }
//   
    
    
    public final FileExtractorSevice init(AutoNewsExtractorConfiguration extractorProperties){
        this.extractorProp = extractorProperties;
        return this;
    }
    
    public final FileExtractorSevice init(File extractorJsonConfPath){
        this.extractorJsonConfPath = extractorJsonConfPath;
        return this;
    }
    
    public final FileExtractorSevice init(String np){
        this.newsPaper = np;
        return this;
    }
    
    public List<NewsExtractedData> processCutAndExtractFromText(
            String team, 
            TextParserInfo info,
            AutoNewsExtractorConfiguration extractorProp,
            JSONObject extractorJsonConf){
        String text = info.getTextToParse();
        int parserId = info.getParserId(); 
        PublicationInfo publicationInfo = info.getPublicationInfo();
        List<NewsExtractedData> ret=new ArrayList<>();
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();        
        params.add("team", team);
        params.add("text", text);
        params.add("parser_id", parserId);
        params.add("publication_info", publicationInfo.getAllDataAsJson());
        params.add("news_paper", newsPaper);
        if(extractorProp!=null){
            params.add("cfg_properties", serialize(extractorProp));
        }
        if(extractorJsonConfPath!=null){
            params.add("cfg_json_parsers", extractorJsonConf.toString());
        }        
        try {
            String strResp = sendPostAsFormatParams("pr/cutAndExtractFromText", "java", params, String.class);
            JSONObject resp= new JSONObject(strResp);
            if(resp.getInt("statusCode")==0){
                JSONArray l = resp.getJSONArray("extractedlist");
                for(int i=0; i< l.length(); i++){
                    ret.add(new MutableNewsExtractedData(l.getJSONObject(i)));
                }
            }else{
                //publish
                publishErrorProgress(info.getInformationUnitName(), "extract", (int)(100*info.getCompletedRatio()), 100, resp.getInt("statusCode"));
            }
        } catch (PortadaMicroserviceCallException ex) {
//            strRet = ex.getJsonFormat();
            //PUBLISH
            publishErrorInfo(ex.getMessage(), "extract", ex.getErrorcode());
        }
        return ret;
    }
    
    public BoatFactVersionUpdater.BoatFactVersionUpdaterResponse processUpdate(){
        BoatFactVersionUpdater.BoatFactVersionUpdaterResponse ret;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(new HttpMessageConverter[]{new FormHttpMessageConverter(), new StringHttpMessageConverter()}));
        ConnectionMs c = conDataList.get("java");
        String strUrl = String.format("%s://%s:%s%s%s", c.getProtocol(), c.getHost(), c.getPort(),c.getPref(), "config_json_parsers_update_version");
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();        
        params.add("news_paper", newsPaper);
        String strresp = restTemplate.postForObject(strUrl, params, String.class);
        JSONObject resp = new JSONObject(strresp);
        if(resp.getInt("statusCode")==0){
            ret = BoatFactVersionUpdater.BoatFactVersionUpdaterResponse.valueOf(resp.get("response").toString());
        }else{
            throw new RuntimePapiCliException(resp.getInt("statusCode"),resp.getJSONObject("response").getString("message"));
        }
        return ret;
    }
    
    public JSONObject getRemoteJsonConfigParser(){
        JSONObject ret = null;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(new HttpMessageConverter[]{new FormHttpMessageConverter(), new StringHttpMessageConverter()}));
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();        
        ConnectionMs c = conDataList.get("java");
        String strUrl = String.format("%s://%s:%s%s%s", c.getProtocol(), c.getHost(), c.getPort(),c.getPref(), "get_extractor_json_config_parser");
        params.add("news_paper", newsPaper);
        String strresp = restTemplate.postForObject(strUrl, params, String.class);
        JSONObject resp = new JSONObject(strresp);
        if(resp.getInt("statusCode")==0){
            ret = new JSONObject(resp.getString("response"));
        }else{
            throw new RuntimePapiCliException(resp.getInt("statusCode"),resp.getJSONObject("response").getString("message"));
        }
        return ret;
    }
    
    public AutoNewsExtractorConfiguration getRemoteProperties(){
        AutoNewsExtractorConfiguration ret;
        RestTemplate restTemplate = new RestTemplate();
        ConnectionMs c = conDataList.get("java");
        String strUrl = String.format("%s://%s:%s%s%s", c.getProtocol(), c.getHost(), c.getPort(),c.getPref(), "get_extractor_properties");
        restTemplate.setMessageConverters(Arrays.asList(new HttpMessageConverter[]{new FormHttpMessageConverter(), new StringHttpMessageConverter()}));
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("news_paper", newsPaper);
        String strresp = restTemplate.postForObject(strUrl, params, String.class);
        JSONObject resp = new JSONObject(strresp);
        if(resp.getInt("statusCode")==0){
            ret = deserialize(resp.getString("response"), AutoNewsExtractorConfiguration.class);
        }else{
            throw new RuntimePapiCliException(resp.getInt("statusCode"),resp.getJSONObject("response").getString("message"));
        }
        return ret;
    }
    
    public void processFiles(String team, String outputFile, String originDir, String extension){
        ConnectionMs c = conDataList.get("java");
        AutoNewsExtractorConfiguration autoNewsExtractorConfiguration;
        
        if(extractorProp!=null){
            autoNewsExtractorConfiguration = extractorProp;
        }else{
            autoNewsExtractorConfiguration = getRemoteProperties();
        }
        processExtractDataList(team, outputFile, originDir, extension, autoNewsExtractorConfiguration);        
    }
    
    private  void processExtractDataList(String team, String outputFile, String originDir, String extension, 
                            AutoNewsExtractorConfiguration autoNewsExtractorConfiguration){
        List<NewsExtractedData> extractDataList=null;
        BoatFactVersionUpdater.BoatFactVersionUpdaterResponse r;
        JSONObject autoNewsExtractorJsonConfiguration = null;
        if(extractorJsonConfPath!=null){
            try {
                autoNewsExtractorJsonConfiguration = new JSONObject(Files.readString(extractorJsonConfPath.toPath()));
            } catch (IOException ex) {
                throw new RuntimePapiCliException(-100, "Error reading parser json config file.\n ".concat(ex.getMessage()), ex); 
            }
        }else{
            autoNewsExtractorJsonConfiguration = getRemoteJsonConfigParser();
        }

        try{
            if(this.extractorJsonConfPath!=null){
                r = BoatFactVersionUpdater.tryToUpdate(autoNewsExtractorJsonConfiguration);
            }else{
                r = processUpdate();
            }
            if(!r.isError()){
                try{
                    int parseModels = autoNewsExtractorConfiguration.getParseModel().length;
                    for(int i=0; i<parseModels; i++){
                        final int id = i;
                        BoatFactReader boatFactReader = new BoatFactReader();
                        final JSONObject jsonCfg = autoNewsExtractorJsonConfiguration;
                        List<NewsExtractedData> tmpExtractDataList = new ArrayList<>();
                        String fn = String.format("%s_%s",outputFile, autoNewsExtractorConfiguration.getParseModel()[i]);
                        String fn_tmp = String.format("%s_tmp", fn);
                        JSONObject csvParams = readConfigCsv(autoNewsExtractorJsonConfiguration, autoNewsExtractorConfiguration.getParseModel()[i]);
                        extractDataList = boatFactReader.initInfoUnitBuilderConfig(autoNewsExtractorConfiguration).initProcessInformationUnitCallback((param) -> {
                            List<NewsExtractedData> l;
                            l = this.processCutAndExtractFromText(team, param, extractorProp, jsonCfg);
                            //PUBLISH
                            publishProgress(param.getInformationUnitName(), String.format("extract (%s)", autoNewsExtractorConfiguration.getParseModel()[id]), (int)(100*param.getCompletedRatio()), 100);
                            tmpExtractDataList.addAll(l);
                            JsonFileFormatterForExtractedData<NewsExtractedData> formatter = new JsonFileFormatterForExtractedData<>(tmpExtractDataList);
                            formatter.toFile(fn_tmp);
                            if(csvParams!=null){
                                BoatFactCsvFormatter csvFormatter = new BoatFactCsvFormatter();
                                csvFormatter.configHeaderFields(csvParams).format(tmpExtractDataList).toFile(fn_tmp);
                            }
                            return l;
                        }).processFiles(originDir, extension, i);
                        //Emmagatzemar
                        JsonFileFormatterForExtractedData<NewsExtractedData> formatter = new JsonFileFormatterForExtractedData<>(extractDataList);
                        formatter.toFile(fn);
                        if(csvParams!=null){
                            BoatFactCsvFormatter csvFormatter = new BoatFactCsvFormatter();
                            List d = extractDataList;
                            csvFormatter.configHeaderFields(csvParams).format(d).toFile(fn);
                        }
                        if(Files.exists(Paths.get(fn_tmp))){
                            Files.delete(Paths.get(fn_tmp));                            
                        }
                        if(Files.exists(Paths.get(fn_tmp.concat(".json")))){
                            Files.delete(Paths.get(fn_tmp.concat(".json")));                            
                        }
                        if(Files.exists(Paths.get(fn_tmp.concat(".csv")))){
                            Files.delete(Paths.get(fn_tmp.concat(".csv")));                            
                        }
                    }
                    if(r.equals(BoatFactVersionUpdater.BoatFactVersionUpdaterResponse.JSON_UPDATED) || r.equals(BoatFactVersionUpdater.BoatFactVersionUpdaterResponse.JSON_UPDATED_WITH_WARNIGS)){
                        if(extractorJsonConfPath!=null){
                            //save file
                            Files.writeString(
                                extractorJsonConfPath.toPath(), 
                                autoNewsExtractorJsonConfiguration.toString(4)
                            );
                        }
                    }
                    //savelistJSON i saveListCsv
                }catch(RuntimeException ex){
                    if(extractorJsonConfPath!=null){
                        //raload
                        String jsc = Files.readString(extractorJsonConfPath.toPath());
                        autoNewsExtractorJsonConfiguration = new JSONObject(jsc); 
                        List<BoatFactVersionVerifier.BoatFactVersionVerifierResponse> l = BoatFactVersionVerifier.verify(autoNewsExtractorJsonConfiguration);
                        if(l.size()>1){
                            publishErrorInfo(
                                    "Error due to version of config json file.\n\n".concat(ex.getMessage()), 
                                    "Extract process", -1);
                            //infoCallback.call("Error due to version of config json file.\n\n".concat(ex.getMessage())); //TO DO CHANGE THE MESSAGE 
                        }else{
                            publishErrorInfo(String.format("Unexpected error: %s", ex.getMessage()), "Extrcat process", -2);
                            //infoCallback.call(ex.getMessage());
                        }
                    }else{
                        
                    }
                }
            }else{
                String m;
                switch (r) {
                    case JSON_IS_NOT_UPDATABLE:
                        m = "VERSION ERROR: Update the config JSON is not possible automatically";
                        break;
                    case UNKNOWN_VERSION:
                        m = "VERSION ERROR: Unknown version of config JSON. It is not possible update automatically";
                        break;
                    default:
                        m = "VERSION ERROR: Unknown error trying to update the config JSON";
                }
//                infoCallback.call(m);
                publishErrorInfo(m, "Extract process", -3);
            }
        }catch (IOException ex) {
            if(extractorJsonConfPath!=null){
            String message = String.format("Error: The file \"%s\" can not be read. please revise it", extractorJsonConfPath);
//            infoCallback.call(message);
                publishErrorInfo(message, "Extract process", -4);
            }else{
                //PUBLISH
                publishErrorInfo(String.format("Unexpected error: %s", ex.getMessage()), "Extract process", -5);
            }
        }   
    }
    
    private JSONObject readConfigCsv(JSONObject jsonCgf, String parserName){
        JSONObject ret=null;
        JSONObject parser = jsonCgf.optJSONObject(parserName);
        if(parser!=null){            
            ret = parser.optJSONObject("csv_view");
        }
        return ret;
    }
    
    
    
//    private String serializeAndGetParam(String type, Object value){
//        return getParam(type, null, value);
//    }
    
//    private String getParam(String type, Object value){
//        return getParam(type, null, value);
//    }
//    
//    private String getParam(String type, String name, Object value){
//        JSONObject ret = new JSONObject();
//        ret.put("param_type", type);
//        if(name!=null){
//            ret.put("param_name", name);
//        }
//        ret.put("param_value", value);
//        return ret.toString();
//    }
//    
//    private String serializeAndGetParam(String type, String name, Object value){
//        ObjectMapper objectMapper = new ObjectMapper();
//        JSONObject ret = new JSONObject();
//        try {
//            ret.put("param_type", type);
//            if(name!=null){
//                ret.put("param_name", name);
//            }
//            ret.put("param_value", new JSONObject(objectMapper.writeValueAsString(value)));
//        } catch (JsonProcessingException ex) {
//            ret.put("param_value", value);
//        }
//        return ret.toString();
//    }

    private<T> T deserialize(String value, Class<T> clazz){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            T ret = objectMapper.readValue(value, clazz);
            return ret;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private String serialize(Object value){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String ret = objectMapper.writeValueAsString(value);
            return ret;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
