/******************************************************************************
 
 Project     : CMP-5012B - Software Engineering Project:
                EventBookingSystem.
 
 File        : EmailManager.java
 
 Date        : Friday 13 March 2020
 
 Author      : Martin Siddons
 
 Description : Helper class to produce and send emails.
 
 History     :
 13/03/2020 - v1.0 - Initial setup, constructor. Text email built.
 14/03/2020 - v1.1 - HTML email functionality.
 ******************************************************************************/
package Model.Email;

import Model.*;
import Model.Database.DatabaseManager;


import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

public class EmailManager {
    // List of accounts and their details.
    public enum Accounts {
        RECEIPT("receipts.ebs@gmail.com", "receipt123"),
        AUTH("welcome.ebs@gmail.com", "welcome123");
        
        private final String address;
        private final String password;
        
        Accounts(String addr, String pw) {
            this.address  = addr;
            this.password = pw;
        }
        
        public String getAddr() {return this.address;}
        
        public String getPw()   {return this.password;}
    }
    
    // Account details for gmail accounts.
    final String host = "smtp.gmail.com";
    final String port = "465";
    String user;
    String password;
    
    public EmailManager(Accounts a) {
        this.user     = a.getAddr();
        this.password = a.getPw();
    }
    
    /**
     * Forms a receipt email from the passed booking and a HTML template found
     * in the Model.Email folder.
     *
     * @param b : Booking to send a receipt for.
     * @return : True if email is sent, otherwise false.
     */
    public boolean emailReceipt(Booking b) {
        //Get the properties
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);
        
        // get the session
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication
                    getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });
        
        // Read in the HTML template for this email design.
        StringBuilder messageText = new StringBuilder();
        String file = "src/Model/Email/receipt_template.html";
        
        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            stream.forEach(s -> messageText.append(s).append("\n"));
            
            // Replace items in the template with values from the booking.
            int replacePos = messageText.indexOf("%event%");
            messageText.replace(replacePos, (replacePos + 7),
                    b.getEvent().getEventName());
            
            replacePos = messageText.indexOf("%user%");
            messageText.replace(replacePos, (replacePos + 6),
                    b.getCustomer().getUsername());
            // TODO: Change the above when names are added.
            
            String cost = "&pound;" +
                    String.format("%.02f", (float)b.getBookingValue() / 100);
            replacePos = messageText.indexOf("%bookingValue%");
            messageText.replace(replacePos, (replacePos + 14), cost);
            
            
            //Compose the message
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(b.getCustomer().getUsername()));
            message.setSubject("Receipt for your order with EBS");
            
            //Add ticket images as attachments
//            BodyPart messageBodyPart = new MimeBodyPart();
//            messageBodyPart.setText(messageText.toString());
//            Multipart multipart = new MimeMultipart();
//            multipart.addBodyPart(messageBodyPart);

//            messageBodyPart = new MimeBodyPart();
//            for (Ticket t : b.getTickets()) {
//                String filename = t.getType().getPathToTicketDesign();
//                FileDataSource source = new FileDataSource(filename);
//                messageBodyPart.setDataHandler(new DataHandler(source));
//                messageBodyPart.setFileName(filename);
//                multipart.addBodyPart(messageBodyPart);
//            }
//            message.setContent(multipart, "text/html");
    
            message.setContent(messageText.toString(), "text/html");
            
            //send the message
            Transport.send(message);
            return true;
        }
        catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean registerUser(User u) {
        // TODO: Implement this?
        return false;
    }
    
    // Test Harness
    public static void main(String[] args) {
        EmailManager em = new EmailManager(Accounts.RECEIPT);
        DatabaseManager db = new DatabaseManager();
        Event e = db.getEvent(4);
        Booking b = db.getBooking(e, 12);
        boolean sent = em.emailReceipt(b);
        System.out.println("Message sent?: " + sent);
    }
}
