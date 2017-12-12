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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
     *
     * BroadcastListeners should be coded so that they remove themselves
     * from Broadcaster when they do not want to get events anymore. However,
     * we are using weak references to listeners anyway. This willm
     * make sure that we are not "leaking" views that should get
     * closed. Note that views can be around for some time, that is
     * until Vaadin heartbeats time out.
     */
    private static final List<WeakReference<BroadcastListener>> listeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * {@see https://vaadin.com/docs/-/part/framework/advanced/advanced-push.html}
     */
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void notifyUis(List<String> updatedTransactionIds) {
        Iterator<WeakReference<BroadcastListener>> i = listeners.iterator();
        while (i.hasNext()) {
            WeakReference<BroadcastListener> reference = i.next();
            BroadcastListener listener = reference.get();
            if (listener == null) {
                i.remove();
                logger.info("Removed garbage collected listener");
                continue;
            } else {
                executorService.execute(() -> listener.onTransactionDataUpdate(updatedTransactionIds));
            }
        }
        logger.debug("Notified {} broadcast listeners", listeners.size());
    }

    public static void addBroadcastListener(BroadcastListener listener) {
        listeners.add(new WeakReference<>(listener));
        logger.info("Broadcast listener added: {}", listener);
    }

    public static void removeBroadcastListener(BroadcastListener listener) {
        Iterator<WeakReference<BroadcastListener>> i = listeners.iterator();
        while (i.hasNext()) {
            if (i.next().get() == listener) {
                i.remove();
                logger.info("Broadcast listener removed: {}", listener);
                return;
            }
        }
        throw new IllegalStateException("Listener was not removed");
    }

    public static void cancelUpdates() {
        executorService.shutdown();
    }
}
