/******************************************************************************
 
 Project     :  CMP-5012B - Software Engineering Project:
                EventBookingSystem.
 
 File        : DatabaseManager.java
 
 Date        : Monday 17 February 2020
 
 Author      : Martin Siddons
 
 Description : This class defines the methods used by the model classes to
 read and write to/from the SQLite database.
 
 History     :
 17/02/2020 - v1.0 - Initial setup, completed connect method. 9315b2f
 23/02/2020 - v1.1 - Completed methods down to getBookingSet. e2ce398
 27/02/2020 - v1.2 - Completed more methods and changed others. bef90a8
 29/02/2020 - v1.3 - Finished all but getBooking. 2b830f3
 01/03/2020 - v1.4 - Completed class, might break up later.
 05/06/2020 - v1.5 - Added encryption to authenticateUser().
 ******************************************************************************/
package Model.Database;

import Model.*;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;

public class DatabaseManager {
    /**
     * Sets up a connection to the database in order to send or receive data.
     *
     * @return valid connection to the database or error.
     */
    private Connection connect() {
        // String pointing to working Model.Database SQLite file
        String url = "jdbc:sqlite:src/Model/Database/EBSDatabase";
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(url);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    
    /**
     * For the login system. Checks the given username is in the database and
     * if so, retrieves the associated hashed password and salt. Then hashes the
     * given password with the salt and checks the result against the hash from
     * the database. If these match then it creates a User object.
     *
     * @param usr: the username given by the user.
     * @param pwd: the password given by the user.
     * @return User object consisting of information from the database or null
     * if the user was not found or the password was incorrect.
     */
    public User authenticateUser(String usr, String pwd) {
        String sql = "SELECT * FROM user WHERE username = ?";
        User u = null;
        usr = usr.toLowerCase();
        
        // Get the user data from the database
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setString(1, usr);
            ResultSet rs = prep.executeQuery();
            
            // If there is no DB entry matching usr, return null.
            if (! rs.next()) {
                return null;
            }
            
            // User found so fetch the salt and hash the given password.
            byte[] salt = rs.getBytes("salt"); // 16 byte salt.
            byte[] hash = Encryption.generateHash(salt, pwd);
            byte[] pass = rs.getBytes("password");
            
            // Check the password and if matching, define the new User object.
            // There can only be one user of the given username type in the DB.
            boolean match = true;
            for (int i = 0; i < 16 && match; i++) {
                if (hash[i] != pass[i]) {
                    match = false;
                }
            }
            
            if (match) {
                String acc = rs.getString("accountType");
                switch (acc) {
                    case "CUSTOMER":
                        u = new Customer(usr, rs.getString("address"));
                        u.setPassword(rs.getBytes("password"));
                        u.setSalt(rs.getBytes("salt"));
                        break;
                    case "AGENT":
                        u = new Agent(usr);
                        u.setPassword(rs.getBytes("password"));
                        u.setSalt(rs.getBytes("salt"));
                        break;
                    case "EC":
                        u = new EventsCoordinator(usr);
                        u.setPassword(rs.getBytes("password"));
                        u.setSalt(rs.getBytes("salt"));
                        break;
                }
                return u;
            }
        }
        catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Finds all the user accounts marked as Customer in the DB, puts necessary
     * info from each record into a Customer object and each object
     * into an ArrayList.
     *
     * @return: ArrayList of all Customer objects.
     */
    public ArrayList<Customer> getCustomerSet() {
        String sql = "SELECT * FROM user WHERE accountType = 'CUSTOMER'";
        
        try (Connection conn = this.connect();
             Statement stmnt = conn.createStatement();
             ResultSet rs = stmnt.executeQuery(sql)) {
            ArrayList<Customer> customers = new ArrayList<>();
            
            while (rs.next()) {
                Customer c = new Customer(
                        rs.getString("username"),
                        rs.getString("address"));
                c.setPassword(rs.getBytes("password"));
                c.setSalt(rs.getBytes("salt"));
                customers.add(c);
            }
            return customers;
        }
        catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Retrieves all accounts in the database marked as EventCoordinators and
     * returns them.
     *
     * @return : ArrayList of EC objects.
     */
    public ArrayList<EventsCoordinator> getECSet() {
        String sql = "SELECT * FROM user WHERE accountType = 'EC'";
        
        try (Connection conn = this.connect();
             Statement stmnt = conn.createStatement();
             ResultSet rs = stmnt.executeQuery(sql)) {
            ArrayList<EventsCoordinator> ecs = new ArrayList<>();
            
            while (rs.next()) {
                EventsCoordinator ec = new EventsCoordinator(rs.getString("username"));
                ec.setPassword(rs.getBytes("password"));
                ec.setSalt(rs.getBytes("salt"));
                ecs.add(ec);
            }
            return ecs;
        }
        catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Finds all events listed in the Model.Database, pulls only the necessary
     * information and creates an Event object, then places each object into an
     * ArrayList.
     *
     * @return ArrayList of all Event objects.
     */
    public ArrayList<Event> getEventSet() {
        String sql = "SELECT * FROM event";
        
        try (Connection conn = this.connect();
             Statement stmnt = conn.createStatement();
             ResultSet rs = stmnt.executeQuery(sql)) {
            ArrayList<Event> events = new ArrayList<>();
            
            while (rs.next()) {
                Event e = new Event(
                        rs.getInt("eventID"),
                        rs.getString("location"),
                        ZonedDateTime.parse(rs.getString("startTime")),
                        rs.getString("eventName"),
                        rs.getInt("remainingTickets"));
                events.add(e);
            }
            return events;
        }
        catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Matches the given Event to all relevant Bookings in the database and
     * returns the minimal amount of info from them in an ArrayList.
     *
     * @param e: Event object of the Event we're getting bookings for.
     * @return ArrayList of Booking objects.
     */
    public ArrayList<Booking> getBookingSet(Event e) {
        String sql = "SELECT * FROM booking b " +
                "JOIN user u ON b.customer = u.username " +
                "WHERE b.eventID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            // insert the Event ID into the SQL string
            prep.setInt(1, e.getEventID());
            ResultSet rs = prep.executeQuery();
            ArrayList<Booking> bookings = new ArrayList<>();
            
            while (rs.next()) {
                // if the value of paymentStatus returned from the database is
                // 0, set this boolean to false, otherwise set it to true.
                boolean paymentStatus = rs.getInt("paymentStatus") != 0;
                
                Customer customer = new Customer(
                        rs.getString("username"),
                        rs.getString("address"));
                
                Booking b = new Booking(
                        e,
                        customer,
                        paymentStatus);
                b.setBookingID(rs.getInt("bookingID"));
                b.setBookingValue(rs.getInt("bookingValue"));
                
                bookings.add(b);
            }
            return bookings;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return null;
    }
    
    /**
     * Constructs a User object from the database and returns it.
     *
     * @param username : String of user's username to be queried.
     * @return : User object of requested user or null if user not found.
     */
    public User getUser(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            // insert the Event ID into the SQL string
            prep.setString(1, username);
            ResultSet rs = prep.executeQuery();
            User user = null;
            String acc = rs.getString("accountType");
            
            // construct user objects - Customer is a separate DB call.
            switch (acc) {
                case "AGENT":
                    user = new Agent(username);
                    user.setPassword(rs.getBytes("password"));
                    user.setSalt(rs.getBytes("salt"));
                    break;
                case "EC":
                    user = new EventsCoordinator(username);
                    user.setPassword(rs.getBytes("password"));
                    user.setSalt(rs.getBytes("salt"));
                    break;
            }
            return user;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return null;
    }
    
    /**
     * As above but for Customer objects. Code checks the returned data is for
     * a customer before assignment to an object, otherwise returns null.
     *
     * @param username : username of user to return from the database.
     * @return : Customer object of the requested customer or null if not found.
     */
    public Customer getCustomer(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setString(1, username);
            ResultSet rs = prep.executeQuery();
            
            // Check that the account is actually a Customer account beforehand.
            if (rs.getString("accountType").equals("CUSTOMER")) {
                Customer c = new Customer(username, rs.getString("address"));
                c.setPassword(rs.getBytes("password"));
                c.setSalt(rs.getBytes("salt"));
                return c;
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return null;
    }
    
    /**
     * Constructs a full Event object from data in the database.
     *
     * @param eventID : int of the event to be retrieved.
     * @return : Event object as requested, or null if not found.
     */
    public Event getEvent(int eventID) {
        String sql = "SELECT * FROM event WHERE eventID = ?";
        Event e;
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, eventID);
            ResultSet rs = prep.executeQuery();
            
            e = new Event(
                    rs.getInt("eventID"),
                    rs.getString("location"),
                    ZonedDateTime.parse(rs.getString("startTime")),
                    rs.getString("eventName"),
                    rs.getInt("remainingTickets"));
            e.setEndTime(ZonedDateTime.parse(rs.getString("endTime")));
            e.setDescription(rs.getString("description"));
            e.setEventType(Event.EventType.valueOf(rs.getString("eventType")));
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
        
        // Retrieve the ticketTypes.
        sql = "SELECT * from tickettype tt JOIN event_tickettype et " +
                "ON tt.ticketTypeID = et.ticketTypeID " +
                "WHERE eventID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, eventID);
            ResultSet rs = prep.executeQuery();
            ArrayList<TicketType> temp = new ArrayList<>();
            
            while (rs.next()) {
                TicketType tt = new TicketType(
                        rs.getString("ticketName"),
                        rs.getInt("price"),
                        rs.getString("description"),
                        rs.getInt("initialStock"));
                tt.setTicketTypeID(rs.getInt("ticketTypeID"));
                tt.setRemainingStock(rs.getInt("remainingStock"));
                // tt.setTicketDesign(rs.getBlob((InputStream null)));
                // TODO: Code to handle fetching image from database.
                temp.add(tt);
            }
            if (! temp.isEmpty()) {
                e.setTicketType(temp);
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
        
        // Retrieve the sundries.
        sql = "SELECT * from sundry s " +
                "JOIN event_sundry es ON s.sundryID = es.sundryID " +
                "WHERE eventID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, eventID);
            ResultSet rs = prep.executeQuery();
            ArrayList<Sundry> temp = new ArrayList<>();
            
            while (rs.next()) {
                Sundry s = new Sundry(
                        rs.getString("sundryName"),
                        rs.getInt("price"),
                        rs.getString("description"),
                        rs.getInt("initialStock"));
                s.setSundryID(rs.getInt("sundryID"));
                s.setRemainingStock(rs.getInt("remainingStock"));
                temp.add(s);
            }
            
            if (! temp.isEmpty()) {
                e.setSundries(temp);
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
        
        // Retrieve the eventCoordinator.
        sql = "SELECT * from user u " +
                "JOIN event e ON u.username = e.eventsCoordinator " +
                "WHERE eventID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, eventID);
            ResultSet rs = prep.executeQuery();
            EventsCoordinator ec = new EventsCoordinator(
                    rs.getString("username"));
            ec.setPassword(rs.getBytes("password"));
            ec.setSalt(rs.getBytes("salt"));
            
            e.setEventCoordinator(ec);
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
        
        // Retrieve the list of bookings using the above method.
        ArrayList<Booking> bookings = getBookingSet(e);
        e.setBookings(bookings);
        return e;
    }
    
    /**
     * Checks the database for the start times of all events and returns the
     * next nearest Event in the future.
     *
     * @return : Event type for the next future event or null if no future
     * event exists.
     */
    public Event getUpcomingEvent() {
        String sql = "SELECT eventID, startTime FROM event";
        ArrayList<AbstractMap.Entry<Integer, ZonedDateTime>> dates =
                new ArrayList<>();
        //        HashMap<Integer, ZonedDateTime> dates = new HashMap<>();
        
        try (Connection conn = this.connect();
             Statement stmnt = conn.createStatement();
             ResultSet rs = stmnt.executeQuery(sql)) {
            
            // Add each returned start time to a list with it's eventID.
            while (rs.next()) {
                AbstractMap.Entry<Integer, ZonedDateTime> entry =
                        new AbstractMap.SimpleEntry<>(
                                rs.getInt("eventID"),
                                ZonedDateTime.parse(rs.getString("startTime")));
                dates.add(entry);
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
        
        // Sort the dates in order of earliest first.
        dates.sort((d1, d2) ->
                d1.getValue().isEqual(d2.getValue()) ? 0 :
                        d1.getValue().isAfter(d2.getValue()) ? 1 : - 1);
        
        // Iterate through the returned dates and find the next one later than
        // the current date.
        ZonedDateTime now = ZonedDateTime.now();
        Integer upcomingDate = null;
        Iterator<AbstractMap.Entry<Integer, ZonedDateTime>> iter =
                dates.iterator();
        while (iter.hasNext() && upcomingDate == null) {
            AbstractMap.Entry<Integer, ZonedDateTime> date = iter.next();
            if (date.getValue().isAfter(now)) {
                upcomingDate = date.getKey();
            }
        }
        if (upcomingDate == null) {
            return null;
        }
        
        // Return the event from the DB using the date found above.
        return getEvent(upcomingDate);
    }
    
    /**
     * Sum up the values of all the bookings in the database and return it.
     *
     * @return : The combined value of all bookings or -1 if the value is not
     * found in the database.
     */
    public int getAllProfits() {
        String sql = "SELECT SUM(bookingValue) FROM booking";
        int profit = 0;
        
        try (Connection conn = this.connect();
             Statement stmnt = conn.createStatement();
             ResultSet rs = stmnt.executeQuery(sql)) {
            
            // Pass the sum out and return.
            while (rs.next()) {
                profit = rs.getInt("SUM(bookingValue)");
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return - 1;
        }
        return profit;
    }
    
    /**
     * Constructs a full booking object from data held in the database.
     * Includes all ticket data.
     *
     * @param bookingID : integer corresponding to the requested bookingID
     * @return : Requested Booking object or null if not found.
     */
    public Booking getBooking(Event e, int bookingID) {
        Booking b;
        ArrayList<Ticket> tickets = new ArrayList<>();
        ArrayList<Sundry> sundries = new ArrayList<>();
        Customer c;
        boolean payStatus = false;
        
        // First, build the list of tickets.
        String sql = "SELECT * FROM ticket t " +
                "JOIN tickettype tt ON t.ticketTypeID = tt.ticketTypeID " +
                "WHERE bookingID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, bookingID);
            ResultSet rs = prep.executeQuery();
            
            while (rs.next()) {
                TicketType tt = new TicketType(
                        rs.getString("ticketName"),
                        rs.getInt("price"),
                        rs.getString("description"),
                        rs.getInt("initialStock"));
                tt.setRemainingStock(rs.getInt("remainingStock"));
                tt.setTicketTypeID(rs.getInt("ticketTypeID"));
                // tt.setTicketDesign(rs.getBlob(InputStream null));
                // TODO: Code to handle fetching image from database.
                
                Ticket t = new Ticket(tt);
                t.setTicketID(rs.getInt("ticketID"));
                tickets.add(t);
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
        
        // Next build the Sundries
        sql = "SELECT * from sundry s " +
                "JOIN event_sundry es ON s.sundryID = es.sundryID " +
                "JOIN event e ON es.eventID = e.eventID " +
                "JOIN booking b ON e.eventID = b.eventID " +
                "WHERE bookingID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, bookingID);
            ResultSet rs = prep.executeQuery();
            
            while (rs.next()) {
                Sundry s = new Sundry(
                        rs.getString("sundryName"),
                        rs.getInt("price"),
                        rs.getString("description"),
                        rs.getInt("initialStock"));
                s.setSundryID(rs.getInt("sundryID"));
                s.setRemainingStock(rs.getInt("remainingStock"));
                sundries.add(s);
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
        
        // Fetch the rest of the booking.
        sql = "SELECT customer, paymentstatus, eventID, bookingvalue, " +
                "u.address FROM booking b " +
                "JOIN user u ON b.customer = u.username " +
                "WHERE bookingID = ?";
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, bookingID);
            ResultSet rs = prep.executeQuery();
            
            c = new Customer(
                    rs.getString("customer"),
                    rs.getString("address"));
            
            int ps = rs.getInt("paymentStatus");
            payStatus = ps == 1; // set Paystatus true if ps is equal to '1'
            
            b = new Booking(e, c, payStatus);
            b.setBookingID(bookingID);
            b.setBookingValue(rs.getInt("bookingValue"));
            b.setTickets(tickets);
            if (! sundries.isEmpty()) {
                b.setSundries(sundries);
            }
            return b;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return null;
    }
    
    /**
     * Call to DB to insert a new user record.
     *
     * @param u: User object of user to add to database.
     * @param pwd: Plaintext password to encrypt and store with user object.
     * @return true if user is added to the database, false if not.
     */
    public boolean addUser(User u, String pwd) {
        String sql = "INSERT INTO user VALUES (?,?,?,0,?)"; // no address stored
        byte[] salt = Encryption.generateSalt();
        byte[] hash = Encryption.generateHash(salt, pwd);
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            // Break User object into variables and insert into the SQL string
            prep.setString(1, u.getUsername());
            prep.setBytes (2, hash);
            prep.setString(3, u.getAccountType().toString());
            prep.setBytes (4, salt);
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Same as above, but adds a Customer object to the DB rather than a generic
     * user. Required as Customer have different fields to generic user objects.
     *
     * @param c: Customer object to be added to the database.
     * @return: true if customer is added to the database, false if not.
     */
    public boolean addCustomer(Customer c, String pwd) {
        String sql = "INSERT INTO user VALUES (?,?,?,?,?)";
        byte[] salt = Encryption.generateSalt();
        byte[] hash = Encryption.generateHash(salt, pwd);
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            // Break User object into variables and insert into the SQL string
            prep.setString(1, c.getUsername());
            prep.setBytes (2, hash);
            prep.setString(3, c.getAccountType().toString());
            prep.setString(4, c.getAddress());
            prep.setBytes (5, salt);
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Call the database to insert a new tickettype record. Also assigns an ID
     * to the tt object as generated by the database. Note: This does not link
     * to an existing TicketType unless linkEventToTT is called.
     *
     * @param tt: TicketType object to be added to DB
     * @return: true if the TicketType is added to the database, false if not.
     */
    public boolean addTicketType(TicketType tt) {
        String sql = "INSERT INTO tickettype(ticketName, price, description," +
                "initialStock, remainingStock) VALUES (?,?,?,?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            // insert the given fields into the SQL string
            prep.setString(1, tt.getTicketName());
            prep.setInt(2, tt.getPrice());
            prep.setString(3, tt.getDescription());
            prep.setInt(4, tt.getInitialStock());
            prep.setInt(5, tt.getInitialStock());
            //prep.setBlob  (6, (InputStream) null); // TODO: (see trello ref).
            prep.executeUpdate();
            ResultSet rs = prep.getGeneratedKeys();
            tt.setTicketTypeID(rs.getInt(1)); // ensure the TT has an ID
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Add a new Sundry item to the database and assign the given Sundry an ID.
     * Note: This does not link with an existing event unless linkEventToSundry
     * is called.
     *
     * @param s : Sundry to be added to the database.
     * @return : True if the Sundry was added to the database, else false.
     */
    public boolean addSundry(Sundry s) {
        String sql = "INSERT INTO sundry(sundryName, price, description," +
                "initialStock, remainingStock) VALUES (?,?,?,?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            // Insert the given fields into the SQL string
            prep.setString(1, s.getSundryName());
            prep.setInt(2, s.getPrice());
            prep.setString(3, s.getDescription());
            prep.setInt(4, s.getInitialStock());
            prep.setInt(5, s.getRemainingStock());
            prep.executeUpdate();
            ResultSet rs = prep.getGeneratedKeys();
            s.setSundryID(rs.getInt(1)); // ensure the sundry has an ID
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Link a given Event and TicketType together in the database.
     *
     * @param e  : Event to be added to event_tickettype table.
     * @param tt : TicketType to be added to event_tickettype table.
     * @return : True if the insertion worked, else false.
     */
    public boolean linkEventToTT(Event e, TicketType tt) {
        String sql = "INSERT INTO event_tickettype VALUES (?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, e.getEventID());
            prep.setInt(2, tt.getTicketTypeID());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Link a given Event and Sundry together in the database.
     *
     * @param e : Event to be added to event_sundry table
     * @param s : Sundry to be added to event_sundry table
     * @return : true if the link worked, else false.
     */
    public boolean linkEventToSundry(Event e, Sundry s) {
        String sql = "INSERT INTO event_sundry VALUES (?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, e.getEventID());
            prep.setInt(2, s.getSundryID());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Call to the database to insert a new event record. Note that the
     * ticketType and Sundry must already exist before this is called.
     *
     * @param e: Event object of the event to add to the database.
     * @return: true if the event is added to the database, false if not.
     */
    public boolean addEvent(Event e) {
        String sql = "INSERT INTO event (location, startTime, endTime, " +
                "description, eventName, eventType, remainingTickets, " +
                "eventsCoordinator) VALUES (?,?,?,?,?,?,?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            // Take Event object and assign fields to SQL string
            prep.setString(1, e.getLocation());
            prep.setString(2, e.getStartTime().toString());
            prep.setString(3, e.getEndTime().toString());
            prep.setString(4, e.getDescription());
            prep.setString(5, e.getEventName());
            prep.setString(6, e.getEventType().toString());
            prep.setInt(7, e.getRemainingTickets());
            prep.setString(8, e.getEventCoordinator().getUsername());
            prep.executeUpdate();
            ResultSet rs = prep.getGeneratedKeys();
            e.setEventID(rs.getInt(1)); // ensure the event object has its ID
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
        
        // Link this event to existing TicketType(s).
        for (TicketType tt : e.getTicketTypes()) {
            if (! this.linkEventToTT(e, tt)) {
                return false;
            }
        }
        
        // Link existing sundry(s) to this event.
        for (Sundry s : e.getSundries()) {
            if (! this.linkEventToSundry(e, s)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Link a given booking and an ordered sundry together in the database.
     *
     * @param b : Booking to link.
     * @param s : Sundry to link.
     * @return : True if link works, else false.
     */
    public boolean linkBookingToSundry(Booking b, Sundry s) {
        String sql = "INSERT INTO booking_sundry VALUES (?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setInt(1, b.getBookingID());
            prep.setInt(2, s.getSundryID());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Call to the database to insert a new booking record. We assume that the
     * event this booking is tied to already exists in the database. Then add
     * the attached tickets and sundries to the database.
     *
     * @param b: Booking object of the booking to add to the database.
     * @return: true if the booking is added to the database, false if not.
     */
    public boolean addBooking(Booking b) {
        // Create an entry in the booking table
        String sql = "INSERT INTO booking (customer, paymentStatus, eventID, " +
                "bookingvalue) VALUES (?,?,?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            // Add the customer's username to the DB. It is assumed the
            // Customer already exists in the user table of the DB.
            prep.setString(1, b.getCustomer().getUsername());
            // Check the payment status of the booking object and insert
            // 1 or 0 into the DB depending on if it's true or false.
            prep.setInt(2, (b.isPaymentStatus() ? 1 : 0));
            prep.setInt(3, b.getEvent().getEventID());
            prep.setInt(4, b.getBookingValue());
            prep.executeUpdate();
            ResultSet rs = prep.getGeneratedKeys();
            b.setBookingID(rs.getInt(1)); // ensure the booking object has an ID
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
        
        // Add the tickets to the database.
        // TODO: This might need to be a separate method, in line with
        //  addTicketType and addSundry.
        sql = "INSERT INTO ticket (bookingID, ticketTypeID) " +
                "VALUES (?,?)";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            
            // execute a statement for every ticket in the batch.
            for (Ticket t : b.getTickets()) {
                prep.setInt(1, b.getBookingID());
                prep.setInt(2, (t.getType().getTicketTypeID()));
                prep.executeUpdate();
                ResultSet rs = prep.getGeneratedKeys();
                t.setTicketID(rs.getInt(1)); // ensure the ticket has an ID
            }
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
        
        // Link any sundries in the booking object to the sundries in the DB.
        for (Sundry s : b.getSundries()) {
            if (! this.linkBookingToSundry(b, s)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Overwrite the given fields in the database to update the user record.
     * Assumes salt has already been set, otherwise returns false.
     *
     * @param u: User object of the user to update in the database.
     * @return: true if the user is updated in the database, false if not.
     */
    public boolean updateUser(User u) {
        String sql = "UPDATE user SET password = ?, accountType = ?, " +
                "salt = ? WHERE username = ?";
        if (u.getSalt() == null){
            return false;
        }
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setBytes(1, u.getPassword());
            prep.setString(2, u.getAccountType().toString());
            prep.setBytes (3, u.getSalt());
            prep.setString(4, u.getUsername());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * As above, overwrite the given fields to update customer. Assumes salt has
     * already been set, otherwise returns false.
     *
     * @param c : Customer object to edit in database.
     * @return: True if customer record is amended in DB, false if not.
     */
    public boolean updateCustomer(Customer c) {
        String sql = "UPDATE user SET password = ?, accountType = ?," +
                "address = ?, salt = ? WHERE username = ?";
        if (c.getSalt() == null){
            return false;
        }
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setBytes (1, c.getPassword());
            prep.setString(2, c.getAccountType().toString());
            prep.setString(3, c.getAddress());
            prep.setBytes (4, c.getSalt());
            prep.setString(5, c.getUsername());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Updates the fields on a TicketType where required. Note: price can not
     * be changed as it could cause issues with tickets already bought.
     * Warning: Do not change initialStock without also adding that amount on
     * to remainingStock.
     *
     * @param tt : TicketType object to update in database.
     * @return : True if database is updated, else false.
     */
    public boolean updateTicketType(TicketType tt) {
        String sql = "UPDATE tickettype SET description = ?, " +
                "initialStock = ?, remainingStock = ? " +
                "WHERE ticketTypeID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setString(1, tt.getDescription());
            prep.setInt(2, tt.getInitialStock());
            prep.setInt(3, tt.getRemainingStock());
            //prep.setBlob(4, (InputStream) null);
            // TODO: Placeholder for future image-handling system.
            prep.setInt(4, tt.getTicketTypeID());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Update certain fields of a sundry in the Model.Database.
     *
     * @param s : Sundry object of existing sundry record in database
     * @return : true if the database was updated, else false.
     */
    public boolean updateSundry(Sundry s) {
        String sql = "UPDATE sundry SET sundryName = ?, description = ?, " +
                "remainingStock = ? WHERE sundryID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setString(1, s.getSundryName());
            prep.setString(2, s.getDescription());
            prep.setInt(3, s.getRemainingStock());
            prep.setInt(4, s.getSundryID());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Check the DB for an existing event and if it exists, overwrite the
     * given fields to update the event record. TicketType and Sundry must be
     * linked separately by whatever is calling this method.
     *
     * @param e: Event object of the event to update in the database.
     * @return: True if the event is updated in the database, false if not.
     */
    public boolean updateEvent(Event e) {
        String sql = "UPDATE event SET location = ?, startTime = ?," +
                "endTime = ?, description = ?, eventName = ?, eventType = ?," +
                "remainingTickets = ?, eventsCoordinator = ? WHERE eventID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setString(1, e.getLocation());
            prep.setString(2, e.getStartTime().toString());
            prep.setString(3, e.getEndTime().toString());
            prep.setString(4, e.getDescription());
            prep.setString(5, e.getEventName());
            prep.setString(6, e.getEventType().toString());
            prep.setInt(7, e.getRemainingTickets());
            prep.setString(8, e.getEventCoordinator().getUsername());
            prep.setInt(9, e.getEventID());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Check the DB for an existing booking and if it exists, overwrite the
     * given fields to update the booking record. Does not add new tickets to
     * a booking.
     *
     * @param b: Booking object of the booking to update in the Model.Database.
     * @return: true if the user is updated in the database, false if not.
     */
    public boolean updateBooking(Booking b) {
        String sql = "UPDATE booking SET paymentStatus = ?, eventID = ?, " +
                "bookingValue = ? WHERE bookingID = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            // Check the payment status of the booking object and insert
            // 1 or 0 into the DB depending on if it's true or false.
            prep.setInt(1, (b.isPaymentStatus() ? 1 : 0));
            prep.setInt(2, b.getEvent().getEventID());
            prep.setInt(3, b.getBookingValue());
            prep.setInt(4, b.getBookingID());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Check the DB for an existing user and if it exists, delete it and
     * cascade the update to all relevant tables.
     *
     * @param u: User object of the user to remove from the Model.Database.
     * @return: True if the user is removed from the database, false if not.
     */
    public boolean deleteUser(User u) {
        String sql = "DELETE FROM user WHERE username = ?";
        
        try (Connection conn = this.connect();
             PreparedStatement prep = conn.prepareStatement(sql)) {
            prep.setString(1, u.getUsername());
            prep.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Check the DB for an existing event and if it exists, delete it and
     * cascade the update to all relevant tables.
     *
     * @param e: Event object of the event to remove from the Model.Database.
     * @return: true if the event is removed from the database, false if not.
     */
    public boolean deleteEvent(Event e) {
        // First get all the bookings for this event and delete them.
        ArrayList<Booking> bookings = new ArrayList<>(getBookingSet(e));
        for (Booking b : bookings){
            deleteBooking(b.getBookingID());
        }
        
        // Next get lists of sundryIDs and tickettypeIDs related to this Event.
        ArrayList<Sundry> sundries = e.getSundries();
        ArrayList<TicketType> ticketTypes = e.getTicketTypes();
        
        // Now we can delete event_sundry, sundry, event_tickettype, tickettype
        // and event entries.
        try (Connection conn = this.connect()){
            conn.setAutoCommit(false);
            
            String sql = "DELETE FROM event_sundry WHERE eventID = ?";
            PreparedStatement prep = conn.prepareStatement(sql);
            prep.setInt(1, e.getEventID());
            prep.executeUpdate();
    
            for (Sundry s : sundries) {
                sql  = "DELETE FROM sundry WHERE sundryID = ?";
                prep = conn.prepareStatement(sql);
                prep.setInt(1, s.getSundryID());
                prep.executeUpdate();
            }
    
            sql = "DELETE FROM event_tickettype WHERE eventID = ?";
            prep = conn.prepareStatement(sql);
            prep.setInt(1, e.getEventID());
            prep.executeUpdate();
    
            for (TicketType tt : ticketTypes) {
                sql  = "DELETE FROM tickettype WHERE ticketTypeID = ?";
                prep = conn.prepareStatement(sql);
                prep.setInt(1, tt.getTicketTypeID());
                prep.executeUpdate();
            }
    
            sql = "DELETE FROM event WHERE eventID = ?";
            prep = conn.prepareStatement(sql);
            prep.setInt(1, e.getEventID());
            prep.executeUpdate();
    
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        }
        catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Check the DB for an existing booking and if it exists, delete it and
     * cascade the update to all relevant tables.
     *
     * @param bookingID: Booking to be removed from the Model.Database.
     * @return: True if the booking is removed from the database, false if not.
     */
    public boolean deleteBooking(int bookingID) {
        // Delete from booking, booking_sundry and ticket in one transaction
        try(Connection conn = this.connect()){
            conn.setAutoCommit(false);
            
            String sql = "DELETE FROM booking WHERE bookingID = ?";
            PreparedStatement prep = conn.prepareStatement(sql);
            prep.setInt(1, bookingID);
            prep.executeUpdate();
            
            sql = "DELETE FROM booking_sundry WHERE bookingID = ?";
            prep = conn.prepareStatement(sql);
            prep.setInt(1, bookingID);
            prep.executeUpdate();
            
            sql = "DELETE FROM ticket WHERE bookingID = ?";
            prep = conn.prepareStatement(sql);
            prep.setInt(1, bookingID);
            prep.executeUpdate();
            
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        }
        catch(SQLException ex){
            System.out.println("Error: " + ex.getMessage());
        }
        return false;
    }
    
    // Test Harness.
    public static void main(String[] args) {
        // authenticateUser();
        DatabaseManager db = new DatabaseManager();
        User usr = db.authenticateUser("bob", "abc");
        System.out.println("missing user = " + usr);
        User adam = db.authenticateUser("adam@ebss.com", "123");
        System.out.println(adam.getUsername() + " logged in.\n");
        
        // getCustomerSet();
        ArrayList<Customer> customers = db.getCustomerSet();
        System.out.println("Retrieved users: ");
        for (Customer c : customers) {
            System.out.println(c.getUsername() + ", with address: " +
                    c.getAddress());
        }
        
        // getEventSet();
        ArrayList<Event> events = db.getEventSet();
        System.out.println("\nRetrieved events: ");
        for (Event e : events) {
            System.out.println(
                    e.getEventID() + ": " + e.getEventName() +
                            "\nLocation: " + e.getLocation() +
                            "\nDescription: " + e.getDescription() +
                            "\nStart Time: " + e.getStartTime() +
                            "\nTickets Remaining: " + e.getRemainingTickets() +
                            "\n");
        }
        
        // getBookingSet();
        ArrayList<Booking> bookings = db.getBookingSet(events.get(0));
        System.out.println("Retrieved bookings:");
        for (Booking b : bookings) {
            System.out.println(
                    b.getBookingID() + ": " +
                            b.getCustomer().getUsername() +
                            "\nAddr: " + b.getCustomer().getAddress() +
                            "\nPayment Status: " + b.isPaymentStatus() + "\n");
        }
        
        //        // addUser();
        EventsCoordinator ec = new EventsCoordinator("bob@ebs.com");
        //        boolean addedU = db.addUser(ec);
        //        System.out.println("Added User to database? " + addedU +
        //        "\n");
        //
        //        // addTicketType();
        TicketType tt = new TicketType("Some ticket", 1500, "ticket desc", 55);
        //        boolean addedTT = db.addTicketType(tt);
        //        System.out.println("Added TicketType to DB? " + addedTT +
        //        "\n");
        //
        //        // addSundry();
        Sundry s = new Sundry("some sundry", 1000, "description here", 50);
        //        boolean addedS = db.addSundry(s);
        //        System.out.println("Added Sundry to DB? " + addedS + "\n");
        //
        //        // addEvent();
        ArrayList<TicketType> tts = new ArrayList<>();
        tts.add(tt);
        ArrayList<Sundry> sund = new ArrayList<>();
        sund.add(s);
        
        Event e = db.getEvent(4);
        //        boolean addedE = db.addEvent(e);
        //        System.out.println("Added Event to database? " + addedE +
        //        "\n");
        //
        //        // addBooking();
        Customer c = db.getCustomer("emily@fremal.ru");
        Booking b = new Booking(e, c, false);
        //        boolean addedB = db.addBooking(b);
        //        System.out.println("Added Booking to database? " + addedB +
        //        "\n");
        //
        //        // updateUser();
        c.resetPassword("abc"); // just sets the pw via setter for now.
        db.updateCustomer(c);
        //        boolean updateC = db.updateUser(c);
        //        System.out.println("Updated customer? " + updateC + "\n");
        
        // getBooking();
        Booking b2 = db.getBooking(e, 10);
        System.out.println("EventID: " + b2.getEvent().getEventID() +
                "\nBookingID: " + b2.getBookingID() +
                "\nAddress: " + b2.getCustomer().getAddress() +
                "\nValue: " + b2.getBookingValue() +
                "\nPaid?: " + b2.isPaymentStatus() +
                "\nNum of Tickets: " + b2.getTickets().size() +
                "\nNum of Sundries: " + b2.getSundries().size());
        
        System.out.println("\nCurrent total profits are: " + db.getAllProfits());
        
        ArrayList<EventsCoordinator> ecs = db.getECSet();
        for (EventsCoordinator eventsCoordinator : ecs) {
            System.out.println(eventsCoordinator.getUsername());
        }
    }
}