package oleksii.queriestask.datamodel;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Query {

    private long id;

    private String query;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Query query1 = (Query) o;
        return Objects.equals(query, query1.query);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(query);
    }
}
