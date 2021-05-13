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

import de.study.app.model.Automaton;
import de.study.app.model.Cell;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * Serialisation Model, contains the logic for serialisation of a population
 */
public class SerialController {

    private static final File userHomeDirectory = new File(System.getProperty("user.home"));
    private static final FileChooser fileChooser;
    private static final Logger LOG = LogManager.getLogger(SerialController.class);

    static {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(userHomeDirectory);
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("*.ser", "*.ser");
        fileChooser.getExtensionFilters().add(filter);
    }

    private SerialController() {
        throw new IllegalStateException("Error 404 - You can't instantiate a utility class :'(");
    }

    public static void savePopulationAsSerialFile(Automaton automaton, Stage specificStage) {
        fileChooser.setTitle("Store: Population (Serial)");
        File saveNewFile = fileChooser.showSaveDialog(specificStage);
        if (saveNewFile != null) {
            if (!saveNewFile.getName().endsWith(".ser")) {
                saveNewFile = new File(saveNewFile.getAbsolutePath().concat(".ser"));
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveNewFile))) {
                synchronized (automaton.getSyncObject()) {
                    oos.writeInt(automaton.getNumberOfStates());
                    oos.writeObject(automaton.getCellField());
                }
            } catch (IOException e) {
                LOG.error(e);
                AlertController.alertPrompt(Alert.AlertType.ERROR, "Error (Saving)", "Population konnte nicht Serialisiert werden.");
            }
        }
    }

    public static void loadPopulationFromSerialFile(Automaton automaton, Stage specificStage) {
        fileChooser.setTitle("Load: Population (Serial)");
        File loadNewPopulationFile = fileChooser.showOpenDialog(specificStage);
        if (loadNewPopulationFile != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(loadNewPopulationFile))) {
                // TODO: Add Handling to change state size
                automaton.changePopulationOnCellfield((Cell[][]) ois.readObject());
            } catch (Exception e) {
                LOG.error(e);
                AlertController.alertPrompt(Alert.AlertType.ERROR, "Error (Loading)", "Ungültige Datei");
            }
        }
    }
}
