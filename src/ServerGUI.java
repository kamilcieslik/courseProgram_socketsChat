import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ServerGUI extends JFrame implements WindowListener {
    private static final long serialVersionUID = 1L;
    private JTextField JTextFieldServerPort;
    private JTextPane JTextPaneChat;
    private JTextPane JTextPaneEventLog;
    private JButton JButtonClearChat;
    private JButton JButtonClearEventLog;
    private JLabel JLabelServerAddress;
    private JButton JButtonStartServer;
    private JTextPane JTextPaneOnlineUsersWithIPAddress;
    private Server server;
    private JButton JButtonBan;
    private JTextField JTextFieldReasonForBan;
    private JTextField JTextFieldUserAddressIP;
    private JButton JButtonUnlockIPAddress;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ServerGUI frame = new ServerGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ServerGUI() {
        addWindowListener(this);
        server = null;

        setTitle("Serwer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 1163, 440);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        setLocationRelativeTo(null);
        setResizable(false);
        contentPane.setLayout(null);

        JLabel JLabelServerPort = new JLabel("Port nas\u0142uchu:");
        JLabelServerPort.setFont(new Font("Tahoma", Font.BOLD, 11));
        JLabelServerPort.setHorizontalAlignment(SwingConstants.LEFT);
        JLabelServerPort.setBounds(10, 11, 103, 22);
        contentPane.add(JLabelServerPort);

        JTextFieldServerPort = new JTextField();
        JTextFieldServerPort.setBounds(99, 12, 75, 20);
        contentPane.add(JTextFieldServerPort);
        JTextFieldServerPort.setColumns(10);

        JButtonStartServer = new JButton("Rozpocznij dzia\u0142anie serwera");
        JButtonStartServer.addActionListener(e -> {
            int port;
            try {
                port = Integer.parseInt(JTextFieldServerPort.getText());
            } catch (Exception er) {
                AddTextToEventLog("Invalid port number\n");
                return;
            }
            try {
                server = new Server(port, ServerGUI.this);
            } catch (ClassNotFoundException ignored) {
            }
            server.start();

            JButtonClearChat.setEnabled(true);
            JButtonClearEventLog.setEnabled(true);
            JTextFieldUserAddressIP.setEnabled(true);
            JButtonUnlockIPAddress.setEnabled(true);
            JTextFieldReasonForBan.setEnabled(true);
            JButtonBan.setEnabled(true);
            JTextFieldServerPort.setEditable(false);
            JButtonStartServer.setEnabled(false);
            SetServerAddress();
        });
        JButtonStartServer.setBounds(184, 12, 200, 20);
        contentPane.add(JButtonStartServer);

        JTextPaneChat = new JTextPane();
        JTextPaneChat.setEditable(false);
        JScrollPane JScrollPaneChat = new JScrollPane(JTextPaneChat, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPaneChat.setBounds(10, 80, 374, 287);
        JScrollPaneChat.setViewportView(JTextPaneChat);
        DefaultCaret DefaultCaretChat = (DefaultCaret) JTextPaneChat.getCaret();
        DefaultCaretChat.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        contentPane.add(JScrollPaneChat);

        JLabel JLabelChat = new JLabel("Chat");
        JLabelChat.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelChat.setBounds(10, 47, 374, 22);
        contentPane.add(JLabelChat);

        JLabel JLabelEventLog = new JLabel("Rejestr zdarze\u0144");
        JLabelEventLog.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelEventLog.setBounds(400, 47, 374, 22);
        contentPane.add(JLabelEventLog);

        JTextPaneEventLog = new JTextPane();
        JTextPaneEventLog.setEditable(false);
        JScrollPane JScrollPaneEventLog = new JScrollPane(JTextPaneEventLog,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPaneEventLog.setBounds(400, 80, 374, 287);
        JScrollPaneEventLog.setViewportView(JTextPaneEventLog);
        DefaultCaret DefaultCaretEventLog = (DefaultCaret) JTextPaneEventLog.getCaret();
        DefaultCaretEventLog.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        contentPane.add(JScrollPaneEventLog);

        JButtonClearChat = new JButton("Wyczy\u015B\u0107 chat");
        JButtonClearChat.setEnabled(false);
        JButtonClearChat.addActionListener(e -> JTextPaneChat.setText(""));
        JButtonClearChat.setBounds(10, 378, 374, 23);
        contentPane.add(JButtonClearChat);

        JButtonClearEventLog = new JButton("Wyczy\u015B\u0107 rejestr zdarze\u0144");
        JButtonClearEventLog.setEnabled(false);
        JButtonClearEventLog.addActionListener(e -> JTextPaneEventLog.setText(""));
        JButtonClearEventLog.setBounds(400, 378, 374, 23);
        contentPane.add(JButtonClearEventLog);

        JLabelServerAddress = new JLabel("Adres serwera: brak - nie uruchomiono");
        JLabelServerAddress.setForeground(new Color(102, 51, 0));
        JLabelServerAddress.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelServerAddress.setFont(new Font("Tahoma", Font.BOLD, 11));
        JLabelServerAddress.setBounds(400, 11, 374, 22);
        contentPane.add(JLabelServerAddress);

        JTextPaneOnlineUsersWithIPAddress = new JTextPane();
        JTextPaneOnlineUsersWithIPAddress.setEditable(false);
        JScrollPane JScrollPaneOnlineUsersWithIPAddress = new JScrollPane(JTextPaneOnlineUsersWithIPAddress,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPaneOnlineUsersWithIPAddress.setBounds(790, 80, 355, 239);
        JScrollPaneOnlineUsersWithIPAddress.setViewportView(JTextPaneOnlineUsersWithIPAddress);
        DefaultCaret DefaultCaretOnlineUsersWithIPAddress = (DefaultCaret) JTextPaneOnlineUsersWithIPAddress.getCaret();
        DefaultCaretOnlineUsersWithIPAddress.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        contentPane.add(JScrollPaneOnlineUsersWithIPAddress);

        JButtonBan = new JButton("Dodaj IP do blokowanych");
        JButtonBan.setEnabled(false);
        JButtonBan.addActionListener(e -> {
            if ((JTextFieldUserAddressIP.getText().isEmpty()) || (JTextFieldReasonForBan.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null,
                        "Aby zbanować użytkownika w pierwszej kolejności należy wprowadzić jego adres IP oraz " +
                                "powód.");
            } else {
                server.AddIPToBannedIP(JTextFieldUserAddressIP.getText(), JTextFieldReasonForBan.getText());
                JTextFieldUserAddressIP.setText("");
                JTextFieldReasonForBan.setText("");
                server.SaveBannedIp();
            }
        });
        JButtonBan.setBounds(790, 378, 355, 23);
        contentPane.add(JButtonBan);

        JLabel JLabelOnlineUsers = new JLabel("Lista u\u017Cytkownik\u00F3w online:");
        JLabelOnlineUsers.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelOnlineUsers.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JLabelOnlineUsers.setBounds(790, 51, 355, 20);
        contentPane.add(JLabelOnlineUsers);

        JLabel JLabelReasonForBan = new JLabel("Pow\u00F3d:");
        JLabelReasonForBan.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelReasonForBan.setBounds(790, 353, 89, 22);
        contentPane.add(JLabelReasonForBan);

        JTextFieldReasonForBan = new JTextField();
        JTextFieldReasonForBan.setEnabled(false);
        JTextFieldReasonForBan.setBounds(879, 354, 266, 20);
        contentPane.add(JTextFieldReasonForBan);
        JTextFieldReasonForBan.setColumns(10);

        JLabel JLabelUserAddressIP = new JLabel("IP:");
        JLabelUserAddressIP.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelUserAddressIP.setBounds(790, 330, 89, 22);
        contentPane.add(JLabelUserAddressIP);

        JTextFieldUserAddressIP = new JTextField();
        JTextFieldUserAddressIP.setEnabled(false);
        JTextFieldUserAddressIP.setColumns(10);
        JTextFieldUserAddressIP.setBounds(879, 330, 135, 20);
        contentPane.add(JTextFieldUserAddressIP);

        JButtonUnlockIPAddress = new JButton("Odblokuj");
        JButtonUnlockIPAddress.addActionListener(e -> {
            if (JTextFieldUserAddressIP.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Aby odblokować adres IP w pierwszej kolejności musisz go wprowadzić.");
            } else {
                server.UnlockIPAddress(JTextFieldUserAddressIP.getText());
                JTextFieldUserAddressIP.setText("");
                server.SaveBannedIp();
            }
        });
        JButtonUnlockIPAddress.setEnabled(false);
        JButtonUnlockIPAddress.setBounds(1024, 330, 121, 20);
        contentPane.add(JButtonUnlockIPAddress);

        Image imageIcon = new ImageIcon(this.getClass().getResource("/resources/serverIcon.png")).getImage();
        this.setIconImage(imageIcon);

    }

    void AddTextToChat(String text) {
        String textFromChat = JTextPaneChat.getText();
        JTextPaneChat.setText(textFromChat + text);
    }

    void AddTextToEventLog(String text) {
        String textFromEventLog = JTextPaneEventLog.getText();
        JTextPaneEventLog.setText(textFromEventLog + text);
    }

    void SetEditableTextFieldWithServerPort(boolean option) {
        if (option)
            JTextFieldServerPort.setEditable(true);
        else
            JTextFieldServerPort.setEditable(false);
    }

    void RefreshOnlineUsersWithIPAddress(String onlineUsers) {
        JTextPaneOnlineUsersWithIPAddress.setText(onlineUsers);
    }

    private void SetServerAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String serverAddress = socket.getLocalAddress().getHostAddress();
            JLabelServerAddress.setText("Adres serwera: " + serverAddress);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (server != null) {
            try {
                server.stopServer();
            } catch (Exception ignored) {
            }
            server = null;
        }
        dispose();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }
}
