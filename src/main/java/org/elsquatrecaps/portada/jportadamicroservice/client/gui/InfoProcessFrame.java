/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client.gui;

/**
 *
 * @author josep
 */
public interface InfoProcessFrame {

    void setProgressBarPanelVisible(boolean v);

    void setVisible(boolean v);

    void updateList(String message, String process, double percent, boolean processOk);

    void updateProgress(String file, String process, double percent);

    void updateInfo(String message, String process);
    
    void updateErrorInfo(String message, String process, int errorcode);

    void updateStatusInfo(String status, String message, String process);
    
    void pack();
    
}
