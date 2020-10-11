package Model.Email;

import Model.Database.DatabaseManager;
import Model.Booking;
import Model.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailManagerTest {
    private EmailManager em = new EmailManager(EmailManager.Accounts.RECEIPT);
    private DatabaseManager db = new DatabaseManager();
    private Event e = db.getEvent(4);
    private Booking b = db.getBooking(e, 12);

    @Test
    void emailReceipt() {
        assertTrue(em.emailReceipt(b));
    }

    @Test
    void registerUser() {
    }
}