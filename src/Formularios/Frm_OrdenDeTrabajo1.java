package Formularios;

import Clases.Cls_OrdenTrabajo;
import Clases.Cls_Salida;
//import Clases.Cls_Salida;
import static Formularios.Frm_Principal.contenedor;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class Frm_OrdenDeTrabajo1 extends javax.swing.JInternalFrame {

    private final Cls_OrdenTrabajo clsOrdenTrabajo;
    private Cls_Salida clsSalida;
    TableColumnModel columnModel;
    DefaultTableModel tableModel;
    public static int enviar = 0;
    int num = 0;
    int filaSeleccionada = -1;
    String nroOrden = " ";
    ArrayList<Object[]> listaDeSalidas = new ArrayList<Object[]>();


    public Frm_OrdenDeTrabajo1() {
        initComponents();
        clsOrdenTrabajo = new Cls_OrdenTrabajo();
        clsSalida = new Cls_Salida();

        tableModel = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            } 
        };
        jtb_lista_salidas.setModel(tableModel);
//        jtb_lista_salidas.setEnabled(false);
//        tableModel.addColumn("#");

        tableModel.addColumn("Nro. Factura");
        tableModel.addColumn("Cod. Producto");
        tableModel.addColumn("Fecha");
        tableModel.addColumn("Cantidad");
        tableModel.addColumn("Opciones");


        iniciar();
    }


    private void iniciar() {
        txt_nfactura.setEnabled(false);
        txt_cantidad.setEnabled(false);
        jdc_fecha.setEnabled(false);
        jbt_buscar.setEnabled(false);
        btn_agregar_salida.setEnabled(false);
        lbl_editando.setVisible(false);
        btn_actualizar_salida.setVisible(false);

    }

    private void activar() {
        txt_nfactura.setEnabled(true);
        txt_cantidad.setEnabled(true);
        jdc_fecha.setEnabled(true);
        jbt_buscar.setEnabled(true);
        btn_agregar_salida.setEnabled(true);
        txt_nfactura.requestFocus();
        btn_agregar_salida.setEnabled(true);
        lbl_editando.setVisible(false);
        btn_actualizar_salida.setVisible(false);

    }

    private void limpiar() {
        txt_nfactura.setText("");
        txt_codigo.setText("");
        txt_descripcion.setText("");
        txt_cantidad.setText("");
        jdc_fecha.setDate(null);
        txt_nfactura.requestFocus();
        jtb_lista_salidas.clearSelection();
        lbl_editando.setText("");
    }

    public void editarOrden(ArrayList<Object[]>orden, String nroOrden){        
        limpiar();
        activar();
        this.nroOrden = nroOrden;
        btn_nueva_orden.setEnabled(false);
        btn_guardar_orden.setEnabled(false);
        lbl_editando.setText("Editando Nro Orden: "+" "+nroOrden);
        lbl_editando.setVisible(true);
//        btn_guardar_orden.setVisible(false);
        btn_actualizar_salida.setVisible(true);
        cargarSalidasParaEditar(orden);
    }
    
    private void cargarSalidasParaEditar(ArrayList<Object[]>orden){
        int longitud = orden.size();
        for(int i = 0; i < longitud; i++){
            tableModel.addRow(orden.get(i));
            listaDeSalidas.add(orden.get(i));
        }
    }
    
    
    /**
    * Si el numero de fatura no existe en la BD, entonces recien
    * verifica:
    *  - si la lista de orden de salida esta vacia, 
    * agrega sin preguntar si existe ese nro de factura en la orden
    * de salida que se esta creando
    *  - Si la ya tiene contenido, entonces se pregunta si 
    * no existe en la orden de salida que se esta creando
    */
    private void agregarSalida() throws Exception {
        //Copia de la lista de salidas
        ArrayList<Object[]> lista = listaDeSalidas;
        
        String nfac = txt_nfactura.getText();
        String codigo = txt_codigo.getText();
        int cantidad = Integer.parseInt(txt_cantidad.getText());
        
        Date fechaa = jdc_fecha.getDate();
        long d = fechaa.getTime();
        java.sql.Date fecha_sql = new java.sql.Date(d);
        
        boolean existeNroFacturaEnBD = false;
        
        int stock = clsSalida.verificarStock(codigo);
        //Si existe stock
        if (cantidad < stock) {
            Object[] fila = {nfac, codigo, fecha_sql, cantidad};
            try{
                existeNroFacturaEnBD = existeNroFacturaEnBD(fila); //Verifico que la lista de salidas no exista el numero de factura
                try {
                    if(!existeNroFacturaEnBD){
                        if(!lista.isEmpty()){
                            boolean existeNroFacturaEnListaSalidas = existeNroFacturaEnListaSalidas(lista, fila);
                            if(!existeNroFacturaEnListaSalidas){
                                lista.add(fila);
                                tableModel.addRow(fila);
                                if(!this.nroOrden.equals(" ")){
                                    clsSalida.registrarSalida(fila[0].toString(), fila[1].toString(), fecha_sql,Integer.parseInt(fila[3].toString()), Integer.parseInt(this.nroOrden));
                                    this.nroOrden = " ";
                                }
                                
                            }
                        }else{
                                lista.add(fila);
                                tableModel.addRow(fila);
                        }
                    }
                } catch(Exception e){
                    JOptionPane.showMessageDialog(null, "Ya existe"
                            + " ese numero de factura en la orden de trabajo. "
                            + "Verifique, por favor");
                }
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Ya existe"
                        + " ese numero de factura en la Base de datos. "
                        + "Verifique, por favor");
            }
            
        }
        else{
            JOptionPane.showMessageDialog(null, "¡No hay stock"
                    + " suficiente!");
            txt_cantidad.setText("");
            txt_cantidad.requestFocus();
        }
        listaDeSalidas = lista;
        limpiar();
    }
    
    private boolean existeNroFacturaEnListaSalidas(ArrayList<Object[]> salida, Object[] fila) throws Exception{
        for (Object[] lista : salida){
            if(lista[0].equals(fila[0])){
                throw new Exception();
            }
        }
        return false;
    }
    
    private boolean existeNroFacturaEnBD (Object[] fila) throws Exception{
        boolean existeEnBD = clsSalida.verificarNroFacturaUnico(Integer.parseInt(fila[0].toString()));
        if(existeEnBD){
            throw new Exception();
        }
        return existeEnBD;
    }

    private void registrarOrdenDeTrabajo(){
        String nota = txt_nota.getText();
        clsOrdenTrabajo.registrarOrdenDeTrabajo(nota, listaDeSalidas, this.nroOrden);
        this.nroOrden = " ";
        limpiar();
        tableModel.setRowCount(0);
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lbl_titulo_orden_trabajo = new javax.swing.JLabel();
        lbl_subt_orden_trabajo = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lbl_nfactura = new javax.swing.JLabel();
        txt_nfactura = new javax.swing.JTextField();
        lbl_fecha = new javax.swing.JLabel();
        jdc_fecha = new com.toedter.calendar.JDateChooser();
        lbl_codigo = new javax.swing.JLabel();
        txt_codigo = new javax.swing.JTextField();
        jbt_buscar = new javax.swing.JButton();
        lbl_descripcion = new javax.swing.JLabel();
        txt_descripcion = new javax.swing.JTextField();
        lbl_cantidad = new javax.swing.JLabel();
        txt_cantidad = new javax.swing.JTextField();
        btn_agregar_salida = new javax.swing.JButton();
        lbl_salida = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtb_lista_salidas = new javax.swing.JTable();
        lbl_editando = new javax.swing.JLabel();
        btn_actualizar_salida = new javax.swing.JButton();
        btn_nueva_orden = new javax.swing.JButton();
        txt_nota = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        btn_guardar_orden = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();

        setClosable(true);
        setTitle("Orden de Trabajo");
        setPreferredSize(new java.awt.Dimension(1041, 546));
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameClosed(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(1041, 546));

        lbl_titulo_orden_trabajo.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lbl_titulo_orden_trabajo.setText("Orden de trabajo");

        lbl_subt_orden_trabajo.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        lbl_subt_orden_trabajo.setText("Agregue muchas salidas en una orden de trabajo");

        lbl_nfactura.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbl_nfactura.setText("Número de Factura *");

        txt_nfactura.setPreferredSize(new java.awt.Dimension(64, 30));
        txt_nfactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_nfacturaActionPerformed(evt);
            }
        });

        lbl_fecha.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbl_fecha.setText("Fecha *");

        jdc_fecha.setDateFormatString("yyyy/MM/dd");

        lbl_codigo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbl_codigo.setText("Código del Producto *");

        txt_codigo.setEditable(false);
        txt_codigo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_codigoActionPerformed(evt);
            }
        });

        jbt_buscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/ic_consultas.png"))); // NOI18N
        jbt_buscar.setBorderPainted(false);
        jbt_buscar.setContentAreaFilled(false);
        jbt_buscar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jbt_buscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbt_buscarActionPerformed(evt);
            }
        });

        lbl_descripcion.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbl_descripcion.setText("Descripción del Producto *");

        txt_descripcion.setEditable(false);
        txt_descripcion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_descripcionActionPerformed(evt);
            }
        });

        lbl_cantidad.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        lbl_cantidad.setText("Cantidad *");

        txt_cantidad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_cantidadActionPerformed(evt);
            }
        });

        btn_agregar_salida.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/ic_modificar.png"))); // NOI18N
        btn_agregar_salida.setText("Agregar Salida");
        btn_agregar_salida.setBorderPainted(false);
        btn_agregar_salida.setContentAreaFilled(false);
        btn_agregar_salida.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btn_agregar_salida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_agregar_salidaActionPerformed(evt);
            }
        });

        lbl_salida.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lbl_salida.setText("Salida");

        jtb_lista_salidas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jtb_lista_salidas.setFocusable(false);
        jtb_lista_salidas.setName(""); // NOI18N
        jtb_lista_salidas.setRequestFocusEnabled(false);
        jtb_lista_salidas.setShowGrid(true);
        jtb_lista_salidas.getTableHeader().setReorderingAllowed(false);
        jtb_lista_salidas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jtb_lista_salidasMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jtb_lista_salidas);

        lbl_editando.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lbl_editando.setText("Editando Nro Orden: ");

        btn_actualizar_salida.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/ic_modificar.png"))); // NOI18N
        btn_actualizar_salida.setText("Actualizar Salida");
        btn_actualizar_salida.setBorderPainted(false);
        btn_actualizar_salida.setContentAreaFilled(false);
        btn_actualizar_salida.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btn_actualizar_salida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_actualizar_salidaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 696, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_codigo)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txt_codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txt_nfactura, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lbl_nfactura, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jbt_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lbl_salida, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jdc_fecha, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lbl_fecha)
                                .addGap(139, 139, 139)
                                .addComponent(lbl_editando))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txt_descripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lbl_descripcion))
                                .addGap(34, 34, 34)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbl_cantidad)
                                    .addComponent(txt_cantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_actualizar_salida)
                .addGap(18, 18, 18)
                .addComponent(btn_agregar_salida)
                .addGap(248, 248, 248))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_salida)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_nfactura)
                    .addComponent(lbl_fecha)
                    .addComponent(lbl_editando))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jdc_fecha, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_nfactura, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_codigo)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lbl_descripcion)
                        .addComponent(lbl_cantidad)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txt_descripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txt_cantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jbt_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_codigo, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_agregar_salida)
                    .addComponent(btn_actualizar_salida))
                .addGap(226, 226, 226))
        );

        txt_codigo.getAccessibleContext().setAccessibleName("");
        txt_codigo.getAccessibleContext().setAccessibleDescription("");

        btn_nueva_orden.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/ic_nuevo.png"))); // NOI18N
        btn_nueva_orden.setText("Nueva Orden");
        btn_nueva_orden.setBorderPainted(false);
        btn_nueva_orden.setContentAreaFilled(false);
        btn_nueva_orden.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btn_nueva_orden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_nueva_ordenActionPerformed(evt);
            }
        });

        txt_nota.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        txt_nota.setText("Nota:");

        btn_guardar_orden.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/ic_grabar.png"))); // NOI18N
        btn_guardar_orden.setText("Guardar Orden");
        btn_guardar_orden.setBorderPainted(false);
        btn_guardar_orden.setContentAreaFilled(false);
        btn_guardar_orden.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btn_guardar_orden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardar_ordenActionPerformed(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/venta.png"))); // NOI18N
        jButton1.setText("Bucar Orden");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jTextField1)
                        .addComponent(txt_nota)
                        .addComponent(lbl_titulo_orden_trabajo)
                        .addComponent(lbl_subt_orden_trabajo)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(134, 134, 134)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(btn_nueva_orden)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_guardar_orden)))
                .addContainerGap(228, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lbl_titulo_orden_trabajo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_subt_orden_trabajo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_nota)
                .addGap(2, 2, 2)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_guardar_orden)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btn_nueva_orden)
                        .addComponent(jButton1))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1025, Short.MAX_VALUE)
                .addGap(4, 4, 4))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_nueva_ordenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nueva_ordenActionPerformed
        activar();
        limpiar();
        btn_agregar_salida.setEnabled(true);
    }//GEN-LAST:event_btn_nueva_ordenActionPerformed

    private void btn_agregar_salidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_agregar_salidaActionPerformed

        try {
            agregarSalida();
        } catch (Exception ex) {
            Logger.getLogger(Frm_OrdenDeTrabajo1.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_btn_agregar_salidaActionPerformed

    public void agregarSalidaAOrdenYaExistente(){
    
    }
    
    private void txt_descripcionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_descripcionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_descripcionActionPerformed

    private void jbt_buscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbt_buscarActionPerformed
        enviar = 1;

        Frm_BuscarProductos C = new Frm_BuscarProductos();
        Frm_Principal.contenedor.add(C);
        Dimension desktopSize = contenedor.getSize();
        Dimension FrameSize = C.getSize();
        C.setLocation((desktopSize.width - FrameSize.width) / 2, (desktopSize.height - FrameSize.height) / 2);
        C.toFront();
        C.setVisible(true);
    }//GEN-LAST:event_jbt_buscarActionPerformed

    private void btn_guardar_ordenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardar_ordenActionPerformed
        // TODO add your handling code here:
        registrarOrdenDeTrabajo();
    }//GEN-LAST:event_btn_guardar_ordenActionPerformed

    private void txt_nfacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_nfacturaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_nfacturaActionPerformed

    private void txt_codigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_codigoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_codigoActionPerformed

    private void txt_cantidadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cantidadActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cantidadActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        Frm_BuscarOrdenDeTrabajo f = new Frm_BuscarOrdenDeTrabajo();
        contenedor.add(f);
        f.show();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formInternalFrameClosed(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameClosed
        // TODO add your handling code here:
        System.out.println("El JInternalFrame se ha cerrado");
        listaDeSalidas.clone();
        jtb_lista_salidas.removeAll();
    }//GEN-LAST:event_formInternalFrameClosed

    private void jtb_lista_salidasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtb_lista_salidasMouseClicked
        // TODO add your handling code here:
        
        if(evt.getClickCount() == 2){
            this.filaSeleccionada = jtb_lista_salidas.getSelectedRow();
            int columnaSelecciona = jtb_lista_salidas.getSelectedColumn();
            if( columnaSelecciona == 0 ){
                txt_nfactura.setText(jtb_lista_salidas.getValueAt(filaSeleccionada, 0).toString());
                txt_nfactura.setEnabled(false);
                txt_codigo.setText(jtb_lista_salidas.getValueAt(filaSeleccionada, 1).toString());
                txt_cantidad.setText(jtb_lista_salidas.getValueAt(filaSeleccionada, 3).toString());
                jdc_fecha.setDate( (Date) jtb_lista_salidas.getValueAt(filaSeleccionada, 2)); 
            }            
        }
    }//GEN-LAST:event_jtb_lista_salidasMouseClicked

    private void btn_actualizar_salidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_actualizar_salidaActionPerformed
        // TODO add your handling code here:
        Object[] salidaAntesDeActualizar = listaDeSalidas.get(this.filaSeleccionada).clone();
        Object[] salidaDespuesDeActualizar = listaDeSalidas.get(this.filaSeleccionada).clone();
        //Procesando la fecha
        Date fechaa = jdc_fecha.getDate();
        long d = fechaa.getTime();
        java.sql.Date fecha_sql = new java.sql.Date(d);

