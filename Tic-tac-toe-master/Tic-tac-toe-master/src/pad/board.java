package pad;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


public class board extends Panel implements MouseListener,ActionListener{
    // 鼠标是否能使用
    public boolean isMouseEnabled = false;
    // 是否胜利
    public boolean isWinned = false;
    // 是否在下棋中
    public boolean isGaming = false;
    // 棋子的x轴坐标位
    public int chessX_POS = -1;
    // 棋子的y轴坐标位
    public int chessY_POS = -1;
    // 棋子的颜色
    public int chessColor = 1;
    // 黑棋x轴坐标位数组
    public int chessBlack_XPOS[] = new int[200];
    // 黑棋y轴坐标位数组
    public int chessBlack_YPOS[] = new int[200];
    // 白棋x轴坐标位数组
    public int chessWhite_XPOS[] = new int[200];
    // 白棋y轴坐标位数组
    public int chessWhite_YPOS[] = new int[200];
    // 黑棋数量
    public int chessBlackCount = 0;
    // 白棋数量
    public int chessWhiteCount = 0;
    // 黑棋获胜次数
    public int chessBlackVicTimes = 0;
    // 白棋获胜次数
    public int chessWhiteVicTimes = 0;
    // 套接口
    public Socket chessSocket;
    public DataInputStream inputData;
    public DataOutputStream outputData;
    public String chessSelfName = null;
    public String chessPeerName = null;
    public String host = null;
    public int port = 4331;
    public TextField statusText = new TextField("请连接服务器！");
    public boardThread boardThread = new boardThread(this);

    public board()
    {
        setSize(440, 440);
        setLayout(null);
        setBackground(Color.LIGHT_GRAY);
        addMouseListener(this);
        add(statusText);
        statusText.setBounds(new Rectangle(40, 5, 360, 24));
        statusText.setEditable(false);
    }

    // 连接到主机
    public boolean connectServer(String ServerIP, int ServerPort) throws Exception
    {
        try
        {
            // 取得主机端口
            chessSocket = new Socket(ServerIP, ServerPort);
            // 取得输入流
            inputData = new DataInputStream(chessSocket.getInputStream());
            // 取得输出流
            outputData = new DataOutputStream(chessSocket.getOutputStream());
            boardThread.start();
            return true;
        }
        catch (IOException ex)
        {
            statusText.setText("连接失败! \n");
        }
        return false;
    }

    // 设定胜利时的棋盘状态
    public void setVicStatus(int vicChessColor)
    {
        // 清空棋盘
        this.removeAll();
        // 将黑棋的位置设置到零点
        for (int i = 0; i <= chessBlackCount; i++)
        {
            chessBlack_XPOS[i] = 0;
            chessBlack_YPOS[i] = 0;
        }
        // 将白棋的位置设置到零点
        for (int i = 0; i <= chessWhiteCount; i++)
        {
            chessWhite_XPOS[i] = 0;
            chessWhite_YPOS[i] = 0;
        }
        // 清空棋盘上的黑棋数
        chessBlackCount = 0;
        // 清空棋盘上的白棋数
        chessWhiteCount = 0;
        add(statusText);
        statusText.setBounds(40, 5, 360, 24);
        if (vicChessColor == 1)
        { // 黑棋胜
            chessBlackVicTimes++;
            statusText.setText("黑方胜,黑:白 " + chessBlackVicTimes + ":" + chessWhiteVicTimes
                    + ",游戏重启,等待白方...");
        }
        else if (vicChessColor == -1)
        { // 白棋胜
            chessWhiteVicTimes++;
            statusText.setText("白方胜,黑:白 " + chessBlackVicTimes + ":" + chessWhiteVicTimes
                    + ",游戏重启,等待黑方...");
        }else if(vicChessColor == 0){
            statusText.setText("双方平局，黑:白 " + chessBlackVicTimes + ":" + chessWhiteVicTimes
                    + ",游戏重启,等待黑方...");
        }
    }

    // 取得指定棋子的位置
    public void setLocation(int xPos, int yPos, int chessColor)
    {
        if (chessColor == 1)
        { // 棋子为黑棋时
            chessBlack_XPOS[chessBlackCount] = xPos * 20;
            chessBlack_YPOS[chessBlackCount] = yPos * 20;
            chessBlackCount++;
        }
        else if (chessColor == -1)
        { // 棋子为白棋时
            chessWhite_XPOS[chessWhiteCount] = xPos * 20;
            chessWhite_YPOS[chessWhiteCount] = yPos * 20;
            chessWhiteCount++;
        }
    }

