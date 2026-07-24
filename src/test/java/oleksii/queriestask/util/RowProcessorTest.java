package oleksii.queriestask.util;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RowProcessorTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData metaData;

    @InjectMocks
    private RowProcessor rowProcessor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;

    @Test
    public void testProcessRowSuccess() throws SQLException, IOException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);

        // Mock Column 1
        when(metaData.getColumnLabel(1)).thenReturn("user_id");
        when(resultSet.getObject(1)).thenReturn(42);

        // Mock Column 2
        when(metaData.getColumnLabel(2)).thenReturn("username");
        when(resultSet.getObject(2)).thenReturn("john_doe");

        rowProcessor.processRow(resultSet);

        verify(jsonGenerator, times(1)).writeObject(mapCaptor.capture());

        Map<String, Object> capturedRow = mapCaptor.getValue();

        assertNotNull(capturedRow);
        assertEquals(2, capturedRow.size());
        assertEquals(42, capturedRow.get("user_id"));
        assertEquals("john_doe", capturedRow.get("username"));

        // Verify it maintains order (LinkedHashMap requirement)
        Object[] keys = capturedRow.keySet().toArray();
        assertEquals("user_id", keys[0]);
        assertEquals("username", keys[1]);
    }

    @Test
    public void testProcessRowThrowsExceptionResultSetFails() throws Exception {
        when(resultSet.getMetaData()).thenThrow(new SQLException("Connection closed"));

        SQLException thrown = assertThrows(SQLException.class, () -> {
            rowProcessor.processRow(resultSet);
        });

        assertEquals("Failed to serialize row to JSON", thrown.getMessage());
        assertInstanceOf(SQLException.class, thrown.getCause());
        assertEquals("Connection closed", thrown.getCause().getMessage());

        verifyNoInteractions(jsonGenerator);
    }

    @Test
    public void testProcessRowThrowsExceptionWhenJsonGeneratorFails() throws Exception {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("user_id");
        when(resultSet.getObject(1)).thenReturn(42);

        // Force Jackson to throw an IOException when writing
        doThrow(new IOException("Stream closed")).when(jsonGenerator).writeObject(any());

        SQLException thrown = assertThrows(SQLException.class, () -> {
            rowProcessor.processRow(resultSet);
        });

        assertEquals("Failed to serialize row to JSON", thrown.getMessage());
        assertInstanceOf(IOException.class, thrown.getCause());
    }

}
