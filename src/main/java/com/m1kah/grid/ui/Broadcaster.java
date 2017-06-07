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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster {
    private static final Logger logger = LoggerFactory.getLogger(Broadcaster.class);
    /**
     * We are having process level reference to all listeners which are
     * UI objects in this case. We need to make sure that each UI is
     * removed from this list.
     *
     * Each browser tab and window is an UI object. We can update everyone
     * who is connected to the server by iterating through UI objects.
     */
    private static final List<BroadcastListener> listeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * {@see https://vaadin.com/docs/-/part/framework/advanced/advanced-push.html}
     */
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void notifyUis(List<String> updatedTransactionIds) {
        for (BroadcastListener listener : listeners) {
            executorService.execute(() -> listener.onTransactionDataUpdate(updatedTransactionIds));
        }
        logger.debug("Notified {} broadcast listeners", listeners.size());
    }

    public static void addBroadcastListener(BroadcastListener listener) {
        listeners.add(listener);
        logger.info("Broadcast listener added: {}", listener);
    }

    public static void removeBroadcastListener(BroadcastListener listener) {
        listeners.remove(listener);
        logger.info("Broadcast listener removed: {}", listener);
    }

    public static void cancelUpdates() {
        executorService.shutdown();
    }
}
