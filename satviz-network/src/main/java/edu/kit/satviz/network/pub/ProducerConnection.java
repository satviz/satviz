package edu.kit.satviz.network.pub;

import edu.kit.satviz.network.general.Connection;
import edu.kit.satviz.serial.SerializationException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

public class ProducerConnection {
    private final String address;
    private final int port;
    private Connection client = null;
    private final ProducerId pid;

    private final Object SYNC = new Object();
    private volatile boolean shouldClose = false;

    public ProducerConnection(String address, int port, ProducerId pid) {
        this.address = address;
        this.port = port;
        this.pid = pid;
    }

    private void doEstablish() throws IOException, InterruptedException {
        do {
            try {
                client = new Connection(address, port, MessageTypes.satvizBlueprint);
            } catch (ConnectException e) {
                // connection refused by remote machine (no-one listening on port)
                // try again later
                client = null;
                Thread.sleep(1000);
            }
        } while (client == null && !shouldClose);

        Map<String, String> offerData = new HashMap<>();
        offerData.put("version", "1");
        if (pid.type() == OfferType.SOLVER) {
            offerData.put("type", "solver");
            offerData.put("name", pid.solverName());
            offerData.put("hash", Long.toString(pid.instanceHash()));
            offerData.put("delayed", pid.solverDelayed() ? "true" : "false");
        } else {
            offerData.put("type", "proof");
        }
        try {
            client.write(MessageTypes.OFFER, offerData);
        } catch (SerializationException e) {
            throw new RuntimeException(e); // TODO
        }
    }
}
