package Model;

import Presenter.LoginViewPresenter;

public class EventBookingSystem {
    
    public static void main(String[] args) {
        System.out.println("Test system: \n" +
                "EC:       adam@ebss.com       123 \n" +
                "Customer: alice@hottmael1.com password \n" +
                "Agent:    bob@ebss.com        123");
        
        // Print the user directory:
//        System.out.println(System.getProperty("user.dir"));

        LoginViewPresenter.main(null);
    }
}
