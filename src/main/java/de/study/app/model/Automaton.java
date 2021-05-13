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

import de.study.app.controller.etc.Callable;
import de.study.app.controller.etc.Observer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * The abstract class model holds all essential information, which all custom implementations share.
 * Only the transform method is needed to be implemented.
 */
public abstract class Automaton extends Observer {

    private final int numberOfStates;
    private final boolean isMooreNeighborHood;
    private final Object syncObject;

    /**
     * Contains all methods which apply following specific rules:
     * 1. non-static
     * 2. non-abstract
     * 3. two parameters typ int
     * 4. with annotation callable
     */
    private final Set<Method> methodSet = new HashSet<>();
    private final Random random = new Random();
    private int rows;
    private int columns;
    private boolean isTorus;
    private Cell[][] cellField;
    private volatile int zoomCount = 0;
    private String name;

    protected Automaton(int rows, int columns, int numberOfStates, boolean isMooreNeighborHood, boolean isTorus) {
        this.rows = rows;
        this.columns = columns;
        this.numberOfStates = numberOfStates;
        this.isMooreNeighborHood = isMooreNeighborHood;
        this.isTorus = isTorus;
        this.cellField = new Cell[rows][columns];

        this.name = "DefaultAutomaton";
        this.syncObject = new Object();

        initialFillPopulationWithZero();
        initMethodSet();
    }

