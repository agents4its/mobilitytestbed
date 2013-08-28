package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import cz.agents.agentpolis.darptestbed.global.GlobalParams;
import cz.agents.agentpolis.darptestbed.global.Utils;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.VehicleMoveLogger;
import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.data.RequestState;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;
import cz.agents.agentpolis.darptestbed.simmodel.agent.data.TimeWindow;
import cz.agents.agentpolis.darptestbed.simmodel.environment.model.TestbedModel;
import cz.agents.agentpolis.siminfrastructure.logger.LoggerProtocol;
import cz.agents.agentpolis.simmodel.environment.model.AgentPositionModel;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.AllNetworkNodes;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Node;
import cz.agents.agentpolis.simmodel.environment.model.query.AgentPositionQuery;
import cz.agents.agentpolis.simmodel.environment.model.sensor.PositionSensor;
import cz.agents.alite.common.event.EventProcessor;

/**
 * Saves summary data of the whole taxi simulation to evaluate the algorithm.
 * Also, logs every move of vehicles using the callback of PositionSensor.
 * 
 * @author Lukas Canda
 */
@Singleton
@Deprecated
public class StatisticsLogger implements PositionSensor {

	private static final Logger LOGGER = Logger.getLogger(StatisticsLogger.class);
	/**
	 * A storage to manage all data concerning the current state of environment.
	 */
	protected TestbedModel serviceModel;
	/**
	 * The model that takes care of position sensors during the simulation.
	 */
	protected AgentPositionModel agentPositionModel;
	/**
	 * Checks out an agent's current position.
	 */
	protected final AgentPositionQuery positionQuery;
	/**
	 * All nodes in the map
	 */
	protected final Map<Long, Node> allNetworkNodes;
	/**
	 * A storage to save all data concerning taxi drivers and passengers
	 */
	protected final TestbedModel taxiModel;
	/**
	 * A set of useful methods for searching paths, distances etc.
	 */
	protected final Utils utils;
	/**
	 * Logs vehicle movement
	 */
	protected final VehicleMoveLogger vehicleMoveLogger;
	/**
	 * My instance to use after the simulation is finished
	 */
	protected static StatisticsLogger instance = null;

	@Inject
	public StatisticsLogger(LoggerProtocol eventProtocol, EventProcessor eventProcessor, TestbedModel serviceModel,
			AgentPositionModel agentPositionModel, AgentPositionQuery positionQuery, AllNetworkNodes allNetworkNodes,
			TestbedModel taxiModel, Utils utils, VehicleMoveLogger vehicleMoveLogger) {

		this.serviceModel = serviceModel;
		this.agentPositionModel = agentPositionModel;
		this.positionQuery = positionQuery;
		this.allNetworkNodes = allNetworkNodes.getAllNetworkNodes();
		this.taxiModel = taxiModel;
		this.utils = utils;
		this.vehicleMoveLogger = vehicleMoveLogger;
	}

	/**
	 * Registers a taxi driver to check out his position changes all the time
	 * (for logging purposes).
	 * 
	 * @param driverId
	 */
	public void addVehicleForSensor(String driverId) {
		this.agentPositionModel.addSensingPositionNode(driverId, this);
	}

	/**
	 * Change of a taxi driver's position.
	 */
	@Override
	public void newEntityPosition(String entityId, long nodeId) {
		vehicleMoveLogger.logVehicleMove(taxiModel.getVehicleId(entityId), nodeId);
	}

	private String removeQuotation(String strWithQuo) {
		return strWithQuo.substring(1, strWithQuo.length() - 1);
	}

