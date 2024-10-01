package isuru.kafka.services;

import isuru.kafka.entities.ToDoItem;
import isuru.kafka.repositories.ToDoItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ToDoItemService {

    @Autowired
    private ToDoItemRepository toDoItemRepository;

    public List<ToDoItem> findAll() {
        return toDoItemRepository.findAll();
    }

    public Optional<ToDoItem> find(Long id) {
        return toDoItemRepository.findById(id);
    }

    public ToDoItem create(String text, String username) {
        ToDoItem copy = new ToDoItem(text, username);
        return toDoItemRepository.save(copy);
    }

    public Optional<ToDoItem> updateCompletionStatus(Long id, boolean status) {
        return toDoItemRepository.findById(id).map(oldItem -> {
            oldItem.setCompleted(status);
            return toDoItemRepository.save(oldItem);
        });
    }

    public void delete(Long id) {
        toDoItemRepository.deleteById(id);
    }
}
