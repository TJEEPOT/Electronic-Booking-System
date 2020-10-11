/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent an Event
*/

package Model;

import Model.Database.DatabaseManager;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Event {
    public enum EventType {
        CONCERT, PARTY, PREMIERE, LAN_PARTY, ONLINE, CLUB_EVENT, OTHER
    }

    private int                   eventID;
    private String                location;
    private ZonedDateTime         startTime;
    private ZonedDateTime         endTime;
    private String                description;
    private String                eventName;
    private EventType             eventType;
    private int                   remainingTickets;
    private ArrayList<TicketType> ticketTypes = new ArrayList<>();
    private ArrayList<Sundry>     sundries = new ArrayList<>();
    private EventsCoordinator     eventCoordinator;
    private ArrayList<Booking>    bookings = new ArrayList<>();

    //constructor
    public Event(String location, ZonedDateTime startTime,
                 ZonedDateTime endTime, String description, String eventName,
                 EventType eventType, ArrayList<TicketType> ticketTypes,
                 ArrayList<Sundry> sundries,
                 EventsCoordinator eventCoordinator) {
        this.location         = location;
        this.startTime        = startTime;
        this.endTime          = endTime;
        this.description      = description;
        this.eventName        = eventName;
        this.eventType        = eventType;
        this.ticketTypes      = ticketTypes;
        this.sundries         = sundries;
        this.eventCoordinator = eventCoordinator;
        setRemainingTickets();
    }

    // constructor for event list items
    public Event(int eventID, String location, ZonedDateTime startTime,
                 String eventName, int remainingTickets) {
        this.eventID          = eventID;
        this.location         = location;
        this.startTime        = startTime;
        this.eventName        = eventName;
        this.remainingTickets = remainingTickets;
    }

    //getters

    public int getEventID()             {return eventID;}

    public String getLocation()         {return location;}

    public ZonedDateTime getStartTime() {return startTime;}

    public ZonedDateTime getEndTime()   {return endTime;}

    public String getDescription()      {return description;}

    public String getEventName()        {return eventName;}

    public EventType getEventType()     {return eventType;}

    public int getRemainingTickets()    {return remainingTickets;}

    public ArrayList<TicketType> getTicketTypes()  {return ticketTypes;}

    public ArrayList<Sundry> getSundries()         {return sundries;}

    public EventsCoordinator getEventCoordinator() {return eventCoordinator;}

    public ArrayList<Booking> getBookings()        {return bookings;}

    //setters
    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setRemainingTickets() {
        //calculate remaining tickets every time object is constructed
        int x = 0;
        for(TicketType t : ticketTypes)
        {
            x += t.getRemainingStock();
        }
        this.remainingTickets = x;
    }

    public void setTicketType(ArrayList<TicketType> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }

    public void setSundries(ArrayList<Sundry> sundries) {
        this.sundries = sundries;
    }

    public void setEventCoordinator(EventsCoordinator eventCoordinator) {
        this.eventCoordinator = eventCoordinator;
    }

    public void setBookings(ArrayList<Booking> bookings) {
        this.bookings = bookings;
    }

    //public methods
    /**
     * Adds a booking to the list of bookings on this event and commits the
     * change to the database. Booking object must exist in the database before
     * this method is called.
     *
     * @param b : Booking to be added to this Event.
     * @return : True if booking was added to Event object and database,
     * otherwise returns false.
     */
    public boolean addBooking(Booking b) {
        ArrayList<Booking> oldBookings = new ArrayList<>(this.bookings);
        this.bookings.add(b);

        DatabaseManager db = new DatabaseManager();
        // Try to update the db and if it fails, revert the above change.
        if (!db.updateEvent(this)){
            this.bookings = oldBookings;
            return false;
        }
        return true;
    }

    /**
     * Finds the booking which has been edited and commits the change of both
     * the Booking and Event to the database.
     *
     * @param editedBooking : The edited booking object.
     * @return : True if the booking can be found, edited and changed in
     * the database, false if not.
     */
    public boolean editBooking(Booking editedBooking) {
        for (Booking b : this.bookings){
            if (b.getBookingID() == editedBooking.getBookingID()){
                Customer oldCustomer = b.getCustomer();
                b.setCustomer(editedBooking.getCustomer());
                boolean oldPaymentStatus = b.isPaymentStatus();
                b.setPaymentStatus(editedBooking.isPaymentStatus());

                DatabaseManager db = new DatabaseManager();
                // Try to update DB, if this fails, revert the change.
                if (!db.updateBooking(b)){
                    b.setCustomer(oldCustomer);
                    b.setPaymentStatus(oldPaymentStatus);
                    return false;
                }

                // Same check for updating the Event db.
                if (!db.updateEvent(this)){
                    b.setCustomer(oldCustomer);
                    b.setPaymentStatus(oldPaymentStatus);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Finds the booking which needs to be deleted and removes it from event,
     * then calls to the database to remove the booking from the database and
     * updates the Event details.
     *
     * @param bookingID : BookingID of the booking to to removed.
     * @return : True if the booking is removed from the database and event,
     * else returns false.
     */
    public boolean deleteBooking(int bookingID) {
        for (Booking b : this.bookings){
            if (b.getBookingID() == bookingID){
                ArrayList<Booking> oldBookings = new ArrayList<>(this.bookings);
                this.bookings.remove(b);

                DatabaseManager db = new DatabaseManager();
                // Try to update DB, if this fails, revert the change.
                if (!db.deleteBooking(bookingID)){
                    this.bookings = oldBookings;
                    return false;
                }

                // Same check but on updating event.
                if (!db.updateEvent(this)){
                    this.bookings = oldBookings;
                    return false;
                }
            }
        }
        return true;
    }
}
