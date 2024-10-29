
import org.elsquatrecaps.portada.jportadamicroservice.client.JPortadaMicroservice;

/**
 *
 * @author josep
 */
public class Test {
    
    public static void main(String[] args) {
        JPortadaMicroservice prg = new JPortadaMicroservice();
//        String[] args2 = {"requestAccess", "-tm", "bcn", "-m", "josep.canellas@gmail.com", "-f"};
//        String[] args2 = {"verifyCode", "-tm", "bcn", "-m", "josep.canellas@gmail.com", "-c", "dPPvzUcB"};
//        String[] args2 = {"ocrAll", "-tm", "bcn", "-i", "corregides2", "-o", "ocr4"};
        String[] args2 = {"reorderAll", "-tm", "bcn", "-i", "corregides2", "-o", "ordered"};
//        String[] args2 = {"pytest", "-tm", "bcn"};
//        String[] args2 = {"jtest", "-tm", "bcn"};
//        String[] args2 = {"ocrJsonAll", "-tm", "bcn", "-i", "corregides2", "-o", "ocr_json"};
//        String[] args2 = {"accept_key", "-tm", "bcn", "-k", "ogtoledano__AT_SIGN__gmail.com_Cg1Z_M6G_public.pem", "-u", "??", "-p", "???"};
        prg.run(args2);
    }
     
    
}

//-k ogtoledano__AT_SIGN__gmail.com_Cg1Z_M6G_public.pem -tm cub
