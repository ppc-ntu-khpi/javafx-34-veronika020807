package org.example.fxdemo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {

    // ====== МОДЕЛІ ======

    static class Customer {
        private String firstName;
        private String lastName;
        private List<Account> accounts = new ArrayList<>();

        Customer(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        String getFullName() {
            return lastName + ", " + firstName;
        }

        void addAccount(Account acc) {
            accounts.add(acc);
        }

        List<Account> getAccounts() {
            return accounts;
        }
    }

    static abstract class Account {
        protected double balance;

        Account(double balance) {
            this.balance = balance;
        }

        abstract String getType();

        double getBalance() {
            return balance;
        }
    }

    static class CheckingAccount extends Account {
        private double overdraft;

        CheckingAccount(double balance, double overdraft) {
            super(balance);
            this.overdraft = overdraft;
        }

        @Override
        String getType() {
            return "Checking";
        }

        double getOverdraft() {
            return overdraft;
        }
    }

    static class SavingsAccount extends Account {
        private double interestRate;

        SavingsAccount(double balance, double interestRate) {
            super(balance);
            this.interestRate = interestRate;
        }

        @Override
        String getType() {
            return "Savings";
        }

        double getInterestRate() {
            return interestRate;
        }
    }

    // ====== ДАНІ ======

    private List<Customer> customers = new ArrayList<>();

    private ComboBox<String> clientsCombo;
    private Text title = new Text("Client Name");
    private Text details = new Text("Account:\nAcc Type:\nBalance:");

    private TextArea reportArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        BorderPane border = new BorderPane();

        // Верхня панель
        HBox hbox = addHBox();
        border.setTop(hbox);

        // Ліва панель (інформація)
        border.setLeft(addVBox());

        // Нижня панель (звіти)
        border.setBottom(addReportBox());

        // Завантаження клієнтів з файлу
        loadCustomersFromFile();
        updateClientsCombo();

        Scene scene = new Scene(border, 500, 500);
        primaryStage.setTitle("MyBank Clients");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox addVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        title.setFont(Font.font("Arial", 18));
        vbox.getChildren().add(title);

        Line separator = new Line(10, 10, 280, 10);
        vbox.getChildren().add(separator);

        details.setFont(Font.font("Arial", 14));
        vbox.getChildren().add(details);

        return vbox;
    }

    private HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        clientsCombo = new ComboBox<>();
        clientsCombo.setPrefSize(200, 20);
        clientsCombo.setPromptText("Select client");

        Button buttonShow = new Button("Show");
        buttonShow.setPrefSize(100, 20);
        buttonShow.setOnAction(this::handleShowClient);

        Button buttonReport = new Button("Report");
        buttonReport.setPrefSize(100, 20);
        buttonReport.setOnAction(this::handleShowReport);

        hbox.getChildren().addAll(clientsCombo, buttonShow, buttonReport);

        return hbox;
    }

    private VBox addReportBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        Label label = new Label("Customer Report:");
        label.setFont(Font.font("Arial", 16));

        reportArea.setEditable(false);
        reportArea.setPrefRowCount(10);

        vbox.getChildren().addAll(label, reportArea);

        return vbox;
    }

    private void handleShowClient(ActionEvent event) {
        int selectedIndex = clientsCombo.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < customers.size()) {
            Customer customer = customers.get(selectedIndex);

            StringBuilder sb = new StringBuilder();
            sb.append("Client: ").append(customer.getFullName()).append("\n\n");

            List<Account> accounts = customer.getAccounts();
            for (int i = 0; i < accounts.size(); i++) {
                Account acc = accounts.get(i);
                sb.append("Account #").append(i + 1).append("\n");
                sb.append("Type: ").append(acc.getType()).append("\n");
                sb.append("Balance: $").append(acc.getBalance()).append("\n");

                if (acc instanceof SavingsAccount) {
                    sb.append("Interest Rate: ").append(((SavingsAccount) acc).getInterestRate()).append("\n");
                } else if (acc instanceof CheckingAccount) {
                    sb.append("Overdraft: ").append(((CheckingAccount) acc).getOverdraft()).append("\n");
                }
                sb.append("\n");
            }

            title.setText(customer.getFullName());
            details.setText(sb.toString());
        }
    }

    private void handleShowReport(ActionEvent event) {
        StringBuilder report = new StringBuilder();
        for (Customer customer : customers) {
            report.append("CUSTOMER: ").append(customer.getFullName()).append("\n");

            List<Account> accounts = customer.getAccounts();
            for (Account acc : accounts) {
                report.append("    ").append(acc.getType())
                        .append(": $").append(acc.getBalance());

                if (acc instanceof SavingsAccount) {
                    report.append(" (int.rate: ").append(((SavingsAccount) acc).getInterestRate()).append(")");
                } else if (acc instanceof CheckingAccount) {
                    report.append(" (overdraft: ").append(((CheckingAccount) acc).getOverdraft()).append(")");
                }
                report.append("\n");
            }
            report.append("\n");
        }

        reportArea.setText(report.toString());
    }

    private void updateClientsCombo() {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Customer customer : customers) {
            items.add(customer.getFullName());
        }
        clientsCombo.setItems(items);
    }

    private void loadCustomersFromFile() {
        String fileName = "C:\\Users\\aleks\\OneDrive\\Desktop\\FxDemo\\src\\data\\test.dat";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            do {
                line = reader.readLine();
            } while (line != null && line.trim().isEmpty());
            int numCustomers = Integer.parseInt(line.trim());

            for (int i = 0; i < numCustomers; i++) {
                do {
                    line = reader.readLine();
                } while (line != null && line.trim().isEmpty());
                String[] customerLine = line.trim().split("\\s+");

                String firstName = customerLine[0];
                String lastName = customerLine[1];
                int numAccounts = Integer.parseInt(customerLine[2]);

                Customer customer = new Customer(firstName, lastName);

                for (int j = 0; j < numAccounts; j++) {
                    do {
                        line = reader.readLine();
                    } while (line != null && line.trim().isEmpty());
                    String[] accLine = line.trim().split("\\s+");

                    String accType = accLine[0];
                    double balance = Double.parseDouble(accLine[1]);

                    if (accType.equals("S")) {
                        double interest = Double.parseDouble(accLine[2]);
                        customer.addAccount(new SavingsAccount(balance, interest));
                    } else if (accType.equals("C")) {
                        double overdraft = Double.parseDouble(accLine[2]);
                        customer.addAccount(new CheckingAccount(balance, overdraft));
                    }
                }

                customers.add(customer);
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Load Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading customers: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
