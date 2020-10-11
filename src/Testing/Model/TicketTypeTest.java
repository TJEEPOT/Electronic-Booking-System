package Testing.Model;

import Model.TicketType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTypeTest {

    @Test
    void setTicketDesign() {
        TicketType t = new TicketType("Test ticket type", 5000, "This is a test.", 500);
        t.setPathToTicketDesign("/TicketDesigns/VIP.jpg");
        assertNotNull(t.getPathToTicketDesign());
    }
}