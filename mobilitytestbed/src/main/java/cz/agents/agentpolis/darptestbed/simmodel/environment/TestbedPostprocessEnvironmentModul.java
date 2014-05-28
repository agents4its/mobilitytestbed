package cz.agents.agentpolis.darptestbed.simmodel.environment;

import com.google.inject.AbstractModule;

/**
 * This is an empty modul, that's necessary to be returned
 * from an InitModuleFactory.
 * 
 * If you want, you can add here some additional features of the environment
 * (after the first module is injected).
 * 
 * @author Lukas Canda
 */
public class TestbedPostprocessEnvironmentModul extends AbstractModule {

	@Override
	protected void configure() {
	}

}
