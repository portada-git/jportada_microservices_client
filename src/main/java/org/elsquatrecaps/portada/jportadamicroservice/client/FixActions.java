/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client;

/**
 *
 * @author josep
 */
public enum FixActions {
    FIX_TANSPARENCY(1),
    FIX_SKEW(2),
    FIX_WARP(4);

    public int getId() {
        return id;
    }
    
    private FixActions(int v){
        id = v;
    }
    
    public static int getActions(boolean fixTransparency, boolean fixSkew, boolean fixWarp){
        int ret=0;
        if(fixTransparency){
            ret += FIX_TANSPARENCY.getId();
        }
        if(fixSkew){
            ret += FIX_SKEW.getId();
        }
        if(fixWarp){
            ret += FIX_WARP.getId();
        }
        return ret;
    }
    
    public static int getActions(FixActions ... ac){
        int ret = 0;
        for(FixActions i: ac){
            ret +=i.getId();
        }
        return ret;
    }
    
    public static boolean isActionIn(FixActions action, int actions){
        return (actions & action.getId()) > 0;
    }
    
    private int id;
}
