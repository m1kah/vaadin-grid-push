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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT", "IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.m1kah.grid.task;

import com.m1kah.grid.data.Transaction;
import com.m1kah.grid.data.TransactionGenerator;
import com.m1kah.grid.data.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class RefreshDataTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RefreshDataTask.class);
    private final TaskDoneListener taskDoneListener;
    private final TaskFailListener taskFailListener;

    RefreshDataTask(TaskDoneListener taskDoneListener,
                    TaskFailListener taskFailListener) {
        this.taskDoneListener = taskDoneListener;
        this.taskFailListener = taskFailListener;
    }

    @Override
    public void run() {
        logger.debug("Background task called");
        try {
            List<String> updatedTransactionIds = fetchUpdatesToTransactions();
            taskDoneListener.onTaskDone(updatedTransactionIds);
        } catch (RuntimeException e) {
            taskFailListener.onTaskFail(e);
        }
    }

    private List<String> fetchUpdatesToTransactions() {
        return updateRandomTransactions();
    }

    private List<String> updateRandomTransactions() {
        List<String> updatedTransactions = new ArrayList<>();
        for (Transaction transaction : TransactionRepository.get().findAll()) {
            if (Math.random() < 0.3) {
                continue;
            }

            BigDecimal newAmount = transaction.getAmount()
                    .add(new BigDecimal(Math.random() * 100))
                    .setScale(0, BigDecimal.ROUND_HALF_UP);
            transaction.setAmount(newAmount);
            transaction.setUpdated(Instant.now());
            updatedTransactions.add(transaction.getName());
            TransactionRepository.get().update(transaction);
        }
        if (Math.random() < 0.2 && TransactionGenerator.hasMore()) {
            Transaction transaction = TransactionGenerator.create();
            if (TransactionRepository.get().find(transaction.getName()) == null) {
                TransactionRepository.get().insert(transaction);
            }
            updatedTransactions.add(transaction.getName());
        }
        return updatedTransactions;
    }

}
