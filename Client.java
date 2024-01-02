import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Client {

    Scanner in;
    PrintWriter out;

    JFrame frame = new JFrame("DLSUsap Client");

    JTextArea messageArea = new JTextArea(16, 25);
    JTextField textField = new JTextField(25);

    JButton sendMessage = new JButton("Send Message");
    JButton logout = new JButton("Logout");

    JLabel icon = new JLabel();

    public Client() throws IOException {

        Image img = ImageIO.read(getClass().getResource("/images/DLSU.png"));
        icon.setIcon(new ImageIcon(getClass().getResource("/images/DLSU.png")));

        textField.setEditable(true);
        frame.getContentPane().add(textField, BorderLayout.CENTER);

        messageArea.setEditable(false);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.NORTH);

        frame.getContentPane().add(sendMessage, BorderLayout.EAST);
        frame.getContentPane().add(icon, BorderLayout.WEST);
        frame.getContentPane().add(logout, BorderLayout.SOUTH);

        frame.setIconImage(img);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });

        sendMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });

        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println("/quitoramus");
                frame.dispose();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.frame.setVisible(true);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.run();
    }

    private String getIPAddress(){
        return JOptionPane.showInputDialog(frame, "Enter IP:", "IP Selection", JOptionPane.PLAIN_MESSAGE);
    }
    private Integer getPortAddress(){
        return Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter Port:", "Port Selection", JOptionPane.PLAIN_MESSAGE));
    }
    private String getName() {
        return JOptionPane.showInputDialog(frame, "Enter Display Name:", "Name Selection", JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
            String tempA = getIPAddress();
            Integer tempI = getPortAddress();

            Socket socket = new Socket(tempA, tempI);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("name")) {
                    out.println(getName());
                } else if (line.startsWith("ms")) {
                    messageArea.append(line.substring(2) + "\n");
                } else {
                    socket.close();
                }
            }
        } finally {
            frame.dispose();
        }
    }
}