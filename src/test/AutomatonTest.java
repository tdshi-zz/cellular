package de.study.app.test;

import de.study.app.model.Cell;
import de.study.app.model.included.GameOfLifeAutomaton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomatonTest {
    private GameOfLifeAutomaton automaton;

    @BeforeEach
    void createAutomaton() {
        this.automaton = new GameOfLifeAutomaton(50, 50, 2, true, true);
    }

    @Test
    void initialStateTest() {
        Cell[][] cellfield = this.automaton.getCellField();
        int rows = this.automaton.getNumberOfRows();
        int columns = this.automaton.getNumberOfColumns();
        var cellCounter = 0;

        for (var i = 0; i < rows; i++) {
            for (var j = 0; j < columns; j++) {
                if (cellfield[i][j].getState() != 0) {
                    cellCounter++;
                }
            }
        }
        assertTrue(cellCounter == 0);
    }

    @Test
    void stateTest() {
        assertTrue(this.automaton.getNumberOfStates() == 2);
    }

    @Test
    void changeCellStateTest() {
        this.automaton.setStateForCell(1, 1, 1);
        assertTrue(this.automaton.getSpecificCell(1, 1).getState() == 1);
    }

    @Test
    void changeDefaultContextMethodSize() {
        assertTrue(this.automaton.getMethodsForContextMenu().size() == 2);
    }
}
