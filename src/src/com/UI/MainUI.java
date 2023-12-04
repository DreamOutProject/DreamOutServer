package com.UI;

import com.Main.ServerProcessing;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;

public class MainUI extends JPanel {
    public ServerProcessing main;
    public MainUI(ServerProcessing main){
        this.main =main;
        JButton serverOn = new JButton("서버 열기");
        JButton serverOff = new JButton("서버 끄기");
        serverOn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                main.serverOn();
            }
        });
        serverOff.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                main.serverOff();
            }
        });

        JTextArea log_display = new JTextArea();
        log_display.setEnabled(false);


        main.mainFrame.add(serverOn);
        main.mainFrame.add(serverOff);
        main.mainFrame.setVisible(true);
    }


}
