/**
 * Transaction.java
 * Represents an individual bank transaction.
 * Subject Demonstrated: OOPJ (Object-Oriented Programming in Java)
 * Key OOP Concept: Model class encapsulating transaction attributes.
 */
public class Transaction {
    // Private instance variables
    private String transactionId;
    private String accountId;
    private String payeeId;
    private double amount;
    private boolean newPayee;
    private String timestamp;

    // Constructor
    public Transaction(String transactionId, String accountId, String payeeId, double amount, boolean newPayee, String timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.newPayee = newPayee;
        this.timestamp = timestamp;
    }

    // Getter methods
    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isNewPayee() {
        return newPayee;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Method to format transaction details as CSV line
    public String toCsvRow() {
        return transactionId + "," + accountId + "," + payeeId + "," + amount + "," + newPayee + "," + timestamp;
    }

    // Display transaction info
    @Override
    public String toString() {
        return "Txn[" + transactionId + " | Acc:" + accountId + " -> Payee:" + payeeId + 
               " | Rs." + amount + " | NewPayee:" + newPayee + " | Time:" + timestamp + "]";
    }
}
