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

import de.study.app.model.Simulator;
import de.study.app.view.PopulationPanel;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.Set;

public class PopulationPanelController {

    private static final Logger LOG = LogManager.getLogger(PopulationPanelController.class);
    private static final double BORDERSPACING = 20;
    private static final String FUNCTIONALTITLE = "functional_title";
    private static final String FUNCTIONALZOOMIN = "functional_text_zoomed_in";

    private final PopulationPanel panel;
    private final Simulator simulator;
    private final ResourceBundle messages;
    private int selectedStateToggle = 0;
    private int selectedColumn = 0;
    private int selectedRow = 0;
    private int zoomCount = 0;


    public PopulationPanelController(ResourceBundle resources) {
        messages = resources;
        this.simulator = new Simulator();
        this.panel = new PopulationPanel(simulator, 590, 590);
    }

    public void onNextGenerationCall() throws Throwable {
        synchronized (this) {
            simulator.getAutomaton().nextGenerationCalculation();
        }
    }

    public void onResetToZeroCall() {
        simulator.getAutomaton().clearPopulationStateToZero();
    }

    public void onRandomPopulationCall() {
        simulator.getAutomaton().createRandomPopulation();
    }

    public void onTorusCall() {
        simulator.getAutomaton().setTorus(!simulator.getAutomaton().isTorus());
    }

    public void onZoomInCall() {
        zoomCount = simulator.getAutomaton().getZoomCount() + 1;
        simulator.getAutomaton().setZoomCount(zoomCount);
    }

    public void onZoomOutCall() {
        zoomCount = simulator.getAutomaton().getZoomCount() - 1;
        simulator.getAutomaton().setZoomCount(zoomCount);
    }

    /**
     * ActionEvent Handler when the color of a ColorPicker has changed.
     * Gets all Colors from the ColorPickers and changes the colors in the mappingStates Object.
     * <p>
     */
    public void onColorPickerCall(ColorPicker[] colorPickers) {
        for (int i = 0; i < simulator.getAutomaton().getNumberOfStates(); i++) {
            simulator.getMappingStates().changeSpecificStateColorTo(colorPickers[i].getValue().getRed(), colorPickers[i].getValue().getGreen(), colorPickers[i].getValue().getBlue(), i);
        }
    }

