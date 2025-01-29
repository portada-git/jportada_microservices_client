package org.elsquatrecaps.portada.jportadamicroservice.client.services.publickey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.elsquatrecaps.portada.jportadamicroservice.client.PortadaApi;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.ProgressInfo;
import org.elsquatrecaps.portada.jportadamicroservice.client.exceptions.IOPapiCliException;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.PublisherService;
import org.json.JSONObject;

/**
 *
 * @author josep
 */
public class PublicKeyService extends PublisherService{
    
    public static PublicKeyService getInstance(){
        return new PublicKeyService();
    }

    public PublicKeyService() {
        super();
    }
    
    public String sendPublicKeyFile(String command, String inputFile, HashMap<String, String> paramData, String context) throws IOException{
        String ret;
            HttpURLConnection con = flushMultipartRequest(command, "pk", inputFile, paramData, context);
            int responseCode = con.getResponseCode();
            if ((responseCode >= 200) && (responseCode < 400)) { 
                try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                    copyStreams(con.getInputStream(), outputStream);
                    ret = new String(outputStream.toByteArray());
                    Logger.getLogger(PortadaApi.class.getName()).log(Level.INFO, ret);
                }
            } else {
                //error
                try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                    copyStreams(con.getErrorStream(), outputStream);
                    ret = new String(outputStream.toByteArray());
                    Logger.getLogger(PortadaApi.class.getName()).log(Level.WARNING, ret);
                }
                throw new IOPapiCliException(String.format("ERROR: %d - %s (%s)", responseCode, con.getResponseMessage(), ret));
            }
            con.disconnect();
        return ret;        
    }
    
    public void acceptKey(String team, String pkname, String user, String pass){
        String ret;
        try {
            HashMap p = new HashMap();
            p.put("pkname", pkname);
            p.put("team", team);
            p.put("u", user);
            p.put("p", DigestUtils.md5Hex(pass).toUpperCase());
            JSONObject jsonresponse = new JSONObject(sendData("pr/acceptKey", p, "java"));
            if(jsonresponse.getInt("statusCode")==0){
                ret = jsonresponse.getString("message");
                publishInfo(ret, "accept key");
            }else{
                ret = jsonresponse.getString("message");
                publishErrorInfo(ret, "accept key", jsonresponse.getInt("statusCode"));
            }
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "accept key", -1);

        }
    }

    public void deleteKey(String team, String pkname, String user, String pass){
        String ret;
        try {
            HashMap p = new HashMap();
            p.put("pkname", pkname);
            p.put("team", team);
            p.put("u", user);
            p.put("p", pass);
            JSONObject jsonresponse = new JSONObject(sendData("pr/deleteKey", p, "java"));
            if(jsonresponse.getInt("statusCode")==0){
                ret = jsonresponse.getString("message");
                publishInfo(ret, "delete key");
            }else{
                ret = jsonresponse.getString("message");
                publishErrorInfo(ret, "delete key", jsonresponse.getInt("statusCode"));
            }
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "delete key", -1);
        }
    }

    
    public void verifyCodeSentByMail(String team, String email, String code){
        String ret;
        try {
            HashMap p = new HashMap();
            p.put("code", code);
            p.put("team", team);
            p.put("email", email);
            JSONObject jsonresponse = new JSONObject(sendData("verifyRequestedAcessPermission", p, "java"));
            if(jsonresponse.getInt("statusCode")==0){
                ret = jsonresponse.getString("message");
                publishInfo(ret, "verification process");
                //rename public key
                String publicKeyFilename = "public.pem";
                String newPublicKeyFilename = String.format("%s_%s_%s" , email.replaceAll("@", "__AT_SIGN__"), code, publicKeyFilename);
                File publicKeyFile = new File(new File(SECURITY_PATH, team), publicKeyFilename);
                publicKeyFile.renameTo(new File(new File(SECURITY_PATH, team), newPublicKeyFilename));
            }else{
                //error
                if(jsonresponse.getInt("statusCode")==3){
                    //eliminar claus
                    File keyFile = new File(new File(SECURITY_PATH, team), "public.pem");
                    keyFile.delete();
                    keyFile = new File(new File(SECURITY_PATH, team), "private.pem");
                    keyFile.delete();
                }
                ret = jsonresponse.getString("message");
                publishErrorInfo(ret, "verification process", jsonresponse.getInt("statusCode"));
            }
        } catch (Exception ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "verification process", -1);

        }
    }

    public void requestAccesPermission(String team, String email, boolean forceGeneration){
        String ret;
        try {
            File oldKey = null;
            File privateKeyFile = new File(new File(SECURITY_PATH, team), "private.pem").getCanonicalFile().getAbsoluteFile();
            File publicKeyFile = new File(new File(SECURITY_PATH, team), "public.pem").getCanonicalFile().getAbsoluteFile();
            if(privateKeyFile.exists() && !forceGeneration){
                publishStatus("KEY_ALREADY_EXIST", "An access key already exists on this computer. A new key was not created.", "request for accessing");
                return;
            }
            if(forceGeneration){
                File oldKeyDir = publicKeyFile.getParentFile();
                if(oldKeyDir.exists() && oldKeyDir.isDirectory()){
                     for(File n : oldKeyDir.listFiles()){
                         if(n.getName().matches(".*__AT_SIGN__.*_public.pem")){
                            oldKey = n;
                         }
                     }
                }
            }
            KeyPair keyPair = generateKeyPair();
//            String randomName = RandomStringGenerator.builder().get().generate(30);
//            saveKey(keyPair.getPrivate().getEncoded(), new File(SECURITY_PATH, String.format("private%s.pem", randomName)).getCanonicalFile().getAbsoluteFile(), "PRIVATE");
            saveKey(keyPair.getPrivate().getEncoded(), privateKeyFile, "PRIVATE");
            saveKey(keyPair.getPublic().getEncoded(), publicKeyFile, "PUBLIC");
            HashMap p = new HashMap();
            p.put("team", team);
            p.put("email", email);
            if(forceGeneration && oldKey!=null){
                p.put("oldKeyName", oldKey.getName());
            }
            try{
                JSONObject jsonresponse = new JSONObject(sendPublicKeyFile("requestAccessPermission", publicKeyFile.getCanonicalFile().getAbsolutePath(), p, "java"));
                if(jsonresponse.getInt("statusCode")==0){
                    //ret = jsonresponse.getString("message").concat(". Before 10 minutes pass, in the console execute the command: verifyCode -tm [YOUR TEAM] -m [YOUR_E_MAIL] -c [THE CODE RECEIVED]");
                    ret = jsonresponse.getString("message");
                    publishStatus(ProgressInfo.SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST_STATUS, ret, "request for accessing");
                    if(oldKey!=null){
                        oldKey.delete();
                    }
                }else{
                    //error
                    publicKeyFile.delete();
                    privateKeyFile.delete();
                    ret = jsonresponse.getString("message");
                    publishErrorInfo(ret, "request for accessing", jsonresponse.getInt("statusCode"));
                }
            }catch(IOException ex){
                publicKeyFile.delete();
                privateKeyFile.delete();
                Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
                publishErrorInfo(ex.getMessage(), "request for accessing", -1);
            }
        } catch (NoSuchAlgorithmException | IOException ex) {
            Logger.getLogger(PortadaApi.class.getName()).log(Level.SEVERE, null, ex);
            publishErrorInfo(ex.getMessage(), "request for accessing", -1);
        }
    }
    
    private void saveKey(byte[] key, File fileName, String type) throws FileNotFoundException, IOException{
        if(!fileName.getParentFile().exists()){
            fileName.getParentFile().mkdirs();
        }
        String pemKey = String.format("-----BEGIN %s KEY-----\n%s\n-----END %s KEY-----", type,  Base64.getEncoder().encodeToString(key), type);
         try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(pemKey.getBytes());
        }
    }
    
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException{
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair pair = keyPairGen.generateKeyPair();
        return pair;
    }    
}
