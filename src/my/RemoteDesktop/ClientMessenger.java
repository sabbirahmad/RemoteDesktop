/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.RemoteDesktop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sabbir
 */
public class ClientMessenger extends javax.swing.JFrame implements Runnable{

    /**
     * Creates new form ClientMessenger
     */
    
    private Socket clientSocket;//new socket for receiving message
    private ObjectInputStream input;//input data
    private ObjectOutputStream output;//input data
    
    private Thread messenger;//thread for messenger
    
    private String ownMessage;//storing own message
    private String message;//message from server
    private String ip;//keeps ip
    private int port;//keeps port
    private String name;//name for messenger
    
    static boolean connected=false;//connection check varialbe

    //constructor ClientMessenger() begis
    public ClientMessenger(String ip,int port) {
        initComponents();
        
        this.ip=ip;//sets ip
        this.port=port;//sets port
        
        messenger= new Thread(this);//new thread
        messenger.start();//starts thread
    }//end method constructor
    
    //method run() begin
    @Override
    public void run()
    {
        try {
            createConnection(ip,port);//creating connection
        }//end try
        catch (UnknownHostException ex) {
            Logger.getLogger(ClientMessenger.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch
        catch (IOException ex) {
            Logger.getLogger(ClientMessenger.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch
    }//end method run()
    
    
    //method createConnection begin
    private void createConnection(String ip,int port) throws UnknownHostException, IOException
    {
        //System.out.println(ip+" "+port);
        clientSocket=new Socket(ip,port);//request to server
        connected=true;//sets connected true for check elsewhere
        getStreams();//calls get streams for input and output stream
        getMessage();//get message from server
    }
    
    private void getStreams() throws IOException //get streams to send and receive data
    {
        output=new ObjectOutputStream(clientSocket.getOutputStream());//setup output stream connection
        
        input=new ObjectInputStream(clientSocket.getInputStream());//setup input stream connection
        
    }//end method getStreams
    
    private void getMessage() throws IOException
    {
        while(true)//continues through out the connection
        {
            try
            {
                name=(String)input.readObject();//reads alias of server
                message=(String)input.readObject();//reads message from server
                if(message!=null)
                {
                    this.setVisible(true);//if new message then shows messenger
                }//end if
                chatShowArea.append(name+": ");//appends name
                chatShowArea.append(message+"\n");//appends message to text area
            }//end try
            catch(ClassNotFoundException classExcept)
            {
                classExcept.printStackTrace();
            }//end catch
        }//end while
    }//end method getMessage()
    
    //method sendMessage() begin
    private void sendMessage(String messageSend) throws IOException
    {
        output.writeObject(messageSend);//writes message stored in variable 
        output.flush();//flushes output
    }//end method sendMessage()

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chatShowScroll = new javax.swing.JScrollPane();
        chatShowArea = new javax.swing.JTextArea();
        chatWriteField = new javax.swing.JTextField();

        setName("clientMessenger");

        chatShowScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        chatShowArea.setColumns(20);
        chatShowArea.setEditable(false);
        chatShowArea.setLineWrap(true);
        chatShowArea.setRows(5);
        chatShowArea.setWrapStyleWord(true);
        chatShowScroll.setViewportView(chatShowArea);

        chatWriteField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chatWriteFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chatShowScroll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
            .addComponent(chatWriteField, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(chatShowScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chatWriteField, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void chatWriteFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatWriteFieldActionPerformed
        // TODO add your handling code here:    
        if (evt.getSource() == chatWriteField) {
            ownMessage = String.format("%s", evt.getActionCommand());//gets message from textField
            chatShowArea.append(RemoteDesktop.alias+": ");//apends name of client
            chatShowArea.append(ownMessage + "\n");//appends message
            chatWriteField.setText(null);//textField clears
            try {
                sendMessage(RemoteDesktop.alias);//sends alias to server
                sendMessage(ownMessage);//sends message
            }//end try
            catch (IOException ex) {
                Logger.getLogger(ServerMessenger.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
        }//end if
    }//GEN-LAST:event_chatWriteFieldActionPerformed

    /**
     * @param args the command line arguments
     
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientMessenger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientMessenger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientMessenger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientMessenger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ClientMessenger().setVisible(true);
            }
        });
    }
    */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea chatShowArea;
    private javax.swing.JScrollPane chatShowScroll;
    private javax.swing.JTextField chatWriteField;
    // End of variables declaration//GEN-END:variables
}
