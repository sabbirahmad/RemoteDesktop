/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package my.RemoteDesktop;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author sabbir
 */
public class ComputersAndContacts extends javax.swing.JFrame implements Runnable{

    private Scanner logFile;//file input
    
    static String[] whiteList;//white list array
    static String[] blackList;//black list array
    
    static int whiteListLength;//legth of whitelist
    static int blackListLength;//length of blacklist
    
    private Thread computer;//thread for this clas
    
    //constructor ComputesAndContacts() begin
    public ComputersAndContacts()
    {
        computersAndContacts();//calls for getting contacts
        initCompontents();
        
        
        computer=new Thread(this);//assigning thread
        computer.start();//starting thread
    }//end constructor ComputesAndContacts()
    
    //method run() begin
    @Override
    public void run() {
        createNode(whiteListNode,whiteList,whiteListLength);//create nodes in tree
        createNode(blackListNode,blackList,blackListLength);//create nodes in tree
        
        treeModel=new DefaultTreeModel(root);//sets root of tree
        tree=new JTree(treeModel);//new tree
        
        tree.setRootVisible(false);//root is set to invisible
        
        compScroll.setViewportView(tree);//scroll pane settings
    }//end method run()
    
    //method initComponents() begin
    public void initCompontents()
    {
        comp=new JFrame();//new JFrame
        comp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        comp.setSize(200, 320);//set size of frame
        comp.setTitle("Computers");//set title
        
        compScroll=new JScrollPane();
        comp.add(compScroll);
        
        //whiteList tree
        root=new DefaultMutableTreeNode("List");//tree node
        whiteListNode=new DefaultMutableTreeNode("White List");//tree node name
        root.add(whiteListNode);//add node to tree
        
        blackListNode=new DefaultMutableTreeNode("Black List");//tree node name
        root.add(blackListNode);//add node to tree
    }//end method initComponents()
    
    //method createNode() begin
    private void createNode(DefaultMutableTreeNode root, String[] list, int listLength)
    {
        int length=0;
        while(length<listLength)
        {
            DefaultMutableTreeNode node=new DefaultMutableTreeNode(list[length]);
            root.add(node);//add node
            length++;
        }//end while
    }//end method createNode()
    
    //method computersAndContacts() begin
    private void computersAndContacts()
    {
        whiteList=new String[100];
        blackList=new String[100];
        
        openFile("whitelist.log");
        whiteListLength=setListString(whiteList);//reads whitelist from file
        closeFile();
        //System.out.println("whitelist:");
        //printString(whiteListLength,whiteList);
        
        openFile("blacklist.log");
        blackListLength=setListString(blackList);//reads whitelist from file
        closeFile();
        //System.out.println("blacklist:");
        //printString(blackListLength,blackList);
    }//end method computersAndContacts()
    
    
    //method openFile() begin
    private void openFile(String fileName)
    {
        try {
            logFile=new Scanner(new File(fileName));//open file with name
        }//end try 
        catch (FileNotFoundException ex) {
            Logger.getLogger(ComputersAndContacts.class.getName()).log(Level.SEVERE, null, ex);
        }//end catch
    }//end method openFile()
    
    //method closeFile() begin
    private void closeFile()
    {
        if(logFile!=null)
        {
            logFile.close();
        }
    }//end method closeFile()

    //method setString() begin
    private int setListString(String[] list)
    {
        int length=0;
        try
        {
            while(logFile.hasNext())
            {
                list[length++]=logFile.next();//writes ip from file to string
            }//end while
        }//end try
        catch(NoSuchElementException elementException)
        {
            System.out.println("Error reading 1.");
            logFile.close();
        }//end catch
        catch(IllegalStateException stateException)
        {
            System.out.println("Error reading 2.");
        }//end catch
        return length;
    }//end method setString()
    
    //method showAndHideComp() begins
    public static void showAndHideComp(boolean show)
    {
        comp.setVisible(show);//sets this JFrame to show
    }//end method showAndHideComp(0

    
    
    //swing variable declaration
    
    private static JFrame comp;//Jframe for this clas
    private JScrollPane compScroll;//Scroll pane
    private DefaultMutableTreeNode root;//tree root 
    private DefaultMutableTreeNode whiteListNode;//whitenode
    private DefaultMutableTreeNode blackListNode;//blacknode
    private DefaultTreeModel treeModel;//tree model
    private JTree tree;//tree
    
    //end variable declaration
}
