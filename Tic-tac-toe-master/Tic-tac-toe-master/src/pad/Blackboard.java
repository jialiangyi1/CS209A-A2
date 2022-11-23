package pad;

import java.awt.*;


public class Blackboard extends Canvas {
    board padBelonged; // 黑棋所属的棋盘

    public Blackboard(board padBelonged) {
        setSize(20, 20); // 设置棋子大小
        this.padBelonged = padBelonged;
    }

    public void paint(Graphics g) { // 画棋子
        g.setColor(Color.black);
        g.fillOval(0, 0, 14, 14);
    }
}