    // 判断当前状态是否为胜利状态
    public boolean checkVicStatus(int xPos, int yPos, int chessColor)
    {
        int chessLinkedCount = 1; // 连接棋子数
        int chessLinkedCompare = 1; // 用于比较是否要继续遍历一个棋子的相邻网格
        int chessToCompareIndex = 0; // 要比较的棋子在数组中的索引位置
        int closeGrid = 1; // 相邻网格的位置
        if (chessColor == 1)
        { // 黑棋时
            chessLinkedCount = 1; // 将该棋子自身算入的话，初始连接数为1
            //以下每对for循环语句为一组，因为下期的位置能位于中间而非两端
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                { // 遍历棋盘上所有黑棋子
                    if (((xPos + closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && ((yPos * 20) == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount = chessLinkedCount + 1; // 连接数加1
                        if (chessLinkedCount == 3)
                        { // 五子相连时，胜利
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {// 若中间有一个棋子非黑棋，则会进入此分支，此时无需再遍历
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                {
                    if (((xPos - closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && (yPos * 20 == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            // 进入新的一组for循环时要将连接数等重置
            chessLinkedCount = 1;
            chessLinkedCompare = 1;
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                {
                    if ((xPos * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && ((yPos + closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                {
                    if ((xPos * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && ((yPos - closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            chessLinkedCount = 1;
            chessLinkedCompare = 1;
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                {
                    if (((xPos - closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && ((yPos + closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                {
                    if (((xPos + closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && ((yPos - closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            chessLinkedCount = 1;
            chessLinkedCompare = 1;
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                {
                    if (((xPos + closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && ((yPos + closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++)
                {
                    if (((xPos - closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
                            && ((yPos - closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
        }
        else if (chessColor == -1)
        {
            chessLinkedCount = 1;
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if (((xPos + closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && (yPos * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if (((xPos - closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && (yPos * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            chessLinkedCount = 1;
            chessLinkedCompare = 1;
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if ((xPos * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && ((yPos + closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if ((xPos * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && ((yPos - closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            chessLinkedCount = 1;
            chessLinkedCompare = 1;
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if (((xPos - closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && ((yPos + closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if (((xPos + closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && ((yPos - closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            chessLinkedCount = 1;
            chessLinkedCompare = 1;
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if (((xPos + closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && ((yPos + closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return true;
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
            for (closeGrid = 1; closeGrid <= 2; closeGrid++)
            {
                for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++)
                {
                    if (((xPos - closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
                            && ((yPos - closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex]))
                    {
                        chessLinkedCount++;
                        if (chessLinkedCount == 3)
                        {
                            return (true);
                        }
                    }
                }
                if (chessLinkedCount == (chessLinkedCompare + 1)) {
                    chessLinkedCompare++;
                }
                else {
                    break;
                }
            }
        }
        return false;
    }

    // 画棋盘
    public void paint(Graphics g)
    {

        g.drawLine(40, 40, 80, 40);
        g.drawLine(40, 60, 80, 60);
        g.drawLine(40, 80, 80, 80);
        g.drawLine(40, 40, 40, 80);
        g.drawLine(60, 40, 60, 80);
        g.drawLine(80, 40, 80, 80);
    }

    // 画棋子
    public void paintFirPoint(int xPos, int yPos, int chessColor)
    {
        Blackboard firPBlack = new Blackboard(this);
        Whiteboard firPWhite = new Whiteboard(this);
        if (chessColor == 1 && isMouseEnabled)
        { // 黑棋
            // 设置棋子的位置
            setLocation(xPos, yPos, chessColor);
            // 取得当前局面状态
            isWinned = checkVicStatus(xPos, yPos, chessColor);
            if (isWinned == false&&chessWhiteCount+chessBlackCount<9)
            { // 非胜利状态

                    boardThread.sendMessage("/" + chessPeerName + " /chess "
                            + xPos + " " + yPos + " " + chessColor);
                    this.add(firPBlack); // 将棋子添加到棋盘中
                    firPBlack.setBounds(xPos * 20 - 7,
                            yPos * 20 - 7, 16, 16); // 设置棋子边界
                    statusText.setText("黑(第" + chessBlackCount + "步)"
                            + xPos + " " + yPos + ",轮到白方.");
                    isMouseEnabled = false; // 将鼠标设为不可用
                } else if (isWinned == false&&chessWhiteCount+chessBlackCount==9) {
                boardThread.sendMessage("/" + chessPeerName + " /chess "
                        + xPos + " " + yPos + " " + chessColor);
                this.add(firPBlack);
                firPBlack.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(0); // 调用胜利方法，传入参数为黑棋胜利
                isMouseEnabled = false;
            } else
            { // 胜利状态
                boardThread.sendMessage("/" + chessPeerName + " /chess "
                        + xPos + " " + yPos + " " + chessColor);
                this.add(firPBlack);
                firPBlack.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(1); // 调用胜利方法，传入参数为黑棋胜利
                isMouseEnabled = false;
            }
        }
        else if (chessColor == -1 && isMouseEnabled)
        { // 白棋
            setLocation(xPos, yPos, chessColor);
            isWinned = checkVicStatus(xPos, yPos, chessColor);
            if (isWinned == false&&chessWhiteCount+chessBlackCount<9)
            {
                boardThread.sendMessage("/" + chessPeerName + " /chess "
                        + xPos + " " + yPos + " " + chessColor);
                this.add(firPWhite);
                firPWhite.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                statusText.setText("白(第" + chessWhiteCount + "步)"
                        + xPos + " " + yPos + ",轮到黑方.");
                isMouseEnabled = false;
            } else if (isWinned == false&&chessWhiteCount+chessBlackCount==9) {
                boardThread.sendMessage("/" + chessPeerName + " /chess "
                        + xPos + " " + yPos + " " + chessColor);
                this.add(firPWhite);
                firPWhite.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(0); // 调用胜利方法，传入参数为白棋
                isMouseEnabled = false;
            } else
            {
                boardThread.sendMessage("/" + chessPeerName + " /chess "
                        + xPos + " " + yPos + " " + chessColor);
                this.add(firPWhite);
                firPWhite.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(-1); // 调用胜利方法，传入参数为白棋
                isMouseEnabled = false;
            }
        }
    }

    // 画网络棋盘
    public void paintNetFirPoint(int xPos, int yPos, int chessColor)
    {
        Blackboard firPBlack = new Blackboard(this);
        Whiteboard firPWhite = new Whiteboard(this);
        setLocation(xPos, yPos, chessColor);
        if (chessColor == 1)
        {
            isWinned = checkVicStatus(xPos, yPos, chessColor);
            if (isWinned == false&&chessWhiteCount+chessBlackCount<9)
            {
                this.add(firPBlack);
                firPBlack.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                statusText.setText("黑(第" + chessBlackCount + "步)"
                        + xPos + " " + yPos + ",轮到白方.");
                isMouseEnabled = true;
            } else if (isWinned == false&&chessWhiteCount+chessBlackCount==9) {
                boardThread.sendMessage("/" + chessPeerName + " /victory "
                        + chessColor);//djr
                this.add(firPBlack);
                firPBlack.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(0);
                isMouseEnabled = true;
            } else
            {
                boardThread.sendMessage("/" + chessPeerName + " /victory "
                        + chessColor);//djr
                this.add(firPBlack);
                firPBlack.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(1);
                isMouseEnabled = true;
            }
        }
        else if (chessColor == -1)
        {
            isWinned = checkVicStatus(xPos, yPos, chessColor);
            if (isWinned == false&&chessWhiteCount+chessBlackCount<9)
            {
                this.add(firPWhite);
                firPWhite.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                statusText.setText("白(第" + chessWhiteCount + "步)"
                        + xPos + " " + yPos + ",轮到黑方.");
                isMouseEnabled = true;
            } else if (isWinned == false&&chessWhiteCount+chessBlackCount==9) {
                boardThread.sendMessage("/" + chessPeerName + " /victory "
                        + chessColor);
                this.add(firPWhite);
                firPWhite.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(0);
                isMouseEnabled = true;
            } else
            {
                boardThread.sendMessage("/" + chessPeerName + " /victory "
                        + chessColor);
                this.add(firPWhite);
                firPWhite.setBounds(xPos * 20 - 7,
                        yPos * 20 - 7, 16, 16);
                setVicStatus(-1);
                isMouseEnabled = true;
            }
        }
    }

    // 捕获下棋事件
    public void mousePressed(MouseEvent e)
    {
        if (e.getModifiers() == InputEvent.BUTTON1_MASK)
        {
            chessX_POS = (int) e.getX();
            chessY_POS = (int) e.getY();
            int a = (chessX_POS + 10) / 20, b = (chessY_POS + 10) / 20;
            if (chessX_POS / 20 < 2 || chessY_POS / 20 < 2
                    || chessX_POS / 20 > 4 || chessY_POS / 20 > 4)
            {
                // 下棋位置不正确时，不执行任何操作
            }
            else
            {
                paintFirPoint(a, b, chessColor); // 画棋子
            }
        }
    }

    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void actionPerformed(ActionEvent e){}
}
