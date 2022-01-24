public class ThreadManager {
    // Singleton
    private static ThreadManager instance;
    public static ThreadManager getInstance() {
        // Lazy load
        if (instance == null) {
            instance = new ThreadManager();
        }

        return instance;
    }

    public static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Thread createThread(Runnable target) {
        Thread thread = new Thread(target);

        return thread;
    }
}
