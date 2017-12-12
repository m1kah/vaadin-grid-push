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

import com.m1kah.grid.ui.about.AboutView;
import com.m1kah.grid.ui.list.ListView;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Push
public class GridUi extends UI {
    private static final Logger logger = LoggerFactory.getLogger(GridUi.class);
    private CssLayout viewLayout;
    private Navigator navigator;

    @Override
    protected void init(VaadinRequest request) {
        initLayout();
        initNavigator();
        logger.info("UI created");
    }

    private void initNavigator() {
        navigator = new Navigator(this, viewLayout);
        navigator.addView("", new ListView());
        navigator.addView("about", new AboutView());
    }

    private void initLayout() {
        HorizontalLayout root = new HorizontalLayout();
        root.setSizeFull();
        Component menu = createMenu();
        root.addComponent(menu);
        viewLayout = new CssLayout();
        viewLayout.setSizeFull();
        root.addComponent(viewLayout);
        root.setExpandRatio(viewLayout, 1);
        setContent(root);
    }

    private Component createMenu() {
        VerticalLayout menuLayout = new VerticalLayout();
        menuLayout.addComponent(new Label("Menu"));
        menuLayout.setMargin(true);
        menuLayout.setSpacing(true);
        Button listButton = new Button("Grid");
        listButton.addClickListener(navigateTo(""));
        menuLayout.addComponent(listButton);
        Button aboutButton = new Button("About");
        aboutButton.addClickListener(navigateTo("about"));
        menuLayout.addComponent(aboutButton);
        menuLayout.setWidth("120px");
        return menuLayout;
    }

    private Button.ClickListener navigateTo(String viewName) {
        return event -> navigator.navigateTo(viewName);
    }

}