    public void selectedToggleChangedCall(RadioButton[] radioButtons) {
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                selectedStateToggle = i;
            }
        }
    }

    /**
     * This methods calculates the cell on which the user has pressed and then sets the value of the cell based on the selected toggle state
     *
     * @param mouseEvent contains the x and y coordinates of a cell on the population pane
     */
    public void mousePressedOnPopulationPanel(MouseEvent mouseEvent) {
        if (zoomCount <= 0) {
            double canvasCellWidth = panel.getCalculatedCellWidth();
            double canvasCellHeight = panel.getCalculatedCellHeight();
            double xCoordinate = mouseEvent.getX();
            double yCoordinate = mouseEvent.getY();

            this.selectedColumn = (int) ((xCoordinate - BORDERSPACING) / canvasCellWidth);
            this.selectedRow = (int) ((yCoordinate - BORDERSPACING) / canvasCellHeight);
            if (selectedColumn < simulator.getAutomaton().getCellField()[0].length && selectedRow < simulator.getAutomaton().getCellField().length && selectedColumn >= 0 && selectedRow >= 0) {
                simulator.getAutomaton().setStateForCell(selectedRow, selectedColumn, selectedStateToggle);
            }
        } else {
            AlertController.alertPrompt(Alert.AlertType.WARNING, messages.getString(FUNCTIONALTITLE), messages.getString(FUNCTIONALZOOMIN));
        }
    }

    /**
     * This methods changes the state of more then one cell when the x and y coordinates are not equal with the pressed one
     *
     * @param mouseEvent contains the x and y coordinates of a cell on the population pane
     */
    public void mouseReleasedOnPopulationPanel(MouseEvent mouseEvent) {
        if (zoomCount <= 0) {
            double xCoordinateR = mouseEvent.getX();
            double yCoordinateR = mouseEvent.getY();
            double canvasCellWidth = panel.getCalculatedCellWidth();
            double canvasCellHeight = panel.getCalculatedCellHeight();
            int width = simulator.getAutomaton().getCellField().length;
            int length = simulator.getAutomaton().getCellField()[0].length;
            int getColFromClickedCellR = (int) ((xCoordinateR - BORDERSPACING) / canvasCellWidth);
            int getRowFromClickedCellR = (int) ((yCoordinateR - BORDERSPACING) / canvasCellHeight);

            // Left -> Right
            boolean caseZero = getColFromClickedCellR < length && getRowFromClickedCellR < width && getColFromClickedCellR > selectedColumn && getRowFromClickedCellR > selectedRow && getRowFromClickedCellR >= 0 && getColFromClickedCellR >= 0;
            // Right -> Left | Same Row
            boolean caseOne = getColFromClickedCellR < length && getRowFromClickedCellR < width && getColFromClickedCellR < selectedColumn && getRowFromClickedCellR == selectedRow && getRowFromClickedCellR >= 0 && selectedColumn < length && getColFromClickedCellR >= 0;
            // Right -> Left | row >, col <
            boolean caseTwo = getColFromClickedCellR < length && getRowFromClickedCellR < width && getColFromClickedCellR < selectedColumn && getRowFromClickedCellR > selectedRow && selectedColumn < length && getRowFromClickedCellR >= 0 && getColFromClickedCellR >= 0;
            // Right -> Left | row <, col <
            boolean caseThree = getColFromClickedCellR < length && getRowFromClickedCellR < width && getColFromClickedCellR < selectedColumn && getRowFromClickedCellR < selectedRow && selectedColumn < length && selectedRow < width && getRowFromClickedCellR >= 0 && getColFromClickedCellR >= 0;

            if (caseZero) {
                for (int i = selectedRow; i <= getRowFromClickedCellR; i++) {
                    for (int j = selectedColumn; j <= getColFromClickedCellR; j++) {
                        simulator.getAutomaton().setStateForCell(i, j, selectedStateToggle);
                    }
                }
            } else if (caseOne) {
                int i = selectedRow;
                for (int j = selectedColumn; j >= getColFromClickedCellR; j--) {
                    simulator.getAutomaton().setStateForCell(i, j, selectedStateToggle);
                }

            } else if (caseTwo) {
                for (int i = selectedRow; i < getRowFromClickedCellR; i++) {
                    for (int j = selectedColumn; j >= getColFromClickedCellR; j--) {
                        simulator.getAutomaton().setStateForCell(i, j, selectedStateToggle);
                    }
                }
            } else if (caseThree) {
                for (int i = selectedRow; i >= getRowFromClickedCellR; i--) {
                    for (int j = selectedColumn; j >= getColFromClickedCellR; j--) {
                        simulator.getAutomaton().setStateForCell(i, j, selectedStateToggle);
                    }
                }
            }
        } else
            AlertController.alertPrompt(Alert.AlertType.WARNING, messages.getString(FUNCTIONALTITLE), messages.getString(FUNCTIONALZOOMIN));
    }

    public void showCellfieldContextMenu(ContextMenuEvent contextMenuEvent) {
        if (zoomCount <= 0) {
            ContextMenu theContextMenu = new ContextMenu();
            int rows = simulator.getAutomaton().getNumberOfRows();
            int columns = simulator.getAutomaton().getNumberOfColumns();
            Pair<Integer, Integer> xy = new Pair<>((int) ((contextMenuEvent.getX() - BORDERSPACING) / panel.getCalculatedCellWidth()), (int) ((contextMenuEvent.getY() - BORDERSPACING) / panel.getCalculatedCellHeight()));

            if (xy.getValue() < rows && xy.getKey() < columns && contextMenuEvent.getY() > BORDERSPACING && contextMenuEvent.getX() > BORDERSPACING) {
                Set<Method> theMethods = simulator.getAutomaton().getMethodsForContextMenu();
                for (Method method : theMethods) {
                    MenuItem newMenuItem = new MenuItem("void " + method.getName() + "(int row, int column)");
                    theContextMenu.getItems().add(newMenuItem);
                    newMenuItem.setOnAction(event -> {
                        try {
                            method.invoke(simulator.getAutomaton(), xy.getValue(), xy.getKey());
                        } catch (Exception exc) {
                            LOG.error(exc);
                        }
                    });
                }
                theContextMenu.show(this.panel.getScene().getWindow(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            }
        } else
            AlertController.alertPrompt(Alert.AlertType.WARNING, messages.getString(FUNCTIONALTITLE), messages.getString(FUNCTIONALZOOMIN));
    }

    public PopulationPanel getPopulationPanel() {
        return panel;
    }

    public Simulator getSimulator() {
        return simulator;
    }

    public int getZoomCount() {
        return zoomCount;
    }

    public void setZoomCount(int zoomCount) {
        this.zoomCount = zoomCount;
    }
}
