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
package de.study.app.controller;

import de.study.app.controller.etc.RMIInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class StudentController {

    private static final Logger LOG = LogManager.getLogger(StudentController.class);
    private RMIInterface sendToTeacherServer;
    private boolean connected = false;

    public StudentController() {
        establishConnection();
    }

    public void send(String name, String sourceCode) throws RemoteException {
        sendToTeacherServer.sendToTeacher(name, sourceCode);
    }

    public void establishConnection() {
        try {
            Registry registry = LocateRegistry.getRegistry(3579);
            sendToTeacherServer = (RMIInterface) registry.lookup("Server");
            connected = true;
        } catch (RemoteException e) {
            LOG.error("Teacher-Server is not running.");
        } catch (NotBoundException g) {
            LOG.error(g);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
