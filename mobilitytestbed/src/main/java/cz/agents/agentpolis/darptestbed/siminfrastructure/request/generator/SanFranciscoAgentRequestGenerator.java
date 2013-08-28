package cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;

import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Driver;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.GPS;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.Passenger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.PassengerRequest;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.TimeWindow;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.PasssengerGenerator;
import cz.agents.agentpolis.darptestbed.siminfrastructure.request.generator.support.VehicleGenerator;

public class SanFranciscoAgentRequestGenerator {

	private static final Logger LOGGER = Logger.getLogger(SanFranciscoAgentRequestGenerator.class);
	private static final long HALF_HOUR = Duration.standardMinutes(30).getMillis();
	private static final long HOUR24 = Duration.standardDays(1).getMillis();

	private final VehicleGenerator vehicleCapacityGenerator;
	private final PasssengerGenerator requestCallTimeGenerator;
	private final ObjectMapper mapper;
	private final Duration requestCallTimeShifft;

	public SanFranciscoAgentRequestGenerator(VehicleGenerator vehicleCapacityGenerator,
			PasssengerGenerator requestCallTimeGenerator, ObjectMapper mapper, Duration requestCallTimeShifft) {
		super();
		this.vehicleCapacityGenerator = vehicleCapacityGenerator;
		this.requestCallTimeGenerator = requestCallTimeGenerator;
		this.mapper = mapper;
		this.requestCallTimeShifft = requestCallTimeShifft;
	}

	public void generatePopulation(File driverOutputFile, File passengerOutputFile, Path cabDirectory,
			DateTime validPassengerDate) throws JsonGenerationException, JsonMappingException, IOException {

		PassengerBuilder passengerBuilder = new PassengerBuilder();

		List<Passenger> passengers = new ArrayList<Passenger>();
		List<Driver> drivers = new ArrayList<Driver>();

		try (DirectoryStream<Path> ds = Files.newDirectoryStream(cabDirectory, "new_*.txt")) {
			for (Path p : ds) {

				List<String[]> traceItems = loadData(p);
				if (traceItems != null && traceItems.isEmpty() == false) {
					String driverId = p.getFileName().toString().replaceFirst("new_", "").replaceAll(".txt", "");
					drivers.add(createDriver(driverId, traceItems.get(0)));
					passengers.addAll(loadPassenger(traceItems, passengerBuilder, validPassengerDate));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		mapper.writeValue(driverOutputFile, drivers);
		mapper.writeValue(passengerOutputFile, passengers);

		LOGGER.info("For driver population, it was initialized drivers - " + drivers.size());
		LOGGER.info("For passenger population, it was initialized pssangers - " + passengers.size());

	}

	private List<Passenger> loadPassenger(List<String[]> traceItems, PassengerBuilder passengerBuilder,
			DateTime validPassengerDate) throws IOException {

		List<Passenger> passengers = Lists.newArrayList();

		for (String[] traceItem : traceItems) {

			DateTime traceTimeStemp = convertToDateTime(traceItem);

			boolean occupied = traceItem[2].equals("1");

			if (occupied == false && passengerBuilder.isBuildInProgress()) {
				passengers.add(passengerBuilder.buildPassenger());
			}

			if (occupied && passengerBuilder.isBuildInProgress()) {
				passengerBuilder.setDestination(traceItem, traceTimeStemp);
			}

			if (occupied && passengerBuilder.isBuildInProgress() == false
					&& isValidDateTime(traceTimeStemp, validPassengerDate)) {
				passengerBuilder.setOrigin(traceItem, traceTimeStemp);
			}

		}

		if (passengerBuilder.isBuildInProgress()) {
			passengers.add(passengerBuilder.buildPassenger());
		}

		return removeNull(passengers);
	}

	private List<Passenger> removeNull(List<Passenger> passengers) {
		while (passengers.remove(null)) {
		}
		return passengers;
	}

	private boolean isValidDateTime(DateTime traceTimeStemp, DateTime validPassengerDate) {
		return validPassengerDate.getDayOfYear() == traceTimeStemp.getDayOfYear()
				&& validPassengerDate.getYear() == traceTimeStemp.getYear();
	}

	private List<String[]> loadData(Path path) throws FileNotFoundException, IOException {
		FileReader fileReader = new FileReader(path.toFile());
		CSVReader csvReader = new CSVReader(fileReader, ' ');

		return csvReader.readAll();

	}

	private GPS parseGPS(String[] traceItem) {
		return new GPS(Double.valueOf(traceItem[0]), Double.valueOf(traceItem[1]));
	}

	private Driver createDriver(String driverId, String[] traceItem) {
		return new Driver(driverId, parseGPS(traceItem), vehicleCapacityGenerator.generateVehicleCapacity(),
				vehicleCapacityGenerator.generateVehicleEquipments());
	}

	private DateTime convertToDateTime(String[] traceItem) {
		long fromSecondtoMillis = Long.valueOf(traceItem[3]) * 1000;
		return new DateTime(fromSecondtoMillis);
	}

	private class PassengerBuilder {

		private int passengerIdCounter = 0;

		private GPS originPosition = null;
		private GPS destinationnPosition = null;
		private TimeWindow departureTimeWindow = null;
		private TimeWindow arrivalTimeWindow = null;

		public boolean isBuildInProgress() {
			return originPosition != null;
		}

		public void setOrigin(String[] traceItem, DateTime traceTimeStemp) {
			originPosition = parseGPS(traceItem);
			departureTimeWindow = createTimeWindow(traceTimeStemp);
		}

		public void setDestination(String[] traceItem, DateTime traceTimeStemp) {
			destinationnPosition = parseGPS(traceItem);
			arrivalTimeWindow = createTimeWindow(traceTimeStemp);
		}

		private TimeWindow createTimeWindow(DateTime traceTimeStemp) {
			long millisOfDay = traceTimeStemp.getMillisOfDay();
			long extendTime = millisOfDay + HALF_HOUR;
			if (extendTime >= HOUR24) {
				extendTime = HOUR24 - 1;
			}

			return new TimeWindow(millisOfDay, extendTime);

		}

		public Passenger buildPassenger() {

			if (destinationnPosition == null) {
				return null;
			}

			// long requestCallTimeInDayRange = requestCallTimeGenerator
			// .generateRequestCallTimeInDayRange(departureTimeWindow.fromTimeInDayRange);
			long requestCallTimeInDayRange = Math.max(0,
					(departureTimeWindow.fromTimeInDayRange - requestCallTimeShifft.getMillis()));
			PassengerRequest passengerRequest = new PassengerRequest(originPosition, destinationnPosition,
					requestCallTimeInDayRange, departureTimeWindow, arrivalTimeWindow);

			Passenger passenger = new Passenger("passsngerId" + passengerIdCounter,
					requestCallTimeGenerator.generateAdditionalRequirements(), Arrays.asList(passengerRequest));

			originPosition = null;
			destinationnPosition = null;
			departureTimeWindow = null;
			arrivalTimeWindow = null;
			passengerIdCounter++;

			return passenger;

		}
	}

}
