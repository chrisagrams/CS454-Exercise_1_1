import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Chopstick {
    int id;
    Lock lock;
    int philosopher_id = -2; // If < 0, no one has this chopstick
    public Chopstick(int id)
    {
        this.id = id;
        this.lock = new ReentrantLock();
    }

    boolean take(int philosopher_id) throws Exception {

        if (lock.tryLock()) {
            if (this.philosopher_id < 0) {
                this.philosopher_id = philosopher_id;
                return true;
            } else {
                lock.unlock();
                return false;
            }
        } else {
            return false;
        }
    }

    void put()
    {
        lock.unlock();
        this.philosopher_id = -2; // Free
    }
}
class Philosopher extends Thread {
    int id;
    String current_state;
    public Philosopher(int id)
    {
        this.id = id;
        this.current_state = "Just graduated college";
    }

    void think() throws InterruptedException {
        // Think for an amount of time...
        long duration = Math.round(Math.random() * 1000 * 5);
        System.out.println("Philosopher " + this.id + " is thinking... (Duration: " + duration/1000 + " seconds.)");
        this.current_state = "thinking";
        Thread.sleep(duration);
        this.current_state = "hungry";
    }

    void eat() throws Exception
    {
        if ((Main.get_max_turn_id() == this.id) || !Main.take_chopsticks(this.id)) { // If our chopsticks aren't available or we're being greedy, keep thinking
            System.out.println("Philosopher " + this.id + " couldn't take chopsticks.");
            return; // Go back to thinking
        }

        long duration = Math.round(Math.random() * 1000 * 5);
        System.out.println("Philosopher " + this.id + " claimed two chopsticks. Eating for "+ duration/1000 +" seconds ...");
        Thread.sleep(duration);
        System.out.println("Philosopher " + this.id + " returning chopsticks.");

        Main.put_chopsticks(this.id); // Put back two chopsticks
    }

    public void run()
    {
        // The start of the life of a new philosopher
        System.out.println("Philosopher " + id + " decided to become a philosopher for the rest of their lives.");
        while (true) {
            try {
                // Think
                this.think();
                // Eat
                this.eat();
                // Repeat
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}

public class Main {

    public static int num_of_philosophers = 5;
    public static Philosopher[] circular_table = new Philosopher[num_of_philosophers]; // Circular table containing our 5 philosophers.
    public static Chopstick [] chopsticks = new Chopstick[num_of_philosophers]; // 5 chopsticks available to our philosophers.

    public static int [] turns = new int[num_of_philosophers];

    public static boolean take_chopsticks(int philosopher_id) throws Exception
    {
        boolean c1 = chopsticks[philosopher_id].take(philosopher_id);
        boolean c2 = chopsticks[(philosopher_id + 1) % num_of_philosophers].take(philosopher_id);
        if (c1 && c2) // If we got both chopsticks, we are using up a "turn"
            turns[philosopher_id]++;
        else { // We didn't get two chopstick, so give them up.
            if(c1)
                chopsticks[philosopher_id].put();
            if(c2)
                chopsticks[(philosopher_id + 1) % num_of_philosophers].put();
        }
        return c1 && c2;
    }

    public static void put_chopsticks(int philosopher_id) throws Exception
    {
        chopsticks[philosopher_id].put();
        chopsticks[(philosopher_id + 1) % num_of_philosophers].put();
    }

    public static int get_max_turn_id() {
        int max = turns[0];
        int max_i = 0;
        for (int i = 0; i < turns.length; i++)
        {
//            System.out.println("index: " + i + " turns: "+ turns[i]); // DEBUG: print out turns
            if (turns[i] > max)
            {
                max = turns[i];
                max_i = i;
            }
        }
        return max_i;
    }

    public static void main(String[] args) {

        // Send our philosophers to university ...
        for (int i = 0; i < num_of_philosophers; i++)
            circular_table[i] = new Philosopher(i);

        // Order only 5 chopsticks from Amazon...
        for (int i = 0; i < num_of_philosophers; i++)
            chopsticks[i] = new Chopstick(i);

        // Set the turn order from 0 to N
        for (int i = 0; i < num_of_philosophers; i++)
            turns[i] = 0;

        // Our philosophers will now eat and think for the rest of their lives ...
        for (int i = 0; i < num_of_philosophers; i++)
            circular_table[i].start();

    }
}