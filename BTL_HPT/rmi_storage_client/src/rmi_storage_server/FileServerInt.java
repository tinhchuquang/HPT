 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_storage_server;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import rmi_storage_client.FileClientInt;

/**
 *
 * @author robot
 */
public interface FileServerInt extends Remote {
    public void synchronous(FileClientInt fileCI) throws RemoteException;              
    public void disconnectsynchronous(FileClientInt fileCI) throws RemoteException;
    public void showSyncState(FileClientInt fileCI) throws RemoteException;
    public void start() throws Exception;
    public void stop() throws Exception;
    public boolean isStart() throws RemoteException;
    public File getServerFile() throws RemoteException;
    public void setFile(File serverFie) throws RemoteException;
    public OutputStream getFileOutputStream(File f) throws Exception;
    public InputStream getFileInputStream(File f) throws Exception;
}
