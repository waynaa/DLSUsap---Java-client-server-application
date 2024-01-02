import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;
import java.awt.BorderLayout;

import java.time.LocalDate;
import java.time.LocalTime;

public class Server {

    private static Set<String> names = new HashSet<>();
    private static Set<PrintWriter> users = new HashSet<>();
    public static Scanner is = new Scanner(System.in);

    public static String filename;
    static PrintStream pop;

    static JFrame frame = new JFrame("DLSUsap Server");

    static JTextArea mArea = new JTextArea(5, 25);
    static JTextArea logArea = new JTextArea(30, 25);
    static JTextArea label = new JTextArea(1, 25);

    public static void main(String[] args) throws Exception {
        LocalDate now = LocalDate.now();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Integer port = Integer.parseInt(getPort());

        mArea.setEditable(false);
        logArea.setEditable(false);
        frame.getContentPane().add(new JScrollPane(mArea), BorderLayout.NORTH);
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.getContentPane().add(new JScrollPane(logArea), BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("The chat server is running at " + port);
            mArea.append("Server running on localhost at port " + port + "\n");
            mArea.append("Logs are automatically saved\n");
            label.append("LOGS");
            filename = now + "-localhost-" + String.valueOf(port) + ".txt"; // filename for logs

            Thread hooker = new Thread(() -> pop.close());
            Runtime.getRuntime().addShutdownHook(hooker); // when exiting, itll close the prinstream so it won't cause error on the next run

            pop = new PrintStream(new FileOutputStream(filename, false)); // to make file of logs
            while (true) {
                pool.execute(new ChatHandler(listener.accept()));
                if (users.size() == 0) {
                    System.setOut(pop);
                }
            }
        }
    }

    private static String getPort() {
        return JOptionPane.showInputDialog(frame, "Enter Port:", "Port Selection", JOptionPane.PLAIN_MESSAGE);
    }

    private static class ChatHandler implements Runnable {
        private String name = null;
        private Socket socket = null;

        private Scanner in = null;
        private PrintWriter out = null;

        public ChatHandler(Socket socket) {
            this.socket = socket;
        }

        public void run(){
            try {
                LocalDate now = LocalDate.now();
                LocalTime ngayon = LocalTime.now();
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) { //for names
                    out.println("name");
                    name = in.nextLine();
                    synchronized (names) {
                        if (!name.isEmpty() && !names.contains(name)) {
                            names.add(name);
                            System.out.println(now + "-" + ngayon + ": " + name + " has joined");
                            logArea.append(now + "-" + ngayon + ": " + name + " has joined\n");
                            for (PrintWriter user : users) {
                                user.println("ms " + name + " has joined");
                            }
                            users.add(out);
                            filename = name + "-" + filename  + ".txt";
                            break;
                        }
                    }
                }

                while (true) { //dual purpose to check if logged out or message send
                    String input = in.nextLine();
                    if (input.startsWith("Logout")) { //can either type this or just press the button logout which sends this message from client
                        System.out.println(now + "-" + ngayon + ": " + name +" left");
                        logArea.append(now + "-" + ngayon + ": " + name +" left\n");
                        return;
                    }
                    if(users.size() >= 2){ //will only message if there is more than 1 user online
                        for (PrintWriter user : users) { //sending message to other user for them to print
                            user.println("ms " + name + ": " + input);
                        }
                        System.out.println(now + "-" + ngayon + ": " + name + " sent a message (" + input + ")");
                        logArea.append(now + "-" + ngayon + ": " + name + " sent a message (" + input + ")\n");
                    }
                }
            } catch (Exception e) {
            } finally { //on logout remove user
                if (out != null) {
                    users.remove(out);
                }
                if (name != null) {
                    names.remove(name);
                    for (PrintWriter user : users) {
                        user.println("ms " + name + " has left");
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}