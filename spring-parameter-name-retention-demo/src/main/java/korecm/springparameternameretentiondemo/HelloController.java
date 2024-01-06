package korecm.springparameternameretentiondemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello1")
    public String hello1(@RequestParam String name) {
        return "Hello, " + name;
    }

    @GetMapping("/hello2")
    public String hello2(@RequestParam("name") String name) {
        return "Hello, " + name;
    }
}
