package Testing.Model;

import Model.Database.DatabaseManager;
import Model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    DatabaseManager db = new DatabaseManager();
    Event           e;
    Booking         b;

    @BeforeEach
    void setUp() {
        //create beginning and end for dummy event
        ZonedDateTime beginning = ZonedDateTime.of(LocalDate.now(),
                LocalTime.parse("18:00"),
                ZoneId.systemDefault());
        ZonedDateTime end = ZonedDateTime.of(LocalDate.now(),
                LocalTime.parse("23:00"),
                ZoneId.systemDefault());

        //create at least one ticket type for dummy event
        TicketType t1 = new TicketType("Test ticket type", 1200, "Test ticket description.", 500);
        TicketType t2 = new TicketType("VIP ticket", 3000, "A special type of ticket.", 50);
        ArrayList<TicketType> ticketTypes = new ArrayList();
        ticketTypes.add(t1);
        ticketTypes.add(t2);
        db.addTicketType(t1);
        db.addTicketType(t2);

        //create an optional sundry for dummy event
        Sundry s1 = new Sundry("Test sundry", 500, "Test sundry description", 500);
        Sundry s2 = new Sundry("Popcorn", 600, "A crunchy snack.", 500);
        ArrayList<Sundry> sundries = new ArrayList();
        sundries.add(s1);
        sundries.add(s2);
        db.addSundry(s1);
        db.addSundry(s2);

        e = new Event("Norwich Forum", beginning, end, "A test event.", "Test Event", Event.EventType.OTHER, ticketTypes, sundries, (EventsCoordinator) db.getUser("adam@ebss.com"));
        db.addEvent(e);

        b = new Booking(e, db.getCustomerSet().get(0), false);
        db.addBooking(b);
    }

    @AfterEach
    void tearDown() {
        db.deleteEvent(e);
    }

    @Test
    void generateTicket() {
        b.generateTicket(e.getTicketTypes().get(0), 2);
        assertEquals(2, b.getTickets().size());
    }

    @Test
    void generateSundries() {
        b.generateSundries(e.getSundries().get(0), 2);
        assertEquals(2, b.getSundries().size());
    }

    //Doesn't work because email client can't connect
    @Test
    void generateReceipt() {
        assertTrue(b.generateReceipt());
    }
}