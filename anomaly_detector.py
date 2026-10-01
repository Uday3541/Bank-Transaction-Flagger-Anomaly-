"""
anomaly_detector.py
Statistical Outlier Detection for Bank Transactions
Subject Demonstrated: Python & Statistics (Z-Score Outlier Detection)

This script reads transaction data from transactions.csv, calculates the 
mean, standard deviation, and Z-score for transaction amounts per account,
and writes statistical anomaly flags to anomaly_results.csv.
"""

import csv
import math
import os

def detect_anomalies(input_file="transactions.csv", output_file="anomaly_results.csv"):
    print("--- Python Anomaly Detector Started ---")
    
    if not os.path.exists(input_file):
        print(f"Error: {input_file} not found!")
        return

    transactions = []
    account_amounts = {}

    # Step 1: Read transactions.csv using Python's standard csv module
    with open(input_file, mode="r", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            txn_id = row["transaction_id"]
            acc_id = row["account_id"]
            payee_id = row["payee_id"]
            amount = float(row["amount"])
            new_payee = row["new_payee"]
            timestamp = row["timestamp"]

            txn = {
                "transaction_id": txn_id,
                "account_id": acc_id,
                "payee_id": payee_id,
                "amount": amount,
                "new_payee": new_payee,
                "timestamp": timestamp
            }
            transactions.append(txn)

            if acc_id not in account_amounts:
                account_amounts[acc_id] = []
            account_amounts[acc_id].append(amount)

    # Step 2: Compute statistics (Mean & Standard Deviation) for each account
    account_stats = {}
    for acc_id, amounts in account_amounts.items():
        n = len(amounts)
        mean = sum(amounts) / n if n > 0 else 0.0
        
        # Calculate population standard deviation
        if n > 1:
            variance = sum((x - mean) ** 2 for x in amounts) / n
            stdev = math.sqrt(variance)
        else:
            stdev = 0.0

        account_stats[acc_id] = {"mean": mean, "stdev": stdev, "count": n}

    # Step 3: Calculate Z-score for each transaction and set Statistical Flag
    # Formula: Z = (Amount - Mean) / Standard_Deviation
    results = []
    Z_THRESHOLD = 2.0  # Absolute Z-score threshold for outlier detection

    for txn in transactions:
        acc_id = txn["account_id"]
        amount = txn["amount"]
        stats = account_stats[acc_id]

        if stats["stdev"] > 0:
            raw_z = (amount - stats["mean"]) / stats["stdev"]
        else:
            raw_z = 0.0  # If stdev is 0 or insufficient data, Z-score is 0

        z_score = round(raw_z, 2)

        # Statistically unusual if rounded |Z| >= 2.0
        is_anomaly = abs(z_score) >= Z_THRESHOLD
        stat_flag = "YES" if is_anomaly else "NO"

        results.append({
            "transaction_id": txn["transaction_id"],
            "account_id": txn["account_id"],
            "payee_id": txn["payee_id"],
            "amount": txn["amount"],
            "new_payee": txn["new_payee"],
            "z_score": z_score,
            "statistical_flag": stat_flag
        })

    # Step 4: Write anomaly detection results to anomaly_results.csv
    fieldnames = ["transaction_id", "account_id", "payee_id", "amount", "new_payee", "z_score", "statistical_flag"]
    with open(output_file, mode="w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(results)

    print(f"Successfully processed {len(results)} transactions.")
    print(f"Output saved to {output_file}")
    print("--- Python Anomaly Detector Finished ---\n")

if __name__ == "__main__":
    detect_anomalies()
