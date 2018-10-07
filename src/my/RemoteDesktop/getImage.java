/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.RemoteDesktop;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author sabbir
 */
public class getImage implements Runnable
{
    
    private Thread Image;//new thread for this class
    private Socket connectSocket;//new socket for receiving image
    private DataInputStream input;//input data
    
    private ServerSocket newServer;
    
    //getImage() constructor begin
    public getImage(ServerSocket server) throws IOException
    {
        //System.out.println("getImage-constructor");
        newServer=server;
        connectSocket=server.accept();//accept request from client
        //System.out.println("Connection established-2");
        
        input=new DataInputStream(connectSocket.getInputStream());//setup input stream connection
        
        Image=new Thread(this);//creating the thread
        Image.start();///starting the thread
    }//constructor ends
    
    //method run() overridden begins
    @Override
    public void run()
    {
        //infinite while loop to continuously receive image
        while(true)
        {
            try 
            {
                readImage();//calling method readImage to receive image from client
            }//end try
            catch (IOException ex) 
            {
                Logger.getLogger(getImage.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
        }//end while          
    }//end method run()
    
    //method readImage() begins
    private void readImage() throws IOException
    {
        try
        {
            int byteImageLength;//store byteArray length for image
            byteImageLength=input.readInt();//get length of byteArray of BufferedImage of client
            byte[] byteImage=new byte[byteImageLength];//creating byteArray of needed length
            
            int index=0;
            //reading byteArray data from client
            while(index<byteImageLength)
            {
                byteImage[index]=input.readByte();//get byteArray data by each index
                index++;
            }//end while
            
            
            try
            {
                Server.image=ImageIO.read(new ByteArrayInputStream(byteImage));//storing the received image to variable
                Server.imageReceived=true;//setting imageReceives true to update the panel to show new image
               
                //write Image to harddisk
                //ImageIO.write(Server.image,"jpeg",new File("C:/Users/sabbir/Desktop/image/image.jpeg"));
                
            }//end try
            catch(IOException ioException)
            {
                //displayMessage("Error occurred.");
                ioException.printStackTrace();
                //System.out.println("Error converting byteImage to bufferImage");
            }//end catch
        }//end try
        
        catch(IOException e)
        {
            e.printStackTrace();
            //System.out.println("Error getting byteImage");
        }//end catch
    }//end method readImage() 
}//end class getImage
