/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client.exceptions;

/**
 *
 * @author josep
 */
public class RuntimePapiCliException extends RuntimeException{
    int code;

    public RuntimePapiCliException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RuntimePapiCliException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public RuntimePapiCliException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }
}
