/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client.exceptions;

import org.json.simple.JSONObject;

/**
 *
 * @author josep
 */
public class PapiCliException  extends Exception{
    private int errorcode;

    public PapiCliException(int errorcode) {
        this.errorcode = errorcode;
    }

    public PapiCliException(int errorcode, String message) {
        super(message);
        this.errorcode = errorcode;
    }

    public PapiCliException(int errorcode, Throwable cause) {
        super(cause);
        this.errorcode = errorcode;
    }

    public PapiCliException(int errorcode, String message, Throwable cause) {
        super(message, cause);
        this.errorcode = errorcode;
    }

    /**
     * @return the errorcode
     */
    public int getErrorcode() {
        return errorcode;
    }
    
    public String getJsonFormat(){
        return String.format("{\"error\":true, \"status_code\":%d, \"message\":\"%s\"}", errorcode, getMessage());
    }
}
