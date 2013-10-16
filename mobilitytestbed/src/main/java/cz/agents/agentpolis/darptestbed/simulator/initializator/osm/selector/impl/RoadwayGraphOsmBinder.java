package cz.agents.agentpolis.darptestbed.simulator.initializator.osm.selector.impl;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.osm.data.Selector;

import cz.agents.agentpolis.simulator.importer.osm.selector.WaySelector;
import cz.agents.agentpolis.simulator.importer.osm.tag.AtLeastOneIncludedTagSatisfiedEvaluator;
import cz.agents.agentpolis.simulator.importer.osm.tag.TagConditionsEvaluator;

public class RoadwayGraphOsmBinder {

	private RoadwayGraphOsmBinder() {
	}

	private static Selector selector;

	static {

		Set<String> include = new HashSet<String>();
		Set<String> exclude = new HashSet<String>();

		include.add(TagConditionsEvaluator.createConditionTag("highway", "*"));
		include.add(TagConditionsEvaluator.createConditionTag("cycleway", "shared_lane"));

		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "pedestrian"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "path"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "cycleway"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "footway"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "bridleway"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "steps"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "platform"));

		// highway living_street - Consider excluding or speed reduction
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "escape"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "service"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "track"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "bus_guideway"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "raceway"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "construction"));
		exclude.add(TagConditionsEvaluator.createConditionTag("highway", "proposed"));

		AtLeastOneIncludedTagSatisfiedEvaluator evaluator = new AtLeastOneIncludedTagSatisfiedEvaluator(include,
				exclude);

		selector = new WaySelector(evaluator);

	}

	public static Selector getSelector() {
		return selector;

	}

}