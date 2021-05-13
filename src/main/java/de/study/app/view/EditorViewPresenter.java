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
package de.study.app.view;

import de.study.app.controller.AlertController;
import de.study.app.controller.EditorController;
import de.study.app.controller.PopulationPanelController;
import de.study.app.controller.etc.ObservableInterface;
import de.study.app.model.Automaton;
import de.study.app.model.ColorMappingStates;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditorViewPresenter implements Initializable {

    private final EditorController editorController;
    private final PopulationPanelController populationController;
    private final String name;

    @FXML
    TextArea textArea = new TextArea();
    @FXML
    Menu editor;
    @FXML
    MenuItem save;
    @FXML
    MenuItem compile;
    @FXML
    MenuItem close;

    public EditorViewPresenter(String name, PopulationPanelController model, String sourceCode) {
        this.name = name;
        this.editorController = new EditorController(name, sourceCode);
        this.populationController = model;
    }

    // ------------------------------------------------------------------------------------
    // FXML Methods
    // ------------------------------------------------------------------------------------

    @FXML
    public void onSaveCall() {
        editorController.saveToJavaFile(textArea.getText());
    }

    @FXML
    public void onCompileCall() throws IOException {
        Automaton compiledAutomaton = editorController.onCompile(textArea.getText());
        if (compiledAutomaton != null && compiledAutomaton.getNumberOfStates() >= 2) {
            List<ObservableInterface> buffer = populationController.getSimulator().getAutomaton().getAssignedObservers();
            populationController.getSimulator().setAutomaton(compiledAutomaton);
            populationController.getSimulator().setMappingStates(new ColorMappingStates(compiledAutomaton.getNumberOfStates()));
            for (ObservableInterface elem : buffer) {
                populationController.getSimulator().getAutomaton().add(elem);
            }
            populationController.getSimulator().getAutomaton().notifyAssignedObserver();
        } else {
            AlertController.alertPrompt(Alert.AlertType.WARNING, "Warnung", "Dieser Automat wurde kompiliert, aber nur ein Zustand ist nicht erlaubt. Bitte einen Zustand >2 compilieren.");
        }
    }

    @FXML
    public void onCloseCall() {
        Stage stage = (Stage) textArea.getScene().getWindow();
        stage.close();
    }

    // ------------------------------------------------------------------------------------
    // Help Methods
    // ------------------------------------------------------------------------------------

    public void loadActualCodeIntoTextArea() {
        this.textArea.setText(editorController.getActualAutomatonCode());
    }

    public String getTextAreaContent() {
        this.textArea.setText(editorController.getActualAutomatonCode());
        return textArea.getText();
    }

    public void createMenuLanguageBinding() {
        editor.textProperty().bind(I18N.createStringBinding("editor"));
        save.textProperty().bind(I18N.createStringBinding("save"));
        compile.textProperty().bind(I18N.createStringBinding("compile"));
        close.textProperty().bind(I18N.createStringBinding("close"));
    }

    public String getName() {
        return name;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createMenuLanguageBinding();
    }
}
