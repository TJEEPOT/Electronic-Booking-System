/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent a booking made by a Customer for an Event
*/

package Model;

import Model.Email.EmailManager;
import java.util.ArrayList;

public class Booking {
    private Event             event; // reference to the event of this booking.
    private int               bookingID;
    private Customer          customer;
    private int               bookingValue; // value of all tickets and sundries in pence.
    private boolean           paymentStatus; // true if paid, false if not.
    private ArrayList<Ticket> tickets = new ArrayList<>();
    private ArrayList<Sundry> sundries = new ArrayList<>();

    // constructor
    public Booking(Event e, Customer customer, boolean paymentStatus) {
        this.event         = e;
        this.customer      = customer;
        this.paymentStatus = paymentStatus;
    }

    //getters
    public Event getEvent()               {return event;}

    public int getBookingID()             {return bookingID;}

    public Customer getCustomer()         {return customer;}

    public boolean isPaymentStatus()      {return paymentStatus;}

    public int getBookingValue()          {return bookingValue;}

    public ArrayList<Ticket> getTickets() {return tickets;}

    public ArrayList<Sundry> getSundries() {return sundries;}

    //setters
    public void setEvent(Event event) {
        this.event = event;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setPaymentStatus(boolean paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setBookingValue(int bookingValue) {
        this.bookingValue = bookingValue;
    }

    public void setTickets(ArrayList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void setSundries(ArrayList<Sundry> sundries) {
        this.sundries = sundries;
    }

    //public methods
    public void generateTicket(TicketType type, int quantity) {
        ArrayList<Ticket> tickets = this.getTickets();
        //generate tickets for the booking
        for(int i = 0; i < quantity; i++)
        {
            Ticket t = new Ticket(type);
            tickets.add(t);
        }
        //add them to the booking
        this.setTickets(tickets);
    }

    public void generateSundries(Sundry s, int quantity)
    {
        ArrayList<Sundry> sundries = this.getSundries();
        //generate sundries for the booking
        for(int i = 0; i < quantity; i++)
        {
            sundries.add(s);
        }
        //add them to the booking
        this.setSundries(sundries);
        //subtract the new sundries from the remaining stock of that sundry type
        s.setRemainingStock(s.getRemainingStock() - quantity);
    }
    
    /**
     * Create and send a receipt to the email address of the user who's made
     * a booking.
     *
     * @return : True if generated and sent correctly, else false.
     */
    public boolean generateReceipt(){
        EmailManager em = new EmailManager(EmailManager.Accounts.RECEIPT);
        return em.emailReceipt(this);
    }
}
