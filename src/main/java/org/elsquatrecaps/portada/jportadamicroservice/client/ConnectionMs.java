/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author josep
 */
public class ConnectionMs {
    private String port;
    private String host;
    private String pref;

    public ConnectionMs(String ports, String hosts, String prefs) {
        this.port = ports;
        this.host = hosts;
        this.setPref(prefs);
    }

    public ConnectionMs() {
    }

    
    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the pref
     */
    public String getPref() {
        return pref;
    }

    /**
     * @param pref the pref to set
     */
    public void setPref(String pref) {
        if(pref==null || pref.isEmpty() || pref.isBlank()){
            pref="/";
        }else{
            if(!pref.startsWith("/")){
                pref="/".concat(pref);
            }
            if(!pref.endsWith("/")){
                pref=pref.concat("/");
            }
        }
        this.pref = pref;
    }
}
