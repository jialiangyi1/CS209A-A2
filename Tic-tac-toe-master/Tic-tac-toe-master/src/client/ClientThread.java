package client;

import java.io.IOException;
import java.util.StringTokenizer;


public class ClientThread extends Thread {
    public Client client;

    public ClientThread(Client client) {
        this.client = client;
    }

    public void dealWithMsg(String msgReceived) {
        if (msgReceived.startsWith("/userlist ")) { // 若取得的信息为用户列表
            StringTokenizer userToken = new StringTokenizer(msgReceived, " ");
            int userNumber = 0;
            // 清空客户端用户列表
            client.playerList.userList.removeAll();
            // 清空客户端用户下拉框
            client.playerInput.userChoice.removeAll();
            // 给客户端用户下拉框添加一个选项
            client.playerInput.userChoice.addItem("所有用户");
            while (userToken.hasMoreTokens()) { // 当收到的用户信息列表中存在数据时
                String user = userToken.nextToken(" "); // 取得用户信息
                if (userNumber > 0 && !user.startsWith("[inchess]")) { // 用户信息有效时
                    client.playerList.userList.add(user);// 将用户信息添加到用户列表中
                    client.playerInput.userChoice.addItem(user); // 将用户信息添加到用户下拉框中
                }
                userNumber++;
            }
            client.playerInput.userChoice.setSelectedIndex(0);// 下拉框默认选中所有人
        } else if (msgReceived.startsWith("/yourname ")) { // 收到的信息为用户本名时
            client.chessClientName = msgReceived.substring(10); // 取得用户本名
            client.setTitle("Tic-tac-toe Game " + "用户名:"
                    + client.chessClientName); // 设置程序Frame的标题
        } else if (msgReceived.equals("/reject")) { // 收到的信息为拒绝用户时
            try {
                client.board.statusText.setText("不能加入游戏!");
                client.playControl.cancelButton.setEnabled(false);
                client.playControl.joinButton.setEnabled(true);
                client.playControl.createButton.setEnabled(true);
            } catch (Exception ef) {
                client.playerChat.chatTextArea
                        .setText("Cannot close!");
            }
            client.playControl.joinButton.setEnabled(true);
        } else if (msgReceived.startsWith("/peer ")) { // 收到信息为游戏中的等待时
            client.board.chessPeerName = msgReceived.substring(6);
            if (client.isCreator) { // 若用户为游戏建立者
                client.board.chessColor = 1; // 设定其为黑棋先行
                client.board.isMouseEnabled = true;
                client.board.statusText.setText("黑方下...");
            } else if (client.isParticipant) { // 若用户为游戏加入者
                client.board.chessColor = -1; // 设定其为白棋后性
                client.board.statusText.setText("游戏加入，等待对手.");
            }
        } else if (msgReceived.equals("/youwin")) { // 收到信息为胜利信息
            client.isOnChess = false;
            client.board.setVicStatus(client.board.chessColor);
            client.board.statusText.setText("对手退出");
            client.board.isMouseEnabled = false;
        } else if (msgReceived.equals("/OK")) { // 收到信息为成功创建游戏
            client.board.statusText.setText("游戏创建等待对手");
        } else if (msgReceived.equals("/error")) { // 收到信息错误
            client.playerChat.chatTextArea.append("错误，退出程序.\n");
        } else {
            client.playerChat.chatTextArea.append(msgReceived + "\n");
            client.playerChat.chatTextArea.setCaretPosition(
                    client.playerChat.chatTextArea.getText().length());
        }
    }

    public void run() {
        String message = "";
        try {
            while (true) {
                // 等待聊天信息，进入wait状态
                message = client.inputStream.readUTF();
                dealWithMsg(message);
            }
        } catch (IOException es) {
        }
    }
}
