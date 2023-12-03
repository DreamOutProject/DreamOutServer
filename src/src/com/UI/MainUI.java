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
        JButton serveron = new JButton("서버 열기");
        JButton serveroff = new JButton("서버 끄기");
        serveron.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                main.serverOn();
            }
        });
        serveroff.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                main.serverOff();
            }
        });

        JTextArea log_display = new JTextArea();
        log_display.setEnabled(false);


        main.mainFrame.add(serveron);
        main.mainFrame.add(serveroff);
        main.mainFrame.setVisible(true);
    }


}
