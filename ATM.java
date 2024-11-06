import java.sql.*;
import java.util.Scanner;

class Account {
    private String accountNumber;
    private int pin;
    private double balance;

    public Account(String accountNumber, int pin, double balance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int newPin) {
        this.pin = newPin;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited: Rs " + amount);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            System.out.println("Withdrawn: Rs " + amount);
        } else {
            System.out.println("Insufficient balance or invalid amount.");
        }
    }
}

public class ATM {
    private static Connection connection;
    private static Scanner scanner = new Scanner(System.in);
    private static Account currentAccount = null;

    public static void main(String[] args) {
        connectToDatabase();
        System.out.println("1. Already have a bank account");
        System.out.println("2. Want to create a new bank account");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        switch (choice) {
            case 1:
                if (login()) {
                    showMenu();
                } else {
                    System.out.println("Login failed. Exiting...");
                }
                break;
            case 2:
                initializeAccounts();
                System.out.println("Account created successfully. You can now log in.");
                break;
            default:
                System.out.println("Invalid option. Exiting...");
        }
        closeConnection();
    }

    // Initialize the connection to the PostgreSQL database
    private static void connectToDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bank_accounts", "postgres", "Ashutosh@123");
            System.out.println("Connected to the PostgreSQL database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Close the database connection
    private static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Allow user to create a bank account and store it in the PostgreSQL database
    private static void initializeAccounts() {
        System.out.print("Enter new Account Number: ");
        String accountNumber = scanner.nextLine();

        System.out.print("Enter initial PIN: ");
        int pin = scanner.nextInt();

        System.out.print("Enter minimum deposit: ");
        double balance = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        try {
            String sql = "INSERT INTO accounts (account_number, pin, balance) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setInt(2, pin);
            preparedStatement.setDouble(3, balance);
            preparedStatement.executeUpdate();
            System.out.println("Account created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Simulate login
    private static boolean login() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter PIN: ");
        int pin = scanner.nextInt();
        scanner.nextLine();  // Consume the newline

        try {
            String sql = "SELECT * FROM accounts WHERE account_number = ? AND pin = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setInt(2, pin);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                currentAccount = new Account(accountNumber, pin, balance);
                System.out.println("Login successful.");
                return true;
            } else {
                System.out.println("Invalid account number or PIN.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Show the ATM menu
    private static void showMenu() {
        int option = -1;
        do {
            System.out.println("\nATM Menu:");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Change PIN");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            option = scanner.nextInt();
            scanner.nextLine();  // Consume the newline

            switch (option) {
                case 1:
                    checkBalance();
                    break;
                case 2:
                    depositMoney();
                    break;
                case 3:
                    withdrawMoney();
                    break;
                case 4:
                    changePIN();
                    break;
                case 5:
                    System.out.println("Thank you for using the ATM. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (option != 5);
    }

    // Check balance of the current account
    private static void checkBalance() {
        System.out.println("Current Balance: Rs " + currentAccount.getBalance());
    }

    // Deposit money to the current account
    private static void depositMoney() {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();  // Consume the newline
        currentAccount.deposit(amount);
        updateAccountBalance();
    }

    // Withdraw money from the current account
    private static void withdrawMoney() {
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();  // Consume the newline
        currentAccount.withdraw(amount);
        updateAccountBalance();
    }

    // Update the balance of the current account in the database
    private static void updateAccountBalance() {
        try {
            String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, currentAccount.getBalance());
            preparedStatement.setString(2, currentAccount.getAccountNumber());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Change the PIN of the current account
    private static void changePIN() {
        System.out.print("Enter new PIN: ");
        int newPin = scanner.nextInt();
        scanner.nextLine();  // Consume the newline
        currentAccount.setPin(newPin);
        try {
            String sql = "UPDATE accounts SET pin = ? WHERE account_number = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, newPin);
            preparedStatement.setString(2, currentAccount.getAccountNumber());
            preparedStatement.executeUpdate();
            System.out.println("PIN changed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}





// javac -cp ".;C:\Users\DELL\Desktop\ATM_project\postgresql-42.7.4.jar" ATM.java
// java -cp ".;C:\Users\DELL\Desktop\ATM_project\postgresql-42.7.4.jar" ATM
