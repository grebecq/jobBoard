package fedoseev.jobboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
            "/{path:^(?!api$)[^\\.]*}",
            "/{first:^(?!api$)[^\\.]*}/{second:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
