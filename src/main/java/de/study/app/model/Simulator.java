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

import de.study.app.model.included.GameOfLifeAutomaton;

/**
 * This Class is holding the initial model. So far it is a GameOfLifeAutomaton.
 * Also the simulator itself holds the corresponding colors matched to the single states.
 */
public class Simulator {

    private Automaton automaton;
    private ColorMappingStates mappingStates;

    public Simulator() {
        this.automaton = new GameOfLifeAutomaton(25, 25, 2, true, true);
        this.mappingStates = new ColorMappingStates(automaton.getNumberOfStates());
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    public ColorMappingStates getMappingStates() {
        return mappingStates;
    }

    public void setMappingStates(ColorMappingStates mappingStates) {
        this.mappingStates = mappingStates;
    }
}
