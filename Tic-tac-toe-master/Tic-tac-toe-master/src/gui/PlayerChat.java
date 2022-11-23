package gui;

import javax.swing.*;
import java.awt.*;


public class PlayerChat extends JPanel{
    public JTextArea chatTextArea=new JTextArea("命令区域",18,20);
    public PlayerChat(){
        setLayout(new BorderLayout());
        chatTextArea.setAutoscrolls(true);
        chatTextArea.setLineWrap(true);
        add(chatTextArea,BorderLayout.CENTER);
    }
}
