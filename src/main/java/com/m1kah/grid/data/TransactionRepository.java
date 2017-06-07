/*
 * Copyright (c) 2017 Mika Hämäläinen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.m1kah.grid.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionRepository implements Serializable {
    private static final TransactionRepository INSTANCE = new TransactionRepository();
    public static TransactionRepository get() {
        return INSTANCE;
    }
    private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    public TransactionRepository() {
        transactions.add(createTransaction("Opal"));
        transactions.add(createTransaction("Ruby"));
        transactions.add(createTransaction("Sapphire"));
        transactions.add(createTransaction("Topaz"));
        transactions.add(createTransaction("Emerald"));
        transactions.add(createTransaction("Diamond"));
        transactions.add(createTransaction("Zircon"));
        transactions.add(createTransaction("Amethyst"));
    }

    private Transaction createTransaction(String name) {
        Transaction transaction = new Transaction();
        transaction.setName(name);
        transaction.setUpdated(Instant.now());
        return transaction;
    }

    public List<Transaction> findAll() {
        return Collections.unmodifiableList(transactions);
    }

    public void update(Transaction transaction) {
        // This is in-memory example so no external service or data source
        // is updated.
    }

    public Transaction find(String transactionName) {
        for (Transaction transaction : transactions) {
            if (transaction.getName().equals(transactionName)) {
                return transaction;
            }
        }
        return null;
    }

    public void insert(Transaction transaction) {
        transactions.add(transaction);
    }
}
