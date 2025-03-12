/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client.exceptions;

import org.elsquatrecaps.portada.jportadamscaller.exceptions.PortadaMicroserviceCallException;

/**
 *
 * @author josep
 */
public class PapiCliException  extends PortadaMicroserviceCallException{

    public PapiCliException(int errorcode) {
        super(errorcode);
    }

    public PapiCliException(int errorcode, String message) {
        super(errorcode, message);
    }

    public PapiCliException(int errorcode, Throwable cause) {
        super(errorcode, cause);
    }

    public PapiCliException(int errorcode, String message, Throwable cause) {
        super(errorcode, message, cause);
    }
}
