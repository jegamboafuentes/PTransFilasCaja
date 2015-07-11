
package com.baz.scc.transfilascaja.model;

/**
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author Norberto Camacho Flores B938201
 */
public class CjCRTransFilasCajaFilas {
    private int paisId;
    private int canalId;
    private int sucursalId;
    private int fiSemana;
    private int fiTop;
    private int fiIdTipoDiv;
    private int opers;
    private double monto;

    public int getPaisId() {
        return paisId;
    }

    public void setPaisId(int paisId) {
        this.paisId = paisId;
    }

    public int getCanalId() {
        return canalId;
    }

    public void setCanalId(int canalId) {
        this.canalId = canalId;
    }

    public int getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(int sucursalId) {
        this.sucursalId = sucursalId;
    }

    public int getFiSemana() {
        return fiSemana;
    }

    public void setFiSemana(int fiSemana) {
        this.fiSemana = fiSemana;
    }

    public int getFiTop() {
        return fiTop;
    }

    public void setFiTop(int fiTop) {
        this.fiTop = fiTop;
    }

    public int getFiIdTipoDiv() {
        return fiIdTipoDiv;
    }

    public void setFiIdTipoDiv(int fiIdTipoDiv) {
        this.fiIdTipoDiv = fiIdTipoDiv;
    }

    public int getOpers() {
        return opers;
    }

    public void setOpers(int opers) {
        this.opers = opers;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    @Override
    public String toString() {
        return "CjCRTransFilasCajaFilas{" + "paisId=" + paisId + ", canalId=" + canalId + ", sucursalId=" + sucursalId + ", fiSemana=" + fiSemana + ", fiTop=" + fiTop + ", fiIdTipoDiv=" + fiIdTipoDiv + ", opers=" + opers + ", monto=" + monto + '}';
    }
    
    
}
