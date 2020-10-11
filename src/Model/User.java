/*
Authors: Chris Sutton, Martin Siddons, Aurelie Sing-Fat, Lena Almatrodi
Version: 1.0
Last Modified: 16/02/2020
Description: Abstract class to represent a user of the system with general methods available for all users
*/

package Model;

import Model.Database.DatabaseManager;

public abstract class User {
    
    public enum AccountType {
        EC,
        AGENT,
        CUSTOMER
    }
    
    // TODO: Might need to store first/surname in future.
    protected String      username;
    protected byte[]      password;
    public    byte[]      salt;
    public    AccountType accountType;
    
    //constructor
    public User(String username, AccountType accountType) {
        this.username    = username;
        this.accountType = accountType;
    }
    
    // constructor for user lists
    public User(String username) {this.username = username;}
    
    //getters
    public String getUsername() { return username; }
    
    public byte[] getPassword()         { return password; }
    
    public byte[] getSalt()             { return salt; }
    
    public AccountType getAccountType() { return accountType; }
    
    // setters
    // This is only for use by DatabaseManager to set password value from DB.
    public void setPassword(byte[] password) { this.password = password; }
    
    public void setSalt(byte[] salt) { this.salt = salt;}
    
    
    //public methods
    
    /**
     * Method to set the password of the selected user object. This only changes
     * the password locally and does NOT submit to the database. Use UpdateUser
     * etc in DatabaseManager for this functionality.
     *
     * @param pwd : The user's new password, as a plaintext string.
     */
    public void resetPassword(String pwd) {
        this.salt     = Encryption.generateSalt();
        this.password = Encryption.generateHash(salt, pwd);
    }
    
    /**
     * Static method to check the database for the given user.
     *
     * @param username : Username of the User to look for
     * @param password : Password of the User to look for
     * @return : User object of found user, or null if user not found in DB.
     */
    public static User authenticate(String username, String password) {
        DatabaseManager db = new DatabaseManager();
        // return user object of logged-in user.
        return db.authenticateUser(username, password);
    }
}