//        int cantAnterior = Integer.parseInt(salida[3].toString());//cantidad del propducto antes de actualizar
        try {
            if(this.filaSeleccionada >= 0 ){
            salidaDespuesDeActualizar[0] = txt_nfactura.getText();
            salidaDespuesDeActualizar[1] = txt_codigo.getText();
            salidaDespuesDeActualizar[2] = fecha_sql;
            salidaDespuesDeActualizar[3] = txt_cantidad.getText();
            
            clsOrdenTrabajo.actualizarOrdenDeSalida(salidaAntesDeActualizar, salidaDespuesDeActualizar);
            
            // Actualizo la tabla con los nuevos valores 
            jtb_lista_salidas.setValueAt(txt_nfactura.getText(), this.filaSeleccionada, 0);
            jtb_lista_salidas.setValueAt(txt_codigo.getText(), this.filaSeleccionada, 1);
            jtb_lista_salidas.setValueAt(fecha_sql, this.filaSeleccionada, 2);
            jtb_lista_salidas.setValueAt(txt_cantidad.getText(), this.filaSeleccionada, 3);
            JOptionPane.showMessageDialog(null, "Salida actulizada correctamente");
            limpiar();
            
        }else{
            JOptionPane.showMessageDialog(null, "Por favor selecciona una salida para editar antes de grabar.");
        }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        
    }//GEN-LAST:event_btn_actualizar_salidaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_actualizar_salida;
    private javax.swing.JButton btn_agregar_salida;
    private javax.swing.JButton btn_guardar_orden;
    private javax.swing.JButton btn_nueva_orden;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jbt_buscar;
    private com.toedter.calendar.JDateChooser jdc_fecha;
    private javax.swing.JTable jtb_lista_salidas;
    private javax.swing.JLabel lbl_cantidad;
    private javax.swing.JLabel lbl_codigo;
    private javax.swing.JLabel lbl_descripcion;
    private javax.swing.JLabel lbl_editando;
    private javax.swing.JLabel lbl_fecha;
    private javax.swing.JLabel lbl_nfactura;
    private javax.swing.JLabel lbl_salida;
    private javax.swing.JLabel lbl_subt_orden_trabajo;
    private javax.swing.JLabel lbl_titulo_orden_trabajo;
    public static javax.swing.JTextField txt_cantidad;
    public static javax.swing.JTextField txt_codigo;
    public static javax.swing.JTextField txt_descripcion;
    private javax.swing.JTextField txt_nfactura;
    private javax.swing.JLabel txt_nota;
    // End of variables declaration//GEN-END:variables
}
