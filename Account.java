/**
 * Account.java
 * Represents a Bank Account in the system.
 * Subject Demonstrated: OOPJ (Object-Oriented Programming in Java)
 * Key OOP Concept: Encapsulation (private variables with public getters)
 */
public class Account {
    // Private instance variables (Data Hiding)
    private String accountId;
    private String accountHolder;
    private double balance;

    // Constructor to initialize an Account object
    public Account(String accountId, String accountHolder, double balance) {
        this.accountId = accountId;
        this.accountHolder = accountHolder;
        this.balance = balance;
    }

    // Getter methods to access private variables
    public String getAccountId() {
        return accountId;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public double getBalance() {
        return balance;
    }

    // Method to display account details
    public void displayAccount() {
        System.out.println("Account ID: " + accountId + 
                           " | Holder: " + accountHolder + 
                           " | Balance: Rs. " + balance);
    }

    @Override
    public String toString() {
        return accountId + " (" + accountHolder + ")";
    }
}
