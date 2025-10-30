package rmi.calculator.server.math;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import rmi.calculator.common.MathService;
import rmi.calculator.server.common.ServerLogger;

public class MathServiceImpl extends UnicastRemoteObject implements MathService {
    private final ServerLogger logger;

    public MathServiceImpl(ServerLogger logger) throws RemoteException {
        super();
        this.logger = logger;
    }

    @Override
    public double add(double a, double b, String clientId) throws RemoteException {
        logRequest(clientId, "[Math] add", a, b);
        double res = a + b;
        logResult(clientId, res);
        return res;
    }

    @Override
    public double sub(double a, double b, String clientId) throws RemoteException {
        logRequest(clientId, "[Math] sub", a, b);
        double res = a - b;
        logResult(clientId, res);
        return res;
    }

    @Override
    public double mul(double a, double b, String clientId) throws RemoteException {
        logRequest(clientId, "[Math] mul", a, b);
        double res = a * b;
        logResult(clientId, res);
        return res;
    }

    @Override
    public double div(double a, double b, String clientId) throws RemoteException {
        logRequest(clientId, "[Math] div", a, b);
        if (b == 0.0) {
            RemoteException ex = new RemoteException("Division by zero");
            logger.error(withClient(clientId, "[Math] div error: division by zero"), ex);
            throw ex;
        }
        double res = a / b;
        logResult(clientId, res);
        return res;
    }

    @Override
    public double pow(double a, double b, String clientId) throws RemoteException {
        logRequest(clientId, "[Math] pow", a, b);
        double res = Math.pow(a, b);
        logResult(clientId, res);
        return res;
    }

    @Override
    public double sqrt(double a, String clientId) throws RemoteException {
        logRequest(clientId, "[Math] sqrt", a, null);
        if (a < 0.0) {
            RemoteException ex = new RemoteException("Square root of negative number");
            logger.error(withClient(clientId, "[Math] sqrt error: negative input"), ex);
            throw ex;
        }
        double res = Math.sqrt(a);
        logResult(clientId, res);
        return res;
    }

    private void logRequest(String clientId, String op, Double a, Double b) {
        String msg = String.format("[%s] op=%s a=%s b=%s", clientId, op,
                a == null ? "" : a, b == null ? "" : b);
        logger.info(msg);
    }

    private void logResult(String clientId, double result) {
        logger.info(String.format("[%s] result=%s", clientId, result));
    }

    private String withClient(String clientId, String message) {
        return String.format("[%s] %s", clientId, message);
    }
}


