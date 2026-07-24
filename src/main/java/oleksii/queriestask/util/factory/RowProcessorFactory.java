package oleksii.queriestask.util.factory;

import com.fasterxml.jackson.core.JsonGenerator;
import oleksii.queriestask.util.RowProcessor;
import org.springframework.stereotype.Component;

@Component
public class RowProcessorFactory {

    public RowProcessor create(JsonGenerator generator) {
        return new RowProcessor(generator);
    }

}
