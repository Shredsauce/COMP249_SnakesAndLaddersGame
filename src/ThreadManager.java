// -----------------------------------------------------
// Assignment 1 due February 7
//
// Written by: Malcolm Arcand Laliberé - 26334792
// -----------------------------------------------------

/** Used for managing the threads. This is only really used for calling threadSleep. */
public class ThreadManager {
    /** Singleton instance */
    private static ThreadManager instance;

    /** @return The ThreadManager singleton. Lazy loaded. */
    public static ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }

        return instance;
    }

    /** Put the thread to sleep for specified milliseconds.
    * @param millis The number of milliseconds to put the thread to sleep for. */
    public void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
