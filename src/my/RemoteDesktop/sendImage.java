/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.RemoteDesktop;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author sabbir
 */
public class sendImage implements Runnable{
    
    Thread Image;//new thread for this class
    
    private int serverScreenX=1;//server screen width
    private int serverScreenY=1;//server screen height
    
    private int screenWidth=1;//client screen width
    private int screenHeight=1;//client screen height
    
    private BufferedImage image;//storing bufferedimage from screen capture 
    private BufferedImage scaledImage;//store rescaled image for server
    
    private Graphics2D graphics2D;//to draw image to scaledImage
    
    private ByteArrayOutputStream baos;//BufferedImage to byte array convertion
    private byte[] byteImage;//byte array for converted buffered image
    
    private DataOutputStream output;//output data
    
    private Robot robot;//Robot class variable
    
    private Socket connectSocket;//socket for sending images
    
    //sendImage()constructor begins
    public sendImage(String ip,int port,int serverScreenX,int serverScreenY) throws UnknownHostException, IOException
    {
        //System.out.println("SendImage constructor");
        this.serverScreenX=serverScreenX;//setting server screen length
        this.serverScreenY=serverScreenY;//setting server screen width
        try
        {
            robot=new Robot();///create robot class to capture image
        }//end try
        catch(AWTException awt)
        {
            awt.printStackTrace();
        }//end catch
        
        Toolkit toolkit =  Toolkit.getDefaultToolkit ();//toolkit for screensize
        Dimension dim = toolkit.getScreenSize();//toolkit for screensize

        screenWidth=dim.width;//setting screen width
        screenHeight=dim.height;//setting screen height
        
        connectSocket=new Socket(ip,port);//creating new socket to connect to server
        //System.out.println("SendImage constructor");
        
        output=new DataOutputStream(connectSocket.getOutputStream());//setting output stream
        
        
        Image=new Thread(this);//creating sendImage thread
        Image.start();//starting sendImage thread
        
    }//constructor ends
    
    //method run() overridden begins
    @Override
    public void run()
    {
        //infinite while loop to continuously send images
        while(true)
        {
            try 
            {
                image=robot.createScreenCapture(new Rectangle(0,0,screenWidth,screenHeight));//making screen capture
                writeImage();//calling method writeImage to send image to server
            }//end try 
            catch (IOException ex) 
            {
                Logger.getLogger(sendImage.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
            try
            {
                Image.sleep(200);//set thread to sleep for sending image after certain interval(500ms)
            }//end try
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }//end catch
        }//end while loop
        
    }//end run() method
    
    //method writeImage() begins
    private void writeImage() throws IOException
    {
        /*
        * write image to harddisk
        //ImageIO.write(image,"jpeg",new File("C:/Users/sabbir/Desktop/image/image.jpeg"));
        */

        //variable for scaling image
        scaledImage=new BufferedImage(serverScreenX,serverScreenY,BufferedImage.TYPE_INT_ARGB);

        graphics2D=scaledImage.createGraphics();//using graphics to write image to scaledImage
        //setting drawimage rendering values
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics2D.drawImage(image,0,0,serverScreenX,serverScreenY,null);//drawing image to scaled Image
        graphics2D.dispose();//disposing graphics

        /*
        * writing scaled image to harddisk
        //ImageIO.write(scaledImage,"jpeg",new File("C:/Users/sabbir/Desktop/image/scaledImage.jpeg"));
        */

        baos=new ByteArrayOutputStream();//creating byte output stream
        ImageIO.write(scaledImage,"jpeg",baos);//write image to byte output stream
        byteImage=baos.toByteArray();//creating bytearray of image
        baos.close();//closing byteArray output stream


        output.writeInt(byteImage.length);//send byte array length of image
        
        int index=0;
        //sending byteArray data to server
        while(index<byteImage.length)
        {
            output.writeByte(byteImage[index]);//send byteArray data by each index
            index++;
        }//end while
    }//end method writeImage()
}//end class sendImage
