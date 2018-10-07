/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.RemoteDesktop;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author sabbir
 */
public class Client extends javax.swing.JFrame implements Runnable{

    /**
     * Creates new form Client
     */
    
    private Thread clientThread;//Thread for Client class
    
    private int port;//store port from RemoteDesktop.java
    private int posMouseX;//mouse event position
    private int posMouseY;//mouse event position
    private int clicked;//check mouse button click
    private int serverScreenX=1;//server screen width
    private int serverScreenY=1;//server screen height
    private int screenWidth=1;//client screen width
    private int screenHeight=1;//client screen height

    private double tempPosMouseX;//mouse position temporary
    private double tempPosMouseY;//mouse position temporary
    private double ratioX;//ratio of server screen and client screen
    private double ratioY;//ratio of server screen and client screen
    
    private String ipAddress;//store ip address from RemoteDesktop.java
    
    private Socket connection;//connection to server

    private DataInputStream input;//input data
    private DataOutputStream output;//output data
    
    private Robot robot;//Robot class variable
    
    

    
    //Client constructor begin
    public Client(String ipAddress,int port) 
    {
        initComponents();//initializing components
        
        this.ipAddress=ipAddress;//set ip address from RemoteDesktop.java
        this.port=port;//set port from RemoteDesktop.java
        
        clientThread=new Thread(this);//assigning thread
        clientThread.start();//starting thread
        
    }
    
    //override method run() for Runnable
    @Override
    public void run()
    {
        runClient();//calling runServer method
        //System.out.println("runClient called");
    }//end method run()
    
    //method runClient() begin
    private void runClient()
    {
        try
        {
            robot=new Robot();//robot variable
        }//end try
        catch(AWTException awtException)
        {
            awtException.printStackTrace();
        }//end catch
        
        Toolkit toolkit =  Toolkit.getDefaultToolkit ();//toolkit for screensize
        Dimension dim = toolkit.getScreenSize();//toolkit for screensize

        screenWidth=dim.width;//setting screen width
        screenHeight=dim.height;//setting screen height
        
        try
        {
            createConnection();//creating connection with server
            getStreams();//get input and output streams
            processConnection();//process connection
        }//end try
        catch(IOException ioException)
        {
            ioException.printStackTrace();
            closeConnection();//closing server-client connection
        }//end catch
  
    }//end method runClient()
    
    //method createConnection() begin
    private void createConnection() throws IOException
    {
        displayMessage("Connecting server...\n");
        //System.out.println(ipAddress+" "+port);
        checkBlackList();//check for blacklist
        connection = new Socket(ipAddress,port);//requesting server to accept connection
        displayMessage("Connecting Established\n");
        RemoteDesktop.setStatusTextArea("Connected");//set connection status in main jframe
        displayMessage("Connection: "+connection.getInetAddress().getHostName());//displaying hostname of server
        displayMessage("\nIP Address: "+connection.getInetAddress());//displaying ip address of server
    }
    //end method createConnection()
    
    //method checkBlackList begins
    private void checkBlackList()
    {
        int i=0;
        
        while(i<ComputersAndContacts.blackListLength)//black list stored in static array in ComputersAndContacts
        {
            //System.out.println("list: "+ComputersAndContacts.blackList[i]);//+ComputersAndContacts.blackList[i].length());
            //String help=ComputersAndContacts.blackList[i].substring(0,ComputersAndContacts.blackList[i].length()-1);
            String black=ComputersAndContacts.blackList[i];
            //System.out.println(black.length());
            if(ipAddress.equals(black))//if ip are same
            {
                //System.out.println("Black list found");
                
                //shows error message
                JOptionPane.showMessageDialog(null,"You cannot connect to Black List!","Error!",JOptionPane.ERROR_MESSAGE);
                closeConnection();//closes connection immediately
            }//end if
            i++;
        }//end while
    }//end method checkblackList()
    
    //method getStreams() begin
    private void getStreams() throws IOException //get streams to send and receive data
    {
        input=new DataInputStream(connection.getInputStream());//setting input stream
        
        output=new DataOutputStream(connection.getOutputStream());//setting output stream
    }//end method getStreams()
    
