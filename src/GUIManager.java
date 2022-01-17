import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUIManager {
    private static GUIManager instance;

    private JLabel label;
    private JFrame frame;
    private JPanel panel;

    public GUIManager() {
        instance = this;

        Init();
    }

    public static GUIManager getInstance() {
        if (instance == null) {
            System.out.println("Error: No instance found. Creating it.");
            instance = new GUIManager();
        }

        return instance;
    }

    public void Init() {
        frame = new JFrame();

        // Buttons and labels from this tutorial: https://www.youtube.com/watch?v=5o3fMLPY7qY
        JButton button = new JButton("Click");
        button.addActionListener(event -> testEvent("Clicking on the click button"));
        button.setBounds(3, 49, 400, 200);

        JButton anotherButton = new JButton("Here's another button");
        anotherButton.addActionListener(event -> testEvent("Here's another buttonssss"));

        label = new JLabel("Yeahhhhh");

        panel = new JPanel();
        panel.setLayout(null);
//        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
//        panel.setLayout(new GridLayout(0, 1));
//        panel.add(button);
//        panel.add(anotherButton);
//        panel.add(label);

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Snakes and Ladders");
        frame.pack();
        frame.setSize(500, 500);
        frame.setVisible(true);
    }

    private void testEvent(String testText) {
        System.out.println(testText);
    }


}
