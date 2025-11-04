package com.store.pantastoreapp.Utils;

import com.store.pantastoreapp.Models.User;

import java.io.*;
import java.util.logging.Logger;

public class UserDataManager extends User {
    private static final Logger logger = Logger.getLogger(UserDataManager.class.getName());
    public String getUserData() throws IOException {
        String usrname = null;
        String pswrd = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/com/store/pantastoreapp/data/UserInfo.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                usrname = data[0];
                pswrd = data[1];
            }
            br.close();
        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error reading from file", e);
        }
        return usrname + "," + pswrd;
    }
}
