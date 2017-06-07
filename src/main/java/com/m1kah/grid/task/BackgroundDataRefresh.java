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

package com.m1kah.grid.task;

import com.m1kah.grid.ui.Broadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundDataRefresh {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundDataRefresh.class);
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void executeAsRepeatingTask() {
        logger.info("Started repeating background task");
        executorService.scheduleAtFixedRate(
                new RefreshDataTask(
                        BackgroundDataRefresh::updateUiAndRepeat,
                        BackgroundDataRefresh::logAndRepeat),
                5,
                5,
                TimeUnit.SECONDS);
    }

    private static void logAndRepeat(Throwable t) {
        logger.error("Failed to update transactions", t);
    }

    private static void updateUiAndRepeat(List<String> updatedTransactionIds) {
        Broadcaster.notifyUis(updatedTransactionIds);
    }

    public static void cancelRepeatingTask() {
        logger.info("Stopped repeating background task");
        executorService.shutdown();
    }
}
