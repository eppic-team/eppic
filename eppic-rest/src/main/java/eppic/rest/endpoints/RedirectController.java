package eppic.rest.endpoints;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class RedirectController {

    @GetMapping("/")
    public ModelAndView redirect(ModelMap model) {
        return new ModelAndView("redirect:/swagger-ui/index.html", model);
    }

}