package Testing.Model;

import Model.Ticket;
import Model.TicketType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private TicketType testType   = new TicketType("VIP ticket", 500, "A special ticket.", 200);
    private Ticket     testTicket = new Ticket(testType);

    @Test
    void testToString() {
        assertEquals("Ticket{type=VIP ticket, ticketID=0}", testTicket.toString());
    }
}