/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent a customer (ie. external user of the system)
*/

package Model;

public class Customer extends User
{
    String address; // TODO: Could be better stored as ADT
    // TODO: May need to store DoB for "secret word" verification.

    //constructor
    public Customer(String username, String address) {
        super(username, AccountType.CUSTOMER);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) { this.address = address; }
    
}
