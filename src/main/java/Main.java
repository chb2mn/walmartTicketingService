import java.util.Optional;

public class Main {
    public static void main(String[] args){
        TicketService TS = new myTicketService(5, 10, 15, 20);
        SeatHold mySeats = TS.findAndHoldSeats(4, Optional.of(2), Optional.empty(), "chb2mn");
        TS.reserveSeats(mySeats.id, "chb2mn");
        SeatHold myCheapSeats = TS.findAndHoldSeats(15, Optional.empty(), Optional.empty(), "chb2mn");
        TS.reserveSeats(myCheapSeats.id, "chb2mn");
        System.out.println(TS);
    }
}
