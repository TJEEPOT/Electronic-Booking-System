package Testing.Model.Database;

import Model.*;
import Model.Database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {
    
    private static DatabaseManager   db   = new DatabaseManager();
    private static EventsCoordinator dave = new EventsCoordinator("dave@ebss.com");
    private static Agent             rachel = new Agent("rachel@ebss.com");
    private static Customer          angela = new Customer("angela@stuff.com", "123 Fake St.");
    private static Event             e;
    private static Booking           newBooking;
    
    @BeforeEach
    void beforeEach() {
        db.addUser(dave, "password");
        db.addUser(rachel, "thisisapassword");
        db.addCustomer(angela, "supersecurepassword");
        
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
        
        e = new Event("Norwich Forum", beginning, end, "A test event.", "Test Event", Event.EventType.OTHER,
                ticketTypes, sundries, dave);
        db.addEvent(e);
    }
    
    @Test
    void authenticateUser() {
        assertNotNull(db.getUser("dave@ebss.com"));
    }
    
    @Test
    void getCustomerSet() {
        assertEquals("angela@stuff.com", db.getCustomerSet().get(db.getCustomerSet().size() - 1).getUsername());
    }
    
    @Test
    void getECSet() {
        assertEquals("dave@ebss.com", db.getECSet().get(db.getECSet().size() - 1).getUsername());
    }
    
    @Test
    void getEventSet() {
        assertEquals("Test Event", db.getEventSet().get(db.getEventSet().size() - 1).getEventName());
    }
    
    @Test
    void getBookingSet() {
        Booking newBooking = new Booking(e, angela, false);
        db.addBooking(newBooking);
        assertEquals("angela@stuff.com",
                db.getBookingSet(e).get(db.getBookingSet(e).size() - 1).getCustomer().getUsername());
    }
    
    @Test
    void getUser() {
        assertEquals("dave@ebss.com", db.getUser("dave@ebss.com").getUsername());
    }
    
    @Test
    void getCustomer() {
        assertEquals("angela@stuff.com", db.getCustomer("angela@stuff.com").getUsername());
    }
    
    @Test
    void getEvent() {
        int id = db.getEventSet().get(db.getEventSet().size() - 1).getEventID();
        assertEquals("Test Event", db.getEvent(id).getEventName());
    }
    
    @Test
    void getUpcomingEvent() {
        //Test event must be inserted as soonest upcoming event
        assertEquals("Test Event", db.getUpcomingEvent().getEventName());
    }
    
    @Test
    void getAllProfits() {
        assertNotEquals(- 1, db.getAllProfits());
    }
    
    @Test
    void getBooking() {
        Booking b = new Booking(e, angela, false);
        db.addBooking(b);
        assertEquals("angela@stuff.com",
                db.getBooking(e, db.getBookingSet(e).get(db.getBookingSet(e).size() - 1).getBookingID()).getCustomer()
                        .getUsername());
    }
    
    @Test
    void addUser() {
        Agent a = new Agent("joe@ebss.com");
        assertTrue(db.addUser(a, "agreatpassword"));
        db.deleteUser(a);
    }
    
    @Test
    void addCustomer() {
        Customer c = new Customer("henry@stuff.com", "234 New Town, USA");
        assertTrue(db.addCustomer(c, "fantasticpassword"));
        db.deleteUser(c);
    }
    
    @Test
    void addTicketType() {
        TicketType newTicketType = new TicketType("VIP Ticket", 2300, "A very special ticket.", 50);
        assertTrue(db.addTicketType(newTicketType));
    }
    
    @Test
    void addSundry() {
        Sundry s = new Sundry("Ice cream", 500, "A tasty treat.", 2000);
        assertTrue(db.addSundry(s));
    }
    
    @Test
    void linkEventToTT() {
        TicketType newTicketType = new TicketType("Standard Ticket", 1500, "A regular ticket.", 200);
        assertTrue(db.addTicketType(newTicketType));
        assertTrue(db.linkEventToTT(e, newTicketType));
    }
    
    @Test
    void linkEventToSundry() {
        Sundry s = new Sundry("Popcorn", 300, "A crunchy snack", 2000);
        assertTrue(db.addSundry(s));
        assertTrue(db.linkEventToSundry(e, s));
    }
    
    @Test
    void addEvent() {
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
        
        e = new Event("Norwich Forum", beginning, end, "A test event.", "Test Event", Event.EventType.OTHER,
                ticketTypes, sundries, dave);
        assertTrue(db.addEvent(e));
    }
    
    @Test
    void linkBookingToSundry() {
        Sundry s = new Sundry("Test sundry", 4000, "A test sundry.", 300);
        newBooking = new Booking(e, angela, false);
        assertTrue(db.linkBookingToSundry(newBooking, s));
    }
    
    @Test
    void addBooking() {
        newBooking = new Booking(e, angela, false);
        assertTrue(db.addBooking(newBooking));
    }
    
    @Test
    void updateUser() {
        dave.resetPassword("newpassword");
        byte[] daveHash = Encryption.generateHash(dave.getSalt(), "newpassword");
        assertTrue(db.updateUser(dave));
        assertEquals(daveHash, db.getUser("dave@ebss.com").getPassword());
    }
    
    @Test
    void updateCustomer() {
        angela.resetPassword("newpassword");
        byte[] angelaHash = Encryption.generateHash(angela.getSalt(), "newpassword");
        assertTrue(db.updateCustomer(angela));
        assertEquals(angelaHash, db.getCustomer("angela@stuff.com").getPassword());
    }
    
    @Test
    void updateTicketType() {
        TicketType newTicketType = new TicketType("Test ticket type", 3000, "Test ticket description.", 400);
        db.addTicketType(newTicketType);
        db.linkEventToTT(e, newTicketType);
        newTicketType.setDescription("Edited description.");
        assertTrue(db.updateTicketType(newTicketType));
    }
    
    @Test
    void updateSundry() {
        Sundry s = new Sundry("New sundry", 3000, "Another test sundry.", 400);
        db.addSundry(s);
        db.linkEventToSundry(e, s);
        s.setDescription("Edited description.");
        assertTrue(db.updateSundry(s));
    }
    
    @Test
    void updateEvent() {
        e.setDescription("Edited description.");
        assertTrue(db.updateEvent(e));
    }
    
    @Test
    void updateBooking() {
        Booking b = new Booking(e, angela, false);
        db.addBooking(b);
        assertTrue(db.updateBooking(b));
    }
    
    @Test
    void deleteUser() {
        assertTrue(db.deleteUser(rachel));
    }
    
    @Test
    void deleteBooking() {
        Booking b = new Booking(e, angela, false);
        db.addBooking(b);
        assertTrue(db.deleteBooking(db.getBookingSet(e).get(db.getBookingSet(e).size() - 1).getBookingID()));
    }
    
    @Test
    void deleteEvent() {
        assertTrue(db.deleteEvent(e));
    }
    
    @AfterEach
    void afterEach() {
        db.deleteUser(dave);
        db.deleteUser(rachel);
        db.deleteUser(angela);
        db.deleteEvent(e);
    }
}