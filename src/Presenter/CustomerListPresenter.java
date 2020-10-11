package Presenter;

import Model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import Model.Database.*;

public class CustomerListPresenter extends Application implements Initializable
{
    @FXML private TableView<Customer>           table;
    @FXML private TableColumn<Customer, String> usernameColumn;
    @FXML private TableColumn<Customer, String> addressColumn;
    @FXML private Button dashboard;
    @FXML private Button events;
    @FXML private Button customers;
    @FXML private Button reports;
    @FXML private Button logout;
    @FXML private Button createNewCustomerButton;
    @FXML private Button deleteSelectedCustomerButton;
    @FXML private Label deletionMessage;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
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

    //sets currentUser to that which is passed in by previous screen
    public void initData(User currentUser)
    {
        this.currentUser = currentUser;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //These items are for setting the table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("username"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<Customer, String>("address"));

        //load data
        table.setItems(getCustomers());

        //Method to switch to CustomerView screen when a table entry is double clicked
        table.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("../View/CustomerView.fxml"));
                    Parent customerViewParent = null;
                    try {
                        customerViewParent = loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Scene eventViewScene = new Scene(customerViewParent);

                    //access the controller and call a method
                    CustomerViewPresenter presenter = loader.getController();
                    //initialise data in the event view screen with the selected table entry
                    Customer c = db.getCustomer(table.getSelectionModel().getSelectedItem().getUsername());
                    presenter.initData(c, currentUser);

                    Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
                    window.setScene(eventViewScene);
                    window.setTitle("View Customer");
                    window.show();
                }
            }
        });
    }

    //Generates a list of Customer objects to populate the table
    public ObservableList<Customer> getCustomers()
    {
        ArrayList<Customer> customers = db.getCustomerSet();
        customerList.addAll(customers);
        return customerList;
    }

    //Method to switch to EventView screen when the 'Create New Event' button is clicked
    public void createNewCustomer (ActionEvent event) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../View/CustomerView.fxml"));
        Parent customerViewParent = null;
        try {
            customerViewParent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene eventViewScene = new Scene(customerViewParent);

        //access the controller and call a method
        CustomerViewPresenter presenter = loader.getController();
        //initialise data in the event view screen with the selected table entry
        Customer c = null;
        presenter.initData(c, currentUser);

        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
        window.setScene(eventViewScene);
        window.setTitle("View Customer");
        window.show();
    }

    public void deleteSelectedCustomerButtonPressed(ActionEvent event)
    {
        //if there is no customer selected
        if(table.getSelectionModel().getSelectedItem() == null)
        {
            deletionMessage.setText("No customer selected!");
            deletionMessage.setOpacity(1);
        }
        else {
            db.deleteUser(table.getSelectionModel().getSelectedItem());
            customerList.remove(table.getSelectionModel().getSelectedItem());
            deletionMessage.setText("Customer successfully deleted!");
            deletionMessage.setOpacity(1.00);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../View/CustomerListView.fxml"));
        primaryStage.setTitle("Customer List");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
