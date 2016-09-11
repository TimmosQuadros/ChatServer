/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TimmosQuadros
 */
public class Log {

    public static String LOG_NAME = "myLog";

    public static void setLogFile(String logFile, String logName) {
        try {
            LOG_NAME = logName;
            Logger logger = Logger.getLogger(LOG_NAME);
            FileHandler fileTxt = new FileHandler(logFile);
            fileTxt.setFormatter(new java.util.logging.SimpleFormatter());
            logger.addHandler(fileTxt);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(LOG_NAME).log(Level.SEVERE, null, ex);
        }
    }

    public static void closeLogger() {
        for (Handler h : Logger.getLogger(LOG_NAME).getHandlers()) {
            System.out.println("Closing logger");
            h.close();
        }
    }
}
