// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/** This class is used for handling the mouse events. This was used as inspiration: ttp://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/*/
public class MouseHandler extends JComponent {
    /** Constructor that adds the release and drag listeners. */
    public MouseHandler() {
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent mouseEvent) {
                GUIManager.getInstance().onMouseReleased(mouseEvent);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent mouseEvent) {
                GUIManager.getInstance().onMouseDragged(mouseEvent);
            }
        });
    }
}
