# Ticket Service

This is a Ticketing service system for a Theater. It features the ability to hold, reserve, and free tickets.
The Ticketing service also free tickets after 5 seconds due to a timeout (this is excessively short, but for the exercise...)

## Build Instructions

This product was made using Maven. To run:

`$ mvn package`

`$ java -cp target/Test-1.0-SNAPSHOT.jar Main`

To just run tests: (This will take at least 6 seconds due to timeout tests)

`$ mvn test`