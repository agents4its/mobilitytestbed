package cz.agents.dbtokmlexporter.factory;

import cz.agents.resultsvisio.kml.util.TimeKmlFormater;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

                Object resultObject = result.getObject(name);

                if (resultObject instanceof Timestamp) {
                    resultObject = TimeKmlFormater.getTimeForKML(((Timestamp) resultObject).getTime());
                }

                sb.append(name +": " + resultObject +" <br/>");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
