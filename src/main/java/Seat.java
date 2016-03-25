import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class Seat extends TimerTask{
    public enum Status{         //An enum that keeps track of the status of a seat
        open, held, reserved
    }

    private int section;
    private int number;
    private int TTL;
    private Status state;

    public Seat(int section_, int number_, Optional<Integer> TTL_){
        section = section_;
        number = number_;
        state = Status.open;
        try{
            TTL = TTL_.get();
        } catch (NoSuchElementException e){
            TTL = 5000;
        }
    }

    /**
     * if the timer expires and this status is still held, expire it
     */
    public void run(){
        if (this.getState() == Status.held) {
            this.freeSeat();
        }
    }

    public void holdSeat(){
        state = Status.held;
        Timer expiration = new Timer(true);
        expiration.schedule(this, TTL);
    }

    public void reserveSeat(){
        state = Status.reserved;
    }

    public void freeSeat() {
        state = Status.open;
    }

    public Status getState() {
        return state;
    }

    public int getNumber() {
        return number;
    }

    public int getSection() {
        return section;
    }
}
