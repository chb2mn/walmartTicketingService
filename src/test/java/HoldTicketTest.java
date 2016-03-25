import junit.framework.TestCase;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Optional;
import java.util.UUID;

public class HoldTicketTest extends TestCase{
    protected myTicketService TS;
    protected int orc = 5;
    protected int main = 10;
    protected int balc1 = 15;
    protected int balc2 = 20;
    protected int total;

    protected void setUp(){
        TS = new myTicketService(orc, main, balc1, balc2);
        total = orc+main+balc1+balc2;
    }
    public void testHoldAny(){
        int holdSize = 3;
        SeatHold mySeats = TS.findAndHoldSeats(holdSize, Optional.empty(), Optional.empty(), "chb2mn");
        assertEquals(TS.numSeatsAvailable(Optional.empty()), total-holdSize);
        assertEquals(TS.numSeatsAvailable(Optional.of(1)), orc-holdSize);
        assertEquals(mySeats.heldSeats.size(), holdSize);
        for (Seat s : mySeats.heldSeats){
            assertEquals(Seat.Status.held, TS.getStatus(s.getSection(), s.getNumber()));
        }
    }
    public void testHoldWithMin(){
        int holdSize = 3;
        SeatHold mySeats = TS.findAndHoldSeats(holdSize, Optional.of(2), Optional.empty(), "chb2mn");
        assertEquals(TS.numSeatsAvailable(Optional.empty()), total-holdSize);
        assertEquals(TS.numSeatsAvailable(Optional.of(2)), main-holdSize);
        assertEquals(TS.numSeatsAvailable(Optional.of(1)), orc);
        assertEquals(holdSize, mySeats.heldSeats.size());
    }
    public void testHoldWithMax(){
        int holdSize = 3;
        SeatHold mySeats = TS.findAndHoldSeats(holdSize, Optional.of(3), Optional.of(4), "chb2mn");
        assertEquals(TS.numSeatsAvailable(Optional.empty()), total-holdSize);
        assertEquals(TS.numSeatsAvailable(Optional.of(3)), balc1-holdSize);
        assertEquals(TS.numSeatsAvailable(Optional.of(1)), orc);
        assertEquals(mySeats.heldSeats.size(), holdSize);
    }
    public void testHoldMany(){
        int holdSize = 11;
        SeatHold mySeats = TS.findAndHoldSeats(holdSize, Optional.empty(), Optional.empty(), "chb2mn");
        assertEquals(TS.numSeatsAvailable(Optional.empty()), total-holdSize);
        assertEquals(TS.numSeatsAvailable(Optional.of(1)), orc);
        assertEquals(TS.numSeatsAvailable(Optional.of(3)), balc1-holdSize);
        assertEquals(mySeats.heldSeats.size(), holdSize);
    }
    public void testHoldTooMany(){
        int holdSize = 21;
        try {
            TS.findAndHoldSeats(holdSize, Optional.empty(), Optional.empty(), "chb2mn");
            fail("should have failed with Index OoB exception");
        } catch (IndexOutOfBoundsException e){
            assertEquals(e.getMessage(), "No consecutive seats of that size");
        }
    }
    public void testHoldAlreadyFull(){
        int holdSize = 3;
        TS.findAndHoldSeats(holdSize, Optional.empty(), Optional.empty(), "chb2mn");
        try {
            TS.findAndHoldSeats(holdSize, Optional.empty(), Optional.of(1), "chb2mn");
            fail("should have failed with Index OoB exception");
        } catch (IndexOutOfBoundsException e){
            assertEquals(e.getMessage(), "No consecutive seats of that size");
        }
    }
    //This handles the test cases in which a timeout occurs
    public void testHoldTimeout(){
        int holdSize = 3;
        SeatHold timedSeats = TS.findAndHoldSeats(holdSize, Optional.empty(), Optional.empty(), "chb2mn");
        try {
            Thread.sleep(6000); //Sleep for too long
        } catch (InterruptedException e){
            fail("interrupt occurred");
        }
        //The seats should now be open
        for(Seat s : timedSeats.heldSeats){
            assertEquals(Seat.Status.open, s.getState());
            assertEquals(Seat.Status.open, TS.getStatus(s.getSection(), s.getNumber()));
        }
        //And reserving should fail
        try{
            TS.reserveSeats(timedSeats.id, "chb2mn");
            fail("should have failed with NullPointerException");
        } catch(NullPointerException e){
            assertEquals(e.getMessage(), "Held Reservation Expired");
        }

    }

}
