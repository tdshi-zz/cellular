/*
 * Cellular Automaton - study project winter term 2020/2021
 * Copyright (C) 2021 tdshi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. See LICENSE File in the main directory. If not, see <https://www.gnu.org/licenses/>.
 */
package de.study.app;

import de.study.app.view.MainViewPresenter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;


public class App extends Application {

    private static final Logger LOG = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initializeMainWindow(primaryStage);
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(1);
    }

    // ------------------------------------------------------------------------------------
    // Help Methods
    // ------------------------------------------------------------------------------------

    private void initializeMainWindow(Stage primaryStage) {
        try {
            ResourceBundle messages = ResourceBundle.getBundle("casimulator");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), ResourceBundle.getBundle("casimulator"));

            MainViewPresenter presenter = new MainViewPresenter();
            presenter.createMenuModel("DefaultAutomaton");

            loader.setController(presenter);
            BorderPane root = loader.load();
            Scene scene = new Scene(root);

            JMetro jMetro = new JMetro(Style.LIGHT);
            jMetro.setScene(scene);

            primaryStage.setTitle(messages.getString("stage_title"));
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image("img/logo/logo.gif"));
            primaryStage.centerOnScreen();
            primaryStage.show();
            presenter.saveStageAfterInitialisation();

        } catch (Exception throwable) {
            LOG.error(throwable.getMessage());
        }
    }
}
