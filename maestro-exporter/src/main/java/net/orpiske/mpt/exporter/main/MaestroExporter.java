package net.orpiske.mpt.exporter.main;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import net.orpiske.mpt.exporter.collectors.ConnectionCount;
import net.orpiske.mpt.exporter.collectors.MessageCount;
import net.orpiske.mpt.exporter.collectors.PingInfo;
import net.orpiske.mpt.exporter.collectors.RateCount;
import net.orpiske.mpt.maestro.Maestro;
import net.orpiske.mpt.maestro.client.MaestroCollector;
import net.orpiske.mpt.maestro.notes.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class MaestroExporter {
    private static final Logger logger = LoggerFactory.getLogger(MaestroExporter.class);

    private static final MessageCount messagesSent;
    private static final MessageCount messagesReceived;
    private static final RateCount senderRate;
    private static final RateCount receiverRate;

    private static final ConnectionCount senderChildCount;
    private static final ConnectionCount receiverChildCount;

    private static final PingInfo senderPingInfo;
    private static final PingInfo receiverPingInfo;

    private static final Counter failures;
    private static final Counter successes;
    private static final Counter abnormal;

    private boolean running = true;
    private Maestro maestro = null;

    static {
        messagesSent = new MessageCount("sent");
        messagesReceived = new MessageCount("received");
        senderRate = new RateCount("sender");
        receiverRate = new RateCount("receiver");

        senderChildCount = new ConnectionCount("sender");
        receiverChildCount = new ConnectionCount("receiver");

        senderPingInfo = new PingInfo("sender");
        receiverPingInfo = new PingInfo("receiver");


        failures = Counter.build()
                 .name("maestro_failures")
                 .help("Test failures")
                 .register();

        successes = Counter.build().name("maestro_success")
                .help("Test success")
                .register();

        abnormal = Counter.build().name("maestro_abnormal_disconnect")
                .help("Abnormal disconnect count")
                .register();

    }

    public MaestroExporter(final String maestroUrl) throws MqttException {
        maestro = new Maestro(maestroUrl);

        messagesSent.register();
        messagesReceived.register();
        senderRate.register();
        receiverRate.register();

        senderChildCount.register();
        receiverChildCount.register();

        senderPingInfo.register();
        receiverPingInfo.register();
    }

    private void processNotes(List<MaestroNote> notes) {


        for (MaestroNote note : notes) {
            if (note instanceof StatsResponse) {
                StatsResponse statsResponse = (StatsResponse) note;

                if (statsResponse.getRole().equals("sender")) {
                    messagesSent.eval(statsResponse);
                    senderChildCount.eval(statsResponse);
                    senderRate.eval(statsResponse);
                }
                else {
                    if (statsResponse.getRole().equals("receiver")) {
                        messagesReceived.eval(statsResponse);
                        receiverChildCount.eval(statsResponse);
                        receiverRate.eval(statsResponse);
                    }
                }
            }
            else {
                if (note instanceof PingResponse) {
                    PingResponse pingResponse = (PingResponse) note;

                    if (pingResponse.getName().contains("sender")) {
                        senderPingInfo.eval(pingResponse);
                    }
                    else {
                        if (pingResponse.getName().contains("receiver")) {
                            receiverPingInfo.eval(pingResponse);
                        }
                    }
                }
                else {
                   if (note instanceof TestFailedNotification) {
                       failures.inc();
                   }
                   else {
                       if (note instanceof TestSuccessfulNotification) {
                           successes.inc();
                       }
                       else {
                           if (note instanceof AbnormalDisconnect) {
                               abnormal.inc();
                           }
                       }
                   }

                }
            }

            logger.trace("Note: {}", note.toString());
        }


    }


    public int run(int port) throws MqttException, IOException {
        logger.info("Exporting metrics on 0.0.0.0:" + port);

        HTTPServer server = null;

        try {
            server = new HTTPServer(port);


            while (running) {
                logger.debug("Sending requests");
                maestro.statsRequest();
                maestro.pingRequest();

                List<MaestroNote> notes = maestro.collect(1000, 5);

                if (notes != null) {
                    processNotes(notes);
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        finally {
            if (server != null) {
                server.stop();
            }
        }

        return 0;
    }
}