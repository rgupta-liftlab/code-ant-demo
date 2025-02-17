package com.example.demo.controller;

import com.example.demo.service.DummyService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DummyController {

  @Autowired
  DummyService dummyService;

  private final Map<Integer, String> data = new HashMap<>();

  @GetMapping("/items")
  public Map<Integer, String> getAllItems() {
    return data;
  }

  @PostMapping("/items")
  public String addItem(@RequestParam int id, @RequestParam String name) {
    data.put(id, name);
    return "Item added successfully";
  }

}
