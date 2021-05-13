package de.study.app.test;

import de.study.app.model.Cell;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    @Test
    void createCellTest() {
        Cell firstCell = new Cell(1);
        assertTrue(firstCell.getState() == 1);

        Cell secondCell = new Cell(firstCell);
        assertTrue(secondCell.getState() == 1);

        Cell thirdCell = new Cell();
        assertTrue(thirdCell.getState() == 0);
    }

}