	/**
	 * Writes out the summary report about the whole simulation into the report
	 * file. It uses log file (CSV) as the input to read data from.
	 * 
	 * @param fileReport
	 *            output file
	 * @param fileCSV
	 *            input file
	 */
	public void writeReport(File fileReport, File fileCSV) {

		BufferedReader reader = null;
		String inputRow;
		String[] arrSplitRow;
		// the type of the logged event
		String strType;

		final int PRICE_PER_KM = GlobalParams.getPricePerKilometer();
		// the last position of each taxi driver (node number)
		Map<String, Long> mapOfLastNodes = new HashMap<String, Long>();
		// the number of kilometers traveled with x passengers on board
		List<Double> listOfDistancesWithPassengers = new ArrayList<Double>();
		listOfDistancesWithPassengers.add(0.0);
		// the map of logged requests and their state
		Map<Request, RequestState> reqs = new HashMap<Request, RequestState>();
		// the map of requests and the time they had to wait
		Map<Request, Long> reqsWaitingTime = new HashMap<Request, Long>();
		// the map of taxis with the list of passengers currently on board
		Map<String, List<String>> taxisWithPassengers = new HashMap<String, List<String>>();
		// the map of passengers with total distance they've traveled
		Map<String, Double> passengersAndDistTraveled = new HashMap<String, Double>();
		// the map of passengers with total cost they've paid
		Map<String, Double> passengersAndPricePaid = new HashMap<String, Double>();

		// variables read from log file
		String vehicleId;
		String passengerId;
		long nodeNumber;
		long time;
		// time windows
		long nodeFrom;
		long nodeTo;
		long timeWinOpen;
		long timeWinClose;
		String timeWinOpenStr;
		String timeWinCloseStr;

		// variables counted from the previous ones
		int numOfPassengers;
		long totalWaitingTime = 0;
		double prevDistance = 0;
		double tmpDistance;
		double tmpPrice;
		Request tmpReq;

		try {
			// read CSV file (log file)
			reader = new BufferedReader(new FileReader(fileCSV));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			inputRow = reader.readLine();

			// read the log file row by row
			while (inputRow != null && inputRow.length() > 0) {
				arrSplitRow = inputRow.split(",");
				strType = removeQuotation(arrSplitRow[2]);

				if (strType.equals("VEHICLE_MOVEMENT")) {
					// a taxi has moved to a new node
					nodeNumber = Long.parseLong(removeQuotation(arrSplitRow[3]));
					vehicleId = removeQuotation(arrSplitRow[1]);

					// initial taxi position
					if (!mapOfLastNodes.containsKey(vehicleId)) {
						mapOfLastNodes.put(vehicleId, nodeNumber);
						taxisWithPassengers.put(vehicleId, new ArrayList<String>());
					} else {
						// current number of passengers on board
						numOfPassengers = taxisWithPassengers.get(vehicleId).size();
						while (listOfDistancesWithPassengers.size() <= numOfPassengers) {
							// initiate
							listOfDistancesWithPassengers.add(0.0);
						}
						prevDistance = listOfDistancesWithPassengers.get(numOfPassengers);
						tmpDistance = utils.computeDistance(mapOfLastNodes.get(vehicleId), nodeNumber);
						listOfDistancesWithPassengers.set(numOfPassengers, prevDistance + tmpDistance);
						// the price paid by each passenger on board for this
						// piece of trip
						tmpPrice = PRICE_PER_KM * tmpDistance / 1000 / numOfPassengers;
						// passengers' counters
						for (String passId : taxisWithPassengers.get(vehicleId)) {
							passengersAndDistTraveled.put(passId, passengersAndDistTraveled.get(passId) + tmpDistance);
							passengersAndPricePaid.put(passId, passengersAndPricePaid.get(passId) + tmpPrice);
						}
						mapOfLastNodes.put(vehicleId, nodeNumber);
					}

				} else if (strType.equals("PASSENGER_GOT_IN_TAXI")) {
					time = Long.parseLong(removeQuotation(arrSplitRow[0]));
					passengerId = removeQuotation(arrSplitRow[1]);
					vehicleId = removeQuotation(arrSplitRow[4]);
					// init counters
					passengersAndDistTraveled.put(passengerId, 0.0);
					passengersAndPricePaid.put(passengerId, 0.0);
					// remember the passenger on board
					List<String> passOnBoard = taxisWithPassengers.get(vehicleId);
					passOnBoard.add(passengerId);
					// TODO: this might be useless...
					taxisWithPassengers.put(vehicleId, passOnBoard);
					// count the waiting time
					tmpReq = findRequest(passengerId, reqs);
					if (tmpReq != null && tmpReq.getTimeWindow() != null) {
						long earliestDeparture = tmpReq.getTimeWindow().getEarliestDeparture();
						if (time > earliestDeparture) {
							reqsWaitingTime.put(tmpReq, time - earliestDeparture);
						}
					}

				} else if (strType.equals("PASSENGER_GOT_OFF_TAXI")) {
					time = Long.parseLong(removeQuotation(arrSplitRow[0]));
					passengerId = removeQuotation(arrSplitRow[1]);
					vehicleId = removeQuotation(arrSplitRow[4]);
					// remove the passenger from the board list
					List<String> passOnBoard = taxisWithPassengers.get(vehicleId);
					passOnBoard.remove(passengerId);
					// TODO: this might be useless...
					taxisWithPassengers.put(vehicleId, passOnBoard);
					// requests
					tmpReq = findRequest(passengerId, reqs);
					long delay = 0;
					if (tmpReq.getTimeWindow() != null) {
						delay = tmpReq.getTimeWindow().getArrivalDelay(time);
					}
					// if the delay is less than a minute, we'll ignore it
					if (utils.toMinutes(delay) == 0) {
						reqs.put(tmpReq, RequestState.SERVED_ON_TIME);
						Long waitTime = reqsWaitingTime.get(tmpReq);
						if (waitTime != null) {
							totalWaitingTime += waitTime;
						}
					} else {
						reqs.put(tmpReq, RequestState.SERVED_WITH_DELAY);
					}

				} else if (strType.equals("PASSENGER_SENT_REQUEST")) {
					passengerId = removeQuotation(arrSplitRow[1]);
					nodeFrom = Long.parseLong(removeQuotation(arrSplitRow[5]));
					nodeTo = Long.parseLong(removeQuotation(arrSplitRow[6]));
					timeWinOpenStr = removeQuotation(arrSplitRow[7]);
					timeWinCloseStr = removeQuotation(arrSplitRow[8]);
					// if there are no time windows
					if (timeWinOpenStr.length() == 0 || timeWinCloseStr.length() == 0) {
						reqs.put(new Request(passengerId, nodeFrom, nodeTo, new HashSet<String>()), RequestState.SENT);
					} else {
						timeWinOpen = Long.parseLong(timeWinOpenStr);
						timeWinClose = Long.parseLong(timeWinCloseStr);

						reqs.put(new Request(passengerId, nodeFrom, nodeTo, new TimeWindow(timeWinOpen, timeWinClose),
								new HashSet<String>()), RequestState.SENT);
					}
				}

				inputRow = reader.readLine();
			}
			reader.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// temporary variables

		Double totalTravel;
		Double totalPersonKilometers;

		String newLine = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();

		// count all statistics

		int simulTime = utils.toSeconds(taxiModel.getSimulationRuntime());

		totalTravel = 0.0;
		for (Double travel : listOfDistancesWithPassengers) {
			totalTravel += travel;
		}
		long totalDistTravel = Math.round(totalTravel);

		long distTravelEmptyVeh = Math.round(listOfDistancesWithPassengers.get(0));

		int distTravelEmptyVehPerc = (int) Math.round(100 * listOfDistancesWithPassengers.get(0) / totalTravel);

		int maxPassenOnBoard = listOfDistancesWithPassengers.size() - 1;

		totalPersonKilometers = 0.0;
		for (int i = 1; i < listOfDistancesWithPassengers.size(); i++) {
			totalPersonKilometers += i * listOfDistancesWithPassengers.get(i);
		}
		double avgVehOccupancy = totalPersonKilometers / totalTravel;

		int totalReqs = countNumOfReqs(reqs);

		int reqsOnTime = countNumOfReqs(reqs, RequestState.SERVED_ON_TIME);

		int reqsOnTimePerc = (int) Math.round((double) 100 * reqsOnTime / totalReqs);

		int reqsNotServed = totalReqs - reqsOnTime;

		int reqsNotServedPerc = 100 - reqsOnTimePerc;

		int avgWaitingTime = utils.toMinutes(Math.round((double) totalWaitingTime / reqsOnTime));

		double totalDirectTripLen = 0.0;
		double totalDetourLen = 0.0;
		double totalSaving = 0.0;
		double tmpDirectDistance;
		List<String> passenServedOnTime = getPassengersOnTime(reqs);
		for (String passId : passenServedOnTime) {
			tmpReq = findRequest(passId, reqs);
			tmpDirectDistance = utils.computeDistance(tmpReq.getFromNode(), tmpReq.getToNode());
			totalDirectTripLen += tmpDirectDistance;
			totalDetourLen += passengersAndDistTraveled.get(passId) - tmpDirectDistance;
			totalSaving += PRICE_PER_KM * tmpDirectDistance / 1000 - passengersAndPricePaid.get(passId);
		}
		// every passenger wanted to go this far on average
		long avgDirectTripLen = Math.round(totalDirectTripLen / passenServedOnTime.size());

		// every passenger's trip was this much longer than it was necessary
		long avgDetour = Math.round(totalDetourLen / passenServedOnTime.size());

		// (percentage compared to the total trip length)
		int avgDetourPerc = (int) Math.round(100 * totalDetourLen / totalDirectTripLen);

		// every passenger saved this amount on average
		int avgCostSaving = (int) Math.round(totalSaving / passenServedOnTime.size());

		// (percentage compared to the direct trip price)
		int avgCostSavingPerc = (int) Math.round((double) 100 * avgCostSaving / PRICE_PER_KM * avgDirectTripLen / 1000);

		// write statistics into the result file

		if (GlobalParams.isUseResultsFile()) {
			sb.append("Simulation time: ");
			sb.append(simulTime);
			sb.append(" s");
			sb.append(newLine);

			sb.append("Total distance traveled: ");
			sb.append(totalDistTravel);
			sb.append(" m");
			sb.append(newLine);

			sb.append("Distance traveled with empty vehicles: ");
			sb.append(distTravelEmptyVeh);
			sb.append(" m (");
			sb.append(distTravelEmptyVehPerc);
			sb.append("%)");
			sb.append(newLine);

			sb.append("Max passengers on board: ");
			sb.append(maxPassenOnBoard);
			sb.append(newLine);

			sb.append("Average vehicle occupancy: ");
			sb.append(avgVehOccupancy);
			sb.append(" passengers per km");
			sb.append(newLine);
			sb.append(newLine);

			sb.append("Total number of requests: ");
			sb.append(totalReqs);
			sb.append(newLine);

			sb.append("The number of requests served on time: ");
			sb.append(reqsOnTime);
			sb.append(" (");
			sb.append(reqsOnTimePerc);
			sb.append("%)");
			sb.append(newLine);

			sb.append("The number of requests unserved or served with delay: ");
			sb.append(reqsNotServed);
			sb.append(" (");
			sb.append(reqsNotServedPerc);
			sb.append("%)");
			sb.append(newLine);
			sb.append(newLine);

			sb.append("---All following statistics are counted per served passenger---");
			sb.append(newLine);
			sb.append(newLine);

			sb.append("Average waiting time: ");
			sb.append(avgWaitingTime);
			sb.append(" min");
			sb.append(newLine);

			sb.append("Average direct trip length: ");
			sb.append(avgDirectTripLen);
			sb.append(" m");
			sb.append(newLine);

			sb.append("Average detour length: ");
			sb.append(avgDetour);
			sb.append(" m (");
			sb.append(avgDetourPerc);
			sb.append("%)");
			sb.append(newLine);

			sb.append("Average cost saving: ");
			sb.append(avgCostSaving);
			sb.append(" (");
			sb.append(avgCostSavingPerc);
			sb.append("%)");
			sb.append(newLine);

			Writer writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(fileReport));
				writer.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			LOGGER.info("The result output has been written into the  file " + fileReport.getName());

		} else {
			// use standard output instead

			LOGGER.info("results: " + "| simul_time total_dist dist_empty_veh d_e_v_perc "
					+ "| max_pass_on_board avg_veh_occupancy total_reqs reqs_on_time "
					+ "| r_o_t_perc reqs_not_served r_n_s_perc avg_wait_time "
					+ "| avg_direct_trip avg_detour a_d_perc avg_cost_saving " + "| a_c_s_perc");
			LOGGER.info("| " + simulTime + " " + totalDistTravel + " " + distTravelEmptyVeh + " "
					+ distTravelEmptyVehPerc + " | " + maxPassenOnBoard + " " + avgVehOccupancy + " " + totalReqs + " "
					+ reqsOnTime + " | " + reqsOnTimePerc + " " + reqsNotServed + " " + reqsNotServedPerc + " "
					+ avgWaitingTime + " | " + avgDirectTripLen + " " + avgDetour + " " + avgDetourPerc + " "
					+ avgCostSaving + " | " + avgCostSavingPerc);
		}
	}

