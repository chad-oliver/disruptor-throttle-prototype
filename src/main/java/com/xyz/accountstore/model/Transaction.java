package com.xyz.accountstore.model;

import java.util.Date;

/**
 * Example transaction, simple for demo only, sign of amount determines 
 * directionality when applied to balances. In the real world, we might
 * choose to enforce immutability on such objects.
 */
public class Transaction {
    private Date date;
    private String accountnbr;

    public Transaction() {
    }

    public Transaction(Date date, String accountnbr) {
        this.date = date;
        this.accountnbr = accountnbr;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAccountnbr() {
        return accountnbr;
    }

    public void setAccountnbr(String accountnbr) {
        this.accountnbr = accountnbr;
    }

    @Override
    public String toString() {
        return String.format("[%s] ", accountnbr);
    }
}
