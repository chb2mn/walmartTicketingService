import junit.framework.TestCase;

import java.util.Optional;

public class CountSeatsTest  extends TestCase {
    protected TicketService TS;
    protected int orc = 5;
    protected int main = 10;
    protected int balc1 = 15;
    protected int balc2 = 20;
    protected int total;

    protected void setUp(){
        TS = new myTicketService(orc, main, balc1, balc2);
        total = orc+main+balc1+balc2;
    }
    public void testCount(){
        assertEquals(TS.numSeatsAvailable(Optional.of(1)), orc);
        assertEquals(TS.numSeatsAvailable(Optional.empty()), total);
        assertEquals(TS.numSeatsAvailable(Optional.of(3)), balc1);
    }
}
