/*
public class SeatHoldTimeout implements Runnable{

    private Thread t;
    private int TTL;
    private int id;

    RunnableDemo( int TTL_, int id_){
        TTL = TTL_;
        id = id_;
        System.out.println("Starting Timeout: " +  id + "With time:" + TTL_ );
    }
    public void run() {
        System.out.println("Running " +  id );
        try{
            Thread.sleep(TTL);
        } catch(InterruptedException e){
            System.out.println("Thread "+ id + " Interrupted!");
        }

        System.out.println("Thread " +  id + " timing out.");

    }

    public void start ()
    {
        System.out.println("Starting " +  id );
        if (t == null)
        {
            t = new Thread(this, Integer.toString(id));
            t.start ();
        }
    }

}
*/
