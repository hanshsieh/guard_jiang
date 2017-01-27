package com.handoitadsf.line.group_guard;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineContact;
import line.thrift.Operation;

/**
 * Created by cahsieh on 1/27/17.
 */
public class Test {
    public static void main(String[] args) throws Throwable {
        new Test();
    }

    public Test() throws Throwable {
        String id = "dotinthewhite@outlook.com";
        String password = "7jdb3s5kxx";
        String certificate = "";

        // There are two ways to log in, by id and password.
        // --Certificate token is still not avaliable for log in.--
        // if you have acquired certificate token, you could use it to login too.
        // it will skip the pincode check.

        // init client

        LineClient client = new LineClient(id, password, certificate);
        /*
        LineContact someoneContact = client.getContactByName("someone"); // find contact

        // Sending image by local image path
        someoneContact.sendImage("/Users/treylin/Downloads/yan.jpg");
        // Sending image by file
        File file = new File("/Users/treylin/Downloads/yan.jpg");
        someoneContact.sendImage(file);
        // Sending image by inputstream
        InputStream is = new FileInputStream(file);
        someoneContact.sendImage(is);

        someoneContact.sendImageWithURL("https://goo.gl/qXdQrf");
        // send the sticker
        someoneContact.sendSticker("13", "1", "100", "");
        // send the message
        System.out.println(someoneContact.sendMessage(":) test"));
        while(true){
            client.longPoll();
        }*/
        List<Operation> operations = client.getApi().fetchOperations(149679, 10);
        for (Operation operation : operations) {
            System.out.println("Operation: " + operation);
        }
    }

}
