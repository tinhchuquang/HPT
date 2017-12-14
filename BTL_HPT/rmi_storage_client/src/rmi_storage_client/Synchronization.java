/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_storage_client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.joda.time.DateTimeUtils;
import rmi_storage_server.FileServerInt;

/**
 *
 * @author robot
 */
public class Synchronization implements Runnable {

    private boolean isDone;
    private boolean iscomplete = false;
    private boolean isBegin;
    public int state = 0;
    private File clientFile;
    private File serverFile;
    private FileServerInt server;
    private FileClientInt client;
    private String Username = "";

    public Synchronization(FileClientInt client, FileServerInt server,
            File serverFile, File clientFile, boolean isDone, String userName) {
        this.client = client;
        this.server = server;
        this.clientFile = clientFile;
        this.serverFile = serverFile;
        this.isDone = isDone;
        this.isBegin = true;
        this.Username = userName;
    }

    public void synchronize(File source, File destination) throws Exception {

        if (source.isDirectory()) {
            if (!destination.exists()) {
                if (!destination.mkdirs()) {
                    throw new IOException("Could not create path "
                            + destination);

                }
            } else if (!destination.isDirectory()) {
                throw new IOException(
                        "Source and Destination not of the same type:"
                        + source.getCanonicalPath() + " , "
                        + destination.getCanonicalPath());
            }
            String[] sources = source.list();
            Set<String> srcNames = new HashSet<String>(Arrays.asList(sources));
            String[] dests = destination.list();

            for (String fileName : dests) {
                if (!srcNames.contains(fileName)) {
                    //System.out.println("file "+ fileName+ " không có");
                    delete(new File(destination, fileName));
                    client.setSyncState("Người dùng " + this.Username + " : " + " " + fileName + " đã xóa và được cập nhật trên server");
                    server.showSyncState(client);
                }
            }

            for (String fileName : sources) {
                File srcFile = new File(source, fileName);
                File destFile = new File(destination, fileName);
                synchronize(srcFile, destFile);
            }
        } else {
            if (destination.exists() && destination.isDirectory()) {
                delete(destination);
                client.setSyncState("Người dùng " + this.Username + " : " + "thư mục " + destination.getName() + " đã được xóa đi ");
                server.showSyncState(client);
            }
            if (destination.exists() && source.exists()) {
                long sts = source.lastModified();
                long dts = destination.lastModified();
                if ( sts != dts || source.length() != destination.length()) {
                    copyFile(source, destination);
                    client.setSyncState("Người dùng " + this.Username + " : " + "file " + source.getName() + " đã được upload");
                    server.showSyncState(client);
                }
            } else {
                if(destination.createNewFile() && source.exists()){
                    copyFile(source, destination);
                    client.setSyncState("Người dùng " + this.Username + " : " + "file " + source.getName() + " đã được upload");
                    server.showSyncState(client);
                }else {
                    client.setSyncState("Người dùng " + this.Username + " : " + "file " + source.getName() + " không tạo được trên file đích");
                    server.showSyncState(client);
                }
               
            }

//                copyFile(source, destination);

        }
    }

    private void copyFileBegin(File srcFile, File destFile) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        is = server.getFileInputStream(srcFile);
        os = new FileOutputStream(destFile, false);
        try {
            byte[] byteBuff = new byte[16 * 1024 * 1024];
            int len = 0;
            while ((len = is.read(byteBuff)) >= 0) {
                os.write(byteBuff, 0, len);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private void copyFile(File srcFile, File destFile)
            throws Exception {
        InputStream is = null;
        OutputStream os = null;

        try {
            if (state == 1) {
                is = server.getFileInputStream(srcFile);
                os = new FileOutputStream(destFile, false);
            } else if (state == 2) {
                is = new FileInputStream(srcFile);
                os = server.getFileOutputStream(destFile);
            } 

            byte[] byteBuff = new byte[16 * 1024 * 1024];
            int len = 0;
            while ((len = is.read(byteBuff)) >= 0) {
                os.write(byteBuff, 0, len);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }

        boolean successTimestampOp = destFile.setLastModified(srcFile
                .lastModified());
        if (!successTimestampOp) {
//            JOptionPane.showMessageDialog(null, "Lỗi trong quá trình sửa đổi ngày cập nhật file :"
//                    + destFile);
            client.setSyncState("Người dùng " + this.Username + " : " + "Lỗi trong quá trình sửa đổi ngày cập nhật file : " + destFile);
            server.showSyncState(client);
        }
    }

    public void delete(File file) throws RemoteException {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                delete(subFile);
            }
        }
        if (file.exists()) {
            if (!file.delete()) {
                //JOptionPane.showMessageDialog(null ,"Không thể xóa file : " + file);
                client.setSyncState("Người dùng " + this.Username + " : " + "Không thể xóa file " + file.getName());
                server.showSyncState(client);
            } else {
                client.setSyncState("Người dùng " + this.Username + " : " + "Xóa file thành công " + file.getName());
                server.showSyncState(client);
            }
        }
    }

    public void uploadBegin(File source, File destination) throws Exception {

        String[] sources = source.list();
        String[] dests = destination.list();
        Set<String> destsNames = new HashSet<String>(Arrays.asList(dests));

        for (String fileName : sources) {
            if (!destsNames.contains(fileName)) {

                File sourceFile = new File(source, fileName);
                File destFile = new File(destination, fileName);
                if (sourceFile.isDirectory()) {

                    if (destFile.mkdir()) {
                        uploadBegin(sourceFile, destFile);
                        client.setSyncState("Người dùng " + this.Username + " : " + "thư mục " + destFile.getName() + " đã upload lên server");
                        server.showSyncState(client);
                    } else {
                        client.setSyncState("Người dùng " + this.Username + " : " + "Không tạo được thư mục " + destFile.getName());
                        server.showSyncState(client);
                    }

                } else {

                    if (destFile.createNewFile()) {
                        copyFileBegin(sourceFile, destFile);
                        client.setSyncState("Người dùng " + this.Username + " : " + fileName + " đã upload lên server");
                        server.showSyncState(client);
                    } else {
                        client.setSyncState("Người dùng " + this.Username + " : " + "Không tạo được file " + sourceFile.getName());
                        server.showSyncState(client);
                    }

                }

            }
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (isBegin) {
            try {
                uploadBegin(clientFile, serverFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            isBegin = false;
        }

        try {
            while (!isDone) {
                if (clientFile.length() > serverFile.length()
                        || clientFile.lastModified() > serverFile
                        .lastModified()) {
                    state = 1;
                    iscomplete = false;
                    synchronize(clientFile, serverFile);
                } else if (serverFile.length() > clientFile.length()
                        || clientFile.lastModified() < serverFile
                        .lastModified()) {
                    state = 2;
                    iscomplete = false;
                    synchronize(serverFile, clientFile);
                } else {
                    state = 0;
                    if(!iscomplete){
                        client.setSyncState("Người dùng " + this.Username + " : đã hoàn thành đồng bộ");
                        server.showSyncState(client);
                        iscomplete = true;
                    }
                    
                }

                if (server.isStart()) {
                    continue;
                } else {
                    server.disconnectsynchronous(client);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSync() throws RemoteException {
        DateTimeUtils.setCurrentMillisSystem();
        isDone = true;
        client.setSyncState("Người dùng " + this.Username + " : đã dừng đồng bộ");
        server.showSyncState(client);
    }
}
