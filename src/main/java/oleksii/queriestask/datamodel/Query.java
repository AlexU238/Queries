package oleksii.queriestask.datamodel;

import lombok.*;
import org.springframework.stereotype.Component;

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
}
