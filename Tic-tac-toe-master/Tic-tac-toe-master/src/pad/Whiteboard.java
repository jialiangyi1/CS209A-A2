package pad;

import java.awt.*;


public class Whiteboard extends Canvas{
    board padBelonged; // 白棋所属的棋盘

    public Whiteboard(board padBelonged)
    {
        setSize(20, 20);
        this.padBelonged = padBelonged;
    }

    public void paint(Graphics g)
    { // 画棋子
        g.setColor(Color.white);
        g.fillOval(0, 0, 14, 14);
    }
}
