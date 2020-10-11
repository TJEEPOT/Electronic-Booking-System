/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent a ticket contained within a booking
*/
package Model;

public class Ticket
{
    private TicketType type;
    private int ticketID;

    //constructor
    public Ticket(TicketType type)
    {
        this.type = type;
    }

    //getters

    public TicketType getType()     { return type; }
    public int getTicketID()        { return ticketID; }

    // setters
    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "type=" + type.getTicketName() +
                ", ticketID=" + ticketID +
                "}";
    }
}