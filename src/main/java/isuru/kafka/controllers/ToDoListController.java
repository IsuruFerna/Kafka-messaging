package isuru.kafka.controllers;

import isuru.kafka.entities.ToDoItem;
import isuru.kafka.services.ToDoItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ToDoListController {

    @Autowired
    private ToDoItemService toDoItemService;

    @GetMapping("/")
    public String toDoPage(Model model) {
        List<ToDoItem> listItems = toDoItemService.findAll();
        if(listItems.equals(null)) {
            listItems = new ArrayList<>();
        }
        model.addAttribute("todoitems", listItems);

        return "todolist/index";
    }
}
