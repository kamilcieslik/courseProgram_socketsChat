import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.ArrayList;

public class ClientGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField JTextFieldYourNick;
    private JTextField JTextFieldServerAddress;
    private JTextField JTextFieldServerPort;
    private JLabel JLabelConnectedToServerAs;
    private JLabel JLabelCurrentUserNick;
    private JTextPane JTextPaneChat;
    private JLabel JLabelNotYetConnected;
    private JTextField JTextFieldYourMessage;
    private JLabel JLabelSoundOnOff;
    private JButton JButtonSendMessage;
    private JButton JButtonSoundOnOff;
    private JList<String> JListOnlineUsers;
    private JButton JButtonConnect;
    private JButton JButtonDisconnect;
    private JButton JButtonWriteToAll;
    private boolean userIsSelected = false;
    boolean isSoundOn = true;

    private Client client;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ClientGUI frame = new ClientGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ClientGUI() {
        setTitle("Prosty komunikator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 840, 750);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(null);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(contentPane);

        JLabel JLabelServerAddress = new JLabel("Adres serwera:");
        JLabelServerAddress.setBounds(10, 19, 105, 14);
        contentPane.add(JLabelServerAddress);

        JLabel JLabelServerPort = new JLabel("Port nas\u0142uchu:");
        JLabelServerPort.setBounds(10, 44, 105, 14);
        contentPane.add(JLabelServerPort);

        JLabel JLabelYourNick = new JLabel("Tw\u00F3j nick:");
        JLabelYourNick.setBounds(10, 69, 105, 14);
        contentPane.add(JLabelYourNick);

        JTextFieldServerAddress = new JTextField();
        JTextFieldServerAddress.setText("192.168.0.6");
        JTextFieldServerAddress.setBounds(139, 16, 113, 20);
        JTextFieldServerAddress.setColumns(10);
        contentPane.add(JTextFieldServerAddress);

        JTextFieldServerPort = new JTextField();
        JTextFieldServerPort.setText("1500");
        JTextFieldServerPort.setColumns(10);
        JTextFieldServerPort.setBounds(139, 41, 113, 20);
        contentPane.add(JTextFieldServerPort);

        JTextFieldYourNick = new JTextField();
        JTextFieldYourNick.setBounds(139, 66, 113, 20);
        JTextFieldYourNick.setColumns(10);
        contentPane.add(JTextFieldYourNick);

        JButtonConnect = new JButton("Po\u0142\u0105cz");
        JButtonConnect.addActionListener(e -> {
            if ((JTextFieldServerAddress.getText().isEmpty()) || (JTextFieldServerPort.getText().isEmpty())
                    || (JTextFieldYourNick.getText().isEmpty())) {
                JOptionPane.showMessageDialog(null, "Nie podano wystarczających danych!");
            } else {
                String serverAddress = JTextFieldServerAddress.getText();
                String username = JTextFieldYourNick.getText();
                int serverPort;
                try {
                    serverPort = Integer.parseInt(JTextFieldServerPort.getText());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Podano niepoprawny port!");
                    return;
                }
                client = new Client(serverAddress, serverPort, username, ClientGUI.this);
                if (!client.startDialog())
                    return;
                JButtonSoundOnOff.setEnabled(true);
                JButtonSendMessage.setEnabled(true);
                JLabelNotYetConnected.setVisible(false);
                JLabelConnectedToServerAs.setVisible(true);
                JLabelCurrentUserNick.setText(username);
                JLabelCurrentUserNick.setVisible(true);
                JButtonDisconnect.setEnabled(true);
                JButtonConnect.setEnabled(false);
                JTextFieldServerAddress.setEditable(false);
                JTextFieldServerPort.setEditable(false);
                JTextFieldYourNick.setEditable(false);
            }
        });
        JButtonConnect.setBounds(281, 15, 89, 32);
        contentPane.add(JButtonConnect);

        JButtonDisconnect = new JButton("Roz\u0142\u0105cz");
        JButtonDisconnect.addActionListener(e -> client.sendMessage("logout"));
        JButtonDisconnect.setEnabled(false);
        JButtonDisconnect.setBounds(281, 54, 89, 32);
        contentPane.add(JButtonDisconnect);

        JLabel JLabelOnlineUsers = new JLabel("Lista u\u017Cytkownik\u00F3w online:");
        JLabelOnlineUsers.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelOnlineUsers.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JLabelOnlineUsers.setBounds(10, 117, 360, 20);
        contentPane.add(JLabelOnlineUsers);

        JListOnlineUsers = new JList<>();
        JListOnlineUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JListOnlineUsers.getSelectionModel().addListSelectionListener(event -> {
            JButtonWriteToAll.setEnabled(true);
            userIsSelected = true;
            if (JListOnlineUsers.isSelectionEmpty()) {
                JButtonWriteToAll.setEnabled(false);
                userIsSelected = false;
            }
        });
        JScrollPane JScrollPaneOnlineUsers = new JScrollPane(JListOnlineUsers,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPaneOnlineUsers.setBounds(10, 148, 360, 522);
        JScrollPaneOnlineUsers.setViewportView(JListOnlineUsers);
        contentPane.add(JScrollPaneOnlineUsers);

        JLabelConnectedToServerAs = new JLabel("Po\u0142\u0105czono z serwerem jako:");
        JLabelConnectedToServerAs.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelConnectedToServerAs.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JLabelConnectedToServerAs.setBounds(380, 44, 242, 20);
        JLabelConnectedToServerAs.setVisible(false);
        contentPane.add(JLabelConnectedToServerAs);

        JLabelCurrentUserNick = new JLabel("");
        JLabelCurrentUserNick.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelCurrentUserNick.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JLabelCurrentUserNick.setBounds(621, 44, 203, 20);
        JLabelCurrentUserNick.setVisible(false);
        contentPane.add(JLabelCurrentUserNick);

        JLabelNotYetConnected = new JLabel("Niepo\u0142\u0105czono");
        JLabelNotYetConnected.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelNotYetConnected.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JLabelNotYetConnected.setVisible(false);
        JLabelNotYetConnected.setBounds(380, 41, 444, 20);
        JLabelNotYetConnected.setVisible(true);
        contentPane.add(JLabelNotYetConnected);

        JLabel JLabelEnterYourMessageBelow = new JLabel("Pisz poni\u017Cej");
        JLabelEnterYourMessageBelow.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelEnterYourMessageBelow.setBounds(380, 102, 444, 14);
        contentPane.add(JLabelEnterYourMessageBelow);

        JTextFieldYourMessage = new JTextField();
        JTextFieldYourMessage.setBounds(405, 119, 370, 20);
        contentPane.add(JTextFieldYourMessage);
        JTextFieldYourMessage.setColumns(10);

        JTextPaneChat = new JTextPane();
        JScrollPane JScrollPaneChat = new JScrollPane(JTextPaneChat, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPaneChat.setBounds(380, 148, 444, 522);
        JScrollPaneChat.setViewportView(JTextPaneChat);
        DefaultCaret DefaultCaretChat = (DefaultCaret) JTextPaneChat.getCaret();
        DefaultCaretChat.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        contentPane.add(JScrollPaneChat);

        JButtonSendMessage = new JButton("");
        JButtonSendMessage.addActionListener(e -> {
            if (JTextFieldYourMessage.getText().isEmpty()) {
                AddTextToChat("Komunikat: Nie można wysłać pustej wiadomości.\n");
            } else {
                if (!userIsSelected) {
                    client.sendMessage(JTextFieldYourMessage.getText());
                } else {
                    String selectedUserId = JListOnlineUsers.getSelectedValue().replaceAll("[\\D]", "");
                    client.sendMessage("328 " + selectedUserId + " " + JTextFieldYourMessage.getText());
                }
                JTextFieldYourMessage.setText("");
            }
        });
        Image imageEnvelope = new ImageIcon(this.getClass().getResource("/resources/koperta.png")).getImage();
        Image imageScaledEnvelope = imageEnvelope.getScaledInstance(45, 20, java.awt.Image.SCALE_SMOOTH);
        JButtonSendMessage.setIcon(new ImageIcon(imageScaledEnvelope));
        JButtonSendMessage.setEnabled(false);
        JButtonSendMessage.setBounds(779, 118, 45, 20);
        contentPane.add(JButtonSendMessage);

        JButtonWriteToAll = new JButton("Pisz do wszystkich");
        JButtonWriteToAll.setEnabled(false);
        JButtonWriteToAll.addActionListener(e -> {
            JListOnlineUsers.clearSelection();
            JButtonWriteToAll.setEnabled(false);
            userIsSelected = false;
        });
        JButtonWriteToAll.setBounds(10, 678, 360, 32);
        contentPane.add(JButtonWriteToAll);

        JButton JButtonClearChat = new JButton("Wyczy\u015B\u0107 chat");
        JButtonClearChat.addActionListener(e -> JTextPaneChat.setText(""));
        JButtonClearChat.setBounds(380, 678, 444, 32);
        contentPane.add(JButtonClearChat);

        JButtonSoundOnOff = new JButton("");
        JButtonSoundOnOff.addActionListener(e -> {
            if (isSoundOn) {
                isSoundOn = false;
                JLabelSoundOnOff.setText("OFF");
            } else {
                isSoundOn = true;
                JLabelSoundOnOff.setText("ON");
            }
        });
        Image imageSpeaker = new ImageIcon(this.getClass().getResource("/resources/speaker.png")).getImage();
        Image imageScaledSpeaker = imageSpeaker.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
        JButtonSoundOnOff.setIcon(new ImageIcon(imageScaledSpeaker));
        JButtonSoundOnOff.setEnabled(false);
        JButtonSoundOnOff.setBounds(380, 119, 20, 20);
        contentPane.add(JButtonSoundOnOff);

        JLabelSoundOnOff = new JLabel("ON");
        JLabelSoundOnOff.setHorizontalAlignment(SwingConstants.CENTER);
        JLabelSoundOnOff.setBounds(370, 102, 40, 14);
        contentPane.add(JLabelSoundOnOff);

        Image imageIcon = new ImageIcon(this.getClass().getResource("/resources/clientIcon.png")).getImage();
        this.setIconImage(imageIcon);

    }

    void disconnected() {
        JTextFieldServerAddress.setEditable(true);
        JTextFieldServerPort.setEditable(true);
        JTextFieldYourNick.setEditable(true);
        JLabelNotYetConnected.setVisible(true);
        JLabelConnectedToServerAs.setVisible(false);
        JLabelCurrentUserNick.setVisible(false);
        JButtonDisconnect.setEnabled(false);
        JButtonConnect.setEnabled(true);
        JButtonSendMessage.setEnabled(false);

        DefaultListModel<String> defaultListModelStudents = new DefaultListModel<>();
        defaultListModelStudents.clear();
        JListOnlineUsers.setModel(defaultListModelStudents);
    }

    void AddTextToChat(String text) {
        String textFromChat = JTextPaneChat.getText();
        JTextPaneChat.setText(textFromChat + text);
    }

    void AddTextToOnlineUsers(ArrayList<String> arrayList) {
        DefaultListModel<String> defaultListModelStudents = new DefaultListModel<>();
        for (String anArrayList : arrayList) defaultListModelStudents.addElement(anArrayList);
        JListOnlineUsers.setModel(defaultListModelStudents);
    }
}
