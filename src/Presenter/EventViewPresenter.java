package Presenter;

import Model.*;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

import Model.Database.*;
import javafx.util.Callback;

public class EventViewPresenter extends Application implements Initializable {
    
    private Event                           selectedEvent;
    private ObservableList<TicketType>      ticketTypeList =
            FXCollections.observableArrayList();
    private ObservableList<Sundry>          sundryList     =
            FXCollections.observableArrayList();
    private ObservableList<Event.EventType> eventTypes     =
            FXCollections.observableArrayList(Event.EventType.values());

    private ObservableList<Booking>           bookingList        =
            FXCollections.observableArrayList();
    private DatabaseManager                   db                 = new DatabaseManager();
    private User                              currentUser;
    private ObservableList<EventsCoordinator> eventsCoordinators = FXCollections.observableArrayList(db.getECSet());
    
    @FXML
    private Button dashboard;
    @FXML
    private Button events;
    @FXML
    private Button customers;
    @FXML
    private Button reports;
    @FXML
    private Button logout;
    
    @FXML
    private TextField                        eventName;
    @FXML private Label eventNameNoEdit;
    @FXML
    private ChoiceBox<Event.EventType>                        eventType;
    @FXML private Label eventTypeNoEdit;
    @FXML
    private TextField                        venue;
    @FXML private Label venueNoEdit;
    @FXML
    private TextField                        description;
    @FXML private Label descriptionNoEdit;
    @FXML
    private DatePicker                       startDate;
    @FXML private Label startDateNoEdit;
    @FXML
    private DatePicker                       endDate;
    @FXML private Label endDateNoEdit;
    @FXML
    private TextField                        startTime;
    @FXML private Label startTimeNoEdit;
    @FXML
    private TextField                        endTime;
    @FXML private Label endTimeNoEdit;
    @FXML
    private ChoiceBox<EventsCoordinator>    eventCoordinator;
    @FXML private Label eventCoordinatorNoEdit;
    @FXML
    private Label                      ticketsRemaining;
    @FXML
    private TableView<TicketType>            ticketTypes;
    @FXML
    private TableColumn<TicketType, String>  ticketNameColumn;
    @FXML
    private TableColumn<TicketType, Double> priceColumn;
    @FXML
    private TableColumn<TicketType, String>  descriptionColumn;
    @FXML
    private TableColumn<TicketType, Integer> stockColumn;
    @FXML
    private TextField                        ticketName;
    @FXML
    private TextField                        ticketPrice;
    @FXML
    private TextField                        ticketDescription;
    @FXML
    private TextField                        ticketStock;
    @FXML
    private Button                           addNewTicketType;
    @FXML
    private TableView<Sundry>                sundries;
    @FXML
    private TableColumn<Sundry, String>      sundryNameColumn;
    @FXML
    private TableColumn<Sundry, Integer>     sundryPriceColumn;
    @FXML
    private TableColumn<Sundry, String>      sundryDescriptionColumn;
    @FXML
    private TableColumn<Sundry, Integer>     sundryStockColumn;
    @FXML
    private TextField                        sundryName;
    @FXML
    private TextField                        sundryPrice;
    @FXML
    private TextField                        sundryDescription;
    @FXML
    private TextField                        sundryStock;
    @FXML
    private Button                           addNewSundry;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> bookingIDColumn;
    @FXML private TableColumn<Booking, String> customerNameColumn;
    @FXML private TableColumn<Booking, Boolean> paymentStatusColumn;
    @FXML private Button makeNewBookingButton;
    @FXML private Button deleteSelectedBookingButton;
    @FXML private Button                           saveEventButton;
    @FXML private Label                            message;
    @FXML private Label deletionMessage;

    //This is for formatting the prices in the TicketType table
    private class ColumnFormatter<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
        private Format format;

