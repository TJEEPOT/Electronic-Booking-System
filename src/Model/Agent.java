/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent a booking agent
*/

package Model;

public class Agent extends User
{
    //constructor
    public Agent(String username)
    {
        super(username, AccountType.AGENT);
    }
}
