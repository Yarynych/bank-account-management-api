CREATE DATABASE IF NOT EXISTS api_bank_db;

USE api_bank_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    second_name VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS bank_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    currency VARCHAR(3) NOT NULL,
    amount DECIMAL(15,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_bank_account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    bank_account_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS operation_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bank_account_id INT NOT NULL,
    user_id INT,
    operation_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    operation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT
);

CREATE TABLE IF NOT EXISTS currency_rates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    base_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(10,4) NOT NULL,
    rate_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (base_currency, target_currency, rate_date)
);