    //method processConnection() begin
    private void processConnection() throws IOException
    {
        try
        {
            serverScreenX=input.readInt();//get server screen width
            serverScreenY=input.readInt();//get server screen height
        }//end try
        catch(IOException e)
        {
            connectionLostDialog.pack();
            connectionLostDialog.setVisible(true);//making visible to exit the JFrame of client
            closeConnection();//closing server-client connection
            //System.out.println("Error getting server screen");
        }//end catch
        
        ratioX=screenWidth/(serverScreenX*1.0);//setting ratio of server and client screen
        ratioY=screenHeight/(serverScreenY*1.0);//setting ratio of server and client screen

        //System.out.printf("ratio x: %f ratio y: %f\n",ratioX,ratioY);
        
        
        //starting new thread sendImage that sends image to server after certain interval of time(500ms)
        new sendImage(ipAddress,port,serverScreenX,serverScreenY);
            
        //start getting mouseposition and click
        //and sending new images from client
        
        while(true)
        {	
            try
            {
                tempPosMouseX=ratioX*input.readInt();//read mouseposition and set to screen
                tempPosMouseY=ratioY*input.readInt();//read mouseposition and set to screen
                clicked=input.readInt();//read the value for click

                posMouseX=(int)tempPosMouseX;//set mouse position to integer
                posMouseY=(int)tempPosMouseY;//set mouse position to integer

                //System.out.printf("x,y: %d,%d\n",x,y);

                //check for mouse button click
                if(clicked==0)//0 implies no click
                {
                    robot.mouseMove(posMouseX,posMouseY);//move mouse position with robot class
                }//end if
                
                else if(clicked==1)//1 implies left button clicked
                {
                    robot.mousePress(InputEvent.BUTTON1_MASK);//left button press in client pc
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);//left button release in client pc
                }//end else if

                else if(clicked==2)//2 implies right button clicked
                {
                    robot.mousePress(InputEvent.BUTTON3_MASK);//right button press in client pc
                    robot.mouseRelease(InputEvent.BUTTON3_MASK);//right button release in client pc
                }//end else if
                
                else if(clicked==3)//3 implies mouse pressed
                {
                    robot.mousePress(InputEvent.BUTTON1_MASK);//left button press in client pc
                }//end else if
                
                else if(clicked==4)//4 implies mouse released
                {
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);//left button release in client pc
                }//end else if
       
            }//end try
            catch(IOException ioException)
            {
                connectionLostDialog.pack();
                connectionLostDialog.setVisible(true);//making visible to exit the JFrame of client
                closeConnection();//closing server-client connection
                ioException.printStackTrace();
                break;//break while loop
            }//end catch
            
        }//end while 
    }//end method processConnection()
    
    
    //method closeConnection() begin
    private void closeConnection()
    {
        //System.out.println("close connection");
        displayMessage("\nConnection Terminated\n");
        RemoteDesktop.setStatusTextArea("Ready to connect");//set connection status in main jframe
        ClientMessenger.connected=false;
        try
        {
            output.close();//close output stream
            input.close();//close input stream
            connection.close();//close socket
            //connectionLostDialog.setVisible(true);
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
        connectionInformationArea = new javax.swing.JScrollPane();
        displayArea = new javax.swing.JTextArea();
        connectionInformationClient = new javax.swing.JLabel();

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
        connectionLostLabel.setText("Connection lost from server.");

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
                .addContainerGap(19, Short.MAX_VALUE))
        );

        setTitle("Client");
        setPreferredSize(new java.awt.Dimension(300, 200));
        setResizable(false);

        displayArea.setColumns(20);
        displayArea.setEditable(false);
        displayArea.setRows(5);
        connectionInformationArea.setViewportView(displayArea);

        connectionInformationClient.setFont(new java.awt.Font("Georgia", 1, 13)); // NOI18N
        connectionInformationClient.setText("Connection Information");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(connectionInformationArea, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(connectionInformationClient)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(connectionInformationClient)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectionInformationArea, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void connectionLostButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionLostButtonActionPerformed
        // TODO add your handling code here:
        connectionLostDialog.setVisible(false);
        connectionLostDialog.dispose();
        this.dispose();//disposing client
    }//GEN-LAST:event_connectionLostButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String args[]) {
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
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        */
        //</editor-fold>

        /*
         * Create and display the form
         */
        /*
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Client().setVisible(true);
            }
        });
    }
    */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane connectionInformationArea;
    private javax.swing.JLabel connectionInformationClient;
    private java.awt.Button connectionLostButton;
    private javax.swing.JDialog connectionLostDialog;
    private javax.swing.JLabel connectionLostLabel;
    private javax.swing.JTextArea displayArea;
    // End of variables declaration//GEN-END:variables
}//end class Client
