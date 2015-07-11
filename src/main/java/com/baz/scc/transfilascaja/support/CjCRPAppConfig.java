
package com.baz.scc.transfilascaja.support;

/**
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author Norberto Camacho Flores B938201
 */
public class CjCRPAppConfig {
    private String procesoModo;
    private String procesoFechaInicio;
    private String procesoFechaFin;
    private Integer cantidadDatosOracle;
    private Integer procesoIDPais;

    public Integer getProcesoIDPais() {
        return procesoIDPais;
    }

    public void setProcesoIDPais(Integer procesoIDPais) {
        this.procesoIDPais = procesoIDPais;
    }

    public String getProcesoModo() {
        return procesoModo;
    }

    public void setProcesoModo(String procesoModo) {
        this.procesoModo = procesoModo;
    }

    public String getProcesoFechaInicio() {
        return procesoFechaInicio;
    }

    public void setProcesoFechaInicio(String procesoFechaInicio) {
        this.procesoFechaInicio = procesoFechaInicio;
    }

    public String getProcesoFechaFin() {
        return procesoFechaFin;
    }

    public void setProcesoFechaFin(String procesoFechaFin) {
        this.procesoFechaFin = procesoFechaFin;
    }

    public Integer getCantidadDatosOracle() {
        return cantidadDatosOracle;
    }

    public void setCantidadDatosOracle(Integer cantidadDatosOracle) {
        this.cantidadDatosOracle = cantidadDatosOracle;
    }    
    
    
}
