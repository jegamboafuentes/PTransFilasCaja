/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baz.scc.transfilascaja.main;

import com.baz.scc.commons.util.CjCRSpringContext;
import com.baz.scc.commons.util.CjCRUtils;
import com.baz.scc.transfilascaja.logic.CjCRTransFilasCajaLogic;
import java.text.ParseException;
import org.apache.log4j.Logger;

/**
 *
 * @author B938201
 */
public class CjCRBootstrap {
    
    private static final Logger LOG = Logger.getLogger(CjCRBootstrap.class);
    
    private CjCRBootstrap(){
    }
    
    public static void main(String[] args) {
        try {
            long begin = System.currentTimeMillis();
            CjCRSpringContext.init();
            LOG.info("----------------------------------------------------");
            LOG.info("Inicio Proceso de Transferencia Filas de Caja");
            
            //LOGICA DEL PROCESO
            CjCRTransFilasCajaLogic logicaFilas = CjCRSpringContext.getBean(CjCRTransFilasCajaLogic.class);
            logicaFilas.procesarFilas();
            
            LOG.info("Termino Proceso");
            long end = System.currentTimeMillis();
            LOG.info(CjCRUtils.concat("----- Proceso completo en [",CjCRUtils.formatElapsedTime(begin, end), "]"));
        } catch (ParseException ex) {
            LOG.error(String.format("Error en aplicaci\u00F3n - ", ex.getMessage()), ex);
        }
    }
}
