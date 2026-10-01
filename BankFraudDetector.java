import java.io.*;
import java.util.*;

/**
 * BankFraudDetector.java
 * Main driver class for Bank Transaction Anomaly Flagger mini-project.
 * 
 * Subjects Demonstrated:
 * 1. DMGT (Discrete Mathematics & Graph Theory):
 *    - U1: Propositional Logic Rule Engine (amount > 50000 AND newPayee == true)
 *    - U2: Account-Payee Transaction Graph representation
 * 2. AI (Artificial Intelligence):
 *    - U1: Rational Agent Decision Rules (combining rule & statistical evidence)
 * 3. ADSA (Advanced Data Structures & Algorithms):
 *    - U2: Graph representation using Adjacency List (HashMap & HashSet)
 * 4. OOPJ (Object-Oriented Programming in Java):
 *    - Account and Transaction object creation & encapsulation
 * 5. Python Integration:
 *    - Exporting data to CSV & reading Python Z-score results
 */
public class BankFraudDetector {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   BANK TRANSACTION ANOMALY FLAGGER    ");
        System.out.println("========================================\n");

        // STEP 1: Create Sample Accounts (OOPJ - Object Creation)
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account("A101", "Rahul Sharma", 150000.0));
        accounts.add(new Account("A102", "Priya Patel", 250000.0));
        accounts.add(new Account("A103", "Amit Verma", 90000.0));

        System.out.println("--- 1. REGISTERED BANK ACCOUNTS ---");
        for (Account acc : accounts) {
            acc.displayAccount();
        }
        System.out.println();

        // STEP 2: Create Realistic Bank Transactions (OOPJ)
        List<Transaction> transactions = new ArrayList<>();
        
        // Account A101 transactions (Normal range: Rs 4,800 - 6,000)
        transactions.add(new Transaction("T001", "A101", "P201", 5000.0, false, "2026-09-29 10:00"));
        transactions.add(new Transaction("T002", "A101", "P201", 6000.0, false, "2026-09-29 10:15"));
        transactions.add(new Transaction("T003", "A101", "P202", 5500.0, false, "2026-09-29 10:30"));
        transactions.add(new Transaction("T004", "A101", "P201", 4800.0, false, "2026-09-29 10:45"));
        transactions.add(new Transaction("T005", "A101", "P999", 80000.0, true, "2026-09-29 11:00"));  // Both Flags: Rule YES + Stat YES

        // Account A102 transactions (Normal range: Rs 9,500 - 11,000)
        transactions.add(new Transaction("T006", "A102", "P301", 10000.0, false, "2026-09-29 11:15"));
        transactions.add(new Transaction("T007", "A102", "P301", 10500.0, false, "2026-09-29 11:30"));
        transactions.add(new Transaction("T008", "A102", "P301", 11000.0, false, "2026-09-29 11:45"));
        transactions.add(new Transaction("T009", "A102", "P301", 9500.0,  false, "2026-09-29 12:00"));
        transactions.add(new Transaction("T010", "A102", "P301", 120000.0, false, "2026-09-29 12:15")); // Stat Flag: YES (High Amount, Existing Payee)

        // Account A103 transactions
        transactions.add(new Transaction("T011", "A103", "P777", 2000.0, false, "2026-09-29 12:30"));
        transactions.add(new Transaction("T012", "A103", "P888", 60000.0, true, "2026-09-29 12:45"));  // Rule Flag: YES (High Amount + New Payee)

        // STEP 3: ADSA U2 & DMGT U2 - Represent Transaction Network as a Graph
        // Graph Representation: Map<AccountId, Set<PayeeId>> (Adjacency List)
        Map<String, Set<String>> transactionGraph = new HashMap<>();
        for (Transaction t : transactions) {
            transactionGraph.computeIfAbsent(t.getAccountId(), k -> new HashSet<>()).add(t.getPayeeId());
        }

        System.out.println("--- 2. ADSA TRANSACTION GRAPH (Account -> Connected Payees) ---");
        for (Map.Entry<String, Set<String>> entry : transactionGraph.entrySet()) {
            System.out.println("Account [" + entry.getKey() + "] is connected to Payees: " + entry.getValue());
        }
        System.out.println();

        // STEP 4: DMGT U1 - Propositional Logic Rule Engine
        // Rule: (Amount > 50000) AND (newPayee == true)
        double RULE_THRESHOLD = 50000.0;
        Map<String, Boolean> ruleFlags = new HashMap<>();

        for (Transaction t : transactions) {
            boolean conditionP = t.getAmount() > RULE_THRESHOLD; // P: High Amount
            boolean conditionQ = t.isNewPayee();                 // Q: New Payee
            boolean ruleEvaluated = conditionP && conditionQ;    // P ^ Q

            ruleFlags.put(t.getTransactionId(), ruleEvaluated);
        }

        // STEP 5: Export Transactions to CSV file for Python processing
        String csvFileName = "transactions.csv";
        exportToCsv(transactions, csvFileName);

        // STEP 6: Run Python Anomaly Detector Script
        runPythonDetector();

        // STEP 7: Read Python Results from anomaly_results.csv
        Map<String, String[]> pythonResults = readPythonResults("anomaly_results.csv");

        // STEP 8: AI U1 - Rational Agent Decision Rules & Display Final Output
        System.out.println("--- 3. TRANSACTION ANOMALY DETECTION RESULTS ---");
        List<String[]> manualReviewList = new ArrayList<>();

        for (Transaction t : transactions) {
            String tid = t.getTransactionId();
            boolean ruleFlag = ruleFlags.getOrDefault(tid, false);
            String ruleFlagStr = ruleFlag ? "YES" : "NO";

            String[] pyData = pythonResults.getOrDefault(tid, new String[]{"0.0", "NO"});
            String zScoreStr = pyData[0];
            String statFlagStr = pyData[1];
            boolean statFlag = statFlagStr.equalsIgnoreCase("YES");

            // Rational Agent Decision Logic:
            // Final Status = MANUAL REVIEW if (Rule Flag OR Statistical Flag) else SAFE
            String finalStatus;
            List<String> reasons = new ArrayList<>();

            if (ruleFlag && statFlag) {
                finalStatus = "MANUAL REVIEW";
                reasons.add("High Amount + New Payee + Statistical Anomaly (Z=" + zScoreStr + ")");
            } else if (ruleFlag) {
                finalStatus = "MANUAL REVIEW";
                reasons.add("High Amount (>Rs.50000) + New Payee");
            } else if (statFlag) {
                finalStatus = "MANUAL REVIEW";
                reasons.add("Statistical Outlier (Z-Score: " + zScoreStr + ")");
            } else {
                finalStatus = "SAFE";
            }

            System.out.println("Transaction " + tid);
            System.out.println("  Account:          " + t.getAccountId());
            System.out.println("  Amount:           Rs. " + t.getAmount());
            System.out.println("  Payee ID:         " + t.getPayeeId() + " (New Payee: " + t.isNewPayee() + ")");
            System.out.println("  Rule Flag:        " + ruleFlagStr);
            System.out.println("  Statistical Flag: " + statFlagStr + " (Z-Score: " + zScoreStr + ")");
            System.out.println("  Final Status:     " + finalStatus);
            System.out.println("----------------------------------------");

            if (!finalStatus.equals("SAFE")) {
                manualReviewList.add(new String[]{
                    tid, 
                    t.getAccountId(), 
                    "Rs." + t.getAmount(), 
                    String.join(" | ", reasons)
                });
            }
        }

        // STEP 9: Print Summary of Transactions Requiring Manual Review
        System.out.println("\n========================================");
        System.out.println("    TRANSACTIONS REQUIRING REVIEW       ");
        System.out.println("========================================");
        if (manualReviewList.isEmpty()) {
            System.out.println("No suspicious transactions found. All clear!");
        } else {
            for (String[] row : manualReviewList) {
                System.out.println(row[0] + " | " + row[1] + " | " + row[2] + " | " + row[3]);
            }
        }
        System.out.println("========================================\n");
    }

    /**
     * Helper method to export Transaction objects to CSV format.
     */
    private static void exportToCsv(List<Transaction> transactions, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("transaction_id,account_id,payee_id,amount,new_payee,timestamp");
            for (Transaction t : transactions) {
                writer.println(t.toCsvRow());
            }
            System.out.println("Successfully exported transactions to " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    /**
     * Helper method to execute Python script from Java using ProcessBuilder.
     */
    private static void runPythonDetector() {
        System.out.println("Executing anomaly_detector.py via Python...");
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "anomaly_detector.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Python] " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Note: Python script auto-execution via Java encountered an issue.");
            System.out.println("You can manually run 'python anomaly_detector.py' in terminal.");
        }
    }

    /**
     * Helper method to read Python results from anomaly_results.csv.
     */
    private static Map<String, String[]> readPythonResults(String fileName) {
        Map<String, String[]> resultsMap = new HashMap<>();
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Warning: " + fileName + " not found. Run Python script first!");
            return resultsMap;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length >= 7) {
                    String tid = cols[0].trim();
                    String zScore = cols[5].trim();
                    String statFlag = cols[6].trim();
                    resultsMap.put(tid, new String[]{zScore, statFlag});
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + fileName + ": " + e.getMessage());
        }
        return resultsMap;
    }
}
