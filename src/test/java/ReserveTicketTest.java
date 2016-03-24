import junit.framework.TestCase;

import java.util.Optional;

public class ReserveTicketTest extends TestCase {
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

    public void testReserveAny(){
        int holdSize = 3;
        SeatHold mySeats = TS.findAndHoldSeats(holdSize, Optional.empty(), Optional.empty(), "chb2mn");
        String confirmation = TS.reserveSeats(mySeats.id, "chb2mn");
        SeatHold confirmedSeats = TS.confirmResv(confirmation);
        assertEquals(mySeats.id, confirmedSeats.id);
        for (Seat s : confirmedSeats.heldSeats){
            assertEquals(TS.getStatus(s.section, s.number), myTicketService.Status.reserved);
        }
    }
}
