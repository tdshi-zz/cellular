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
package de.study.app.controller.etc;

import java.util.ArrayList;
import java.util.List;

/**
 * Own Implementation of Observer Pattern.
 * <p>
 * Custom Class for Observer Pattern
 */
public class Observer {

    private final List<ObservableInterface> assignedObservers = new ArrayList<>();

    public void add(ObservableInterface observer) {
        this.assignedObservers.add(observer);
    }

    public void remove(ObservableInterface observer) {
        this.assignedObservers.remove(observer);
    }

    public void notifyAssignedObserver() {
        for (ObservableInterface observer : this.assignedObservers) {
            observer.update(this);
        }
    }

    public List<ObservableInterface> getAssignedObservers() {
        return assignedObservers;
    }
}
