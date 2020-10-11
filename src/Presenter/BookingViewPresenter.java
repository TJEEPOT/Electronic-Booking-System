package Presenter;

import Model.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import Model.Database.*;

public class BookingViewPresenter extends Application implements Initializable
{

    private Booking                      selectedBooking;
    private Event                        selectedEvent;
    private ObservableList<TicketType>   ticketTypeList =
            FXCollections.observableArrayList();
    private ObservableList<Sundry>       sundryList     =
            FXCollections.observableArrayList();
    private ObservableList<Customer>     customerList   = FXCollections.observableArrayList();
    private HashMap<TicketType, Integer> quantities     = new HashMap<>();
    private HashMap<Sundry, Integer> sundryQuantities = new HashMap<>();

    private DatabaseManager db = new DatabaseManager();
    private User    currentUser;
    private boolean verified = false;

    @FXML private Button dashboard;
    @FXML private Button events;
    @FXML private Button customers;
    @FXML private Button reports;
    @FXML private Button logout;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> customerNameColumn;
    @FXML private TextField customerNameTextField;
    @FXML private TextField customerPasswordTextField;
    @FXML private TextField customerAddressTextField;
    @FXML private Button addNewCustomerButton;
    @FXML private TableView<TicketType> ticketTypeTableView;
    @FXML private TableColumn<TicketType, String> ticketNameColumn;
    @FXML private TableColumn<TicketType, Integer> remainingTicketsColumn;
    @FXML private TableColumn ticketQuantityColumn;
    @FXML private TableView<Sundry> sundries;
    @FXML private TableColumn<Sundry, String> sundryNameColumn;
    @FXML private TableColumn<Sundry, Integer> remainingSundriesColumn;
    @FXML private TableColumn sundryQuantityColumn;
    @FXML private CheckBox paymentStatusCheckBox;
    @FXML private Button saveBookingButton;
    @FXML private Label successMessage;
    @FXML private Label totalBookingValue;
    @FXML private Button resendReceiptButton;
    @FXML private TextField secretWord;
    @FXML private Button verifyButton;
    @FXML private Label verifyStatus;

    //These methods switch between pages
    public void dashboardButton(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/DashboardView.fxml"));
        Parent dashboardViewParent = null;
        try {
            dashboardViewParent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene dashboardViewScene = new Scene(dashboardViewParent);

        //access the controller and call a method
        DashboardViewPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);

        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(dashboardViewScene);
        window.setTitle("Dashboard");
        window.show();
    }

    public void customerListButton(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/customerListView.fxml"));
        Parent customerListViewParent = null;
        try {
            customerListViewParent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene customerListViewScene = new Scene(customerListViewParent);

        //access the controller and call a method
        CustomerListPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);

        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(customerListViewScene);
        window.setTitle("Customer List");
        window.show();
    }

    public void eventListButton(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/EventListView.fxml"));
        Parent eventListViewParent = null;
        try {
            eventListViewParent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene eventListViewScene = new Scene(eventListViewParent);

        //access the controller and call a method
        EventListPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);

        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(eventListViewScene);
        window.setTitle("Event List");
        window.show();
    }

    public void reportsButton(ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/ReportsView.fxml"));
        Parent reportsViewParent = null;
        try {
            reportsViewParent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene reportsViewScene = new Scene(reportsViewParent);

        //access the controller and call a method
        ReportsPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);

        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(reportsViewScene);
        window.setTitle("Reports");
        window.show();
    }

