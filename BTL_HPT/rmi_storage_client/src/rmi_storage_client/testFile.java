/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmi_storage_client;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mr.robot
 */
public class testFile {

    public static void main(String[] args) {
        File source = new File("D:\\Client");
        File destination = new File("D:\\Server");
        String[] sources = source.list();
        Set<String> srcNames = new HashSet<String>(Arrays.asList(sources));
        String[] dests = destination.list();

        for (String fileName : dests) {
            System.out.println(fileName);
            if (!srcNames.contains(fileName)) {
                System.out.println("file " + fileName + " không có");
            }
        }
    }
}
