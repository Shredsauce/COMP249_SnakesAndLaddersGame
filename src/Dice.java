import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Random;

public class Dice {
    private Random random = new Random();

    private boolean isRolling = false;
    private int currentDieValue = 6;
    private int previousDieValue = currentDieValue;
    private int maxDiePositionOffset = 8;
    private double dieAngle;

    private Thread thread;

    public void roll() {
        isRolling = true;

        // TODO: Add as thread type (replace current). Implement method from interface onThreadCanceled

        thread = ThreadManager.createThread(() -> doRollDieAnimation());
        thread.start();
    }

    public void AddActionListener(ActionEvent event) {


    }

    private void doRollDieAnimation() {
        int dieValue = animateDie();
        ThreadManager.threadSleep(500);
//            onRollDieAnimComplete(dieValue, diceRollAction);

        isRolling = false;

        System.out.println("Roll complete ");

//        onRollComplete(this, dieValue);


    }

    private int animateDie() {
        int actualRoll = flipDice();

        // Roll animation
        int lastRoll = 0;
        int numFakeRolls = 6;
        int currentFakeRollIndex = 0;

        // Make sure the fake die animation rolling doesn't get repeats
        // Roll until the number of fake rolls has exhausted, but also make sure we don't end on the same roll as the actual rolls
        // Otherwise the die appears to stall at the end of its roll
        while (currentFakeRollIndex < numFakeRolls || lastRoll == actualRoll){
            int uniqueRoll = getUniqueFlipDice(lastRoll);
            lastRoll = uniqueRoll;

            currentDieValue = uniqueRoll;

            ThreadManager.threadSleep(100);

            currentFakeRollIndex++;
        }

        currentDieValue = actualRoll;

        return actualRoll;
    }

    public int flipDice() {
        int dieSides = 6;
        return random.nextInt(1, dieSides + 1);
    }

    public int getUniqueFlipDice(int excludeValue) {
        int result = flipDice();

        if (excludeValue == result) {
            getUniqueFlipDice(excludeValue);
        }

        return result;
    }
}
