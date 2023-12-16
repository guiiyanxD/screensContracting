/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

import Conexion.Conectar;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author willi
 */
public class Cls_OrdenTrabajo {
    private Cls_Salida clsSalida;
    private PreparedStatement PS;
    private ResultSet RS;
    private final Conectar CN;
    private DefaultTableModel DT;
    private final String SQL_SELECT_ORDEN_TRABAJO = "SELECT * from "
            + "orden_de_trabajo";
    private final String SQL_INSERT_ORDEN_TRABAJO = "INSERT INTO "
            + "orden_de_trabajo (nota) values (?)";
    
    public Cls_OrdenTrabajo(){
        clsSalida = new Cls_Salida();
        PS = null;
        CN = new Conectar();
    }
    
    private DefaultTableModel setTitutloOrdenTrabajo(){
        DT = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int colum){
                return false;
            }
        };
        DT.addColumn("Nro. Orden de Trabajo");
        DT.addColumn("Nota");
        
        return DT;
    }
    
    public DefaultTableModel showOrdenTrabajo(String nroOrden){
        DefaultTableModel DTaux =  new DefaultTableModel(){
           @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            } 
        };
        DTaux.addColumn("N° de Factura");
        DTaux.addColumn("Código de Producto");
        DTaux.addColumn("Fecha");
        DTaux.addColumn("Cantidad");
