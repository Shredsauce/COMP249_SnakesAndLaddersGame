import javax.swing.*;
import java.awt.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class DrawingManager extends JPanel {

    private String textToDisplay;
    private JFrame frame;

    // Singleton
    private static DrawingManager instance;
    public static DrawingManager getInstance() {
//         Lazy load
//        if (instance == null) {
//            instance = new DrawingManager();
//        }

        return instance;
    }

    public DrawingManager (JFrame frame) {
        instance = this;
        this.frame = frame;
        frame.getContentPane().add(this, BorderLayout.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        graphicsLoop(g);
    }

    private void graphicsLoop(Graphics g) {
        System.out.println("asdf");
        Graphics2D g2d = (Graphics2D) g;

        repaint();
    }

}
