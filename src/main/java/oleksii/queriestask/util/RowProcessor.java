package oleksii.queriestask.util;

import com.fasterxml.jackson.core.JsonGenerator;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RowProcessor implements RowCallbackHandler {

    private final JsonGenerator jsonGenerator;

    public RowProcessor(JsonGenerator jsonGenerator) {
        this.jsonGenerator = jsonGenerator;
    }

    public void processRow(ResultSet rs) throws SQLException {
        try {
            Map<String, Object> row = new LinkedHashMap<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }

            // Generates the object and appends commas automatically since it's inside an active array
            jsonGenerator.writeObject(row);
        } catch (Exception e) {
            throw new SQLException("Failed to serialize row to JSON", e);
        }
    }

}
