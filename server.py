"""
server.py
Simple HTTP Server & Anomaly Detection API for Bank Transaction Flagger.
Runs using Python's standard library (No pip dependencies required).
"""

import http.server
import socketserver
import json
import csv
import math
import os

PORT = 8080

def get_account_stats(input_file="transactions.csv"):
    """Reads transactions.csv and computes mean and standard deviation per account."""
    account_amounts = {}
    if os.path.exists(input_file):
        with open(input_file, mode="r", newline="") as f:
            reader = csv.DictReader(f)
            for row in reader:
                acc_id = row["account_id"]
                amount = float(row["amount"])
                if acc_id not in account_amounts:
                    account_amounts[acc_id] = []
                account_amounts[acc_id].append(amount)

    account_stats = {}
    for acc_id, amounts in account_amounts.items():
        n = len(amounts)
        mean = sum(amounts) / n if n > 0 else 0.0
        if n > 1:
            variance = sum((x - mean) ** 2 for x in amounts) / n
            stdev = math.sqrt(variance)
        else:
            stdev = 0.0
        account_stats[acc_id] = {"mean": mean, "stdev": stdev, "amounts": amounts}

    return account_stats

def evaluate_transaction(account_id, payee_id, amount, is_new_payee):
    """
    Evaluates transaction against:
    1. DMGT Rule Engine (amount > 50000 AND new_payee == true)
    2. Python Z-Score Outlier Detection (|Z| >= 2.0)
    3. AI Rational Agent Decision (SAFE vs MANUAL REVIEW)
    """
    # 1. DMGT Propositional Logic Rule
    # Rule: (Amount > 50000) AND (newPayee == true)
    rule_flag = (amount > 50000.0) and is_new_payee
    rule_flag_str = "YES" if rule_flag else "NO"

    # 2. Python Z-Score Statistical Detection
    stats_map = get_account_stats("transactions.csv")
    acc_stats = stats_map.get(account_id)

    z_score = 0.0
    stat_flag = False

    if acc_stats:
        amounts = acc_stats["amounts"]
        n = len(amounts)
        mean = acc_stats["mean"]
        stdev = acc_stats["stdev"]

        if stdev > 0:
            raw_z = (amount - mean) / stdev
            z_score = round(raw_z, 2)
        else:
            z_score = 0.0
    else:
        # Unknown account ID
        z_score = 0.0

    if abs(z_score) >= 2.0:
        stat_flag = True

    stat_flag_str = "YES" if stat_flag else "NO"

    # 3. AI Rational Agent Decision Rules
    if rule_flag and stat_flag:
        final_status = "MANUAL REVIEW"
        reason = f"High Amount (>Rs. 50,000) + New Payee + Statistical Anomaly (Z-Score: {z_score})"
    elif rule_flag:
        final_status = "MANUAL REVIEW"
        reason = "High Amount (>Rs. 50,000) + New Payee"
    elif stat_flag:
        final_status = "MANUAL REVIEW"
        reason = f"Statistical Outlier (Z-Score: {z_score})"
    else:
        final_status = "SAFE"
        reason = "Transaction parameters are within normal baseline thresholds"

    return {
        "account_id": account_id,
        "payee_id": payee_id,
        "amount": amount,
        "new_payee": is_new_payee,
        "rule_flag": rule_flag_str,
        "statistical_flag": stat_flag_str,
        "z_score": z_score,
        "final_status": final_status,
        "reason": reason
    }

class FlaggerRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_POST(self):
        if self.path == "/api/check":
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            
            try:
                data = json.loads(post_data.decode('utf-8'))
                account_id = str(data.get("account_id", "")).strip()
                payee_id = str(data.get("payee_id", "")).strip()
                amount = float(data.get("amount", 0.0))
                new_payee_raw = str(data.get("new_payee", "NO")).strip().upper()
                is_new_payee = new_payee_raw in ["YES", "TRUE", "1"]

                result = evaluate_transaction(account_id, payee_id, amount, is_new_payee)

                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(json.dumps(result).encode('utf-8'))
            except Exception as e:
                self.send_response(400)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                error_resp = {"error": str(e)}
                self.wfile.write(json.dumps(error_resp).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

    def do_GET(self):
        if self.path == "/" or self.path == "":
            self.path = "/index.html"
        return super().do_GET()

def run_server():
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    handler = FlaggerRequestHandler
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), handler) as httpd:
        print(f"==================================================")
        print(f"  BANK TRANSACTION ANOMALY FLAGGER WEB SERVER")
        print(f"==================================================")
        print(f" Output page is running live at: http://localhost:{PORT}")
        print(f" Press Ctrl+C in terminal to stop server.\n")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nServer stopped.")

if __name__ == "__main__":
    run_server()
