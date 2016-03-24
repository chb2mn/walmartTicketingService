
import java.util.*;
import java.util.concurrent.Semaphore;

public class myTicketService implements TicketService {
    public enum Status{
        open, held, reserved
    }
    private Semaphore allSeatsLock;

    private ArrayList<ArrayList<Status>> allSeats;

    private HashMap<Integer, SeatHold> HoldStorage = new HashMap<>();
    private HashMap<String, SeatHold> ResvStorage = new HashMap<>();

    public myTicketService(int orchestra, int main, int balcony1, int balcony2){
        allSeats = new ArrayList<>(4);
        allSeatsLock = new Semaphore(1);
        allSeats.add(init_seating(orchestra));
        allSeats.add(init_seating(main));
        allSeats.add(init_seating(balcony1));
        allSeats.add(init_seating(balcony2));

    }

    private ArrayList<Status> init_seating(int num_seats){
        ArrayList<Status> area = new ArrayList<>(num_seats);
        for (int i = 0; i < num_seats; i++){
            area.add(Status.open);
        }
        return area;
    }

    public String toString(){
        String retString = "";
        for (ArrayList<Status> area : allSeats) {
            for (Status chair : area){
                if (chair == Status.open) {
                    retString += "[O]";
                }
                else if (chair == Status.held) {
                    retString += "[/]";
                }
                else if (chair == Status.reserved) {
                    retString += "[x]";
                }
                else {
                    retString += "[E]";
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
            for (Status seatState : allSeats.get(level-1)){
                if (seatState.equals(Status.open)) total++;
            }
            return total;
        }
        catch (NoSuchElementException ex){
            int total = 0;
            for (ArrayList<Status> area : allSeats){
                for (Status seatState : area){
                    if (seatState.equals(Status.open)) total++;
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
        //FindSeats
        FoundSeats seatInfo = FindSeats(numSeats, minLevel, maxLevel);
        if (seatInfo == null){
            throw new IndexOutOfBoundsException("No consecutive seats of that size");
        }
        ArrayList<Seat> heldSeats = new ArrayList<>(numSeats);
        for (int i = 0; i < numSeats; i++){
            allSeats.get(seatInfo.section).set(seatInfo.startIndex+i, Status.held);
            heldSeats.add(new Seat(seatInfo.section, seatInfo.startIndex+i));
        }
        allSeatsLock.release();
        SeatHold myHold = new SeatHold(heldSeats, customerEmail);
        HoldStorage.put(myHold.id, myHold);
        return myHold;
    }

    private class FoundSeats{
        public int section;
        public int startIndex;
        public FoundSeats(int section_, int startIndex_){
            section=section_;
            startIndex=startIndex_;
        }
    }

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
            ArrayList area = allSeats.get(level-1);
            int tmpLength = 0;
            for (int i = 0; i < area.size(); i++){
                if (area.get(i) == Status.open){
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


    public boolean removeHold(int seatHoldId){
        try{
            allSeatsLock.acquire();
        } catch (InterruptedException e){
            System.out.println("An interrupted Exception occurred when trying to remove held seats");
            return false;
        }
        SeatHold seatsToRemove = HoldStorage.remove(seatHoldId);
        for (Seat s : seatsToRemove.heldSeats){
            allSeats.get(s.section).set(s.number, Status.open);
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
            allSeats.get(s.section).set(s.number, Status.reserved);
        }
        String resvID = UUID.randomUUID().toString();
        ResvStorage.put(resvID, seatsToRemove);
        allSeatsLock.release();

        return resvID;
    }

    public SeatHold confirmResv(String confirmationID){
        return ResvStorage.get(confirmationID);
    }

    public Status getStatus(int section, int seat){
        return allSeats.get(section).get(seat);
    }
}
