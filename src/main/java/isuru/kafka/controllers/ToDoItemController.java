package isuru.kafka.controllers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.util.JsonUtils;
import isuru.kafka.entities.ToDoItem;
import isuru.kafka.services.ToDoItemService;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("api")
public class ToDoItemController {

    @Autowired
    private ToDoItemService toDoItemService;

    private AblyRest ablyRest;

    private void setAblyRest(@Value("${ABLY_API_KEY}") String apikey) throws AblyException {
        ablyRest = new AblyRest(apikey);
    }

    private final String CHANNEL_NAME = "default";

    @GetMapping
    public ResponseEntity<List<ToDoItem>> findAll() {
        List<ToDoItem> items = toDoItemService.findAll();
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ToDoItem> find(@PathVariable("id") Long id) {
        Optional<ToDoItem> item = toDoItemService.find(id);
        return ResponseEntity.of(item);
    }

    @PostMapping
    public ResponseEntity<ToDoItem> create(@CookieValue(value = "username") String username,
                                           @RequestBody Map<String, String> json) {
        if(json.get("text") == null) {
            return ResponseEntity.badRequest().body(null);
        }
        ToDoItem newItem = toDoItemService.create(json.get("text"), username);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newItem.getId())
                .toUri();

        JsonUtils.JsonUtilsObject object = io.ably.lib.util.JsonUtils.object();
        object.add("text", json.get("text"));
        object.add("username", username);
        object.add("id", newItem.getId().toString());

        publishToChannel("add", object.toJson());
        return ResponseEntity.created(location).body(newItem);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ToDoItem> completed(@PathVariable("id") Long id) {
        Optional<ToDoItem> updated = toDoItemService.updateCompletionStatus(id, true);

        return updated.map(value -> {
            publishToChannel("complete", Long.toString(id));
            return ResponseEntity.ok().body(value);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/uncomplete")
    public ResponseEntity<ToDoItem> uncompleted(@PathVariable("id") Long id) {
        Optional<ToDoItem> updated = toDoItemService.updateCompletionStatus(id, false);
        return updated
                .map(value -> {
                    publishToChannel("incomplete", Long.toString(id));
                    return ResponseEntity.ok().body(value);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ToDoItem> delete(
            @CookieValue(value = "username") String username,
            @PathVariable("id") Long id
    ) {
        Optional<ToDoItem> todoOptional = toDoItemService.find(id);
        if(todoOptional.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        if(todoOptional.get().getUsername().equals(username)) {
            toDoItemService.delete(id);
            publishToChannel("remove", Long.toString(id));
        }
        return ResponseEntity.noContent().build();
    }

    private boolean publishToChannel(String name, Object data) {
        try {
            ablyRest.channels.get(CHANNEL_NAME).publish(name, data);
        } catch (AblyException err) {
            System.out.println(err.errorInfo);
            return false;
        }
        return true;
    }

}
