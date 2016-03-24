import java.util.ArrayList;
import java.util.UUID;

public class SeatHold {
    ArrayList<Seat> heldSeats;
    String email;
    int id;
    public SeatHold(ArrayList<Seat> seats, String email_){
        id = UUID.randomUUID().hashCode();
        heldSeats = seats;
        email = email_;
    }
}