        public ColumnFormatter(Format format) {
            super();
            this.format = format;
        }
        @Override
        public TableCell<S, T> call(TableColumn<S, T> arg0) {
            return new TableCell<S, T>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(new Label(format.format(item)));
                    }
                }
            };
        }
    }

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
    
    //This method accepts an Event object to initialise the view
    public void initData(Event event, User currentUser) {
        this.currentUser = currentUser;
        selectedEvent = event;
        if (selectedEvent != null)
            {
            eventName.setText(selectedEvent.getEventName());
            eventType.setValue(selectedEvent.getEventType());
            venue.setText(selectedEvent.getLocation());
            description.setText(selectedEvent.getDescription());
            startDate.setValue(selectedEvent.getStartTime().toLocalDate());
            endDate.setValue(selectedEvent.getEndTime().toLocalDate());
            startTime.setText(selectedEvent.getStartTime().format(ISO_LOCAL_TIME));
            endTime.setText(selectedEvent.getEndTime().format(ISO_LOCAL_TIME));
            eventCoordinator.setValue(selectedEvent.getEventCoordinator());
            selectedEvent.setRemainingTickets();
            ticketsRemaining.setText(Integer.toString(
                    selectedEvent.getRemainingTickets()));
    
            ticketTypeList.addAll(selectedEvent.getTicketTypes());
            sundryList.addAll(selectedEvent.getSundries());
            bookingList.addAll(selectedEvent.getBookings());
            }
        //to remove buttons and textfields for Agents
        if (this.currentUser.getAccountType() != User.AccountType.EC) {
            eventNameNoEdit.setText(selectedEvent.getEventName());
            eventTypeNoEdit.setText(selectedEvent.getEventType().toString());
            venueNoEdit.setText(selectedEvent.getLocation());
            descriptionNoEdit.setText(selectedEvent.getDescription());
            startDateNoEdit.setText(selectedEvent.getStartTime().toLocalDate().toString());
            endDateNoEdit.setText(selectedEvent.getEndTime().toLocalDate().toString());
            startTimeNoEdit.setText(selectedEvent.getStartTime().format(ISO_LOCAL_TIME));
            endTimeNoEdit.setText(selectedEvent.getEndTime().format(ISO_LOCAL_TIME));
            eventCoordinatorNoEdit.setText(selectedEvent.getEventCoordinator().getUsername());
            eventNameNoEdit.setOpacity(1);
            eventTypeNoEdit.setOpacity(1);
            venueNoEdit.setOpacity(1);
            descriptionNoEdit.setOpacity(1);
            startDateNoEdit.setOpacity(1);
            endDateNoEdit.setOpacity(1);
            startTimeNoEdit.setOpacity(1);
            endTimeNoEdit.setOpacity(1);
            eventCoordinatorNoEdit.setOpacity(1);
            eventName.setManaged(false);
            eventType.setManaged(false);
            eventType.setDisable(true);
            eventType.setOpacity(0);
            venue.setManaged(false);
            description.setManaged(false);
            startDate.setManaged(false);
            startDate.setDisable(true);
            startDate.setOpacity(0);
            endDate.setManaged(false);
            endDate.setDisable(true);
            endDate.setOpacity(0);
            startTime.setManaged(false);
            endTime.setManaged(false);
            eventCoordinator.setManaged(false);
            eventCoordinator.setDisable(true);
            eventCoordinator.setOpacity(0);
            saveEventButton.setManaged(false);
            addNewSundry.setManaged(false);
            addNewTicketType.setManaged(false);
            ticketName.setManaged(false);
            ticketDescription.setManaged(false);
            ticketPrice.setManaged(false);
            ticketStock.setManaged(false);
            sundryName.setManaged(false);
            sundryDescription.setManaged(false);
            sundryPrice.setManaged(false);
            sundryStock.setManaged(false);
        }
    }
    
    //this method takes the text box inputs and generates a new ticket type
    public void NewTicketTypeButtonPushed(ActionEvent event) {
        TicketType newTicketType = new TicketType(ticketName.getText(),
                Integer.parseInt(ticketPrice.getText()),
                ticketDescription.getText(),
                Integer.parseInt(ticketStock.getText()));
        
        //get all the items from the ticket type table, then add the new
        // ticket type object
        //ticketTypes.getItems().add(newTicketType);
        //add the new ticket type to the local list
        ticketTypeList.add(newTicketType);
        //add the new ticket type to the database
        db.addTicketType(newTicketType);
        if (selectedEvent != null){
            db.linkEventToTT(selectedEvent, newTicketType);
        }
        int current = Integer.parseInt(ticketsRemaining.getText());
        ticketsRemaining.setText(Integer.toString(current + newTicketType.getRemainingStock()));
        ticketName.clear();
        ticketPrice.clear();
        ticketDescription.clear();
        ticketStock.clear();
    }
    
    //this method takes the text box inputs and generates a new sundry
    public void NewSundryButtonPushed(ActionEvent event) {
        Sundry newSundry = new Sundry(sundryName.getText(),
                Integer.parseInt(sundryPrice.getText()),
                sundryDescription.getText(),
                Integer.parseInt(sundryStock.getText()));
        
        //get all the items from the ticket type table, then add the new
        // sundry object
        sundryList.add(newSundry);
        db.addSundry(newSundry);
        if (selectedEvent != null){
            db.linkEventToSundry(selectedEvent, newSundry);
        }
        sundryName.clear();
        sundryPrice.clear();
        sundryDescription.clear();
        sundryStock.clear();
    }
    
    //this method saves any changes made to an Event to the database
    public void SaveEventButtonPressed(ActionEvent event) {
        if (ticketTypeList.isEmpty())
        {
            message.setText("An Event must have at least one Ticket Type!");
            message.setOpacity(1);
        }
        //if creating new event
        else if (selectedEvent == null) {
            ZonedDateTime z = ZonedDateTime.of(startDate.getValue(),
                    LocalTime.parse(startTime.getText()),
                    ZoneId.systemDefault());
            ZonedDateTime z2 = ZonedDateTime.of(endDate.getValue(),
                    LocalTime.parse(endTime.getText()),
                    ZoneId.systemDefault());
            ArrayList<TicketType> ttList = new ArrayList<>(ticketTypeList);
            ArrayList<Sundry> sList = new ArrayList<>(sundryList);
            Event e = new Event(venue.getText(), z, z2, description.getText(),
                    eventName.getText(),
                            eventType.getSelectionModel().getSelectedItem(),
                    ttList, sList,
                    eventCoordinator.getSelectionModel().getSelectedItem());
            db.addEvent(e);
            selectedEvent = e;
            message.setText("Event saved successfully!");
            message.setOpacity(1.00);
        }
        //if editing an existing event
        else {
            selectedEvent.setEventName(eventName.getText());
            selectedEvent.setEventType(
                    eventType.getSelectionModel().getSelectedItem());
            selectedEvent.setLocation(venue.getText());
            selectedEvent.setDescription(description.getText());
            ZonedDateTime z = ZonedDateTime.of(startDate.getValue(),
                    LocalTime.parse(startTime.getText()),
                    ZoneId.systemDefault());
            selectedEvent.setStartTime(z);
            ZonedDateTime z2 = ZonedDateTime.of(endDate.getValue(),
                    LocalTime.parse(endTime.getText()),
                    ZoneId.systemDefault());
            selectedEvent.setEndTime(z2);
            selectedEvent.setEventCoordinator(eventCoordinator.getSelectionModel().getSelectedItem());
            ArrayList<TicketType> arr = new ArrayList<>(ticketTypeList);
            selectedEvent.setTicketType(arr);
            ArrayList<Sundry> arr2 = new ArrayList<>(sundryList);
            selectedEvent.setSundries(arr2);
            
            db.updateEvent(selectedEvent);
            message.setText("Event saved successfully!");
            message.setOpacity(1.00);
        }
    }

    public void newBookingButtonPushed(ActionEvent event)
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/BookingView.fxml"));
        Parent bookingViewParent = null;
        try {
            bookingViewParent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene eventViewScene = new Scene(bookingViewParent);

        //access the controller and call a method
        BookingViewPresenter presenter = loader.getController();
        //initialise data in the event view screen with the selected table entry
        Booking b = null;
        Event e = selectedEvent;
        presenter.initData(b, e, currentUser);

        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(eventViewScene);
        window.setTitle("View Booking");
        window.show();
    }

    public void deleteSelectedBookingButtonPushed(ActionEvent event)
    {
        //if there is no booking selected
        if(bookingTable.getSelectionModel().getSelectedItem() == null)
        {
            deletionMessage.setText("No Booking selected!");
            deletionMessage.setOpacity(1);
        }
        else {
            db.deleteBooking(bookingTable.getSelectionModel().getSelectedItem().getBookingID());
            bookingList.remove(bookingTable.getSelectionModel().getSelectedItem());
            deletionMessage.setText("Booking successfully deleted!");
            deletionMessage.setOpacity(1.00);
        }
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //These items are for setting the table columns
        ticketNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("ticketName"));

        //This whole mess is for formatting the prices in TicketType table
        priceColumn.setCellFactory(new ColumnFormatter<TicketType, Double>(new DecimalFormat("£0.00")));
        priceColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TicketType, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<TicketType, Double> p) {
                return new SimpleDoubleProperty((double)p.getValue().getPrice()/100).asObject();
            }
        });

        descriptionColumn.setCellValueFactory(
                new PropertyValueFactory<>("description"));
        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("remainingStock"));
        ticketTypes.setItems(ticketTypeList);
        
        sundryNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("sundryName"));
        sundryPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price"));
        sundryDescriptionColumn.setCellValueFactory(
                new PropertyValueFactory<>("description"));
        sundryStockColumn.setCellValueFactory(
                new PropertyValueFactory<>("remainingStock"));
        sundries.setItems(sundryList);

        eventType.setItems(eventTypes);
        eventCoordinator.setItems(eventsCoordinators);

        bookingIDColumn.setCellValueFactory(new PropertyValueFactory<>("bookingID"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        customerNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Booking, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Booking, String> data) {
                return new ReadOnlyStringWrapper(data.getValue().getCustomer().getUsername());
            }
        });

        bookingTable.setItems(bookingList);

        //Method to switch to BookingView screen when a table entry is double clicked
        bookingTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("../View/BookingView.fxml"));
                    Parent bookingViewParent = null;
                    try {
                        bookingViewParent = loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Scene eventViewScene = new Scene(bookingViewParent);

                    //access the controller and call a method
                    BookingViewPresenter presenter = loader.getController();
                    //initialise data in the event view screen with the selected table entry
                    Booking b = db.getBooking(selectedEvent, bookingTable.getSelectionModel().getSelectedItem().getBookingID());
                    Event e = selectedEvent;
                    presenter.initData(b, e, currentUser);

                    Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
                    window.setScene(eventViewScene);
                    window.setTitle("View Booking");
                    window.show();
                }
            }
        });
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
