package Presenter;

import Model.Database.DatabaseManager;
import Model.Booking;
import Model.Event;
import Model.User;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

public class DashboardViewPresenter extends Application implements Initializable {
    
    private static DatabaseManager db = new DatabaseManager();
    private        User            currentUser;
    
    @FXML
    private Button   dashboard;
    @FXML
    private Button   events;
    @FXML
    private Button   customers;
    @FXML
    private Button   reports;
    @FXML
    private Button   logout;
    @FXML
    private Label    eventNameLabel;
    @FXML
    private Label    eventLocationLabel;
    @FXML
    private Label    eventDescriptionLabel;
    @FXML
    private Label    eventStartTimeLabel;
    @FXML
    private Label    eventEndTimeLabel;
    @FXML
    private Label    eventTicketsRemainingLabel;
    @FXML
    private Label    eventCoordinatorLabel;
    @FXML
    private Label    eventStartDateLabel;
    @FXML
    private Button   makeABookingButton;
    @FXML
    private Label    totalProfit;
    @FXML
    private PieChart pieChart1;
    
    private Event upcomingEvent;
    
    
    //These methods switch between pages
    public void dashboardButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/DashboardView.fxml"));
        Parent dashboardViewParent = null;
        try {
            dashboardViewParent = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Scene dashboardViewScene = new Scene(dashboardViewParent);
        
        //access the controller and call a method
        DashboardViewPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);
        
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(dashboardViewScene);
        window.setTitle("Dashboard");
        window.show();
    }
    
    public void customerListButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/customerListView.fxml"));
        Parent customerListViewParent = null;
        try {
            customerListViewParent = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Scene customerListViewScene = new Scene(customerListViewParent);
        
        //access the controller and call a method
        CustomerListPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);
        
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(customerListViewScene);
        window.setTitle("Customer List");
        window.show();
    }
    
    public void eventListButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/EventListView.fxml"));
        Parent eventListViewParent = null;
        try {
            eventListViewParent = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Scene eventListViewScene = new Scene(eventListViewParent);
        
        //access the controller and call a method
        EventListPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);
        
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(eventListViewScene);
        window.setTitle("Event List");
        window.show();
    }
    
    public void reportsButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/ReportsView.fxml"));
        Parent reportsViewParent = null;
        try {
            reportsViewParent = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Scene reportsViewScene = new Scene(reportsViewParent);
        
        //access the controller and call a method
        ReportsPresenter presenter = loader.getController();
        //pass the current user to the new window
        presenter.initData(currentUser);
        
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(reportsViewScene);
        window.setTitle("Reports");
        window.show();
    }
    
    public void logoutButton(ActionEvent event) throws IOException {
        Parent loginViewParent =
                FXMLLoader.load(getClass().getResource("../View/LoginView.fxml"));
        Scene loginViewScene = new Scene(loginViewParent);
        
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(loginViewScene);
        window.setTitle("EBS Login");
        window.show();
    }
    
    //sets currentUser to that which is passed in by previous screen
    public void initData(User currentUser) {
        this.currentUser   = currentUser;
        this.upcomingEvent = db.getUpcomingEvent();
        eventStartDateLabel.setText(upcomingEvent.getStartTime().toLocalDate().toString());
        eventNameLabel.setText(upcomingEvent.getEventName());
        eventLocationLabel.setText(upcomingEvent.getLocation());
        eventDescriptionLabel.setText(upcomingEvent.getDescription());
        eventStartTimeLabel.setText(upcomingEvent.getStartTime().format(ISO_LOCAL_TIME));
        eventEndTimeLabel.setText(upcomingEvent.getEndTime().format(ISO_LOCAL_TIME));
        eventTicketsRemainingLabel.setText(Integer.toString(upcomingEvent.getRemainingTickets()));
        eventCoordinatorLabel.setText(upcomingEvent.getEventCoordinator().getUsername());
        String formattedString = String.format("%.02f",
                (float)(db.getAllProfits() / 100));
        totalProfit.setText("£" + formattedString);
        
        pieChart1.setData(getPieChartData());
        pieChart1.getData().forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(data.getName(), " £",
                                Bindings.format(new Locale("en", "UK"), "%.02f",
                                        data.pieValueProperty())
                        )
                )
        );
    }
    
    public static ObservableList<PieChart.Data> getPieChartData() {
        ArrayList<Event> events = db.getEventSet();
        ArrayList<PieChart.Data> eventProfits = new ArrayList<>();
        
        // Sum up all the booking values for an event and combine it with the
        // Event name as a piece of data.
        for (Event e : events) {
            float profit = 0;
            ArrayList<Booking> bookings = db.getBookingSet(e);
            
            for (Booking b : bookings) {
                profit = profit + b.getBookingValue();
            }
            
            PieChart.Data data =
                    new PieChart.Data(e.getEventName(), profit / 100); // p to £
            eventProfits.add(data);
        }
        
        // Sort list.
        //        eventProfits.sort(Comparator.comparing(PieChart.Data::getName));
        return FXCollections.observableArrayList(eventProfits);
    }
    
    public void makeABookingButtonPressed(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/BookingView.fxml"));
        Parent bookingViewParent = null;
        try {
            bookingViewParent = loader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Scene eventViewScene = new Scene(bookingViewParent);
        
        //access the controller and call a method
        BookingViewPresenter presenter = loader.getController();
        //initialise data in the event view screen with the upcoming booking
        Booking b = null;
        Event e = upcomingEvent;
        presenter.initData(b, e, currentUser);
        
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(eventViewScene);
        window.setTitle("View Booking");
        window.show();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../View/DashboardView.fxml"));
        primaryStage.setTitle("Dashboard");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
