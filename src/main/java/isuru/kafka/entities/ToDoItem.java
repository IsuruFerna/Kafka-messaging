package isuru.kafka.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ToDoItem {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String text;

    @NonNull
    private String username;

    private Boolean completed = false;
}
