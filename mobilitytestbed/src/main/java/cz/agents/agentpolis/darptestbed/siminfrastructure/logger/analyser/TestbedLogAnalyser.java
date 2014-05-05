package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.eventbus.Subscribe;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init.TestbedProcessor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.structure.AvgMaxProcessor;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.item.*;
import cz.agents.agentpolis.siminfrastructure.logger.agent.activity.logitem.EndDrivingLogItem;
import cz.agents.agentpolis.siminfrastructure.logger.agent.activity.logitem.MovementArrivalLogItem;
import cz.agents.agentpolis.siminfrastructure.logger.agent.activity.logitem.StartDrivingLogItem;
import cz.agents.agentpolis.util.InitAndGetterUtil;
import eu.superhub.wp4.simulator.analyser.processor.util.AvgCounter;
import eu.superhub.wp4.simulator.analyser.structure.VehiclePath;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TestbedLogAnalyser {

    private static final Logger LOGGER = Logger.getLogger(TestbedLogAnalyser.class);
    private final File resultOutputFile;
    private final long simulationStartTime;

    // String == vehicle id
    private final Map<String, VehiclePath> vehiclePaths = new HashMap<String, VehiclePath>();

    private final List<TestbedProcessor> testbedProcessors = new ArrayList<TestbedProcessor>();
    private final AvgMaxProcessor passengerOnBoardTime = new AvgMaxProcessor();
    private final AvgMaxProcessor passengerWaitTime = new AvgMaxProcessor();
    private final AvgMaxProcessor passengerTravelTime = new AvgMaxProcessor();
    private final Multiset<VehicleHourKey> productivity = HashMultiset.create();
    private final Map<String, PassengerRequestLogItem> passengerRequestLogItems = new HashMap<>();
    private long algRealTime = 0;
    private int successfulRequests = 0;
    private int failedRequests = 0;
    private int impossibleRequests = 0;


    public TestbedLogAnalyser(File result) {
        this(result, System.currentTimeMillis());
    }

    public TestbedLogAnalyser(File result, long simulationStartTime) {
        super();
        this.resultOutputFile = result;
        this.simulationStartTime = simulationStartTime;
    }

    @Subscribe
    public void processPassengerRequest(PassengerRequestLogItem passengerRequest) {
        passengerWaitTime.addStartLogItem(passengerRequest.passengerId,
                passengerRequest.timeWindow.getEarliestDeparture());
        passengerTravelTime.addStartLogItem(passengerRequest.passengerId,
                passengerRequest.timeWindow.getEarliestDeparture());
        passengerRequestLogItems.put(passengerRequest.passengerId, passengerRequest);

        long interval = passengerRequest.timeWindow.getLatestArrival() -
                passengerRequest.timeWindow.getEarliestDeparture();

        long drivingTime = passengerRequest.utils.computeDrivingTime(passengerRequest.fromNodeId,
                passengerRequest.toNodeId, 30);
        long slack = interval - drivingTime;
        if (slack < 0) {
            ++impossibleRequests;
            LOGGER.debug("#" + impossibleRequests + " - " + slack + " - " + drivingTime +
                    " trip length - " + passengerRequest.utils.computeDistance(
                    passengerRequest.fromNodeId, passengerRequest.toNodeId) + " - impossible " +
                    "request - " + passengerRequest);
        }
    }

    @Subscribe
    public void processPassengerGetInVehicle(PassengerGetInVehicleLogItem passengerGetInVehicle) {
        productivity.add(VehicleHourKey.newinstance(passengerGetInVehicle.vehicleId,
                passengerGetInVehicle.simulationTime));
        passengerWaitTime.addEndLogItem(passengerGetInVehicle.passengerId, passengerGetInVehicle.simulationTime);
        passengerOnBoardTime.addStartLogItem(passengerGetInVehicle.passengerId, passengerGetInVehicle.simulationTime);
    }

    @Subscribe
    public void processPassengerGetOffVehicle(PassengerGetOffVehicleLogItem passengerGetOffVehicle) {
        passengerOnBoardTime.addEndLogItem(passengerGetOffVehicle.passengerId, passengerGetOffVehicle.simulationTime);
        passengerTravelTime.addEndLogItem(passengerGetOffVehicle.passengerId, passengerGetOffVehicle.simulationTime);
        PassengerRequestLogItem passengerRequestLogItem = passengerRequestLogItems
                .get(passengerGetOffVehicle.passengerId);
        if (passengerRequestLogItem.timeWindow.getLatestArrival() < passengerGetOffVehicle.simulationTime) {
            failedRequests++;
        } else {
            successfulRequests++;
        }
    }

    @Subscribe
    public void handleStartDrivingLogItem(StartDrivingLogItem startDrivingLogItem) {
        assert vehiclePaths.containsKey(startDrivingLogItem.driverId) == false : "Driver is driving ";

        VehiclePath vehiclePath = new VehiclePath(startDrivingLogItem.vehicleId, startDrivingLogItem.simulationTime);
        vehiclePaths.put(startDrivingLogItem.driverId, vehiclePath);

    }

    @Subscribe
    public void processRequestRejected(RequestRejectedLogItem requestRejected) {
        failedRequests++;
    }

    @Subscribe
    public void handleEndDrivingLogItem(EndDrivingLogItem endDrivingLogItem) {
        assert vehiclePaths.containsKey(endDrivingLogItem.driverId) : "Driver is not driving";

        VehiclePath vehiclePath = vehiclePaths.remove(endDrivingLogItem.driverId);

        if (vehiclePath.path.size() > 0) {

            for (TestbedProcessor vehiclePathProcessor : testbedProcessors) {
                vehiclePathProcessor.process(endDrivingLogItem.driverId, vehiclePath);
            }
        }

    }

    @Subscribe
    public void handleAlgRealTimeLogItem(AlgRealTimeLogItem algRealTimeLogItem) {
        this.algRealTime += algRealTimeLogItem.realTime;
    }

    public void addVehiclePathProcessor(TestbedProcessor testbedProcessor) {
        testbedProcessors.add(testbedProcessor);
    }

    @Subscribe
    public void handleMovementArrivalLogItem(MovementArrivalLogItem movementArrivalLogItem) {
        VehiclePath vehiclePath = vehiclePaths.get(movementArrivalLogItem.agentId);

        if (vehiclePath != null) {
            vehiclePath.addPath(movementArrivalLogItem);
            vehiclePaths.put(movementArrivalLogItem.agentId, vehiclePath);
        } else {

        }

    }

    public void processResult() {

        StringBuilder resultOutput = new StringBuilder();
        resultOutput.append(System.lineSeparator());
        appendWithNewLine(resultOutput, "------ Simulation result -----------");
        appendWithNewLine(resultOutput, "Successful requests: %s", successfulRequests);
        appendWithNewLine(resultOutput, "Failed requests: %s", failedRequests);
        appendWithNewLine(resultOutput, "Found impossible requests: %s", impossibleRequests);
        appendWithNewLine(resultOutput, "Average passenger travel time (on-board) is : %s",
                parseTime(passengerTravelTime.getAvg()));
        appendWithNewLine(resultOutput, "Max passenger travel time (on-board) is: %s",
                parseTime(passengerTravelTime.getMax()));
        appendWithNewLine(resultOutput, "Median passenger travel time (on-board) is: %s",
                parseTime(passengerTravelTime.getMedian()));
        appendWithNewLine(resultOutput, "Average passenger ride time (on-board) is: %s",
                parseTime(passengerOnBoardTime.getAvg()));
        appendWithNewLine(resultOutput, "Max passenger ride time (on-board) is: %s",
                parseTime(passengerOnBoardTime.getMax()));
        appendWithNewLine(resultOutput, "Median passenger ride time (on-board) is: %s",
                parseTime(passengerOnBoardTime.getMedian()));
        appendWithNewLine(resultOutput, "Average passenger wait time is: %s", parseTime(passengerWaitTime.getAvg()));
        appendWithNewLine(resultOutput, "Max passenger wait time is: %s", parseTime(passengerWaitTime.getMax()));
        appendWithNewLine(resultOutput, "Median passenger wait time is: %s", parseTime(passengerWaitTime.getMedian()));

        for (TestbedProcessor testbedProcessor : testbedProcessors) {
            resultOutput.append(testbedProcessor.provideResult());
            resultOutput.append(System.lineSeparator());
        }

        appendWithNewLine(resultOutput, "Alg. real time: %s", parseTime(this.algRealTime));
        appendWithNewLine(resultOutput, "Simulation real time: %s", parseTime(System.currentTimeMillis()
                - simulationStartTime));
        resultOutput.append(printProductivity());

        LOGGER.info(resultOutput.toString());
        writeResultToFile(resultOutput.toString());
    }

    private void appendWithNewLine(StringBuilder resultOutput, String text, Object... args) {
        resultOutput.append(String.format(text, args));
        resultOutput.append(System.lineSeparator());
    }

    private void writeResultToFile(String resultOutput) {

        try (FileWriter fileWriter = new FileWriter(resultOutputFile)) {
            fileWriter.write(resultOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");

    private String parseTime(double time) {
        return parseTime((long) time);
    }

    private String parseTime(long time) {
        if (time <= 0) {
            return "NaN";
        }
        return fmt.print(new DateTime(1999, 1, 2, 0, 0, 0).plus(time));
    }

    private String printProductivity() {
        SortedMap<Long, AvgCounter> avgCounters = computeAvgProductivity();
        if (avgCounters.isEmpty()) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        String pattern = "Hour - %s : avg. passenger per vehicle hour - %s";

        for (long key = 0; key <= avgCounters.lastKey(); key++) {
            AvgCounter avgPassengerPerVehicleHour = avgCounters.get(key);
            if (avgPassengerPerVehicleHour == null) {
                output.append(String.format(pattern, key, 0));
            } else {
                output.append(String.format(pattern, key, avgPassengerPerVehicleHour.getCurrentAvgValue()));
            }

            output.append(System.lineSeparator());
        }

        return output.toString();

    }

    private SortedMap<Long, AvgCounter> computeAvgProductivity() {
        SortedMap<Long, AvgCounter> avgCounters = new TreeMap<>();
        for (VehicleHourKey vehicleHourKey : productivity) {
            AvgCounter avgCounter = InitAndGetterUtil.getDataOrInitFromMap(avgCounters, vehicleHourKey.hour,
                    new AvgCounter());
            avgCounter.addValue(productivity.count(vehicleHourKey));
            avgCounters.put(vehicleHourKey.hour, avgCounter);
        }

        return avgCounters;

    }

    private static class VehicleHourKey {
        private final static long HOUR = Duration.standardHours(1).getMillis();

        public final String vehicleId;
        public final long hour;

        private VehicleHourKey(String vehicleId, long hour) {
            super();
            this.vehicleId = vehicleId;
            this.hour = hour;
        }

        public static VehicleHourKey newinstance(String vehicleId, long simulationTime) {
            return new VehicleHourKey(vehicleId, (long) simulationTime / HOUR);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (hour ^ (hour >>> 32));
            result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VehicleHourKey other = (VehicleHourKey) obj;
            if (hour != other.hour)
                return false;
            if (vehicleId == null) {
                if (other.vehicleId != null)
                    return false;
            } else if (!vehicleId.equals(other.vehicleId))
                return false;
            return true;
        }

    }

}
