/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.*;
import ui.FXMLDocumentBase;
import utility.JsonConverter;
import utility.ServerRequestHandling;
import static utility.ServerRequestHandling.clientData;

/**
 *
 * @author Marwan Adel
 */
public class ServerRequestsHandler {

    private ServerSocket serverSocket;
    private Socket socket;

    private String address;

    private boolean flag = false;

    private Thread th;

    static Stage stage;

    public static ServerRequestsHandler serverRequestsHandler = null;
    public static Vector<PrintStream> connected = new Vector<>();

    private ServerRequestsHandler(Stage stage) {
        this.stage = stage;
        address = new String();

        try {
            address = InetAddress.getLocalHost().getHostAddress();
            System.out.println(address);
        } catch (IOException ex) {
            Logger.getLogger(ServerRequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        stage.setOnCloseRequest((WindowEvent event) -> { /// na msh fahm logic eny leh 3mltaha hna w f handling s7 wla 3`lt l mfrod en hna 3lashan low mfesh users
            try { //// hn3ml print 3lashan n4of meen ly 3`al fehom 
                if (serverSocket != null) {
                    DatabaseManage databaseManage = new DatabaseManage();
                    databaseManage.updateAllStatus();

                    serverSocket.close();
                } else {
                    DatabaseManage databaseManage = new DatabaseManage();
                    databaseManage.updateAllStatus();
                }
                Platform.exit();
                System.exit(0);
            } catch (IOException ex) {
                Logger.getLogger(ServerRequestHandling.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static ServerRequestsHandler createInstance(Stage stage) {
        if (serverRequestsHandler == null) {
            serverRequestsHandler = new ServerRequestsHandler(stage);
        }
        return serverRequestsHandler;
    }

    public void startServer() {
        if (!flag) {
            try {
                serverSocket = new ServerSocket(11114);
            } catch (IOException ex) {
                Logger.getLogger(ServerRequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            socket = serverSocket.accept();

                            connected.add(new PrintStream(socket.getOutputStream()));

                            new ServerRequestHandling(socket, stage);
                        } catch (IOException ex) {
                            try {
                                if (socket != null) {
                                    socket.close();
                                }
                            } catch (IOException ex1) {
                                Logger.getLogger(ServerRequestsHandler.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                        }
                    }
                }
            });
            th.start();

            flag = true;
        }
    }

    public void stopServer() {
        if (flag) {
            DatabaseManage databaseManage = new DatabaseManage();
            databaseManage.updateAllStatus();

            sayByeToAllConnected();

            connected.clear();

            th.suspend();
            flag = false;
            try {
                if (socket != null) {
                    socket.close();
                }
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerRequestsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void sayByeToAllConnected() {
        for (PrintStream printStream : connected) {
            printStream.println(JsonConverter.convertSayByeToJson());
        }
        connected.clear();
        clientData.clear();

        DatabaseManage databaseManage = new DatabaseManage();
        int Online = databaseManage.fetchOnlinePlayers();
        int Offline = databaseManage.fetchOfflinePlayers();
        FXMLDocumentBase.updateChart(Online, Offline);
    }

    /**
     * @return the address
     */
    public boolean getFlag() {
        return flag;
    }

}
