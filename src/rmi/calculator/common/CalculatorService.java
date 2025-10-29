package rmi.calculator.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CalculatorService extends Remote {
    double add(double a, double b, String clientId) throws RemoteException;
    double sub(double a, double b, String clientId) throws RemoteException;
    double mul(double a, double b, String clientId) throws RemoteException;
    double div(double a, double b, String clientId) throws RemoteException;
    double pow(double a, double b, String clientId) throws RemoteException;
    double sqrt(double a, String clientId) throws RemoteException;
    double sin(double a, String clientId) throws RemoteException;
    double cos(double a, String clientId) throws RemoteException;
    double tan(double a, String clientId) throws RemoteException;
}


