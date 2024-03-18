package org.elsquatrecaps.portada.jportadamicroservice.client;

/**
 *
 * @author josepcanellas
 */
public class JPortadaMicroservice {

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.parseArgumentsAndConfigure(args);
        
        PortadaApi papi = new PortadaApi();
        papi.deskewImageFile(config.getInputFile(), config.getOutputFile(), "data/error.txt");
    }
}
