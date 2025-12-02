package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import org.springframework.web.bind.annotation.*;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @GetMapping()
    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    @GetMapping("/translation")
    public Map<String, String> getAllCategories() {
        return Arrays.stream(Category.values())
                .collect(Collectors.toMap(Category::name, Category::getGermanName));
    }
}