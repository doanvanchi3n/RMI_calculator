package rmi.calculator.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TrigService extends Remote {
    double sin(double a, String clientId) throws RemoteException;
    double cos(double a, String clientId) throws RemoteException;
    double tan(double a, String clientId) throws RemoteException;
}


