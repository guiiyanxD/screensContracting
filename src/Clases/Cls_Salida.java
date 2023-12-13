package Clases;

import Conexion.Conectar;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class Cls_Salida {
    public String nrofactura;
    public String prodCod;
    public String cantidad;
    public String fecha;
    private PreparedStatement PS;
    private ResultSet RS;
    private final Conectar CN;
    private DefaultTableModel DT;
    private final String SQL_INSERT_SALIDA = "INSERT INTO salida (sal_factura, "
            + "sal_pro_codigo, sal_fecha, sal_cantidad, sal_orden_id) "
            + "values (?,?,?,?,?)";
    private final String SQL_SELECT_SALIDA = "SELECT sal_factura, sal_fecha, "
            + "sal_pro_codigo, pro_descripcion, sal_cantidad FROM salida "
            + "INNER JOIN producto ON sal_pro_codigo = pro_codigo";
    
    public Cls_Salida(){
        PS = null;
        CN = new Conectar();
    }
    
    public Cls_Salida(String nrofactura, String prodCod, String fecha ,String cantidad  ){
        this.nrofactura = nrofactura;
        this.prodCod = prodCod;
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.CN = null;
    }
    
    public void setNroFactura(String nrofactura){
        this.nrofactura = nrofactura;
    }
    
    public void setProdCod(String prodcod){
        this.prodCod = prodcod;
    }
    
    public void setCantidad(String cantidad){
        this.cantidad = cantidad;
    }
    
    public void setFecha(String fecha){
        this.fecha = fecha;
    }
    
    public String getNroFactura(){
        return this.nrofactura;
    }
    
    public String getProdCod(){
        return this.prodCod;
    }
    
    public String getCantidad(){
        return this.cantidad;
    }
    
    public String getFecha(){
        return this.fecha;
    }
    
    public DefaultTableModel setTitulosSalida(){
        DT = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
        };
        DT.addColumn("N° de Factura");
        DT.addColumn("Fecha");
        DT.addColumn("Código de Producto");
        DT.addColumn("Descripción");
        DT.addColumn("Cantidad");
        DT.addColumn("OrdenId");
        return DT;
    }
    
    public DefaultTableModel getDatosSalida(){
        try {
            setTitulosSalida();
            PS = CN.getConnection().prepareStatement(SQL_SELECT_SALIDA);
            RS = PS.executeQuery();
            Object[] fila = new Object[5];
            while(RS.next()){
                fila[0] = RS.getString(1);
                fila[1] = RS.getDate(2);
                fila[2] = RS.getString(3);
                fila[3] = RS.getString(4);
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
    
    public int registrarSalida(String nfactura, String codigo, Date fecha, int cantidad, int orden_id){
        int res=0;
        try {
            PS = CN.getConnection().prepareStatement(SQL_INSERT_SALIDA);
            PS.setString(1, nfactura);
            PS.setString(2, codigo);
            PS.setDate(3, fecha);
            PS.setInt(4, cantidad);
            PS.setInt(5, orden_id);
            res = PS.executeUpdate();
            if(res > 0){
                JOptionPane.showMessageDialog(null, "Salida realizada con éxito.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "No se pudo registrar la salida.");
            System.err.println("Error al registrar la salida." +e.getMessage());
        } finally{
            PS = null;
            CN.desconectar();
        }
        return res;
    }
    
    public int verificarStock(String codigo){
        int res=0;
        try {
            PS = CN.getConnection().prepareStatement("SELECT inv_stock from inventario where inv_pro_codigo='"+codigo+"'");
            RS = PS.executeQuery();
            
            while(RS.next()){
                res = RS.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al devolver cantidad de registros." +e.getMessage());
        } finally{
            PS = null;
            CN.desconectar();
        }
        return res;
    }
    
    /**
     * Verifica que el numero de factura ingresado no haya sido registrado
     * anteriormente;
     * @param nfactura
     * @return 
     */
    public boolean verificarNroFacturaUnico(int nfactura){
       int res=-1;
       boolean existe = true;
       try  {
           PS = CN.getConnection().prepareCall("SELECT count(sal_factura) FROM salida where sal_factura='"+nfactura+"'");
           RS = PS.executeQuery();
           while(RS.next()){
                res = RS.getInt(1);
                if (res == 0){
                    existe = false;
                }
            }
           
       } catch(SQLException e){
           System.out.println("Error al verificar unicidad del numero de factura en salida");
       }
       
        return existe; 
    }
    
    public void actualizarSalida(Object[] salida){
        try  {
            
//            String query = "UPDATE salida SET "
//                + "sal_pro_codigo = '"+salida[1]+"', "
//                + "sal_cantidad = "+ Integer.valueOf(salida[3].toString()) +", "
//                + "sal_fecha = '"+salida[2]+"' "
//                + "WHERE sal_factura = '"+salida[0] +"'";
            String query= "UPDATE salida SET sal_fecha = ?, sal_pro_codigo = ?, sal_cantidad = ? where sal_factura = ?";
            PS = CN.getConnection().prepareCall(query);
            PS.setString(2, salida[1].toString()); //codigo producto
            PS.setInt(3, Integer.parseInt(salida[3].toString())); //cantidad
            PS.setDate(1, (Date) salida[2]); //Fecha
            PS.setString(4, salida[0].toString());

            int response = PS.executeUpdate();
            System.out.println(response);
       } catch(SQLException e){
           System.out.println("Error al actualizar salida"+ e.getMessage());
       }
    }
}
