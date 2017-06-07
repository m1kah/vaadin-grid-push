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

package com.m1kah.grid.ui;

import com.m1kah.grid.data.Transaction;
import com.m1kah.grid.data.TransactionRepository;
import com.vaadin.annotations.Push;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Push
public class GridUi extends UI implements BroadcastListener {
    private static final Logger logger = LoggerFactory.getLogger(GridUi.class);
    private Grid<Transaction> grid;
    private VerticalLayout container;
    private ListDataProvider<Transaction> dataProvider;
    private List<Transaction> dataList;

    @Override
    protected void init(VaadinRequest request) {
        initGrid();
        initContainer();
        initData();
        initReceiveData();
        setContent(container);
        logger.info("UI created");
    }

    @Override
    public void detach() {
        Broadcaster.removeBroadcastListener(this);
        super.detach();
    }

    private void initReceiveData() {
        Broadcaster.addBroadcastListener(this);
    }

    private void initData() {
        dataList = new ArrayList<>(TransactionRepository.get().findAll());
        dataProvider = new ListDataProvider<>(dataList);
        grid.setDataProvider(dataProvider);
        updateCaption();
    }

    private void initContainer() {
        container = new VerticalLayout(grid);
        container.setMargin(true);
        container.setSpacing(true);
    }

    private void initGrid() {
        grid = new Grid<>("Example Grid");
        grid.setHeightByRows(8);
        grid.addColumn(Transaction::getName)
                .setCaption("Name");
        grid.addColumn(Transaction::getAmount)
                .setCaption("Amount");
        grid.addColumn(Transaction::formattedUpdateTime)
                .setCaption("Updated");
    }

    @Override
    public void onTransactionDataUpdate(List<String> updatedTransactionIds) {
        access(() -> {
            // This code is executed by a background thread (which has delegated
            // updated to thread owned by broadcaster). We are only refreshing
            // updated rows or adding new rows for new data. Another option
            // is to simply reload all data in the grid.
            //
            // If more logic is added here then only UIs that need updates
            // can be updated. Maybe only some of the views need pushed
            // updates. Maybe the updated data is not visible in the view.
            // There are many options.
            for (String updatedTransactionId : updatedTransactionIds) {
                Transaction transaction = TransactionRepository.get().find(updatedTransactionId);
                if (!dataList.contains(transaction)) {
                    dataList.add(transaction);
                    // This is needed to refresh the rows in grid. Otherwise it does
                    // not notice that a new row has been added.
                    grid.clearSortOrder();
                    updateCaption();
                } else {
                    dataProvider.refreshItem(transaction);
                }
            }
        });
    }

    private void updateCaption() {
        grid.setCaption("Example Grid with " + dataList.size() + " precious stones");
    }
}
