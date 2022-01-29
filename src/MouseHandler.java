import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MouseHandler extends JComponent {
    // Mouse related stuff inspired by this: http://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/
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
