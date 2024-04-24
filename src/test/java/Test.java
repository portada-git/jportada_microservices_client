
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
        String[] args2 = {"fixAll", "-i", "corregidas", "-o", "corregidaw", "-w"};
        prg.run(args2);
    }
     
    
}
