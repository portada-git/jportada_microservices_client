package org.elsquatrecaps.portada.jportadamicroservice.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josepcanellas
 */
public class PortadaApi {
    String host;
    String port;

    public PortadaApi() {
        init();
    }

    public PortadaApi(String host, String port) {
        this.host = host;
        this.port = port;
    }
    
    
    public final void init(){
        Properties prop = new Properties();
        try {
            FileReader freader = new FileReader("config/init.properties");
            prop.load(freader);
            host = prop.getProperty("host");
            port = prop.getProperty("port");
        } catch (IOException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void deskewImageFile(String inputFile){
        deskewImageFile(inputFile, inputFile);
    }
    
    public void deskewImageFile(String inputFile, String outputFile){
//        String extension="jpg";
//        String strUrl = "http://".concat(host).concat(":").concat(port).concat("/deskewImageFile");
        String strUrl = String.format("http://%s:%s/deskewImageFile",host, port);
//        String[] aInput = inputFile.split("\\.");
//        if(aInput.length>=2){
//            extension = aInput[aInput.length-1];
//        }
        try{  
            
//            MultipartEntityBuilder mpbuilder = MultipartEntityBuilder.create();
//            mpbuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//            mpbuilder.addPart("image", new FileBody(new File(inputFile), ContentType.IMAGE_JPEG));
//            
//            HttpPost post = new HttpPost(strUrl);
//            post.setEntity(mpbuilder.build());
//            
//            try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
//                CloseableHttpResponse response = client.execute(post);
//                try(InputStream in = response.getEntity().getContent()){
//                    try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
//                        copyStreams(in, outputStream);
//                    }
//                }
//            }
            File inFile = new File(inputFile);
            StringBuilder hwriter = new StringBuilder();
            hwriter.append("--*****\r\n");
            hwriter.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(inFile.getName()).append("\"\r\n");
            hwriter.append("Content-Type: ").append(HttpURLConnection.guessContentTypeFromName(inFile.getName())).append("\r\n");
            hwriter.append("Content-Length: ").append(String.valueOf(inFile.length())).append("\r\n");
            hwriter.append("\r\n"); 
            StringBuilder fwriter = new StringBuilder();
            fwriter.append("\r\n");
            fwriter.append("--*****--\r\n");
//            long clength = hwriter.length() +  fwriter.length() + inFile.length();
            
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + "*****");
//            con.setRequestProperty("Content-Length", String.valueOf(clength));
            con.setRequestMethod("POST");
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
            int responseCode = con.getResponseCode();
            if ((responseCode >= 200) && (responseCode < 400)) {  
                try(FileOutputStream outputStream = new FileOutputStream(outputFile)){
                    copyStreams(con.getInputStream(), outputStream);
                }
            } else {
                //error
                //con.getErrorStream();
            }
            con.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private long copyStreams(InputStream in, OutputStream out) throws IOException{
        BufferedOutputStream bos = new BufferedOutputStream(out);
        BufferedInputStream bis = new BufferedInputStream(in);
        byte[] buffer = new byte[12288]; // 12K
        long count = 0L;
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    
}
