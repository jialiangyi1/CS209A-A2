package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import gui.PlayerChat;
import gui.PlayControl;
import gui.PlayerInput;
import gui.PlayerList;
import pad.board;


public class Client extends Frame implements ActionListener,KeyListener {

    Socket clientSocket;

    DataInputStream inputStream;
    // 数据输出流
    DataOutputStream outputStream;

    String chessClientName = null;

    String host = null;

    int port = 4331;

    boolean isOnChat = false;

    boolean isOnChess = false;

    boolean isGameConnected = false;

    boolean isCreator = false;

    boolean isParticipant = false;

    PlayerList playerList = new PlayerList();

    PlayerChat playerChat = new PlayerChat();

    PlayControl playControl = new PlayControl();

    PlayerInput playerInput = new PlayerInput();

    board board = new board();

    Panel southPanel = new Panel();
    Panel northPanel = new Panel();
    Panel centerPanel = new Panel();
    Panel eastPanel = new Panel();

    // 构造方法，创建界面
    public Client()
    {
        super("Tic-tac-toe Game");
        setLayout(new BorderLayout());
        host = playControl.ipInputted.getText();

        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(playerList, BorderLayout.NORTH);
        eastPanel.add(playerChat, BorderLayout.CENTER);
        eastPanel.setBackground(Color.LIGHT_GRAY);

        playerInput.contentInputted.addKeyListener(this);

        board.host = playControl.ipInputted.getText();
        centerPanel.add(board, BorderLayout.CENTER);
        centerPanel.add(playerInput, BorderLayout.SOUTH);
        centerPanel.setBackground(Color.LIGHT_GRAY);
        playControl.connectButton.addActionListener(this);
        playControl.createButton.addActionListener(this);
        playControl.joinButton.addActionListener(this);
        playControl.cancelButton.addActionListener(this);
        playControl.exitButton.addActionListener(this);
        playControl.createButton.setEnabled(false);
        playControl.joinButton.setEnabled(false);
        playControl.cancelButton.setEnabled(false);

        southPanel.add(playControl, BorderLayout.CENTER);
        southPanel.setBackground(Color.LIGHT_GRAY);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (isOnChat)
                { // 聊天中
                    try
                    { // 关闭客户端套接口
                        clientSocket.close();
                    }
                    catch (Exception ed){}
                }
                if (isOnChess || isGameConnected)
                { // 下棋中
                    try
                    { // 关闭下棋端口
                        board.chessSocket.close();
                    }
                    catch (Exception ee){}
                }
                System.exit(0);
            }
        });

        add(eastPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
        pack();
        setSize(670, 560);
        setVisible(true);
        setResizable(false);
        this.validate();
    }

    // 按指定的IP地址和端口连接到服务器
    public boolean connectToServer(String serverIP, int serverPort) throws Exception
    {
        try
        {
            // 创建客户端套接口
            clientSocket = new Socket(serverIP, serverPort);
            // 创建输入流
            inputStream = new DataInputStream(clientSocket.getInputStream());
            // 创建输出流
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            // 创建客户端线程
            ClientThread clientthread = new ClientThread(this);
            // 启动线程，等待聊天信息
            clientthread.start();
            isOnChat = true;
            return true;
        }
        catch (IOException ex)
        {
            playerChat.chatTextArea
                    .setText("不能连接!\n");
        }
        return false;
    }

    // 客户端事件处理
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == playControl.connectButton)
        { // 连接到主机按钮单击事件
            host = board.host = playControl.ipInputted.getText(); // 取得主机地址
            try
            {
                if (connectToServer(host, port))
                { // 成功连接到主机时，设置客户端相应的界面状态
                    playerChat.chatTextArea.setText("");
                    playControl.connectButton.setEnabled(false);
                    playControl.createButton.setEnabled(true);
                    playControl.joinButton.setEnabled(true);
                    board.statusText.setText("连接成功，请等待!");
                }
            }
            catch (Exception ei)
            {
                playerChat.chatTextArea
                        .setText("不能连接!\n");
            }
        }
        if (e.getSource() == playControl.exitButton)
        { // 离开游戏按钮单击事件
            if (isOnChat)
            { // 若用户处于聊天状态中
                try
                { // 关闭客户端套接口
                    clientSocket.close();
                }
                catch (Exception ed){}
            }
            if (isOnChess || isGameConnected)
            { // 若用户处于游戏状态中
                try
                { // 关闭游戏端口
                    board.chessSocket.close();
                }
                catch (Exception ee){}
            }
            System.exit(0);
        }
        if (e.getSource() == playControl.joinButton)
        { // 加入游戏按钮单击事件
            String selectedUser = (String) playerList.userList.getSelectedItem(); // 取得要加入的游戏
            if (selectedUser == null || selectedUser.startsWith("[inchess]") ||
                    selectedUser.equals(chessClientName))
            { // 若未选中要加入的用户，或选中的用户已经在游戏，则给出提示信息
                board.statusText.setText("必须选择一个用户!");
            }
            else
            { // 执行加入游戏的操作
                try
                {
                    if (!isGameConnected)
                    { // 若游戏套接口未连接
                        if (board.connectServer(board.host, board.port))
                        { // 若连接到主机成功
                            isGameConnected = true;
                            isOnChess = true;
                            isParticipant = true;
                            playControl.createButton.setEnabled(false);
                            playControl.joinButton.setEnabled(false);
                            playControl.cancelButton.setEnabled(true);
                            board.boardThread.sendMessage("/joingame "
                                    + (String) playerList.userList.getSelectedItem() + " "
                                    + chessClientName);
                        }
                    }
                    else
                    { // 若游戏端口连接中
                        isOnChess = true;
                        isParticipant = true;
                        playControl.createButton.setEnabled(false);
                        playControl.joinButton.setEnabled(false);
                        playControl.cancelButton.setEnabled(true);
                        board.boardThread.sendMessage("/joingame "
                                + (String) playerList.userList.getSelectedItem() + " "
                                + chessClientName);
                    }
                }
                catch (Exception ee)
                {
                    isGameConnected = false;
                    isOnChess = false;
                    isParticipant = false;
                    playControl.createButton.setEnabled(true);
                    playControl.joinButton.setEnabled(true);
                    playControl.cancelButton.setEnabled(false);
                    playerChat.chatTextArea
                            .setText("不能连接: \n" + ee);
                }
            }
        }
        if (e.getSource() == playControl.createButton)
        { // 创建游戏按钮单击事件
            try
            {
                if (!isGameConnected)
                { // 若游戏端口未连接
                    if (board.connectServer(board.host, board.port))
                    { // 若连接到主机成功
                        isGameConnected = true;
                        isOnChess = true;
                        isCreator = true;
                        playControl.createButton.setEnabled(false);
                        playControl.joinButton.setEnabled(false);
                        playControl.cancelButton.setEnabled(true);
                        board.boardThread.sendMessage("/creatgame "
                                + "[inchess]" + chessClientName);
                    }
                }
                else
                { // 若游戏端口连接中
                    isOnChess = true;
                    isCreator = true;
                    playControl.createButton.setEnabled(false);
                    playControl.joinButton.setEnabled(false);
                    playControl.cancelButton.setEnabled(true);
                    board.boardThread.sendMessage("/creatgame "
                            + "[inchess]" + chessClientName);
                }
            }
            catch (Exception ec)
            {
                isGameConnected = false;
                isOnChess = false;
                isCreator = false;
                playControl.createButton.setEnabled(true);
                playControl.joinButton.setEnabled(true);
                playControl.cancelButton.setEnabled(false);
                ec.printStackTrace();
                playerChat.chatTextArea.setText("不能连接: \n"
                        + ec);
            }
        }
        if (e.getSource() == playControl.cancelButton)
        { // 退出游戏按钮单击事件
            if (isOnChess)
            { // 游戏中
                board.boardThread.sendMessage("/giveup " + chessClientName);
                board.setVicStatus(-1 * board.chessColor);
                playControl.createButton.setEnabled(true);
                playControl.joinButton.setEnabled(true);
                playControl.cancelButton.setEnabled(false);
                board.statusText.setText("请创建或加入游戏!");
            }
            if (!isOnChess)
            { // 非游戏中
                playControl.createButton.setEnabled(true);
                playControl.joinButton.setEnabled(true);
                playControl.cancelButton.setEnabled(false);
                board.statusText.setText("请创建或加入游戏!");
            }
            isParticipant = isCreator = false;
        }
    }

    public void keyPressed(KeyEvent e)
    {
        TextField inputwords = (TextField) e.getSource();
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        { // 处理回车按键事件
            if (playerInput.userChoice.getSelectedItem().equals("所有用户"))
            { // 给所有人发信息
                try
                {
                    // 发送信息
                    outputStream.writeUTF(inputwords.getText());
                    inputwords.setText("");
                }
                catch (Exception ea)
                {
                    playerChat.chatTextArea
                            .setText("不能连接到服务器!\n");
                    playerList.userList.removeAll();
                    playerInput.userChoice.removeAll();
                    inputwords.setText("");
                    playControl.connectButton.setEnabled(true);
                }
            }
            else
            { // 给指定人发信息
                try
                {
                    outputStream.writeUTF("/" + playerInput.userChoice.getSelectedItem()
                            + " " + inputwords.getText());
                    inputwords.setText("");
                }
                catch (Exception ea)
                {
                    playerChat.chatTextArea
                            .setText("不能连接到服务器!\n");
                    playerList.userList.removeAll();
                    playerInput.userChoice.removeAll();
                    inputwords.setText("");
                    playControl.connectButton.setEnabled(true);
                }
            }
        }
    }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public static void main(String args[])
    {
        Client chessClient = new Client();
    }
}
