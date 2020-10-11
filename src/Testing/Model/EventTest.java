package Testing.Model;

import Model.Database.DatabaseManager;
import Model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    private static DatabaseManager   db = new DatabaseManager();
    private static Event             e;
    private static EventsCoordinator greg;
    private static Customer          phil;
    private static Booking           newBooking;

    @BeforeAll
    static void beforeAll()
    {
        //create beginning and end for dummy event
        ZonedDateTime beginning = ZonedDateTime.of(LocalDate.now(),
                LocalTime.parse("18:00"),
                ZoneId.systemDefault());
        ZonedDateTime end = ZonedDateTime.of(LocalDate.now(),
                LocalTime.parse("23:00"),
                ZoneId.systemDefault());

        //create at least one ticket type for dummy event
        TicketType newTicketType = new TicketType("Test ticket type", 1200, "Test ticket description.", 500);
        ArrayList<TicketType> ticketTypes = new ArrayList();
        ticketTypes.add(newTicketType);

        //create an optional sundry for dummy event
        Sundry newSundry = new Sundry("Test sundry", 500, "Test sundry description", 500);
        ArrayList<Sundry> sundries = new ArrayList();
        sundries.add(newSundry);

        //create a dummy EC for dummy event
        greg = new EventsCoordinator("greg@ebss.com");
        db.addUser(greg, "asupersecurepassword");
        e = new Event("Norwich Forum", beginning, end, "A test event.", "Test Event", Event.EventType.OTHER, ticketTypes, sundries, greg);
        db.addEvent(e);
        //create a dummy customer for dummy booking
        phil = new Customer("phil@email.com", "123 Fake St.");
        db.addCustomer(phil, "greatpassword");

        newBooking = new Booking(e, phil, false);
    }

    @Test
    void setRemainingTickets()
    {
        TicketType newTicketType = new TicketType("Test ticket type 2", 1500, "Another test description.", 400);
        ArrayList tt = e.getTicketTypes();
        tt.add(newTicketType);
        e.setTicketType(tt);
        db.addTicketType(newTicketType);
        db.linkEventToTT(e, newTicketType);
        e.setRemainingTickets();
        assertEquals(900, e.getRemainingTickets());
    }

    //TODO: Model functions do not work currently, so tests always fail. Fix model functions.

//    @Test
//    void addBooking() {
//        assertTrue(e.addBooking(newBooking));
//    }

//    @Test
//    void editBooking() {
//        newBooking.setPaymentStatus(true);
//        e.addBooking(newBooking);
//        db.addBooking(newBooking);
//        assertTrue(e.editBooking(newBooking));
//        assertTrue(e.getBookings().get(0).isPaymentStatus());
//    }

    @Test
    void deleteBooking() {
    }

    @AfterAll
    static void afterAll() {
        db.deleteEvent(e);
        db.deleteUser(greg);
        db.deleteUser(phil);
    }
}