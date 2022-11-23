package gui;

import java.awt.*;


public class PlayerList extends Panel {
    public List userList = new List(10);

    public PlayerList() {
        setLayout(new BorderLayout());
        for (int i = 0; i < 10; i++) {
            userList.add(i + "." + "无用户");
        }
        add(userList, BorderLayout.CENTER);
    }
}