    public void clearPopulationStateToZero() {
        synchronized (syncObject) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    cellField[i][j].setState(0);
                }
            }
        }
        notifyAssignedObserver();
    }

    public void initialFillPopulationWithZero() {
        synchronized (syncObject) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    cellField[i][j] = new Cell();
                }
            }
        }
        notifyAssignedObserver();
    }

    public void createRandomPopulation() {
        synchronized (syncObject) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    cellField[i][j].setState(random.nextInt(this.numberOfStates));
                }
            }
        }
        notifyAssignedObserver();
    }

    public void changePopulationOnCellfield(Cell[][] newPopulation) {
        synchronized (syncObject) {
            this.cellField = newPopulation;
            this.rows = newPopulation.length;
            this.columns = newPopulation[0].length;
        }
        notifyAssignedObserver();
    }

    /**
     * In this method we create first an array for buffering and fill it with the current cellfield content.
     * Then we check for each cell in which row the cell is located. We divide the rows to three locations:
     * 1. row zero ; 2 all middle rows ; 3 last row
     * <p>
     * In each possible location then we check, if the cell is located at the outer left column or outer right column or in between.
     * <p>
     * When the position is known we delegate the calculation of the neighbor cells to the concrete neighbor calculation method.
     *
     * @throws Throwable if something went wrong
     */
    public void nextGenerationCalculation() throws Throwable {
        synchronized (syncObject) {
            Cell[][] bufferArray = new Cell[rows][columns];

            for (int i = 0; i < cellField.length; i++) {
                for (int j = 0; j < cellField[i].length; j++) {
                    bufferArray[i][j] = new Cell(cellField[i][j]);
                }
            }

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    ArrayList<Cell> neighborCells;
                    if (i == 0) {
                        if (j == 0) {
                            neighborCells = calculateNeighborsOnLeftColumn(i);
                        } else if (j == columns - 1) {
                            neighborCells = calculateNeighborsOnRightColumn(i);
                        } else {
                            neighborCells = calculateNeighborsOnMiddleColumns(i, j);
                        }
                    } else if (i == rows - 1) {
                        if (j == 0) {
                            neighborCells = calculateNeighborsOnLeftColumn(i);
                        } else if (j == columns - 1) {
                            neighborCells = calculateNeighborsOnRightColumn(i);
                        } else {
                            neighborCells = calculateNeighborsOnMiddleColumns(i, j);
                        }
                    } else {
                        if (j == 0) {
                            neighborCells = calculateNeighborsOnLeftColumn(i);
                        } else if (j == columns - 1) {
                            neighborCells = calculateNeighborsOnRightColumn(i);
                        } else {
                            neighborCells = calculateNeighborsOnMiddleColumns(i, j);
                        }
                    }

                    Cell[] neighborArray = new Cell[neighborCells.size()];
                    for (int k = 0; k < neighborCells.size(); k++) {
                        neighborArray[k] = neighborCells.get(k);
                    }
                    bufferArray[i][j] = transform(bufferArray[i][j], neighborArray);
                }
            }
            cellField = bufferArray;
        }
        notifyAssignedObserver();
    }

    protected abstract Cell transform(Cell cell, Cell[] neighbors) throws Throwable;

    private ArrayList<Cell> calculateNeighborsOnLeftColumn(int row) {
        synchronized (syncObject) {
            ArrayList<Cell> neighborCells = new ArrayList<>();
            int j = 0;
            /* first row */
            if (row == 0) {
                /* right side cell */
                neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                /* cell below */
                neighborCells.add(this.cellField[((row + 1) % this.rows)][j]);
                if (isMooreNeighborHood) {
                    /* right below cell */
                    neighborCells.add(this.cellField[((row + 1) % this.rows)][((j + 1) % this.columns)]);
                    if (isTorus) {
                        /* left side cell */
                        neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                        /* left below cell */
                        neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((j - 1), this.columns)]);
                        /* left side upper cell */
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((j - 1), this.columns)]);
                        /* right side upper cell */
                        neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((j + 1) % this.columns)]);
                        /* top cell */
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                    }
                } else if (isTorus) {
                    /* top cell */
                    neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                    /* left side cell */
                    neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                }
                /* last row */
            } else if (row == rows - 1) {
                /* top cell */
                neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                /* right side cell */
                neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                if (isMooreNeighborHood) {
                    /* right side upper cell */
                    neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((j + 1) % this.columns)]);
                    if (isTorus) {
                        /* right below cell */
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][((j + 1) % this.columns)]);
                        /* cell below */
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][j]);
                        /* left below cell */
                        neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((j - 1), this.columns)]);
                        /* left side cell */
                        neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                        /* left side upper cell */
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((j - 1), this.columns)]);
                    }
                } else if (isTorus) {
                    /* left side cell */
                    neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                    /* cell below */
                    neighborCells.add(this.cellField[((row + 1) % this.rows)][j]);
                }
                /* all rows between zero and last */
            } else {
                /* top cell */
                neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                /* right side cell */
                neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                /* cell below */
                neighborCells.add(this.cellField[((row + 1) % this.rows)][j]);
                if (isMooreNeighborHood) {
                    /* right below cell */
                    neighborCells.add(this.cellField[((row + 1) % this.rows)][((j + 1) % this.columns)]);
                    /* right side upper cell */
                    neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((j + 1) % this.columns)]);
                    if (isTorus) {
                        /* left below cell */
                        neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((j - 1), this.columns)]);
                        /* left side cell */
                        neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                        /* left side upper cell */
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((j - 1), this.columns)]);
                    }
                } else if (isTorus) {
                    /* left side cell */
                    neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                }
            }
            return neighborCells;
        }
    }

    private ArrayList<Cell> calculateNeighborsOnRightColumn(int row) {
        synchronized (syncObject) {
            ArrayList<Cell> neighborCells = new ArrayList<>();
            int j = columns - 1;
            // first row
            if (row == 0) {
                // left side cell
                neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                // cell below
                neighborCells.add(this.cellField[((row + 1) % this.rows)][j]);
                if (isMooreNeighborHood) {
                    // left below cell
                    neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((j - 1), this.columns)]);
                    if (isTorus) {
                        // right below cell
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][((j + 1) % this.columns)]);
                        // right side upper cell
                        neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((j + 1) % this.columns)]);
                        // right side cell
                        neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                        // top cell
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                        // left side upper cell
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((j - 1), this.columns)]);
                    }
                } else if (isTorus) {
                    // top cell
                    neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                    // right side cell
                    neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                }
                // last row
            } else if (row == rows - 1) {
                // top cell
                neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                // left side cell
                neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                if (isMooreNeighborHood) {
                    // left side upper cell
                    neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((j - 1), this.columns)]);
                    if (isTorus) {
                        // right side upper cell
                        neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((j + 1) % this.columns)]);
                        // right side cell
                        neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                        // right below cell
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][((j + 1) % this.columns)]);
                        // cell below
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][j]);
                        // left below cell
                        neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((j - 1), this.columns)]);
                    }
                } else if (isTorus) {
                    // right side cell
                    neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                    // right below cell
                    neighborCells.add(this.cellField[((row + 1) % this.rows)][((j + 1) % this.columns)]);
                }
                // all rows between zero and last
            } else {
                // top cell
                neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][j]);
                // left side cell
                neighborCells.add(cellField[row][Math.floorMod((j - 1), this.columns)]);
                // cell below
                neighborCells.add(this.cellField[((row + 1) % this.rows)][j]);
                if (isMooreNeighborHood) {
                    // left below cell
                    neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((j - 1), this.columns)]);
                    // left side upper cell
                    neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((j - 1), this.columns)]);
                    if (isTorus) {
                        // right side upper cell
                        neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((j + 1) % this.columns)]);
                        // right side cell
                        neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                        // right below cell
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][((j + 1) % this.columns)]);
                    }
                } else if (isTorus) {
                    // right side cell
                    neighborCells.add(this.cellField[row][((j + 1) % this.columns)]);
                }
            }
            return neighborCells;
        }
    }

    private ArrayList<Cell> calculateNeighborsOnMiddleColumns(int row, int col) {
        synchronized (syncObject) {
            ArrayList<Cell> neighborCells = new ArrayList<>();
            // first row
            if (row == 0) {
                // left side cell
                neighborCells.add(cellField[row][Math.floorMod((col - 1), this.columns)]);
                // right side cell
                neighborCells.add(this.cellField[row][((col + 1) % this.columns)]);
                // cell below
                neighborCells.add(this.cellField[((row + 1) % this.rows)][col]);
                if (isMooreNeighborHood) {
                    // right below cell
                    neighborCells.add(this.cellField[((row + 1) % this.rows)][((col + 1) % this.columns)]);
                    // left below cell
                    neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((col - 1), this.columns)]);
                    if (isTorus) {
                        // top cell
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][col]);
                        // left side upper cell
                        neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((col - 1), this.columns)]);
                        // right side upper cell
                        neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((col + 1) % this.columns)]);
                    }
                }
                if (isTorus) {
                    // top cell
                    neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][col]);
                }
                // last row
            } else if (row == rows - 1) {
                // left side cell
                neighborCells.add(cellField[row][Math.floorMod((col - 1), this.columns)]);
                // right side cell
                neighborCells.add(this.cellField[row][((col + 1) % this.columns)]);
                // top cell
                neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][col]);
                if (isMooreNeighborHood) {
                    // left side upper cell
                    neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((col - 1), this.columns)]);
                    // right side upper cell
                    neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((col + 1) % this.columns)]);
                    if (isTorus) {
                        // right below cell
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][((col + 1) % this.columns)]);
                        // left below cell
                        neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((col - 1), this.columns)]);
                        // cell below
                        neighborCells.add(this.cellField[((row + 1) % this.rows)][col]);
                    }
                }
                if (isTorus) {
                    // cell below
                    neighborCells.add(this.cellField[((row + 1) % this.rows)][col]);
                }
                // all rows between zero and last
            } else {
                // left side cell
                neighborCells.add(cellField[row][Math.floorMod((col - 1), this.columns)]);
                // right side cell
                neighborCells.add(this.cellField[row][((col + 1) % this.columns)]);
                // top cell
                neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][col]);
                // cell below
                neighborCells.add(this.cellField[((row + 1) % this.rows)][col]);
                if (isMooreNeighborHood) {
                    // right below cell
                    neighborCells.add(this.cellField[((row + 1) % this.rows)][((col + 1) % this.columns)]);
                    // left below cell
                    neighborCells.add(cellField[((row + 1) % this.rows)][Math.floorMod((col - 1), this.columns)]);
                    // left side upper cell
                    neighborCells.add(this.cellField[Math.floorMod((row - 1), this.rows)][Math.floorMod((col - 1), this.columns)]);
                    // right side upper cell
                    neighborCells.add(cellField[Math.floorMod((row - 1), this.rows)][((col + 1) % this.columns)]);
                }
            }
            return neighborCells;
        }
    }

    public void changeCellfieldSizeAndCopyValues(int rows, int columns) {
        synchronized (syncObject) {
            Cell[][] bufferArray = new Cell[rows][columns];
            if (rows != this.rows && columns == this.columns) {
                if (rows < this.rows) {
                    for (int i = 0; i < rows; i++) {
                        if (columns >= 0) System.arraycopy(cellField[i], 0, bufferArray[i], 0, columns);
                    }
                } else {
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < columns; j++) {
                            if (i < this.rows) {
                                bufferArray[i][j] = new Cell(cellField[i][j].getState());
                            } else {
                                bufferArray[i][j] = new Cell(0);
                            }
                        }
                    }
                }
            } else if (rows <= this.rows && columns <= this.columns) {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        bufferArray[i][j] = new Cell(cellField[i][j].getState());
                    }
                }
            } else if (rows == this.rows) {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        if (j < this.columns)
                            bufferArray[i][j] = new Cell(cellField[i][j].getState());
                        else if (j >= this.rows) {
                            bufferArray[i][j] = new Cell(0);
                        }
                    }

                }
            } else {
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        if (i < this.rows && j < this.columns)
                            bufferArray[i][j] = new Cell(cellField[i][j].getState());
                        else {
                            Cell newCell = new Cell(0);
                            bufferArray[i][j] = newCell;
                        }
                    }
                }
            }
            this.cellField = new Cell[rows][columns];
            for (int i = 0; i < rows; i++)
                if (columns >= 0) System.arraycopy(bufferArray[i], 0, cellField[i], 0, columns);
            this.rows = rows;
            this.columns = columns;
        }
        notifyAssignedObserver();
    }

    /**
     * This methods iterates on initialization of the model class through all methods of the model
     * and checks if the methods has a Callable-Annotation,
     * if the Modifier is public and if the method has 2 integer parameters
     */
    public void initMethodSet() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            Parameter[] theParameterOfMethod = method.getParameters(); // Containing all parameters of the viewed method

            boolean checkCallable = method.isAnnotationPresent(Callable.class);
            boolean checkModifier = method.getModifiers() == Modifier.PUBLIC;
            boolean checkParameter = theParameterOfMethod[0].getType().equals(int.class) && theParameterOfMethod[1].getType().equals(int.class);

            if (checkCallable && checkModifier && checkParameter) {
                methodSet.add(method);
            }
        }
    }

    public Cell[][] getCellField() {
        synchronized (syncObject) {
            return cellField;
        }
    }

    public Cell getSpecificCell(int row, int column) {
        synchronized (syncObject) {
            return cellField[row][column];
        }
    }

    public void setStateForCell(int row, int column, int state) {
        synchronized (syncObject) {
            cellField[row][column].setState(state);
        }
        notifyAssignedObserver();
    }

    public void setStateForRangeOfCells(int fromRow, int fromColumn, int toRow, int toColumn, int state) {
        synchronized (syncObject) {
            for (int i = fromRow - 1; i < toRow - 1; i++) {
                for (int j = fromColumn - 1; j < toColumn - 1; j++) {
                    cellField[i][j].setState(state);
                }
            }
        }
        notifyAssignedObserver();
    }


    /**
     * @return true if population should be viewed as torus ; false if not
     */
    public boolean isTorus() {
        synchronized (syncObject) {
            return isTorus;
        }
    }

    public void setTorus(boolean isTorus) {
        synchronized (syncObject) {
            this.isTorus = isTorus;
            notifyAssignedObserver();
        }
    }

    public int getNumberOfStates() {
        synchronized (syncObject) {
            return numberOfStates;
        }
    }

    public int getNumberOfRows() {
        synchronized (syncObject) {
            return rows;
        }
    }

    public int getNumberOfColumns() {
        synchronized (syncObject) {
            return columns;
        }
    }

    /**
     * @return true if model has moore neighborhood ; false if model has von Neumann neighborhood
     */
    public boolean isMooreNeighborHood() {
        synchronized (syncObject) {
            return isMooreNeighborHood;
        }
    }

    public String getName() {
        synchronized (syncObject) {
            return this.name;
        }
    }

    public void setName(String name) {
        synchronized (syncObject) {
            this.name = name;
        }
    }

    public Object getSyncObject() {
        return syncObject;
    }

    public Set<Method> getMethodsForContextMenu() {
        return methodSet;
    }

    public int getZoomCount() {
        return zoomCount;
    }

    public void setZoomCount(int zoomCount) {
        synchronized (syncObject) {
            this.zoomCount = zoomCount;
        }
        notifyAssignedObserver();
    }
}