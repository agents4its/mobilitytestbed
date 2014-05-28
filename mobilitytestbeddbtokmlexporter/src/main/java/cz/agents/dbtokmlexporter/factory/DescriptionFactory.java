package cz.agents.dbtokmlexporter.factory;

import java.sql.ResultSet;

/**
 *
 *@author Marek Cuchy
 *
 */
public interface DescriptionFactory {

	public String createDescription(ResultSet resultSet);

}
