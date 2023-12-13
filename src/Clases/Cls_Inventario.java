package Clases;

import Conexion.Conectar;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class Cls_Inventario {
    private PreparedStatement PS;
    private ResultSet RS;
    private final Conectar CN;
    private DefaultTableModel DT;
    private final String SQL_SELECT_INVENTARIO = "SELECT inv_pro_codigo, pro_descripcion, inv_entradas, inv_salidas, inv_stock FROM inventario INNER JOIN producto ON inv_pro_codigo = pro_codigo";
    
    public Cls_Inventario(){
        PS = null;
        CN = new Conectar();
    }
    
    private DefaultTableModel setTitulosInventario(){
        DT = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
        };
        DT.addColumn("Código");
        DT.addColumn("Descripción");
        DT.addColumn("Entrada");
        DT.addColumn("Salida");
        DT.addColumn("Stock");
        return DT;
    }
    
    public DefaultTableModel getDatosInventario(){
        try {
            setTitulosInventario();
            PS = CN.getConnection().prepareStatement(SQL_SELECT_INVENTARIO);
            RS = PS.executeQuery();
            Object[] fila = new Object[5];
            while(RS.next()){
                fila[0] = RS.getString(1);
                fila[1] = RS.getString(2);
                fila[2] = RS.getInt(3);
                fila[3] = RS.getInt(4);
                fila[4] = RS.getInt(5);
                DT.addRow(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar los datos."+e.getMessage());
        } finally{
            PS = null;
            RS = null;
            CN.desconectar();
        }
        return DT;
    }
    
    public Object[] getInventarioxProducto(String inv_pro_codigo){
        // []
        //[0] = inv_entrada
        //[1] = inv_salida
        //[2] = inv_stock
        String query = "SELECT * from inventario where inv_pro_codigo = ?";
        int inventarioResult = 0;
        Object[] inventarioXproducto = new Object[3];
        try {
            PS = CN.getConnection().prepareStatement(query);
            PS.setString(1, inv_pro_codigo);
            RS = PS.executeQuery();
            if( RS.next() ){
                inventarioXproducto[0] = RS.getInt(2);
                inventarioXproducto[1] = RS.getInt(3);
                inventarioXproducto[2] = RS.getInt(4);

            }

        } catch (SQLException ex) {
            Logger.getLogger(Cls_Inventario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return inventarioXproducto;
    }
    
    public void ActualizarInventario(String inv_pro_codigo, int cantidad, int nuevo_inv_salida){
        try {
            String query= "UPDATE inventario SET inv_stock = ?, inv_salidas = ? where inv_pro_codigo = ?";
            PS = CN.getConnection().prepareCall(query);
            PS.setInt(1, cantidad ); //cantidad
            PS.setInt(2, nuevo_inv_salida ); //cantidad

            PS.setString(3, inv_pro_codigo);
            PS.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Cls_Entrada.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