    public void logoutButton(ActionEvent event) throws IOException
    {
        Parent loginViewParent =
                FXMLLoader.load(getClass().getResource("../View/LoginView.fxml"));
        Scene loginViewScene = new Scene(loginViewParent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(loginViewScene);
        window.setTitle("EBS Login");
        window.show();
    }

    //This method accepts a Booking object to initialise the view
    public void initData(Booking booking, Event event, User currentUser)
    {
        this.currentUser = currentUser;
        selectedBooking = booking;
        selectedEvent = event;
        ticketTypeList.addAll(event.getTicketTypes());
        sundryList.addAll(event.getSundries());
        customerList.addAll(db.getCustomerSet());
        if(selectedBooking != null) {
            totalBookingValue.setText("£" + String.format("%.02f", (float) selectedBooking.getBookingValue()/100));
        } else {
            totalBookingValue.setText("0");
        }
    }

    //This method allows a user to double click on a the ticketType table quantity cells and edit them
    public void changeTicketTypeQuantityCellEvent(TableColumn.CellEditEvent editedCell)
    {
        TicketType ticketType = ticketTypeTableView.getSelectionModel().getSelectedItem();
        quantities.put(ticketType, Integer.parseInt(editedCell.getNewValue().toString()));
        int value = 0;
        for (TicketType i : quantities.keySet())
        {
            if (selectedBooking != null) {
                value = quantities.get(i) * i.getPrice() + selectedBooking.getBookingValue();
            } else {
                value = quantities.get(i) * i.getPrice();
            }
        }
        String priceString = totalBookingValue.getText().replace("£", "");
        totalBookingValue.setText("£" + String.format("%.02f", Float.parseFloat(priceString) + (float)value/100));
    }

    //This method allows a user to double click on a the sundry table quantity cells and edit them
    public void changeSundryQuantityCellEvent(TableColumn.CellEditEvent editedCell)
    {
        Sundry sundry = sundries.getSelectionModel().getSelectedItem();
        sundryQuantities.put(sundry, Integer.parseInt(editedCell.getNewValue().toString()));
        int value = 0;
        for (Sundry i : sundryQuantities.keySet())
        {
            if (selectedBooking != null) {
                value = sundryQuantities.get(i) * i.getPrice() + selectedBooking.getBookingValue();
            }
            else
            {
                value = sundryQuantities.get(i) * i.getPrice();
            }
        }
        String priceString = totalBookingValue.getText().replace("£", "");
        totalBookingValue.setText("£" + String.format("%.02f", Float.parseFloat(priceString) + (float)value/100));
    }

    public void addNewCustomerButtonPressed()
    {
        Customer c = new Customer(customerNameTextField.getText(),
                                  customerAddressTextField.getText());
        boolean success = db.addCustomer(c, customerPasswordTextField.getText());
        customerList.add(c);
        if (success)
        {
            successMessage.setText("Customer Added Successfully");
        }
        else{
            successMessage.setText("Customer Not Added");
        }
    }
    
    public void verifySecretWord(){
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c != null){
            byte[] providedPwd = Encryption.generateHash(c.getSalt(), secretWord.getText());
            boolean match = true;
            // compare each byte of the hash of the provided password with the password held in the DB.
            for (int i = 0; i < 16; i++) {
                if (providedPwd[i] != c.getPassword()[i]){
                    match = false;
                }
            }
            if (match){
                verifyStatus.setText("Secret Word Verified");
                verified = true;
            }
            else{
                verifyStatus.setText("Secret Word not verified");
                verified = false;
            }
        }
        else{
            verifyStatus.setText("Please select a Customer");
        }
    }

    //this method creates a new Booking and saves it to the database. It also updates the payment status of a booking.
    public void SaveBookingButtonPressed()
    {
        successMessage.setText("");
        if (selectedBooking == null) // if this is a new booking
        {
            if(quantities.isEmpty())
            {
                successMessage.setText("A booking must have at least one ticket!");
            }
            else if (!verified) {
                successMessage.setText("Please verify customer Secret Word.");
            }
            else{
                Customer c = customerTable.getSelectionModel().getSelectedItem();
                Event e = selectedEvent;
                boolean paymentStatus = paymentStatusCheckBox.isSelected();
                Booking newBooking = new Booking(e, c, paymentStatus);
    
                for (TicketType i : quantities.keySet()) {
                    int numTickets = quantities.get(i);
                    newBooking.generateTicket(i, numTickets);
                    //here subtracting the quantity requested from the remaining stock of tickets
                    i.setRemainingStock(i.getRemainingStock() - numTickets);
                    db.updateTicketType(i);
                    selectedEvent.setRemainingTickets();
                    db.updateEvent(selectedEvent);
                }
                for (Sundry x : sundryQuantities.keySet()) {
                    int numSundries = sundryQuantities.get(x);
                    newBooking.generateSundries(x, numSundries);
                    //here subtracting the quantity requested from the remaining stock of sundries
                    x.setRemainingStock(x.getRemainingStock() - numSundries);
                    db.updateSundry(x);
                }
                int value = 0;
                for (Ticket t : newBooking.getTickets()) {
                    value += t.getType().getPrice();
                }
                for (Sundry s : newBooking.getSundries()) {
                    value += s.getPrice();
                }
                newBooking.setBookingValue(value);
                db.addBooking(newBooking);
                newBooking.generateReceipt();
                successMessage.setText("Booking saved successfully!");
            }
        }
        else { // update the payment status (does not need Secret Word verification)
            selectedBooking.setPaymentStatus(paymentStatusCheckBox.isSelected());
            db.updateBooking(selectedBooking);
            successMessage.setText("Booking saved successfully!");
        }
    }

    public void resendReceiptButtonPushed()
    {
        if(selectedBooking == null)
        {
            successMessage.setText("Booking must be saved first!");
        }
        else
        {
            selectedBooking.generateReceipt();
            successMessage.setText("Receipt resent!");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        //These items are for setting the table columns
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        customerTable.setItems(customerList);
        if (selectedBooking != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    customerTable.requestFocus();
                    customerTable.getSelectionModel().select(selectedBooking.getCustomer());
                    customerTable.getFocusModel().focus(customerTable.getSelectionModel().getSelectedIndex());
                }
            });
        }

        ticketNameColumn.setCellValueFactory(new PropertyValueFactory<>("ticketName"));
        remainingTicketsColumn.setCellValueFactory(new PropertyValueFactory<>("remainingStock"));
        ticketTypeTableView.setItems(ticketTypeList);
        ticketTypeTableView.setEditable(true);
        ticketQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        sundryNameColumn.setCellValueFactory(new PropertyValueFactory<>("sundryName"));
        remainingSundriesColumn.setCellValueFactory(new PropertyValueFactory<>("remainingStock"));
        sundries.setItems(sundryList);
        sundries.setEditable(true);
        sundryQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../View/EventView.fxml"));
        primaryStage.setTitle("Event View");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
