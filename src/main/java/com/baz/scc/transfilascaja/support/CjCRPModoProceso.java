/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baz.scc.transfilascaja.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author B941337
 */
@Component("applicationTF")
public class CjCRPModoProceso {

    @Autowired
    private CjCRPAppConfig appConfig; 
  
    public Integer getModo() { 
        String procesoModo;
        Integer modo;
        
        procesoModo = appConfig.getProcesoModo();

        if (("automatico").equals(procesoModo)) {
            modo = 1;
        } else {
            if (("reproceso").equals(procesoModo)) {
                modo = 2;
            } else {
                modo = 3;
            }
        }
        
        return modo;
    }
}
