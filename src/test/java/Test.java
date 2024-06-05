
import org.elsquatrecaps.portada.jportadamicroservice.client.JPortadaMicroservice;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author josep
 */
public class Test {
    
    public static void main(String[] args) {
        JPortadaMicroservice prg = new JPortadaMicroservice();
        String[] args2 = {"ocrAll", "-i", "corregidas", "-o", "ocr2", "-tm", "bcn"};
        prg.run(args2);
    }
     
    
}
