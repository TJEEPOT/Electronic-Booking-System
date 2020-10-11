package Testing.Model;

import Model.Database.DatabaseManager;
import Model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private static DatabaseManager   db     = new DatabaseManager();
    private static EventsCoordinator dave   = new EventsCoordinator("dave@ebss.com");
    private static Agent             rachel = new Agent("rachel@ebss.com");
    private static Customer          angela = new Customer("angela@stuff.com", "123 Fake St.");
    
    @BeforeAll
    static void beforeAll() {
        db.addUser(dave, "password");
        db.addUser(rachel, "thisisapassword");
        db.addCustomer(angela, "supersecurepassword");
    }
    
    @Test
    void resetPassword() {
        dave.resetPassword("password123");
        byte[] davePwd = Encryption.generateHash(dave.salt, "password123");
        assertArrayEquals(davePwd, dave.getPassword());
        
        rachel.resetPassword("password123");
        byte[] rachelPwd = Encryption.generateHash(rachel.salt, "password123");
        assertArrayEquals(rachelPwd, rachel.getPassword());
        
        angela.resetPassword("password123");
        byte[] angelaPwd = Encryption.generateHash(angela.salt, "password123");
        assertArrayEquals(angelaPwd, angela.getPassword());
    }
    
    @Test
    void authenticate() {
        User u = User.authenticate("adam@ebss.com", "123");
        assertNotNull(u);
        assertEquals("adam@ebss.com", u.getUsername());
    }
    
    @AfterAll
    static void afterAll() {
        db.deleteUser(dave);
        db.deleteUser(rachel);
        db.deleteUser(angela);
    }
}