	public static StatisticsLogger getInstance() {
		return instance;
	}

	public static void setInstance(StatisticsLogger instance) {
		if (StatisticsLogger.instance == null) {
			StatisticsLogger.instance = instance;
		}
	}

	/**
	 * Return the total number of requests in the given state
	 * 
	 * @param requests
	 *            the map of requests
	 * @param state
	 *            the state of requests we're looking for
	 * @return the number of requests in the given state
	 */
	private int countNumOfReqs(Map<Request, RequestState> requests, RequestState state) {

		int numOfReqs = 0;
		for (Request req : requests.keySet()) {
			if (requests.get(req) == state) {
				numOfReqs++;
			}
		}
		return numOfReqs;
	}

	/**
	 * Return the total number of requests
	 * 
	 * @param requests
	 *            the map of requests
	 * @return the number of requests
	 */
	private int countNumOfReqs(Map<Request, RequestState> requests) {
		return requests.keySet().size();
	}

	/**
	 * Get the list of passengers, whose requests have been served on time
	 * 
	 * @param requests
	 *            the map of requests
	 * @return the list of ids of passengers
	 */
	private List<String> getPassengersOnTime(Map<Request, RequestState> requests) {

		List<String> passOnTime = new ArrayList<String>();

		for (Request req : requests.keySet()) {
			if (requests.get(req) == RequestState.SERVED_ON_TIME) {
				passOnTime.add(req.getPassengerId());
			}
		}
		return passOnTime;
	}

	/**
	 * Return the requests from the given map according to the id
	 * 
	 * @param passengerId
	 *            id of the passenger (to identify the request)
	 * @param requests
	 *            the map of requests
	 * @return the found request from the map, or null, if it hasn't been found
	 */
	private Request findRequest(String passengerId, Map<Request, RequestState> requests) {

		for (Request req : requests.keySet()) {
			if (req.getPassengerId().equals(passengerId)) {
				return req;
			}
		}
		return null;
	}

}