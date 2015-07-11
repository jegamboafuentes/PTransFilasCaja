//
package com.baz.scc.transfilascaja.logic;

import com.baz.scc.transfilascaja.dao.CjCRTransFilasCajaDao;
import com.baz.scc.transfilascaja.model.CjCRTransFilasCajaFilas;
import com.baz.scc.transfilascaja.support.CjCRPAppConfig;
import com.baz.scc.transfilascaja.support.CjCRPModoProceso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author Norberto Camacho Flores B938201
 */
@Component
public class CjCRTransFilasCajaLogic {

    @Autowired
    private CjCRTransFilasCajaDao transferFilasDao;
    @Autowired
    private CjCRPAppConfig appConfig;

    @Autowired
    private CjCRPModoProceso procesoModo;

    private static final Logger LOG = Logger.getLogger(CjCRTransFilasCajaLogic.class);
    private static final String MODO = "Modo: ";
    private static final String FECHA = "Fecha: ";
    private static final String LBLREPROCESO = "reproceso";
    private static final SimpleDateFormat FORMATOSEMANA = new SimpleDateFormat("ww");
    private static final SimpleDateFormat FORMATOFECHA = new SimpleDateFormat("yyyyww");
    private static final SimpleDateFormat FORMATOANIO = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat FORMATOMES = new SimpleDateFormat("MM");
    private static final int AUTOMATICO = 1;
    private static final int REPROCESO = 2;
    private static final int AUTOMATICOPRUEBAS = 3;

    private List<CjCRTransFilasCajaFilas> obtenerFilas(String semana) {
        LOG.info("Inicio - Consulta filas de caja AS400 semana" + semana);
        Integer idPais = appConfig.getProcesoIDPais();
        List<CjCRTransFilasCajaFilas> filas = new ArrayList<CjCRTransFilasCajaFilas>();

        filas = transferFilasDao.getFilasOperacion(semana, idPais);

        LOG.info("Final - Consulta filas de caja AS400");
        LOG.info("Total Filas obtenidas: " + filas.size());

        return filas;
    }

    private void insertarFilas() throws ParseException {

        List<CjCRTransFilasCajaFilas> filasAutomatico = new ArrayList<CjCRTransFilasCajaFilas>();

        String fInicio = appConfig.getProcesoFechaInicio();
        String fFinal = appConfig.getProcesoFechaFin();
        Date semanaInicio;
        Date semanaFin;
        int modo;
        String week;
        String month;
        String year;
        String cadena;
        String semanaProcesada;
        String semanaProc;

        modo = procesoModo.getModo();

        switch (modo) {
            case AUTOMATICO:

                LOG.info(MODO + appConfig.getProcesoModo() + FECHA + " " + getSemanaActual());
                semanaProcesada = " = " + getSemanaActual();

                filasAutomatico = obtenerFilas(semanaProcesada);

                if (filasAutomatico.isEmpty()) {
                    LOG.info("No hay registros de la semana pasada para insertar en Oracle");
                } else {

                    transferFilasDao.clearFilas(semanaProcesada);

                    partirLista(filasAutomatico);
                }
                break;

            case REPROCESO:

                LOG.info(MODO + appConfig.getProcesoModo() + " Entre fechas: "
                        + appConfig.getProcesoFechaInicio() + " - " + appConfig.getProcesoFechaFin());

                Calendar calendarInicio = getCalendarBAZ();
                Calendar calendarFin = getCalendarBAZ();

                FORMATOFECHA.setCalendar(getCalendarBAZ());
                FORMATOSEMANA.setCalendar(getCalendarBAZ());

                calendarInicio.setTime(FORMATOFECHA.parse(fInicio));

                calendarFin.setTime(FORMATOFECHA.parse(fFinal));
                calendarFin.add(Calendar.WEEK_OF_YEAR, 1);

                semanaInicio = calendarInicio.getTime();
                semanaFin = calendarFin.getTime();
                Calendar calendarSemanaActual = getCalendarBAZ();

                while (semanaInicio.before(semanaFin)) {

                    List<CjCRTransFilasCajaFilas> filasReproceso;

                    calendarSemanaActual.setTime(semanaInicio);

                    week = FORMATOSEMANA.format(semanaInicio);
                    month = FORMATOMES.format(semanaInicio);
                    year = FORMATOANIO.format(semanaInicio);

                    if (("01".equals(week)) && ("12".equals(month))) {
                        Date calendarioSemana = getCalendarioActual(year, week);
                        semanaProcesada = FORMATOFECHA.format(calendarioSemana);

                    } else {
                        semanaProcesada = FORMATOFECHA.format(semanaInicio);
                    }

                    semanaProc = " = " + semanaProcesada;

                    filasReproceso = obtenerFilas(semanaProc);

                    if (filasReproceso.isEmpty()) {
                        LOG.info("No hay registros de la semana: " + semanaProcesada + " para insertar en Oracle");
                    } else {

                        transferFilasDao.clearFilas(semanaProc);
                        partirLista(filasReproceso);
                    }

                    calendarInicio.add(Calendar.WEEK_OF_MONTH, 1);
                    semanaInicio = calendarInicio.getTime();

                }

                break;

            case AUTOMATICOPRUEBAS:
                LOG.info(MODO + appConfig.getProcesoModo() + FECHA + "201342");
                cadena = " = 201342";

                filasAutomatico = obtenerFilas(fInicio);

                if (filasAutomatico.isEmpty()) {
                    LOG.info("No hay registros para insertar en Oracle");
                } else {

                    transferFilasDao.clearFilas(cadena);

                    partirLista(filasAutomatico);
                }

                break;

            default:
        }
    }

