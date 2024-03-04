
import org.elsquatrecaps.portada.jportadamicroservice.client.PortadaApi;

/**
 *
 * @author josepcanellas
 */
public class JPortadaMicroservice {

    public static void main(String[] args) {
        //test client
        PortadaApi portadaApi = new PortadaApi();
        portadaApi.deskewImageFile("../../dades/wetransfer_transkribus-jpg-octubre-1853_2023-03-16_0907/transkribus_jpg_Octubre_1853/0025_1853_10_17_FIA_dl_10000032484_img10434285__Pagina4__DiarioDeBarcelonaAno185.jpg"
                , "../../dades/wetransfer_transkribus-jpg-octubre-1853_2023-03-16_0907/transkribus_jpg_Octubre_1853/0025_1853_10_17_FIA_dl_10000032484_img10434285__Pagina4__DiarioDeBarcelonaAno185_deskew.jpg");
    }
}
