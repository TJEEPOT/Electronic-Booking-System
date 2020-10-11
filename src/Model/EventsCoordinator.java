/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent an Events Coordinator
*/

package Model;

import Model.Database.*;

public class EventsCoordinator extends User {
    //constructor
    public EventsCoordinator(String username) {
        super(username, AccountType.EC);
    }
    
    //public methods

    /**
     * Sets the accountType of the given User object and pushes the change
     * to the database. Note: only users logged in on an EventsCoordinator
     * account can invoke this method.
     *
     * @param u : Account of the currently-logged-in user.
     * @param a : AccountType to set the provided user object AccountType to.
     * @return : True if accountType is changed in the DB, false if not.
     */
    // setters
    public boolean setAccountTypeOf(User u, AccountType a) {
        this.accountType = a;
        DatabaseManager db = new DatabaseManager();
        return db.updateUser(this); // return the value from DB call.
    }

    @Override
    public String toString()
    {
        return this.username;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true ;

        if (! (other instanceof EventsCoordinator)) return false ;

        EventsCoordinator ec = (EventsCoordinator) other ;
        return this.username.equals(ec.username);
    }
}