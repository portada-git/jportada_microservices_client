
import java.util.HashMap;
import java.util.Map;
import org.elsquatrecaps.portada.jportadamicroservice.client.Configuration;
import org.elsquatrecaps.portada.jportadamicroservice.client.ConnectionMs;
import org.elsquatrecaps.portada.jportadamicroservice.client.JPortadaMicroservice;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.PublisherService;
import org.elsquatrecaps.portada.jportadamicroservice.client.services.imagefile.ImageQualityFilterService;

/**
 *
 * @author josep
 */
public class Test {
    
    public static void main(String[] args) {
//        JPortadaMicroservice prg = new JPortadaMicroservice();
//        String[] args2 = {"requestAccess", "-tm", "bcn", "-m", "josep.canellas@gmail.com", "-f"};
//        String[] args2 = {"verifyCode", "-tm", "bcn", "-m", "josep.canellas@gmail.com", "-c", "dPPvzUcB"};
//        String[] args2 = {"ocrAll", "-tm", "bcn", "-i", "corregides2", "-o", "ocr4"};
//        String[] args2 = {"reorderAll", "-tm", "bcn", "-i", "corregides2", "-o", "ordered"};
//        String[] args2 = {"pytest", "-tm", "bcn"};
//        String[] args2 = {"jtest", "-tm", "bcn"};
//        String[] args2 = {"ocrJsonAll", "-tm", "bcn", "-i", "corregides2", "-o", "ocr_json"};
//        String[] args2 = {"accept_key", "-tm", "bcn", "-k", "ogtoledano__AT_SIGN__gmail.com_Cg1Z_M6G_public.pem", "-u", "??", "-p", "???"};
//        String[] args2 = {
//            "extract", 
//            "-i", "/home/josep/Dropbox/feinesJordi/github/JPortadaMicroserviceClient/ocr7", 
//            "-o", "/home/josep/Dropbox/feinesJordi/github/JPortadaMicroserviceClient/resultats", 
//            "-tm", "bcn",
//            "--extractConfigMode", "R", 
//            "--extractConfigProtertiesFile", "db"
//        };
//        prg.run(args2);

        Configuration config = new Configuration();
        config.configure();
        Map<String, ConnectionMs> conDataList = new HashMap<>();
        for(String ctx: PublisherService.msContext){
            conDataList.put(ctx, new ConnectionMs(config.getProtocols(ctx), config.getPort(ctx), config.getHosts(ctx), config.getPrefs(ctx)));
        }
        ImageQualityFilterService serv = ImageQualityFilterService.getInstance().init(conDataList);
        System.out.println(serv.processMulticlassFilter("data/imatgeTransparencia.jpg").toString());
        System.out.println(serv.processBinaryFilter("data/imatgeTransparencia.jpg", 0.2).toString());
        System.out.println(serv.processBinaryFilter("data/imatgeTransparencia.jpg").toString());
    }
     
    
}

//-k ogtoledano__AT_SIGN__gmail.com_Cg1Z_M6G_public.pem -tm cub
