// -----------------------------------------------------
// Assignment 1
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

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

    public void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Thread createThread(Runnable target) {
        Thread thread = new Thread(target);

        return thread;
    }
}
