package net.orpiske.mpt.maestro.worker.base;

import net.orpiske.mpt.common.evaluators.LatencyEvaluator;
import net.orpiske.mpt.common.exceptions.DurationParseException;
import net.orpiske.mpt.common.exceptions.MaestroConnectionException;
import net.orpiske.mpt.common.exceptions.MaestroException;
import net.orpiske.mpt.common.test.TestProperties;
import net.orpiske.mpt.common.worker.*;
import net.orpiske.mpt.maestro.MaestroReceiverClient;
import net.orpiske.mpt.maestro.client.AbstractMaestroPeer;
import net.orpiske.mpt.maestro.client.MaestroDeserializer;
import net.orpiske.mpt.maestro.exceptions.MalformedNoteException;
import net.orpiske.mpt.maestro.notes.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static net.orpiske.mpt.maestro.worker.base.WorkerStateInfoUtil.isCleanExit;

public class MaestroWorkerManager extends AbstractMaestroPeer<MaestroEvent> implements MaestroEventListener {
    private static final long TIMEOUT_STOP_WORKER_MILLIS = 1_000;
    private static final Logger logger = LoggerFactory.getLogger(MaestroWorkerManager.class);

    private MaestroReceiverClient client;
    private WorkerContainer container;
    private String host;
    private Class<MaestroWorker> workerClass;

    private File logDir;

    private WorkerOptions workerOptions;
    private Thread latencyWriterThread;
    private Thread rateWriterThread;

    // TODO: should support other evaluations in the future
    private LatencyEvaluator latencyEvaluator;

    private boolean running = true;

    public MaestroWorkerManager(final String maestroURL, final String role, final String host, final File logDir,
                                final Class<MaestroWorker> workerClass) throws MaestroException {
        super(maestroURL, role, MaestroDeserializer::deserializeEvent);

        client = new MaestroReceiverClient(maestroURL, clientName, host, id);

        this.workerClass = workerClass;
        this.host = host;
        this.logDir = logDir;
        this.container = WorkerContainer.getInstance(client);

        workerOptions = new WorkerOptions();
    }

