/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_storage_server;

import com.healthmarketscience.rmiio.SerializableInputStream;
import com.healthmarketscience.rmiio.SerializableOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.joda.time.DateTimeUtils;
import rmi_storage_client.FileClientInt;

/**
 *
 * @author robot
 */
public class FileServer extends UnicastRemoteObject implements FileServerInt{
    public Registry rmiRegistry;
    public Vector connVec = new Vector();
    public File serverFile;
    public boolean isStart = false;
    private ArrayList<cmdServer> information = new ArrayList<>();
    private int sizeQueue = 10;
    
    public FileServer(File serverFile) throws RemoteException{
        super();
        this.serverFile = serverFile;
    }
    
    
    @Override
    public void start() throws Exception{
        isStart = true;
        rmiRegistry = LocateRegistry.createRegistry(6969);
        rmiRegistry.rebind("server", this);
        JOptionPane.showMessageDialog(null, "Server Start!");
    }
    
    @Override
    public void stop() throws Exception {
        isStart = false;
        rmiRegistry.unbind("server");
        unexportObject(this, true);
        unexportObject(rmiRegistry, true);
        JOptionPane.showMessageDialog(null, "Server Stop");
    }
    
    
    @Override
    public File getServerFile() throws RemoteException {
        return serverFile;
    }
    
    @Override
    public void setFile(File serverFile) throws RemoteException{
        this.serverFile = serverFile;
    }
    
    @Override
    public OutputStream getFileOutputStream(File file) throws Exception{
        return new SerializableOutputStream(new FileOutputStream(file));
    }
    
    @Override
    public InputStream getFileInputStream(File file) throws Exception{
        return new SerializableInputStream(new FileInputStream(file));
    }
    
    @Override
    public boolean isStart(){
        return isStart;
    }  

    @Override
    public void synchronous(FileClientInt fileCI) throws RemoteException {
        if(information.size() < sizeQueue){
            information.add(new cmdServer(currentTime(), fileCI.getAddress()+" đang đồng bộ"));
        }else{
            information.remove(0);
            information.add(new cmdServer(currentTime(),  fileCI.getAddress()+" đang đồng bộ"));
        }
    }

    @Override
    public void disconnectsynchronous(FileClientInt fileCI) throws RemoteException {
        if(information.size() < sizeQueue){
            information.add(new cmdServer(currentTime(), fileCI.getAddress()+" đa ngắt đồng bộ"));
        }else{
            information.remove(0);
            information.add(new cmdServer(currentTime(),  fileCI.getAddress()+" đã ngắt đồng bộ"));
        }
    }

    @Override
    public void showSyncState(FileClientInt fileCI) throws RemoteException {
        if(information.size() < sizeQueue){
            information.add(new cmdServer(currentTime(), fileCI.getSyncState()));
        }else{
            information.remove(0);
            information.add(new cmdServer(currentTime(), fileCI.getSyncState()));
        }
    }
    
    public String currentTime(){
        String dateString= "";
        Date time = new Date();
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
        dateString = formatTime.format(time.getTime());
        return dateString;
    }

    public ArrayList<cmdServer> getInformation() {
        return information;
    }
    
    
}
