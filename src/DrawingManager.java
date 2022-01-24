public class DrawingManager {
    // Singleton
    private static DrawingManager instance;
    public static DrawingManager getInstance() {
        // Lazy load
        if (instance == null) {
            instance = new DrawingManager();
        }

        return instance;
    }

}
