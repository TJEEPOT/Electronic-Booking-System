/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent a type of ticket available for an event
*/

package Model;

import java.awt.*;

public class TicketType
{
    private int ticketTypeID;
    private String ticketName;
    private int price;
    private String description;
    private int initialStock;
    private int remainingStock;
    private String pathToTicketDesign;

    //constructor
    public TicketType(String ticketName, int price, String description,
                      int initialStock)
    {
        this.ticketName     = ticketName;
        this.price          = price; // price in pence
        this.description    = description;
        this.initialStock   = initialStock;
        this.remainingStock = initialStock;
    }

    // getters
    public int getTicketTypeID()    {return ticketTypeID;}
    public String getTicketName()   {return ticketName;}
    public int getPrice()           {return price;}
    public String getDescription()  {return description;}
    public int getInitialStock()    {return initialStock;}
    public int getRemainingStock()  {return remainingStock;}
    public String getPathToTicketDesign()  {return pathToTicketDesign;}

    // setters
    public void setTicketTypeID(int ticketTypeID) {
        this.ticketTypeID = ticketTypeID;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public void setPrice(int price) {this.price = price;}

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInitialStock(int initialStock) {
        this.initialStock = initialStock;
    }

    public void setRemainingStock(int remainingStock) {
        this.remainingStock = remainingStock;
    }

    public void setPathToTicketDesign(String pathToTicketDesign) {
        this.pathToTicketDesign = pathToTicketDesign;
    }
}
