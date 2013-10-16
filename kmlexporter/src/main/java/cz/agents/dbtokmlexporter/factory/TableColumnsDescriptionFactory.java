package cz.agents.dbtokmlexporter.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *@author Marek Cuchy
 *
 */
public class TableColumnsDescriptionFactory implements DescriptionFactory {
	
	private final List<String> columnNames;
	
	public TableColumnsDescriptionFactory(List<String> columnNames) {
		super();
		this.columnNames = columnNames;
	}
	public TableColumnsDescriptionFactory(String... columnNames) {
		super();
		this.columnNames = new ArrayList<>(columnNames.length);
		for (String string : columnNames) {
			this.columnNames.add(string);
		}
	}

	@Override
	public String createDescription(ResultSet result) {
		StringBuilder sb = new StringBuilder();
		
		for (String name : columnNames) {
			try {
				sb.append(name +": " + result.getObject(name) +" <br/>");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
