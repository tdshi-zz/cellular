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
package de.study.app.settings;

/**
 * This object contains all data about the settings
 * Further implementation and usage will maybe be implemented in the future
 */
public class Settings {

    private final double xPosition;
    private final double yPosition;
    private final double windowWidth;
    private final double windowHeight;
    private final double zoomCount;
    private final int simulationSpeed;
    private String name;

    public Settings(String name, double xPosition, double yPosition, double windowWidth, double windowHeight, double zoomCount, int simulationSpeed) {
        this.name = name;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.zoomCount = zoomCount;
        this.simulationSpeed = simulationSpeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getXPosition() {
        return xPosition;
    }

    public double getYPosition() {
        return yPosition;
    }

    public double getWindowWidth() {
        return windowWidth;
    }

    public double getWindowHeight() {
        return windowHeight;
    }

    public double getZoomCount() {
        return zoomCount;
    }

    public int getSimulationSpeed() {
        return simulationSpeed;
    }

}