    @Override
    public void connect() throws MaestroConnectionException {
        super.connect();

        client.connect();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private File findLastLogDir() {
        File currentLogDir = new File(logDir, "0");
        File lastLogDir = currentLogDir;
        int count = 0;

        while (currentLogDir.exists()) {
            lastLogDir = currentLogDir;

            count++;
            currentLogDir = new File(logDir, Integer.toString(count));
        }

        return lastLogDir;
    }

    private File findTestLogDir() {
        File testLogDir = new File(logDir, "0");
        int count = 0;

        while (testLogDir.exists()) {
            count++;
            testLogDir = new File(logDir, Integer.toString(count));

        }

        testLogDir.mkdirs();
        return testLogDir;
    }

    @Override
    protected final void noteArrived(MaestroEvent note) throws IOException, MaestroConnectionException {
        logger.debug("Some message arrived: {}", note.toString());
        note.notify(this);
    }


    @Override
    public void handle(StatsRequest note) {
        logger.debug("Stats request received");
    }

    @Override
    public void handle(FlushRequest note) {
        logger.debug("Flush request received");
    }

    @Override
    public void handle(Halt note) {
        logger.debug("Halt request received");

        container.stop();

        running = false;
    }

    @Override
    public void handle(SetRequest note) {
        logger.debug("Set request received");

        switch (note.getOption()) {
            case MAESTRO_NOTE_OPT_SET_BROKER: {
                workerOptions.setBrokerURL(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_DURATION_TYPE: {
                workerOptions.setDuration(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_LOG_LEVEL: {
                workerOptions.setLogLevel(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_PARALLEL_COUNT: {
                workerOptions.setParallelCount(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_MESSAGE_SIZE: {
                workerOptions.setMessageSize(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_THROTTLE: {
                workerOptions.setThrottle(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_SET_RATE: {
                workerOptions.setRate(note.getValue());
                break;
            }
            case MAESTRO_NOTE_OPT_FCL: {
                workerOptions.setFcl(note.getValue());
            }
        }

        container.setWorkerOptions(workerOptions);
        client.replyOk();
    }

    private void writeTestProperties(File testLogDir) throws IOException {
        TestProperties testProperties = new TestProperties();

        testProperties.setBrokerUri(workerOptions.getBrokerURL());
        try {
            testProperties.setDuration(workerOptions.getDuration());
        } catch (DurationParseException e) {
            logger.warn("Failed to parse duration while saving the test properties: {}", e.getMessage(), e);
        }
        testProperties.setParallelCount(workerOptions.getParallelCount());

        // Note: it already sets the variable size flag for variable message sizes
        testProperties.setMessageSize(workerOptions.getMessageSize());

        testProperties.setRate(workerOptions.getRate());
        testProperties.setFcl(workerOptions.getFcl());

        // TODO: collect this
        testProperties.setApiName(workerClass.getName());
        testProperties.setApiVersion("undefined");

        testProperties.write(new File(testLogDir, "test.properties"));
    }

    private boolean doWorkerStart() {
        if (container.isTestInProgress()) {
            logger.warn("Trying to start a new test, but a test execution is already in progress");
            client.notifyFailure("Test already in progress");
            return false;
        }

        final File testLogDir = findTestLogDir();

        try {
            writeTestProperties(testLogDir);
        } catch (IOException e) {
            logger.warn("Unable to write test properties: {}", e.getMessage(), e);
        }

        Double givenLatency = workerOptions.getFclAsDouble();
        if (givenLatency != null) {
            this.latencyEvaluator = new LatencyEvaluator(givenLatency);
        }

        try {
            final List<MaestroWorker> workers = new ArrayList<>();
            logger.debug("Starting the workers {}", workerClass);
            container.start(workerClass, workers, this::onStoppedWorkers, latencyEvaluator);

            if (workers.isEmpty()) {
                logger.warn("No workers has been started!");
            } else {
                logger.debug("Creating the writer threads");
                WorkerLatencyWriter latencyWriter = new WorkerLatencyWriter(testLogDir, workers, latencyEvaluator);
                final Thread latencyThread = new Thread(latencyWriter);
                this.latencyWriterThread = latencyThread;

                WorkerChannelWriter rateWriter = new WorkerChannelWriter(testLogDir, workers);
                final Thread rateThread = new Thread(rateWriter);
                this.rateWriterThread = rateThread;
                logger.debug("Starting the writers threads");

                this.latencyWriterThread.start();
                this.rateWriterThread.start();

                //TODO handle shutdown gently
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    shutdownAndWaitWriters();
                }));
            }


            client.replyOk();
        } catch (Exception e) {
            logger.error("Unable to start workers from the container: {}", e.getMessage(), e);
            client.replyInternalError();
        }
        return true;
    }

    private void shutdownAndWaitWriters(){
        if (this.rateWriterThread != null) {
            this.rateWriterThread.interrupt();
        }
        if (this.latencyWriterThread != null) {
            this.latencyWriterThread.interrupt();
        }
        if (this.rateWriterThread != null) {
            try {
                this.rateWriterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.latencyWriterThread != null) {
            try {
                this.latencyWriterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean awaitWorkers(long startWaitingWorkersEpochMillis, final List<WorkerRuntimeInfo> workers) {
        final long deadLine = startWaitingWorkersEpochMillis + (workers.size() * TIMEOUT_STOP_WORKER_MILLIS * 2);
        //workers are being stopped, just need to check if they have finished their jobs
        boolean allFinished = false;
        while (!allFinished && System.currentTimeMillis() < deadLine) {
            allFinished = true;
            for (int i = 0, size = workers.size(); i < size; i++) {
                final WorkerRuntimeInfo workerRuntimeInfo = workers.get(i);
                try {
                    workerRuntimeInfo.thread.join(TIMEOUT_STOP_WORKER_MILLIS);
                } catch (InterruptedException e) {
                    //no op, just retry
                } finally {
                    allFinished = !workerRuntimeInfo.thread.isAlive();
                    if (!allFinished) {
                        break;
                    }
                }
            }
        }
        return allFinished;
    }

    /**
     * It should be called by a different thread/concurrently too (eg WatchDog) so it can't modify any this.* members or workers too.
     */
    private void onStoppedWorkers(List<WorkerRuntimeInfo> workers) {
        if (this.rateWriterThread != null || this.latencyWriterThread != null) {
            final long startWaitingWorkers = System.currentTimeMillis();
            if (!workers.isEmpty()) {
                final boolean allFinished = awaitWorkers(startWaitingWorkers, workers);
                if (!allFinished) {
                    logger.warn("The writer will be forced to stop with alive workers");
                }
            }

            shutdownAndWaitWriters();
            final long elapsedMillis = System.currentTimeMillis() - startWaitingWorkers;
            logger.info("Awaiting workers and Shutting down writers took {} ms", elapsedMillis);
        }

        boolean failed = false;
        for (WorkerRuntimeInfo ri : workers) {
            WorkerStateInfo wsi = ri.worker.getWorkerState();

            if (!isCleanExit(wsi)) {
                failed = true;
                break;
            }
        }

        createSymlinks(failed);
    }

    private void createSymlinks(boolean failed) {
        File lastLogDir = findLastLogDir();
        Path target = Paths.get(lastLogDir.getAbsolutePath());

        Path lastLink = Paths.get(lastLogDir.getParent() + File.separator + "last");
        FileUtils.deleteQuietly(lastLink.toFile());

        try {
            Files.createSymbolicLink(lastLink, target);
        } catch (IOException e) {
            logger.trace("Symbolic link creation error: " + e.getMessage(), e);
        }

        if (failed) {
            Path lastFailedLink = Paths.get(lastLogDir.getParent() + File.separator + "lastFailed");
            FileUtils.deleteQuietly(lastFailedLink.toFile());

            try {
                Files.createSymbolicLink(lastFailedLink, target);
            } catch (IOException e) {
                logger.trace("Symbolic link creation error: " + e.getMessage(), e);
            }
        }
        else {
            Path lastSuccessfulLink = Paths.get(lastLogDir.getParent() + File.separator + "lastSuccessful");
            FileUtils.deleteQuietly(lastSuccessfulLink.toFile());

            try {
                Files.createSymbolicLink(lastSuccessfulLink, target);
            } catch (IOException e) {
                logger.trace("Symbolic link creation error: " + e.getMessage(), e);
            }
        }
    }


    @Override
    public void handle(StartInspector note) {
        logger.debug("Start inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StartReceiver note) {
        logger.debug("Start receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StartSender note) {
        logger.debug("Start sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            if (!doWorkerStart()) {
                logger.warn("::handle {} can't start worker", note);
            }
        }
    }

    @Override
    public void handle(StopInspector note) {
        logger.debug("Stop inspector request received");

        if (MaestroInspectorWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        client.replyOk();
    }

    @Override
    public void handle(StopReceiver note) {
        logger.debug("Stop receiver request received");

        if (MaestroReceiverWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        client.replyOk();
    }

    @Override
    public void handle(StopSender note) {
        logger.debug("Stop sender request received");

        if (MaestroSenderWorker.class.isAssignableFrom(workerClass)) {
            container.stop();
        }

        client.replyOk();
    }

    @Override
    public void handle(TestFailedNotification note) {
        logger.info("Test failed notification received from {}: {}", note.getName(), note.getMessage());
        container.stop();
    }

    @Override
    public void handle(TestSuccessfulNotification note) {
        logger.info("Test successful notification received from {}: {}", note.getName(), note.getMessage());
    }

    @Override
    public void handle(AbnormalDisconnect note) {
        logger.info("Abnormal disconnect notification received from {}: {}", note.getName(), note.getMessage());
    }

    @Override
    public void handle(PingRequest note) throws MaestroConnectionException, MalformedNoteException {
        client.pingResponse(note.getSec(), note.getUsec());
    }
}
