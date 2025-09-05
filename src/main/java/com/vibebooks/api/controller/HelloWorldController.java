package com.vibebooks.api.controller;


import com.vibebooks.api.domain.User;
import com.vibebooks.api.service.HelloWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello-world")
public class HelloWorldController {

    @Autowired
    private HelloWorldService helloWorldService;

    @GetMapping
    public String helloWorld() {
        return helloWorldService.helloWorld("Diego");
    }

    @PostMapping("/{id}")
    public String HelloWorldPost(@PathVariable("id") String id, @RequestParam(value ="filter", defaultValue = "nenhum") String filter, @RequestBody User body) {
        return "Hello World Post! " + body.getName() + " " + filter;
    }
}
