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

package com.m1kah.grid.ui.list;

import com.m1kah.grid.data.Transaction;
import com.m1kah.grid.data.TransactionRepository;
import com.m1kah.grid.ui.BroadcastListener;
import com.m1kah.grid.ui.Broadcaster;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

public class ListView extends VerticalLayout implements View, BroadcastListener {
    private Grid<Transaction> grid;
    private ListDataProvider<Transaction> dataProvider;
    private List<Transaction> dataList;

    public ListView() {
        initUi();
        initData();
    }

    private void initUi() {
        setSizeFull();
        initGrid();
        addComponent(grid);
        setMargin(true);
        setSpacing(true);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // This is a good place to refresh data or register listeners.
        // When user navigates away from this view then we stop
        // listening to events. That is why we need to register
        // every time new view is entered.
        Broadcaster.addBroadcastListener(this);
    }

    @Override
    public void detach() {
        Broadcaster.removeBroadcastListener(this);
        super.detach();
    }

    private void initData() {
        dataList = new ArrayList<>(TransactionRepository.get().findAll());
        dataProvider = new ListDataProvider<>(dataList);
        grid.setDataProvider(dataProvider);
        updateCaption();
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
        // Vaadin UI will expire after 3 heartbeats are missed. By default
        // heartbeats occur every 5 minutes. After 15 minutes this view
        // will not be attached anymore. So there is no need to listen
        // anymore.
        //
        // Note that only guaranteed way of getting notification about
        // browser closure is to add javascript listener to window.onbeforeunload
        // and notify Vaadin application from there. But for most cases
        // just letting heartbeats to timeout is enough.
        if (!isAttached()) {
            Broadcaster.removeBroadcastListener(this);
            return;
        }
        // Note that we are calling getUI() here and that will target
        // ListView object which is in UI inside some user's session.
        // Updates form background threads must have some way to access
        // correct UI object. This is one of those ways.
        getUI().access(() -> {
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
