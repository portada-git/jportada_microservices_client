//package org.elsquatrecaps.portada.jportadamicroservice.client.services.extractor;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.elsquatrecaps.autonewsextractor.tools.configuration.AutoNewsExtractorConfiguration;
//import org.elsquatrecaps.portada.boatfactextractor.BoatFactVersionUpdater;
//import org.elsquatrecaps.portada.jportadamicroservice.client.Configuration;
//import org.elsquatrecaps.portada.jportadamicroservice.client.ConnectionMs;
//import org.elsquatrecaps.portada.jportadamicroservice.client.exceptions.RuntimePapiCliException;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Sinks;
//
//
@Service
public class FileExtractorReactiveSevice {
//    public static String[] msContext = {"java", "python", "r"};
//    private final Map<String, ConnectionMs> conDataList;
//    private AutoNewsExtractorConfiguration extractorProp=null;
//    private JSONObject extractorJsonConf=null;
//    private String newsPaper=null;
//    
//    @Autowired
//    private WebClient webClient;
//
//    public FileExtractorReactiveSevice() {
//        conDataList = new HashMap<>();
//        initWebClient();
//    }
//    
//    
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
//    private void initWebClient(){
//        if(webClient==null){
//            webClient = FileExtractorWebClientConfig.getFileExtractorWebClient(WebClient.builder());
//        }
//    }
//    
//    public final FileExtractorReactiveSevice init(Configuration config) {
//        for(String ctx: msContext){
//            this.conDataList.put(ctx, new ConnectionMs(config.getProtocols(ctx), config.getPort(ctx), config.getHosts(ctx), config.getPrefs(ctx)));
//        }
//        return this;
//    }
//    
//    public final FileExtractorReactiveSevice init(AutoNewsExtractorConfiguration extractorProperties){
//        this.extractorProp = extractorProperties;
//        return this;
//    }
//    
//    public final FileExtractorReactiveSevice init(JSONObject extractorJsonConf){
//        this.extractorJsonConf = extractorJsonConf;
//        return this;
//    }
//    
//    public final FileExtractorReactiveSevice init(String np){
//        this.newsPaper = np;
//        return this;
//    }
//    
//    public BoatFactVersionUpdater.BoatFactVersionUpdaterResponse processUpdate(){
//        BoatFactVersionUpdater.BoatFactVersionUpdaterResponse ret;
//        ConnectionMs c = conDataList.get("java");
//        String strUrl = String.format("%s://%s:%s%s%s", c.getProtocol(), c.getHost(), c.getPort(),c.getPref(), "config_json_parsers_update_version");
//        Map<String, String> params = new HashMap<>();
//        params.put("news_paper", newsPaper);
//        String strresp = webClient.post().uri(strUrl).bodyValue(new JSONObject(params).toString()).retrieve().bodyToMono(
//                //BoatFactVersionUpdater.BoatFactVersionUpdaterResponse.class
//                String.class
//        ).block();
//        JSONObject resp = new JSONObject(strresp);
//        if(resp.getInt("statusCode")==0){
//            ret = BoatFactVersionUpdater.BoatFactVersionUpdaterResponse.valueOf(resp.get("response").toString());
//        }else{
//            throw new RuntimePapiCliException(resp.getInt("statusCode"),resp.getJSONObject("response").getString("message"));
//        }
//        return ret;
//    }
//    
//    
//    public Flux<String> processFiles(String originDir){
//        File dirBase;
//        dirBase = new File(originDir);
//        File[] files = dirBase.listFiles();
//        Arrays.sort(files);
//        return processFiles(files);
//    }
//    
//    public Flux<String> processFiles(String originDir, String extension){
//        File dirBase;
//        dirBase = new File(originDir);
//        File[] files = dirBase.listFiles((file, string) ->
//                string.endsWith(extension));
//        Arrays.sort(files);
//        return processFiles(files);
//    }
//    
//    public Flux<String> processFiles(File[] files){
//        ConnectionMs c = conDataList.get("java");
//        String strUrl = String.format("%s://%s:%s%s%s", c.getProtocol(), c.getHost(), c.getPort(),c.getPref(), "extract");
//        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
//        Flux<String> dataflux = sink.asFlux();
//        Thread th = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(500);
//                    if(newsPaper!=null){
//                        sink.tryEmitNext(getParam("conf", "news_paper", newsPaper));
//                    }
//                    if(extractorProp!=null){
//                        sink.tryEmitNext(serializeAndGetParam("conf", "extractor_properties", extractorProp));
//                    }
//                    if(extractorJsonConf!=null){
//                        sink.tryEmitNext(getParam("conf", "extractor_json_config", extractorJsonConf));
//                    }
//                    sink.tryEmitNext(getParam("counter", files.length));
//                    sink.tryEmitNext(getParam("list", "start"));
//                    for(int i=0; i<files.length; i++){
//                        File f = files[i];
//                        try{
//                            JSONObject element = new JSONObject();
//                            element.put("pos", i);
//                            element.put("name", f.getName());
//                            element.put("text", new String(Files.readAllBytes(f.toPath())));
//                            sink.tryEmitNext(getParam("item", element));
//                        }catch(IOException ex){
//                            //[TODO: error]
//                        }
//                    }
//                    sink.tryEmitNext(getParam("list", "data_list" ,"end"));
//                    sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);                    
//                } catch (InterruptedException ex) {
//                    //[TODO: error]
//                    throw new RuntimeException(ex);
//                }
//            }
//        });
//        Flux<String> ret = webClient.post()
//                .uri(strUrl)
//                .body(BodyInserters.fromPublisher(dataflux, String.class))
//                .retrieve()
//                .bodyToFlux(String.class);        
//        th.start();
//        try {
//            th.join();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(FileExtractorReactiveSevice.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return ret;
//    }
//    
//    private String serializeAndGetParam(String type, Object value){
//        return getParam(type, null, value);
//    }
//    
//    private String getParam(String type, Object value){
//        return getParam(type, null, value);
//    }
//    
//    private <T> String getParam(String type, String name, Object value){
//        JSONObject ret = new JSONObject();
//        ret.put("param_type", type);
//        if(name!=null){
//            ret.put("param_name", name);
//        }
//        ret.put("param_value", value);
//        return ret.toString();
//    }
//    
//    private <T> String serializeAndGetParam(String type, String name, Object value){
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
//    
//    
}
