package com.baz.scc.transfilascaja.dao;

import com.baz.scc.commons.dao.CjCRPaisDao;
import com.baz.scc.commons.model.CjCRPais;
import com.baz.scc.commons.support.CjCRDaoConfig;
import com.baz.scc.transfilascaja.model.CjCRTransFilasCajaFilas;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.stereotype.Repository;

/**
 * <br><br>Copyright 2013 Banco Azteca. Todos los derechos reservados.
 *
 * @author Norberto Camacho Flores B938201
 */
@Repository
public class CjCRTransFilasCajaDao {

    private static final Logger LOG = Logger.getLogger(CjCRTransFilasCajaDao.class);
    private static final String CAJWSICTRANACUMHISWS = ".CAJWSICTRANACUMHISWS ";
    private static final String WHEREFISEMANA = " WHERE fisemana ";
    private static final String GROUPBY = " GROUP by fipais,ficanal,fisucursal,fisemana,fitop,fiidtipodiv";
    private static final String FILASOBTENIDAS = "Filas Obtenidas de AS400(";
    private int cantidadRegistros;

    @Autowired
    private CjCRDaoConfig daoConfig;

    //AS400
    @Autowired
    @Qualifier("as400JdbcTemplate")
    private JdbcTemplate as400JdbcTemplate;
    //Oracle
    @Autowired
    @Qualifier("usrcajaJdbcTemplate")
    private JdbcTemplate usrcajaJdbcTemplate;
    @Autowired
    private CjCRPaisDao paisDao;

    private Map<Integer, CjCRPais> mapPaises;

    /* Borra los registros de la semana indicada*/
    public void clearFilas(final String cadena) {
        LOG.info("Comenzando el proceso de limpieza");
        String sql = "delete from " + daoConfig.getObjectUser() + ".THCJFILFILAS where FISEMANAID " + cadena;

        try {
            usrcajaJdbcTemplate.execute(sql);
            LOG.info("Proceso de limpieza concluido");
        } catch (DataAccessException e) {
            LOG.error("Error en el proceso de limpieza", e);
        }

    }

    /* Inserta los datos obtenidos de getFilasOperacion*/
    public void setFilasOperacion(final List<CjCRTransFilasCajaFilas> filas, final int cantidadDatos) {
        String sql = "insert into " + daoConfig.getObjectUser() + ".THCJFILFILAS"
                + "(FIPAISID, FICANALID, FISUCURSALID, FISEMANAID, FITOPID, FIDIVISAID,FIOPERACIONES,FNMONTO)"
                + " values (?,?,?,?,?,?,?,?)";

        BatchPreparedStatementSetter batchPreparedStatemmentSetterObject = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                CjCRTransFilasCajaFilas fila = filas.get(i);
                dedicatedFilasOperacion(ps, fila);
            }

            @Override
            public int getBatchSize() {
                return cantidadDatos;
            }
        };

        try {
            usrcajaJdbcTemplate.batchUpdate(sql, batchPreparedStatemmentSetterObject);
            LOG.info("Se agregaron con exito: " + cantidadRegistros);
        } catch (DataAccessException e) {
            LOG.error("Error en la insercion masiva", e);
        }

    }

    public void dedicatedFilasOperacion(PreparedStatement ps, CjCRTransFilasCajaFilas fila) {
        try {
            ps.setInt(1, fila.getPaisId());
            ps.setInt(2, fila.getCanalId());
            ps.setInt(3, fila.getSucursalId());
            ps.setInt(4, fila.getFiSemana());
            ps.setInt(5, fila.getFiTop());
            ps.setInt(6, fila.getFiIdTipoDiv());
            ps.setInt(7, fila.getOpers());
            ps.setDouble(8, fila.getMonto());
            cantidadRegistros++;
        } catch (SQLException e) {
            LOG.error("Error al insertar Registro:  " + fila, e);
        }
    }

    /*Obtiene los datos de construirSql*/
    public List<CjCRTransFilasCajaFilas> getFilasOperacion(String semana, Integer idPais) {
        List<CjCRTransFilasCajaFilas> filas = new ArrayList<CjCRTransFilasCajaFilas>();
        LOG.info("Consultando AS400");
        filas = construirSql(semana, idPais);

        return filas;
    }

    class FilasMapper implements RowMapper<CjCRTransFilasCajaFilas> {

        @Override
        public CjCRTransFilasCajaFilas mapRow(ResultSet rs, int i) throws SQLException {
            CjCRTransFilasCajaFilas filaAct = new CjCRTransFilasCajaFilas();

            filaAct.setPaisId(rs.getInt(1));
            filaAct.setCanalId(rs.getInt(2));
            filaAct.setSucursalId(rs.getInt(3));
            filaAct.setFiSemana(rs.getInt(4));
            filaAct.setFiTop(rs.getInt(5));
            filaAct.setFiIdTipoDiv(rs.getInt(6));
            filaAct.setOpers(rs.getInt(7));
            filaAct.setMonto(rs.getDouble(8));

            return filaAct;
        }
    }

    /*obtiene los registros de uno o todos los paises*/
    private List<CjCRTransFilasCajaFilas> construirSql(String fechas, Integer idPais) {
        List<CjCRTransFilasCajaFilas> listaFilas = new ArrayList<CjCRTransFilasCajaFilas>();
        String sqlBase = "SELECT fipais,ficanal,fisucursal,fisemana,fitop,fiidtipodiv,sum(finoope) "
                + "opers,sum(fnimporte) monto FROM ";

        //Procesar país en específico
        if (idPais != null) {
            StringBuilder statementPais = new StringBuilder();
            CjCRPais objPais = new CjCRPais();
            String queryAllPaises;
            objPais = paisDao.getPais(idPais);
            LOG.info("Procesar pais: " + objPais.getPaisDesc());
            statementPais.append(sqlBase);
            statementPais.append(objPais.getBibBanco());
            statementPais.append(CAJWSICTRANACUMHISWS);
            statementPais.append(WHEREFISEMANA);
            statementPais.append(fechas);
            statementPais.append(GROUPBY);

            queryAllPaises = statementPais.toString();
            listaFilas = as400JdbcTemplate.query(queryAllPaises, new FilasMapper(), (Object[]) null);
            LOG.info(FILASOBTENIDAS + objPais.getPaisDesc() + "): " + listaFilas.size());
            //Procesar todos los paises 
        } else {
            LOG.info("Procesar todos los paises");
            mapPaises = paisDao.getPaises();
            Iterator<CjCRPais> it = mapPaises.values().iterator();

            while (it.hasNext()) {
                String queryPaises;
                StringBuilder statementPaises = new StringBuilder();
                List<CjCRTransFilasCajaFilas> temp = new ArrayList<CjCRTransFilasCajaFilas>();

                CjCRPais pais = it.next();

                statementPaises.append(sqlBase);
                statementPaises.append(pais.getBibBanco());
                statementPaises.append(CAJWSICTRANACUMHISWS);
                statementPaises.append(WHEREFISEMANA);
                statementPaises.append(fechas);
                statementPaises.append(GROUPBY);

                queryPaises = statementPaises.toString();

                temp = as400JdbcTemplate.query(queryPaises, new FilasMapper(), (Object[]) null);
                LOG.info(FILASOBTENIDAS + pais.getPaisDesc() + "): " + temp.size());
                listaFilas.addAll(temp);
            }
        }
        return listaFilas;
    }
}
