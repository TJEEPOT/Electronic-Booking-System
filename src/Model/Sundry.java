/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Class to represent a type of sundry
*/

package Model;

public class Sundry
{
    private int sundryID;
    private String sundryName;
    private int price; // price in pence
    private String description;
    private int initialStock;
    private int remainingStock;

    //constructor
    public Sundry(String sundryName, int price, String description,
                  int initialStock)
    {
        this.sundryName     = sundryName;
        this.price          = price;
        this.description    = description;
        this.initialStock   = initialStock;
        this.remainingStock = initialStock;
    }

    // getters
    public int getSundryID()        {return sundryID;}
    public String getSundryName()   {return sundryName;}
    public int getPrice()           {return price;}
    public String getDescription()  {return description;}
    public int getInitialStock()    {return initialStock;}
    public int getRemainingStock()  {return remainingStock;}

    // setters
    public void setSundryID(int sundryID) {
        this.sundryID = sundryID;
    }

    public void setSundryName(String sundryName) {
        this.sundryName = sundryName;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInitialStock(int initialStock) {
        this.initialStock = initialStock;
    }

    public void setRemainingStock(int remainingStock) {
        this.remainingStock = remainingStock;
    }
}
