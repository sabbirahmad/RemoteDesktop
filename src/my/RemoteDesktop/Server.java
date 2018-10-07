/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.RemoteDesktop;

/**
 *
 * @author sabbir
 */

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Server extends javax.swing.JFrame implements Runnable{

    /**
     * Creates new form Server
     */
       
    private Thread serverThread;//thread for Server class
    
    private int port;//store port from RemoteDesktop.java
    private int posMouseX=0;//mouse event position
    private int posMouseY=0;//mouse event position
    private int picturePanelWidth;//width of screen area
    private int picturePanelHeight;//height of screen area
    
    private boolean leftClickedMouse=false;//check mouse left button click
    private boolean rightClickedMouse=false;//check mouse right button click
    private boolean mousePressed=false;
    private boolean mouseReleased=false;
    private boolean mouseDragged=false;
    
    private ServerSocket server; //server socket
    private Socket connection; //connection to client
    
    private DataOutputStream output;//output data
    private DataInputStream input;//input data
    
    public static BufferedImage image;//storing bufferedimage from client
    public static boolean imageReceived=false;//check image receiving
    
    //Server constructor begin
    public Server(int port) 
    {
        initComponents();//initializing components
        
        this.port=port;//set port from RemoteDesktop.java
        
        serverThread=new Thread(this);//assigning thread
        serverThread.start();//starting thread
    }//end Server constructor
    
    
    //override method run() for Runnable 
    @Override
    public void run()
    {
        try {
            picture.removeAll();//remove previous data of picture panel
            BufferedImage load=ImageIO.read(this.getClass().getResource("LoadingPanel.jpg"));//load loading image while waiting
            JLabel label=new JLabel(new ImageIcon(load));//creating new JLabel to insert image
            picture.add(label,BorderLayout.CENTER);//setting picture panel to BorderLayout
            picture.revalidate();//revalidating picture panel
            picture.repaint();//paint updated picture panel
        }//end try 
        catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch
        
        runServer();//calling runServer method
        //System.out.println("run server called");
    }//end method run()
    
   
    //method runServer() start
    private void runServer()
    {
        picturePanelWidth=picture.getWidth();//setting width of picture panel
        picturePanelHeight=picture.getHeight();//setting height of picture panel
        
        try
        {
            server=new ServerSocket(port); //create new ServerSocket
     
            while(true)
            {
                try
                {
                    waitForConnection();//wait for request to connect
                    getStreams();//get input and output streams
                    processConnection();//process connection
                }//end try
                catch(EOFException eofException)
                {
                    eofException.printStackTrace();
                    displayMessage("\nServer terminated connection.");
                    connectionLostDialog.pack();
                    connectionLostDialog.setVisible(true);//making visible to exit the JFrame of server
                    closeConnection();//closing server-client connection
                }//end catch                
            }//end while
        }//end try
        catch(IOException ioException)
        {
            ioException.printStackTrace();
            connectionLostDialog.pack();
            connectionLostDialog.setVisible(true);//making visible to exit the JFrame of server
            closeConnection();//closing server-client connection
        }//end catch
    }//end method runServer()
    
    //method waitForConnection() begin
    private void waitForConnection() throws IOException
    {
        displayMessage("Waiting for connection...\n");
        connection=server.accept();//allow server to accept request
        
        checkBlackList();
        
        RemoteDesktop.setStatusTextArea("Connected");//set connection status in main jframe
        displayMessage("Connection Established.\n");
        displayMessage("Connection: "+connection.getInetAddress().getHostName());//displaying hostname of client
        displayMessage("\nIP Address: "+connection.getInetAddress());//displaying ip address of client
        
    }//end method waitForConnection
    
    //method checkBlackLisr() begin
    private void checkBlackList()
    {
        String ipConStr=connection.getInetAddress().getHostAddress();//get client ip
        
        int i=0;
        
        while(i<ComputersAndContacts.blackListLength)//checks all black lists to match
        {
            String black=ComputersAndContacts.blackList[i];//matches ip
            //System.out.println(black.length());
            if(ipConStr.equals(black))//check to ip match
            {
                //System.out.println("Black list found");
                
                //if black listed then shows error message
                JOptionPane.showMessageDialog(picture,"Request from Black List.\nBlocked!","Error!",JOptionPane.ERROR_MESSAGE);
                this.dispose();//server is closed
                closeConnection();//connection is closed
            }//end if
            i++;
        }//end while
    }//end method checkBlackList(0
    
    //method getStreams() begin
    private void getStreams() throws IOException //get streams to send and receive data
    {
        output=new DataOutputStream(connection.getOutputStream());//setup output stream connection
        
        input=new DataInputStream(connection.getInputStream());//setup input stream connection
        
    }//end method getStreams
    
    
    //method processConnection begin
    private void processConnection()throws IOException
    {
        int x=0; //mouse position x
        int y=0; //mouse position y

        //displayMessage("\nProcess Connection\n");
        
        //System.out.printf("height= %d  width= %d\n",picturePanelHeight,picturePanelWidth);
        try
        {
            output.writeInt(picturePanelWidth);//sending width of scrren area
            output.writeInt(picturePanelHeight);//sending height of scrren area
        }//end try
        catch(IOException ioException)
        {
            displayMessage("\nError sending data.\n");
            connectionLostDialog.pack();
            connectionLostDialog.setVisible(true);//making visible to exit the JFrame of server
            this.closeConnection();//closing server-client connection
            ioException.printStackTrace();
        }//end catch
        
        //starting new thread getImageto get images from client
        new getImage(server);
        
        //readImage();//call method readImage() to read captured image from client
        
        //start sending mouseposition and click
        //and getting new images from client
        while(true)
        {
            //System.out.println("IN WHILE");
            try
            {
                if(x!=posMouseX || y!=posMouseY)//if mouse position is changed then new position is sent
                {
                    //System.out.println("no click");
                    x=posMouseX;//assigning mouse positionX to x
                    y=posMouseY;//assigning mouse positionY to y
                    output.writeInt(x);//send mouse position
                    output.writeInt(y);//send mouse position
                    output.writeInt(0);//0 signifies no click has been made
                    //System.out.println("moved sent");
                    
                }//end if
                
                if(leftClickedMouse==true)//check is left button is clicked
                {
                    //System.out.println("left click");
                    output.writeInt(x);//send mouse position
                    output.writeInt(y);//send mouse position
                    output.writeInt(1);//1 signifies left mouse button is clicked
                    
                    leftClickedMouse=false;//setting left click false
                    mousePressed=false;//setting mouse pressed false
                    mouseReleased=false;//setting mouse released false
                }//end if
                
                else if(rightClickedMouse==true)//check if right mouse button is clicked
                {
                    //System.out.println("right click");
                    output.writeInt(x);//send mouse position
                    output.writeInt(y);//send mouse position
                    output.writeInt(2);//2 signifies right mouse button is clicked
                    
                    rightClickedMouse=false;//setting right click false
                    mousePressed=false;//setting mouse pressed false
                    mouseReleased=false;//setting mouse released false
                }//end else if
                
                else if(mousePressed==true)//check if mouse is pressed
                {
                    //System.out.println("mouse pressed");
                    output.writeInt(x);//send mouse position
                    output.writeInt(y);//send mouse position
                    output.writeInt(3);//2 signifies mouse is pressed
                    
                    mousePressed=false;//setting right click false
                }//end else if
                else if(mouseReleased==true)//check if mouse is released
                {
                    //System.out.println("mouse released");
                    output.writeInt(x);//send mouse position
                    output.writeInt(y);//send mouse position
                    output.writeInt(4);//4 signifies mouse is released
                    
                    mouseReleased=false;//setting mouse released false
                    mouseDragged=false;//setting mouse dragged false
                }//end else if
                
                else if(mouseDragged==true &&  (x!=posMouseX || y!=posMouseY))//check if mouse is dragged
                {
                    x=posMouseX;
                    y=posMouseY;
                    //System.out.println("mouse dragged");
                    output.writeInt(x);//send mouse position
                    output.writeInt(y);//send mouse position
                    output.writeInt(0);//0 signifies no click has been made
                }
                
                if(imageReceived==true)//checking if new image is received
                {
                    imageReceived=false;//setting imageReceived to false for the next image check
                    updatePicturePanel();//updating image of the panel
                }//end if
               
            }//end try
            
            catch(IOException ioException)
            {
                displayMessage("\nError sending data.\n");
                connectionLostDialog.pack();
                connectionLostDialog.setVisible(true);//making visible to exit the JFrame of server
                this.closeConnection();//closing server-client connection
                ioException.printStackTrace();
                break;//break while loop
            }//end catch  
        }//end while
        
    }//end method processConnection
    
    
    //method closeConnection() begin
    private void closeConnection()
    {
        //System.out.println("close connection");
        displayMessage("\nConnection Terminated\n");
        RemoteDesktop.setStatusTextArea("Ready to connect");//set connection status in main jframe
        ServerMessenger.connected=false;
        try
        {
            output.close();//close output stream
            input.close();//close input stream
            connection.close();//close socket
            server.close();//closing server
        }//end try
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        }//end catch
    }//end method closeConnection
    
    //method displayMessage() begin
    //displaying states/connection status
    private void displayMessage(final String messageToDisplay)
    {
        SwingUtilities.invokeLater(
            new Runnable()
            {
            @Override
                public void run() //updates displayArea
                {
                    displayArea.append(messageToDisplay); //append Message
                }//end method run
            }//end anonymous innerclass
        );//end call SwingUtilities.invokeLater
    }//end method displayMessage
    
   //method updatePicturePanel() begin
    private void updatePicturePanel()
    {
        //picture panel update
        picture.removeAll();//remove previous data of picture panel
        ImageIcon picToInsert=new ImageIcon(image);//getting ImageIcon of bufferedImage from client
        JLabel label=new JLabel("",picToInsert,JLabel.CENTER);//creating new JLabel to insert image
        picture.add(label,BorderLayout.CENTER);//setting picture panel to BorderLayout
        picture.revalidate();//revalidating picture panel
        picture.repaint();//paint updated picture panel
    }//end method updatePicturePanel()
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionLostDialog = new javax.swing.JDialog();
        connectionLostButton = new java.awt.Button();
        connectionLostLabel = new javax.swing.JLabel();
        jFrameServerInfo = new javax.swing.JFrame();
        connectionInformation = new javax.swing.JScrollPane();
        displayArea = new javax.swing.JTextArea();
        connectionInformationServerLabel = new javax.swing.JLabel();
        ghfgh = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        picture = new javax.swing.JPanel();

        connectionLostDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        connectionLostDialog.setTitle("Error!");

        connectionLostButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        connectionLostButton.setLabel("OK");
        connectionLostButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectionLostButtonActionPerformed(evt);
            }
        });

        connectionLostLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        connectionLostLabel.setText("Connection lost from client.");

        javax.swing.GroupLayout connectionLostDialogLayout = new javax.swing.GroupLayout(connectionLostDialog.getContentPane());
        connectionLostDialog.getContentPane().setLayout(connectionLostDialogLayout);
        connectionLostDialogLayout.setHorizontalGroup(
            connectionLostDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionLostDialogLayout.createSequentialGroup()
                .addGroup(connectionLostDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(connectionLostDialogLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(connectionLostLabel))
                    .addGroup(connectionLostDialogLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(connectionLostButton, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        connectionLostDialogLayout.setVerticalGroup(
            connectionLostDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionLostDialogLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(connectionLostLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionLostButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        displayArea.setColumns(20);
        displayArea.setEditable(false);
        displayArea.setRows(5);
        connectionInformation.setViewportView(displayArea);

        connectionInformationServerLabel.setFont(new java.awt.Font("Georgia", 1, 13)); // NOI18N
        connectionInformationServerLabel.setText("Connection Information:");

        javax.swing.GroupLayout jFrameServerInfoLayout = new javax.swing.GroupLayout(jFrameServerInfo.getContentPane());
        jFrameServerInfo.getContentPane().setLayout(jFrameServerInfoLayout);
        jFrameServerInfoLayout.setHorizontalGroup(
            jFrameServerInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(connectionInformation)
            .addGroup(jFrameServerInfoLayout.createSequentialGroup()
                .addComponent(connectionInformationServerLabel)
                .addGap(0, 123, Short.MAX_VALUE))
        );
        jFrameServerInfoLayout.setVerticalGroup(
            jFrameServerInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrameServerInfoLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(connectionInformationServerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionInformation, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
        );

        jMenuItem1.setText("jMenuItem1");
        ghfgh.add(jMenuItem1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Server");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        picture.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        picture.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pictureMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pictureMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pictureMouseReleased(evt);
            }
        });
        picture.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                pictureMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                pictureMouseMoved(evt);
            }
        });
        picture.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(picture, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pictureMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pictureMouseClicked
        // TODO add your handling code here:
        posMouseX=evt.getX();//setting mouse clicked position
        posMouseY=evt.getY();//setting mouse clicked position
        
        //checking left button clicked
        if(SwingUtilities.isLeftMouseButton(evt)==true)
            leftClickedMouse=true;//set left button clicked to true
        //checking right button clicked
        else if(SwingUtilities.isRightMouseButton(evt)==true)
            rightClickedMouse=true;//set left button clicked to true
        
        /*display mouse clicked position to textArea
        String p=Integer.toString(evt.getX());
        String q=Integer.toString(evt.getY());
        displayMessage("Clicked X,Y:"+p+","+q+"\n");
        */
    }//GEN-LAST:event_pictureMouseClicked

    private void pictureMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pictureMouseMoved
        // TODO add your handling code here:
        posMouseX=evt.getX();//setting mouse position
        posMouseY=evt.getY();//setting mouse position
        
        /*display mouse position to textArea
        String p=Integer.toString(evt.getX());
        String q=Integer.toString(evt.getY());
        displayMessage("X,Y:"+p+","+q+"\n");
        */
    }//GEN-LAST:event_pictureMouseMoved

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
        this.closeConnection();//close server-client connection if window closed
    }//GEN-LAST:event_formWindowClosed

    private void connectionLostButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionLostButtonActionPerformed
        // TODO add your handling code here:
        connectionLostDialog.setVisible(false);
        connectionLostDialog.dispose();
        this.dispose();//disposing Server
    }//GEN-LAST:event_connectionLostButtonActionPerformed

    private void pictureMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pictureMousePressed
        // TODO add your handling code here:
        mousePressed=true;
    }//GEN-LAST:event_pictureMousePressed

    private void pictureMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pictureMouseReleased
        // TODO add your handling code here:
        mouseReleased=true;
    }//GEN-LAST:event_pictureMouseReleased

    private void pictureMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pictureMouseDragged
        // TODO add your handling code here:
        posMouseX=evt.getX();//setting mouse position
        posMouseY=evt.getY();//setting mouse position
        mouseDragged=true;//set mouse is being dragged
    }//GEN-LAST:event_pictureMouseDragged

    
    /**
     * @param args the command line arguments
     */
    /*public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
    /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        /*
        final int portmain=6789;
        
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Server(portmain).setVisible(true);
            }
        });
    }*/
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane connectionInformation;
    private javax.swing.JLabel connectionInformationServerLabel;
    private java.awt.Button connectionLostButton;
    private javax.swing.JDialog connectionLostDialog;
    private javax.swing.JLabel connectionLostLabel;
    private javax.swing.JTextArea displayArea;
    private javax.swing.JPopupMenu ghfgh;
    private javax.swing.JFrame jFrameServerInfo;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel picture;
    // End of variables declaration//GEN-END:variables
}//end class server
