package org.elsquatrecaps.portada.jportadamicroservice.client.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.elsquatrecaps.portada.jportadamicroservice.client.ConnectionMs;
import org.elsquatrecaps.portada.jportadamicroservice.client.exceptions.PapiCliException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author josep
 */
public class PublisherService {
    public static final String SECURITY_PATH = "security";
    private Function<ProgressInfo, Void> publish;
    
    
    public static String[] msContext = {"java", "python", "r"};
    protected Map<String, ConnectionMs> conDataList;

    public PublisherService() {
    }
    
    public<P extends PublisherService> P init(Map<String, ConnectionMs> conDataList) {
        this.conDataList = conDataList;
        return (P) this;
    }

    public<P extends PublisherService> P init(Function<ProgressInfo, Void> publish) {
        this.publish = publish;
        return (P) this;
    }
    
    public <T> T sendPostAsFormatParams(String command, String context, JSONObject params, Class<T> type) throws PapiCliException{
        return sendPostAsFormatParams(command, context, params, null, type, true);
    }
    
    public <T> T sendPostAsFormatParams(String command, String context, MultiValueMap<String,Object> params, Class<T> type) throws PapiCliException{
        return sendPostAsFormatParams(command, context, params, null, type, true);
    }
    
    public <T> T sendPostAsFormatParams(String command, String context, MultiValueMap<String, Object> params, SignedData signatureData, Class<T> type, boolean secureRepeat) throws PapiCliException{
        T ret = null;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
//            @Override
//            public void handleError(ClientHttpResponse response) throws IOException {
//                // No fem res aquí, evitant que es llenci una excepció
//            }
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                // Retornem `false` perquè cap resposta es consideri un error
                return false;
            }
        });
        restTemplate.setMessageConverters(Arrays.asList(new HttpMessageConverter[]{new FormHttpMessageConverter(), new StringHttpMessageConverter()}));
        String strUrl = String.format("%s://%s:%s%s%s", getProtocol(context), getHost(context), getPort(context),getPref(context), command);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if(signatureData!=null){
            headers.set("X-Signature", signatureData.getSignedData());
            headers.set("Cookie", signatureData.getSessionCookie());            
        }
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);  
        ResponseEntity<T> response = restTemplate.exchange(strUrl, HttpMethod.POST, requestEntity, type);
        int responseCode = response.getStatusCode().value();
        if ((responseCode >= 200) && (responseCode < 400)) { 
            ret = response.getBody();
        }else if(responseCode == 401 && secureRepeat && params.getFirst("team")!=null){
            try{
                SignedData signedData = signChallengeOfConnection(response, params.getFirst("team").toString());
                if(signedData==null){
                    throw new PapiCliException(-401, "You need generate a security key access");
                }else{
                    ret = sendPostAsFormatParams(command, context, params, signedData, type, false);
                }
            }catch(Exception exc){
                throw new PapiCliException(
                        -100, 
                        String.format("Unexpected error: %s.\nPlease check with the person in charge.", exc.getMessage()),
                        exc
                );
            }
        }else{
            String message = response.getHeaders().getFirst("X-message_error");
            if (message!=null){
                throw new PapiCliException(-responseCode, String.format("Unexpected http error in server process: %s.\nPlease check with the person in charge.", message));
            }else{
                throw new PapiCliException(-responseCode, "Unexpected http error.\nPlease check with the person in charge.");
            }
        }        
        return ret;
    }

    public <T> T sendPostAsFormatParams(String command, String context, JSONObject params, SignedData signatureData, Class<T> type, boolean secureRepeat) throws PapiCliException{
        T ret;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                // No fem res aquí, evitant que es llenci una excepció
                System.out.println(response.getStatusText());
            }

            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                // Retornem `false` perquè cap resposta es consideri un error
                return false;
            }
        });
        String strUrl = String.format("%s://%s:%s%s%s", getProtocol(context), getHost(context), getPort(context),getPref(context), command);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        if(signatureData!=null){
            headers.set("X-Signature", signatureData.getSignedData());
            headers.set("Cookie", signatureData.getSessionCookie());            
        }
        HttpEntity<String> requestEntity = new HttpEntity<>(params.toString(), headers);        
        ResponseEntity<T> response = restTemplate.exchange(strUrl, HttpMethod.POST, requestEntity, type);
        int responseCode = response.getStatusCode().value();
        if ((responseCode >= 200) && (responseCode < 400)) { 
                ret = response.getBody();
        }else if(responseCode == 401 && secureRepeat){
            try{
                SignedData signedData = signChallengeOfConnection(response, params.optString("team", null));
                if(signedData==null){
                    throw new PapiCliException(-401, "You need generate a security key access");
                }else{
                    ret = sendPostAsFormatParams(command, context, params, signedData, type, false);
                }
            }catch(Exception ex){
                throw new PapiCliException(
                        -100, 
                        String.format("Unexpected error: %s.\nPlease check with the person in charge.", ex.getMessage()),
                        ex
                );
            }
        }else{
            String message = response.getHeaders().getFirst("X-message_error");
            if (message!=null){
                throw new PapiCliException(-responseCode, String.format("Unexpected http error in server process: %s.\nPlease check with the person in charge.", message));
            }else{
                throw new PapiCliException(-responseCode, "Unexpected http error.\nPlease check with the person in charge.");
            }
        }        
        return ret;
    }
    
    public String sendData(String command, HashMap<String, String> paramData, String context) throws Exception{
        return sendData(command, paramData, null, context);
    }
    
    public String sendData(String command, HashMap<String, String> paramData, SignedData signatureData, String context) throws Exception{
        String ret;
        String strUrl = String.format("%s://%s:%s%s%s", getProtocol(context), getHost(context), getPort(context),getPref(context), command);
        HttpPost post = new HttpPost(strUrl);
        if(signatureData!=null){
            post.addHeader("X-Signature", signatureData.getSignedData());
            post.addHeader("Cookie", signatureData.getSessionCookie());
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for(String key: paramData.keySet()){
            params.add(new BasicNameValuePair(key, paramData.get(key)));
        }
        post.setEntity(new UrlEncodedFormEntity(params));
        try (CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = (CloseableHttpResponse) client.execute(post)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if ((responseCode >= 200) && (responseCode < 400)) { 
                ret = EntityUtils.toString(response.getEntity());
            }else if(responseCode == 401){
                PublisherService.SignedData signedData = signChallengeOfConnection(response, paramData.getOrDefault("team", null));
                if(signedData==null){
                    ret = "{\"error\":true, \"message\":\"You need generate a security key access\"}";
                }else{
                    ret = sendData(command, paramData,signedData , context);
                }
            }else{
                ret = "{\"error\":true, \"message\":\"Access to resource forbidden\", \"response\":".concat(EntityUtils.toString(response.getEntity())).concat("}");
            }
        }
        return ret;
    }

    protected SignedData signChallengeOfConnection(ResponseEntity response, String team) throws Exception{
        String strContent;
        Object content = response.getBody();
        if(content==null){
            strContent=String.format("{\"challenge\":\"%s\"}", response.getHeaders().getFirst("X-challenge"));
        }else{
            strContent= content.toString();
        }
        return signChallengeOfConnection(team, strContent, response.getHeaders().getFirst("Set-Cookie"));       
    }
    
    protected SignedData signChallengeOfConnection(CloseableHttpResponse response, String team) throws Exception{
        return signChallengeOfConnection(team, response.getEntity().getContent(), response.getFirstHeader("Set-Cookie").getValue());       
    }
    
    protected SignedData signChallengeOfConnection(HttpURLConnection con, String team) throws Exception{
        return signChallengeOfConnection(team, con.getErrorStream(), con.getHeaderField("Set-Cookie"));
    }
    
    protected SignedData signChallengeOfConnection(String team, InputStream stream, String sessionCookie) throws Exception{
        SignedData ret = null;
        File privateKeyFile = new File(new File(SECURITY_PATH, team), "private.pem").getCanonicalFile().getAbsoluteFile();
        if(privateKeyFile.exists()){
            PrivateKey privateKey = loadPrivateKey(privateKeyFile.getAbsolutePath());
            InputStreamReader reader = new InputStreamReader(stream);
            JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
            String signed = signChallenge(jsonResponse.get("challenge").getAsString(), privateKey);
            ret = new SignedData(signed, sessionCookie);
        }
        return ret;
    }
    
    protected SignedData signChallengeOfConnection(String team, String content, String sessionCookie) throws Exception{
        SignedData ret = null;
        File privateKeyFile = new File(new File(SECURITY_PATH, team), "private.pem").getCanonicalFile().getAbsoluteFile();
        if(privateKeyFile.exists()){
            PrivateKey privateKey = loadPrivateKey(privateKeyFile.getAbsolutePath());
            JSONObject jsonResponse = new JSONObject(content);
            String signed = signChallenge(jsonResponse.optString("challenge"), privateKey);
            ret = new SignedData(signed, sessionCookie);
        }
        return ret;
    }
    
    protected static String signChallenge(String challenge, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(challenge.getBytes());

        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }
    
    protected static PrivateKey loadPrivateKey(String filename) throws Exception {
        String key = new String(Files.readAllBytes(new File(filename).toPath()));
        
        // Eliminar les línies d'encapçalament i peu
        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                 .replace("-----END PRIVATE KEY-----", "")
                 .replaceAll("\\s", ""); // Elimina espais i salts de línia

        byte[] keyBytes = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }
    
    protected HttpURLConnection flushMultipartRequest(String command, String fileFieldName, String inputFile, HashMap<String, String> paramData, String context) throws MalformedURLException, IOException{
        return flushMultipartRequest(command, fileFieldName, inputFile, paramData, null, context);
    }
    
    protected HttpURLConnection flushMultipartRequest(String command, String fileFieldName, String inputFile, HashMap<String, String> paramData, SignedData signatureData, String context) throws MalformedURLException, IOException{
        HttpURLConnection con;
        String strUrl = String.format("%s://%s:%s%s%s", getProtocol(context), getHost(context), getPort(context),getPref(context), command);
        File inFile = new File(inputFile);
        StringBuilder hwriter = new StringBuilder();
        if(paramData!=null){
            paramData.forEach((key, val) -> {
                hwriter.append("--*****\r\n");
                hwriter.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n");
                hwriter.append("Content-Type: ").append("text/plain").append("\r\n");
                hwriter.append("\r\n"); 
                hwriter.append(val).append("\r\n");
            });
        }
        hwriter.append("--*****\r\n");
        hwriter.append("Content-Disposition: form-data; name=\"").append(fileFieldName).append("\"; filename=\"").append(inFile.getName()).append("\"\r\n");
        hwriter.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(inFile.getName())).append("\r\n");
        hwriter.append("Content-Length: ").append(String.valueOf(inFile.length())).append("\r\n");
        hwriter.append("\r\n"); 
        StringBuilder fwriter = new StringBuilder();
        fwriter.append("\r\n");
        fwriter.append("--*****--\r\n");

        URL url = new URL(strUrl);
        con = (HttpURLConnection)url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestMethod("POST");
        if(signatureData!=null){
            con.setRequestProperty("X-Signature", signatureData.getSignedData());
            con.setRequestProperty("Cookie", signatureData.getSessionCookie());
        }
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");
        con.connect();
        try (OutputStream outputStream = con.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) { 
            writer.append(hwriter.toString());   
            writer.flush();
            try(FileInputStream in = new FileInputStream(inFile)){
                copyStreams(in, outputStream);
                outputStream.flush();
            }
            writer.append(fwriter.toString());
            writer.flush();
        }        
        return con;
    }    
    
    protected String copyStreamToString(InputStream in) throws IOException{
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(in))){
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }            
        }
        return sb.toString();
    }
    
    protected long copyStreams(InputStream in, OutputStream out) throws IOException{
        BufferedOutputStream bos = new BufferedOutputStream(out);
        BufferedInputStream bis = new BufferedInputStream(in);
        byte[] buffer = new byte[12288]; // 12K
        long count = 0L;
        int n = 0;
        while (-1 != (n = bis.read(buffer))) {
            bos.write(buffer, 0, n);
            count += n;
        }
        bos.flush();
        return count;
    }    
    
    public static final class SignedData{
        private final String signedData;
        private final String sessionCookie;

        public SignedData(String signedData, String sessionCookie) {
            this.signedData = signedData;
            this.sessionCookie = sessionCookie;
        }

        /**
         * @return the signedData
         */
        public String getSignedData() {
            return signedData;
        }

        /**
         * @return the sessionCookie
         */
        public String getSessionCookie() {
            return sessionCookie;
        }
    }  
    
    /**
     * @param key
     * @return the host
     */
    public String getHost(String key) {
        return conDataList.get(key).getHost();
    }

    /**
     * @param key
     * @return the protocol
     */
    public String getProtocol(String key) {
        return conDataList.get(key).getProtocol();
    }

    /**
     * @return the port
     */
    public String getPort(String key) {
        return conDataList.get(key).getPort();
    }

    /**
     * @return the pref
     */
    public String getPref(String key) {
        return conDataList.get(key).getPref();
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
