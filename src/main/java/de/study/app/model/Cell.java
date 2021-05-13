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

import java.io.Serializable;


public class Cell implements Serializable {

    private int state;

    public Cell() {
        this.state = 0;
    }

    public Cell(int state) {
        this.state = state;
    }

    public Cell(Cell cell) {
        this.state = cell.state;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