    /*Divide la lista en bloques segun la cantidad de datos especificado */
    private void partirLista(List<CjCRTransFilasCajaFilas> filas) {
        int bloque = appConfig.getCantidadDatosOracle();
        int numeroBloques = 0;
        List<CjCRTransFilasCajaFilas> bloqueRealizado = new ArrayList<CjCRTransFilasCajaFilas>();

        if (bloque == 0) {
            numeroBloques = 1;
        } else if (bloque > filas.size()) {
            numeroBloques = 1;
            bloque = filas.size();
        } else {
            numeroBloques = filas.size() / bloque;
            if (filas.size() % bloque > 0) {
                numeroBloques += 1;
            }
        }

        LOG.info("Insertar datos a Oracle");
        for (int i = 0; i < numeroBloques; i++) {
            if (i == numeroBloques - 1) {
                bloqueRealizado = filas.subList(i * bloque, filas.size());
            } else {
                bloqueRealizado = filas.subList(i * bloque, i * bloque + bloque);
            }

            transferFilasDao.setFilasOperacion(bloqueRealizado, bloqueRealizado.size());
        }
    }

     //Obtiene la semana actual - 1
    private String getSemanaActual() {      
        DateTime semanas = new DateTime().minusWeeks(1);
        DateTimeFormatter formatoFecha = DateTimeFormat.forPattern("xxxxww");
       
        String valida = (formatoFecha.print(semanas));                
        
        return valida;
    }

    /*Valida que la fecha final no sea menor que la fecha inicial*/
    private boolean validarFechas() {
        int inicio = appConfig.getProcesoFechaInicio().length();
        int fin = appConfig.getProcesoFechaFin().length();
        boolean valida = true;

        if (LBLREPROCESO.equals(appConfig.getProcesoModo())) {
            if (inicio == 6 && fin == 6) {
                int fechaInicio = Integer.parseInt(appConfig.getProcesoFechaInicio());
                int fechaFinal = Integer.parseInt(appConfig.getProcesoFechaFin());
                if (fechaFinal < fechaInicio) {
                    valida = false;
                    LOG.warn("El orden de las fechas es incorrecto");
                }
            } else {
                LOG.warn("Formato de fecha inválido. Verificar: " + appConfig.getProcesoFechaInicio()
                        + "-" + appConfig.getProcesoFechaFin());
                valida = false;
            }
        }
        return valida;
    }

    private Calendar getCalendarBAZ() {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        return c;
    }

    public Date getCalendarioActual(String year, String week) {
        Calendar calendario = getCalendarBAZ();
        calendario.set(Calendar.YEAR, Integer.parseInt(year) + 1);
        calendario.set(Calendar.MONTH, Calendar.JANUARY);
        calendario.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(week));
        calendario.set(Calendar.DAY_OF_MONTH, 1);
        return calendario.getTime();
    }

    public void procesarFilas() throws ParseException {
        if (validarFechas()) {
            insertarFilas();
        }
    }

}
