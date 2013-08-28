package cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.processor;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import cz.agents.agentpolis.darptestbed.siminfrastructure.logger.analyser.init.TestbedProcessor;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.Graph;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.GraphType;
import cz.agents.agentpolis.simmodel.environment.model.citymodel.transportnetwork.elemets.Edge;
import eu.superhub.wp4.analyser.processor.AVehiclePathProcessor;
import eu.superhub.wp4.analyser.processor.FromToInPeriod;
import eu.superhub.wp4.analyser.structure.VehicleAtomicPath;
import eu.superhub.wp4.analyser.structure.VehiclePath;
import eu.superhub.wp4.report.TimeInterval;
import eu.superhub.wp4.report.builder.ReportBuilder;

public class TotalVehicleDistanceProcessor extends AVehiclePathProcessor<FromToInPeriod> implements TestbedProcessor {

	private double vehicleDistanceInMeter = 0.0;

	public TotalVehicleDistanceProcessor(Map<GraphType, Graph> transportNetworksByGraphType, long measureTimePeriodInms) {
		super(transportNetworksByGraphType, measureTimePeriodInms);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void process(FromToInPeriod fromToInPeriod, VehicleAtomicPath vehicleAtomicPath, VehiclePath vehiclePath,
			Edge edge) {

		vehicleDistanceInMeter += edge.getLenght();
	}

	public double getVehicleDistanceInMeter() {
		return vehicleDistanceInMeter;
	}

	@Override
	protected void setReportBuilder(FromToInPeriod fromToInPeriod, TimeInterval timeInterval,
			ReportBuilder reportBuilder) {

		throw new NotImplementedException();

	}

	@Override
	protected Set<FromToInPeriod> createSetOfAllFromToInPeriodKeys() {

		throw new NotImplementedException();

	}

	@Override
	public String provideResult() {
		return String.format("Total vehicle distance driven (in kilometers):%s", vehicleDistanceInMeter / 1000);
	}

}
