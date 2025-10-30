package rmi.calculator.server.trig;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import rmi.calculator.common.TrigService;
import rmi.calculator.server.common.ServerLogger;

public class TrigServiceImpl extends UnicastRemoteObject implements TrigService {
    private final ServerLogger logger;

    public TrigServiceImpl(ServerLogger logger) throws RemoteException {
        super();
        this.logger = logger;
    }

    @Override
    public double sin(double a, String clientId) throws RemoteException {
        logRequest(clientId, "[Trig] sin", a);
        double res = Math.sin(a);
        logResult(clientId, res);
        return res;
    }

    @Override
    public double cos(double a, String clientId) throws RemoteException {
        logRequest(clientId, "[Trig] cos", a);
        double res = Math.cos(a);
        logResult(clientId, res);
        return res;
    }

    @Override
    public double tan(double a, String clientId) throws RemoteException {
        logRequest(clientId, "[Trig] tan", a);
        double res = Math.tan(a);
        logResult(clientId, res);
        return res;
    }

    private void logRequest(String clientId, String op, Double a) {
        String msg = String.format("[%s] op=%s a=%s", clientId, op,
                a == null ? "" : a);
        logger.info(msg);
    }

    private void logResult(String clientId, double result) {
        logger.info(String.format("[%s] result=%s", clientId, result));
    }
}


