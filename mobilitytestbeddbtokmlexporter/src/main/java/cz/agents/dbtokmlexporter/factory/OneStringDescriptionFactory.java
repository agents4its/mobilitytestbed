package cz.agents.dbtokmlexporter.factory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 *@author Marek Cuchy
 *
 */
public class OneStringDescriptionFactory implements DescriptionFactory{
	
	private final String columnName;
	
	public OneStringDescriptionFactory(String columnName) {
		super();
		this.columnName = columnName;
	}

	@Override
	public String createDescription(ResultSet result) {
		try {
			return result.getString(columnName);
		} catch (SQLException e) {
			return null;
		}
	}
}
