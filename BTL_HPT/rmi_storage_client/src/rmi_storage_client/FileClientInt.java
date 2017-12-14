/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_storage_client;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author robot
 */
public interface FileClientInt extends Remote {

    public InetAddress getAddress() throws RemoteException;
    public void setSyncState(String command) throws RemoteException;
    public String getSyncState() throws RemoteException;

}
