package app;

import system.main.MainSystem;

import javax.swing.*;
import java.awt.*;

public class App {
    private JFrame f = new JFrame();
    private JTextArea textArea = new JTextArea();
    private JButton btn = new JButton("Confirm");
    private JLabel label = new JLabel();

    public App() {
        f.setSize(600, 600);

        Container cp = f.getContentPane();
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

        cp.add(new JLabel("Enter the email's contents: "));

        textArea.setSize(900,400);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setSize(600, 400);
        scrollPane.setViewportView(textArea);
        cp.add(scrollPane);

        cp.add(btn);
        btn.addActionListener(e -> {
            label.setText((MainSystem.getInstance().predict(textArea.getText()) == 0)?"Normal":"Spam");
        });

        cp.add(label);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void show() {
        f.setVisible(true);
    }

    public static void main(String[] args) {
        App app = new App();
        app.show();
    }
}
