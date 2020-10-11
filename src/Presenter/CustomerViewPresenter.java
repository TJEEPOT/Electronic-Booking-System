package Presenter;

import Model.Database.*;
import Model.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerViewPresenter extends Application implements Initializable
{
    private Customer selectedCustomer;
    @FXML
    private Button   dashboard;
    @FXML private Button events;
    @FXML private Button customers;
    @FXML private Button reports;
    @FXML private Button logout;
    @FXML private TextField customerName;
    @FXML private TextField customerPassword;
    @FXML private TextField customerAddress;
    @FXML private Button saveCustomerButton;
    @FXML private Label successMessage;

    private DatabaseManager db = new DatabaseManager();
    private User            currentUser;

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

    public void eventListButton(ActionEvent event)
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

    public void reportsButton(ActionEvent event)
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

    //This method accepts a Customer object to initialise the view
    public void initData(Customer customer, User currentUser)
    {
        this.currentUser = currentUser;
        selectedCustomer = customer;
        //if editing a customer
        if (selectedCustomer != null)
        {
            customerPassword.setText(""); // Removed due to encryption
            customerAddress.setText(selectedCustomer.getAddress());
            customerName.setText(selectedCustomer.getUsername());
        }
    }

    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }

    public void saveCustomerButtonPressed()
    {
        if (selectedCustomer == null)
        {
            Customer c = new Customer(customerName.getText(), customerAddress.getText());
            boolean b = db.addCustomer(c, customerPassword.getText());
            if (b)
            {
                successMessage.setOpacity(1);
            }
        }
        else {
            selectedCustomer.setAddress(customerAddress.getText());
            selectedCustomer.resetPassword(customerPassword.getText());
            boolean b = db.updateCustomer(selectedCustomer);
            if (b)
            {
                successMessage.setOpacity(1);
            }
        }

    }

    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../View/CustomerView.fxml"));
        primaryStage.setTitle("Customer View");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}