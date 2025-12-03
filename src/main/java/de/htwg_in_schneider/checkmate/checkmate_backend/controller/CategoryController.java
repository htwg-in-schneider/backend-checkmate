package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.Category;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @GetMapping
    public Category[] getCategories() {
        return Category.values();
    }
}