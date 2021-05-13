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

import de.study.app.controller.etc.ObservableInterface;
import de.study.app.model.Automaton;
import de.study.app.model.ColorMappingStates;
import de.study.app.model.Simulator;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Population Panel for drawing the cellfield
 */
public class PopulationPanel extends Region implements ObservableInterface {

    private static final double BORDER_SPACING = 20;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Simulator simulator;
    private double calculatedCellHeight;
    private double calculatedCellWidth;
    private int zoomFactor = 0;

    /**
     * Creates a new Population Panel with a canvas based on the panelHeight and panelWidth from the view given
     * Adds the canvas to the population panel, we need to this here so the canvas is visible in the scene
     * After then draws the initial state on canvas
     *
     * @param simulator the simulator which contains all we need for the panel
     */
    public PopulationPanel(Simulator simulator, int hei, int wid) {
        this.simulator = simulator;
        this.canvas = new Canvas(hei, wid);
        this.gc = canvas.getGraphicsContext2D();
        this.setPrefSize(hei, wid);
        this.getChildren().addAll(this.canvas);
        calculatedCellHeight = (this.getPrefHeight() - BORDER_SPACING) / simulator.getAutomaton().getCellField().length;
        calculatedCellWidth = (this.getPrefWidth() - BORDER_SPACING) / simulator.getAutomaton().getCellField()[0].length;
        draw();
    }

    private void draw() {
        synchronized (simulator.getAutomaton().getSyncObject()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            for (int i = 0; i < simulator.getAutomaton().getCellField().length; i++) {
                for (int j = 0; j < simulator.getAutomaton().getCellField()[0].length; j++) {
                    gc.setFill(simulator.getMappingStates().getColorArray()[simulator.getAutomaton().getCellField()[i][j].getState()]);
                    gc.setStroke(Color.BLACK);
                    gc.fillRect(BORDER_SPACING + j * calculatedCellWidth, BORDER_SPACING + i * calculatedCellHeight, calculatedCellWidth, calculatedCellHeight);
                    gc.strokeRect(BORDER_SPACING + j * calculatedCellWidth, BORDER_SPACING + i * calculatedCellHeight, calculatedCellWidth, calculatedCellHeight);
                }

            }
        }
    }

    // ------------------------------------------------------------------------------------
    // Help Methods
    // ------------------------------------------------------------------------------------

    @Override
    public void update(Object o) {
        if (o instanceof Automaton) {

            this.simulator.setAutomaton((Automaton) o);
            int width = simulator.getAutomaton().getCellField().length;
            int height = simulator.getAutomaton().getCellField()[0].length;
            if (zoomFactor != ((Automaton) o).getZoomCount())
                calculatePanelSize((Automaton) o);
            if (height <= width) {
                calculatedCellHeight = (this.getPrefHeight() - 1 * BORDER_SPACING) / width;
                calculatedCellWidth = calculatedCellHeight;
            } else {
                calculatedCellWidth = (this.getPrefWidth() - 1 * BORDER_SPACING) / height;
                calculatedCellHeight = calculatedCellWidth;
            }
        } else if (o instanceof ColorMappingStates)
            this.simulator.setMappingStates((ColorMappingStates) o);
        Platform.runLater(this::draw);
    }

    private void calculatePanelSize(Automaton automaton) {
        zoomFactor = automaton.getZoomCount();

        if (zoomFactor < -5) {
            zoomFactor = -5;
            automaton.setZoomCount(zoomFactor);
        } else if (zoomFactor > 15) {
            zoomFactor = 15;
            automaton.setZoomCount(zoomFactor);
        }

        if (zoomFactor > -5 && zoomFactor <= 15 && zoomFactor != 0) {
            double calcVal = Math.pow(1.05, zoomFactor);

            double width = 590 * calcVal;
            double height = 590 * calcVal;

            this.canvas.setWidth(width);
            this.canvas.setHeight(height);

            this.setPrefSize(width, height);
        } else if (zoomFactor == 0) {
            this.canvas.setWidth(590);
            this.canvas.setHeight(590);

            this.setPrefSize(590, 590);
        }
    }

    public double getCalculatedCellHeight() {
        return calculatedCellHeight;
    }

    public double getCalculatedCellWidth() {
        return calculatedCellWidth;
    }
}
