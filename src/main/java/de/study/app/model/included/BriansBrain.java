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
package de.study.app.model.included;

import de.study.app.controller.etc.Callable;
import de.study.app.model.Automaton;
import de.study.app.model.Cell;

/**
 * Implementation of Brians Brain Automaton
 *
 * @See https://en.wikipedia.org/wiki/Brian%27s_Brain
 */
public class BriansBrain extends Automaton {

    public BriansBrain(int rows, int columns, int numberOfStates, boolean isMooreNeighborHood, boolean isTorus) {
        super(rows, columns, numberOfStates, isMooreNeighborHood, isTorus);
    }

    @Override
    protected Cell transform(Cell cell, Cell[] neighbors) throws Throwable {
        Cell returnCell = new Cell(cell);
        int counter = 0;
        for (Cell cells : neighbors) {
            if (cells.getState() == 1) {
                counter++;
            }
        }
        if (cell.getState() == 0 && counter == 2) {
            returnCell.setState(1);
        } else if (cell.getState() == 1) {
            returnCell.setState(2);
        } else if (cell.getState() == 2) {
            returnCell.setState(0);
        }
        return returnCell;
    }

    /**
     * Place's a Oscillator on the field.
     *
     * @param row    selected cell row location
     * @param column selected cell column location
     * @see <a href="https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life#Examples_of_patterns">Conway's Game of Life - Example Figures</a>
     */
    @Callable
    public void setOscillator(int row, int column) {
        setStateForCell(row, column, 1);
        setStateForCell((row + 1) % this.getNumberOfRows(), column, 1);
        setStateForCell((row + 2) % this.getNumberOfRows(), column, 1);
    }

    /**
     * Place's a Glider on the the field
     *
     * @param row    selected cell row location
     * @param column selected cell column location
     * @see <a href="https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life#Examples_of_patterns">Conway's Game of Life - Example Figures</a>
     */
    @Callable
    public void setGlider(int row, int column) {
        setStateForCell(row, column, 1);
        setStateForCell((row + 1) % this.getNumberOfRows(), (column + 1) % this.getNumberOfColumns(), 1);
        setStateForCell((row + 2) % this.getNumberOfRows(), (column - 1) % this.getNumberOfColumns(), 1);
        setStateForCell((row + 2) % this.getNumberOfRows(), column, 1);
        setStateForCell((row + 2) % this.getNumberOfRows(), (column + 1) % this.getNumberOfColumns(), 1);
    }
}
