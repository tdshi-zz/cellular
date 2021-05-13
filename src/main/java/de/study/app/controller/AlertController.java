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
package de.study.app.controller;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

public class AlertController {

    private AlertController() {
        throw new IllegalStateException("Error 404 - You can't instantiate a Utility Class :'(");
    }

    public static void alertPrompt(Alert.AlertType type, String title, String alertMessage) {
        Alert alert = new Alert(type);
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(750);
        alert.setTitle(title);
        alert.setContentText(alertMessage);
        alert.showAndWait();
    }
}
