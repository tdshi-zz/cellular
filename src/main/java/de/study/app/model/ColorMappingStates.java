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
package de.study.app.model;

import de.study.app.controller.etc.Observer;
import javafx.scene.paint.Color;

/**
 * This class is for handling all about the colors from our model states.
 * On creation we got the state number from the model, then we create and fill the color array.
 * We've got some methods to change the size of the array and the colors from corresponding states.
 */
public class ColorMappingStates extends Observer {

    private Color[] colorArray;

    public ColorMappingStates(int states) {
        colorArray = new Color[states];
        colorArray[0] = Color.PURPLE;
        colorArray[1] = Color.LIME;
        if (states > 2) {
            for (int i = 2; i < states; i++) {
                this.colorArray[i] = Color.color(Math.random(), Math.random(), Math.random());
            }
        }
    }

    public boolean changingVisibleColorTo(int newStatesNumber) {
        Color[] buffer = new Color[newStatesNumber];
        System.arraycopy(colorArray, 0, buffer, 0, colorArray.length);
        colorArray = new Color[newStatesNumber];
        System.arraycopy(buffer, 0, colorArray, 0, buffer.length);
        for (int i = colorArray.length + 1; i < newStatesNumber; i++) {
            this.colorArray[i] = Color.color(Math.random(), Math.random(), Math.random());
        }
        return true;
    }

    public void changeSpecificStateColorTo(double rgb1, double rgb2, double rgb3, int state) {
        colorArray[state] = Color.color(rgb1, rgb2, rgb3);
        notifyAssignedObserver();
    }

    public Color[] getColorArray() {
        return colorArray;
    }
}
