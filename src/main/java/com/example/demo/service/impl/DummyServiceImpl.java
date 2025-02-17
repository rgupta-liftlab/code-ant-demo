package com.example.demo.service.impl;

import com.example.demo.service.DummyService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DummyServiceImpl implements DummyService {

  @Override
  public List<String> getNamesData() {
    return List.of("Rajat", "Shivam", "Manoj");
  }
}
