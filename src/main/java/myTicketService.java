
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

/**
 * myTicketService is an implementation of the TicketService Interface
 *
 */
public class myTicketService implements TicketService {

    private Semaphore allSeatsLock;     //A lock to use when writing to allSeats

    private ArrayList<ArrayList<Seat>> allSeats;  //A list of all the sections and the status of the seats thereof

    private HashMap<Integer, SeatHold> HoldStorage = new HashMap<>();   //An in-memory map to keep track of held seats
    private HashMap<String, SeatHold> ResvStorage = new HashMap<>();    //An in-memory map to keep track of reserved seats

    /**
     * Constructor
     * all Params are the number of seats in each section. This could be abstracted, but that is beyond the scope
     *
     * @param orchestra
     * @param main
     * @param balcony1
     * @param balcony2
     */
    public myTicketService(int orchestra, int main, int balcony1, int balcony2){
        allSeats = new ArrayList<>(4);
        allSeatsLock = new Semaphore(1);
        allSeats.add(init_seating(1, orchestra));
        allSeats.add(init_seating(2, main));
        allSeats.add(init_seating(3, balcony1));
        allSeats.add(init_seating(4, balcony2));

    }

    /**
     * Initializes a seating area and returns it
     * @param num_seats
     * @return
     */
    private ArrayList<Seat> init_seating(int section_, int num_seats){
        ArrayList<Seat> area = new ArrayList<>(num_seats);
        for (int i = 0; i < num_seats; i++){
            area.add(new Seat(section_, i, Optional.of(5000)));
        }
        return area;
    }

    // Decent visual representation of the state of the place
    public String toString(){
        String retString = "";
        for (ArrayList<Seat> area : allSeats) {
            for (Seat chair : area){
                if (chair.getState() == Seat.Status.open) {
                    retString += "[O]";
                }
                else if (chair.getState() == Seat.Status.held) {
                    retString += "[/]";
                }
                else if (chair.getState() == Seat.Status.reserved) {
                    retString += "[x]";
                }
                else {
                    retString += "[E]"; //Show an error
                }
            }
            retString += "\n";
        }
        return retString;
    }
    /**
     * The number of seats in the requested level that are neither held nor reserved
     *
     * @param venueLevel a numeric venue level identifier to limit the search
     * @return the number of tickets available on the provided level
     */
    public int numSeatsAvailable(Optional<Integer> venueLevel){
        try{
            int level = venueLevel.get();
            int total = 0;
            for (Seat seat : allSeats.get(level-1)){
                if (seat.getState().equals(Seat.Status.open)) total++;
            }
            return total;
        }
        catch (NoSuchElementException ex){
            int total = 0;
            for (ArrayList<Seat> area : allSeats){
                for (Seat seat : area){
                    if (seat.getState().equals(Seat.Status.open)) total++;
                }
            }
            return total;
        }
    }
    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats the number of seats to find and hold
     * @param minLevel the minimum venue level
     * @param maxLevel the maximum venue level
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
    information
     */
    public SeatHold findAndHoldSeats(int numSeats, Optional<Integer> minLevel,
                              Optional<Integer> maxLevel, String customerEmail){

        try{
            allSeatsLock.acquire();
        }catch(InterruptedException e){
            System.out.println("An interrupted Exception occurred when trying to hold seats");
            return null;
        }
        FoundSeats seatInfo = FindSeats(numSeats, minLevel, maxLevel);
        if (seatInfo == null){
            throw new IndexOutOfBoundsException("No consecutive seats of that size");
        }
        ArrayList<Seat> heldSeats = new ArrayList<>(numSeats);
        for (int i = 0; i < numSeats; i++){
            allSeats.get(seatInfo.section).get(seatInfo.startIndex+i).holdSeat();
            heldSeats.add(allSeats.get(seatInfo.section).get(seatInfo.startIndex+i));
        }
        allSeatsLock.release();
        SeatHold myHold = new SeatHold(heldSeats, customerEmail);
        HoldStorage.put(myHold.id, myHold);
        return myHold;
    }
    /*
      Class exclusively used to return the location of the found seats in FindSeats()
      NOTE: Could probably change this to returning a list of seats or something
     */
    private class FoundSeats{
        public int section;
        public int startIndex;
        public FoundSeats(int section_, int startIndex_){
            section=section_;
            startIndex=startIndex_;
        }
    }

    /**
     * FindSeats() returns the location and section of the first available seats of the given size.
     * Seats are given preference to find the nearest available section
     * If no space is available given the constraints (minLevel, maxLevel) then null is returned.
     * NOTE: Should probably change this to a custom Error at some point
     * @param numSeats Number of consecutive seats to find
     * @param minLevel Minimum Level acceptable by user (1 = orchestra, so min=2 means Main level or higher) (1 indexing because reasons)
     * @param maxLevel Maximum Level acceptable by user (4 = balcony2, so max=3 means balc1 or lower)
     * @return FoundSeats with section and starting index or null
     */
    private FoundSeats FindSeats(int numSeats, Optional<Integer> minLevel, Optional<Integer> maxLevel){
        int level;
        try {
            level = minLevel.get();
        } catch(NoSuchElementException e) {
            level = 1;
        }
        int max;
        try {
            max = maxLevel.get();
        } catch(NoSuchElementException e) {
            max = 4;
        }
        for (; level < max; level++){
            ArrayList<Seat> area = allSeats.get(level-1);
            int tmpLength = 0;
            for (int i = 0; i < area.size(); i++){
                if (area.get(i).getState() == Seat.Status.open){
                    tmpLength++;
                    if (tmpLength == numSeats){
                        //Need to return i-tmpLength+1 to get to beginning
                        return new FoundSeats(level-1, i-tmpLength+1);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Unused but useful feature to remove a hold
     * @param seatHoldId
     * @return true if completed successfully
     */
    public boolean removeHold(int seatHoldId){
        try{
            allSeatsLock.acquire();
        } catch (InterruptedException e){
            System.out.println("An interrupted Exception occurred when trying to remove held seats");
            return false;
        }
        SeatHold seatsToRemove = HoldStorage.remove(seatHoldId);
        for (Seat s : seatsToRemove.heldSeats){
            allSeats.get(s.getSection()).get(s.getNumber()).freeSeat();
        }
        allSeatsLock.release();
        return true;
    }
    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId the seat hold identifier
     * @param customerEmail the email address of the customer to which the seat hold
    is assigned
     * @return a reservation confirmation code
     */
    public String reserveSeats(int seatHoldId, String customerEmail){
        try{
            allSeatsLock.acquire();
        } catch (InterruptedException e){
            System.out.println("An interrupted Exception occurred when trying to reserve seats");
            return null;
        }
        SeatHold seatsToRemove = HoldStorage.remove(seatHoldId);

        for (Seat s : seatsToRemove.heldSeats) {
            if (s.getState() == Seat.Status.held) {
                allSeats.get(s.getSection()-1).get(s.getNumber()).reserveSeat();
            }
            else{
                throw(new NullPointerException("Held Reservation Expired"));
            }
        }
        String resvID = UUID.randomUUID().toString();
        ResvStorage.put(resvID, seatsToRemove);
        allSeatsLock.release();

        return resvID;
    }

    /**
     * returns the SeatHold object given a confirmation ID to confirm a reservation
     * @param confirmationID
     * @return
     */
    public SeatHold confirmResv(String confirmationID){
        return ResvStorage.get(confirmationID);
    }

    /**
     * Get the status of a given section and seat
     * @param section
     * @param seat
     * @return
     */
    public Seat.Status getStatus(int section, int seat){
        return allSeats.get(section-1).get(seat).getState();
    }
}