//        DTaux.addColumn("OrdenId");
        try {
//            DTaux = clsSalida.setTitulosSalida();
            PS =CN.getConnection().prepareStatement("SELECT * FROM salida "
            + "WHERE sal_orden_id = '"+nroOrden+"'");
            RS = PS.executeQuery();
            Object[] fila = new Object[4];
            while(RS.next()){
//                fila[0] = RS.getString(1); //ID
                fila[0] = RS.getString(2);//NRO FACTURA
                fila[1] = RS.getString(3);//cod. prod
                fila[2] = RS.getDate(4); //fecha
                fila[3] = RS.getInt(5);//cantidad
//                fila[4] = RS.getInt(6);//ordenID

                DTaux.addRow(fila);                
            }
            return DTaux;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al "
                    + "listar la orden de trabajo");
        }finally{
            PS = null;
            RS = null;
            CN.desconectar();
        }
        return DTaux;
    }
    
    
    public DefaultTableModel getDatosOrdenDeTrabajo(){
        
        try{
            setTitutloOrdenTrabajo();
            PS = CN.getConnection().prepareStatement(SQL_SELECT_ORDEN_TRABAJO);
            RS = PS.executeQuery();
            Object[] fila = new Object[2];
            while(RS.next()){
                fila[0] = RS.getString(1);
                fila[1] = RS.getString(2);
                DT.addRow(fila);
            }
        }catch (SQLException e){
            System.out.println("Error al listar los datos. "+e.getMessage());
        }finally {
            PS = null; 
            RS = null;
            CN.desconectar();
        }
        return DT;
    }
    
    public int registrarOrdenDeTrabajo(String nota, ArrayList< Object[]>listaDeSalidas, String nroOrden){
        int res =0;
        
        try{
            PS = CN.getConnection().prepareStatement(SQL_INSERT_ORDEN_TRABAJO,
                    Statement.RETURN_GENERATED_KEYS);
            PS.setString(1, nota);
            res = PS.executeUpdate();
            if( res > 0){   
                try (ResultSet tuplaGenerada = PS.getGeneratedKeys()){
                    if( tuplaGenerada.next() ){
                        int idGenerado = tuplaGenerada.getInt(1);
                        for(Object[] lista : listaDeSalidas){
                            java.sql.Date fecha = (java.sql.Date)lista[2];
                            clsSalida.registrarSalida(lista[0].toString(),
                                    lista[1].toString(), fecha, 
                                    Integer.parseInt(lista[3].toString()),
                                    idGenerado);
                        }
                    }
                }
                JOptionPane.showMessageDialog(null, "Orden de "
                        + "trabajo realizada con éxito.");            
            }

            
        } catch (SQLException e){
            JOptionPane.showMessageDialog(null, "No se pudo "
                    + "registrar la Orden de trabajo");
            System.err.println("Erro al registrar la salida." + e.getMessage());
        }finally {
            PS = null;
            CN.desconectar();
        }
        return res;
    }
    
    
    public void actualizarOrdenDeSalida(Object[] salidadAntesActualizar, Object[] salidaDespuesActualizar) throws Exception{
        //[0]NroFactura
        //[1]CodigoProducto
        //[2]Fecha
        //[3]Cantidad
        if(salidadAntesActualizar == salidaDespuesActualizar){
            throw new Exception("No se ha modificado nada de la salida");

        }else{
            Cls_Entrada clsEntrada = new Cls_Entrada();
            Cls_Inventario clsInventario = new Cls_Inventario();

            int cantVieja = Integer.parseInt(salidadAntesActualizar[3].toString());
            int cantNueva = Integer.parseInt(salidaDespuesActualizar[3].toString());
            String prodViejo = salidadAntesActualizar[1].toString();
            String prodNuevo = salidaDespuesActualizar[1].toString();
            
            if( prodViejo.equals(prodNuevo)){
                //Se esta sacando mas/menos del mismo producto
                if(cantVieja > cantNueva){
                //Si la cantidad vieja es mayor a la nueva. Devuelvo Stock
                    //Obteniendo la cantidad de stock actual
                    Object[] cantInventarioActual = clsInventario.getInventarioxProducto(prodViejo);
                    
                    
                    int cantDevolver = cantVieja - cantNueva;
                    //recalculo la cantidad de salidas, retandole la cant a devolver
                    
                    int nuevaCantSalida =  Integer.parseInt(cantInventarioActual[1].toString()) - cantDevolver ;
                    
                    cantDevolver = Integer.parseInt(cantInventarioActual[2].toString()) + cantDevolver;
                    

                    //Devolviendo Stock
                    clsInventario.ActualizarInventario(prodViejo, cantDevolver, nuevaCantSalida);
                    //Registrando cambios en la salida en cuestion
                    clsSalida.actualizarSalida(salidaDespuesActualizar);
                }else{
                    //Si la cantidad vieja es mayor a la nueva. Retiro mas Stock
                    int cantExtra = cantNueva - cantVieja;
                    int stock = clsSalida.verificarStock(prodViejo);
                    if(stock >=  cantExtra){
                        clsSalida.actualizarSalida(salidaDespuesActualizar);
                    }else{
                        throw new Exception("No hay suficiente stock para actualizar el pedido");
                    }
                }
            }else{
                //Se busca cambiar el producto y por lo tanto se debe devolver Stock
                
                //Obtengo el stock del producto viejo para sumarlo con el que se esta devolviendo
                Object[] cantInventarioActual = clsInventario.getInventarioxProducto(prodViejo);
                
                //sumo la cantidad vieja con el stock del prducto viejo para obtener el nuevo stock real
                int cantDevolver = Integer.parseInt(cantInventarioActual[2].toString()) + cantVieja;
                
                
                //recalculo la cantidad de salidas, retandole la cantidad
                int nuevaCantSalida = Integer.parseInt(cantInventarioActual[1].toString()) - cantVieja;
                
                
                //Devuelvo todo el stock del anterior producto solicitado
                clsInventario.ActualizarInventario(prodViejo, cantDevolver, nuevaCantSalida);
                
                //verifico el stock de este nuevo producto
                int stock = clsSalida.verificarStock(prodNuevo);
                if(stock >= cantNueva){
                    clsSalida.actualizarSalida(salidaDespuesActualizar);
                    
                    Object[] cantInvetarioNuevoProducto =  clsInventario.getInventarioxProducto(prodNuevo);
                    
                    //actualizando el stock del nuevo prodcuto
                    int cantNuevoStock = Integer.parseInt(cantInvetarioNuevoProducto[2].toString()) - cantNueva;
                    
                    int cantNuevaSalida = Integer.parseInt(cantInvetarioNuevoProducto[1].toString()) + cantNueva;
                    
                    clsInventario.ActualizarInventario(prodNuevo, cantNuevoStock, cantNuevaSalida);

                }else{
                    throw new Exception("No hay suficiente stock del nuevo producto solicitado para actualizar el pedido");
                }
                
            }   
            
        }
    }


    public DefaultTableModel buscarOrdenDeTrabajo(String buscar){
        int contador = 0;
        
        DefaultTableModel DTaux =  new DefaultTableModel(){
           @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            } 
        };
        DTaux.addColumn("N° de Orden de Trabajo");
        DTaux.addColumn("Nota");
        
        
        try {
//            DTaux = clsSalida.setTitulosSalida();
            PS =CN.getConnection().prepareStatement("SELECT * FROM orden_de_trabajo "
            + "WHERE orden_id LIKE '%"+buscar+" %' OR nota LIKE '&"+buscar+"&'");
            RS = PS.executeQuery();
            Object[] fila = new Object[2];
            while(RS.next()){
//                fila[0] = RS.getString(1); //ID
                fila[0] = RS.getString(1);//Nro Orden de Trabajo 
                fila[1] = RS.getString(2);//Nota
                
                contador++;
                DTaux.addRow(fila);                
            }
            return DTaux;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al "
                    + "buscar la orden de trabajo");
        }finally{
            PS = null;
            RS = null;
            CN.desconectar();
        }
        return DTaux;
    }
}


