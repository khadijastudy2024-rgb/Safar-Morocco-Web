package ma.safar.morocco.web.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class WebController {

    private static final String ATTR_DESTINATIONS = "destinations";

    private final DestinationRepository destinationRepository;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute(ATTR_DESTINATIONS, destinationRepository.findAll());
        return "index";
    }

    @GetMapping("/destinations")
    public String destinations(Model model) {
        model.addAttribute(ATTR_DESTINATIONS, destinationRepository.findAll());
        return ATTR_DESTINATIONS;
    }

    @GetMapping("/destinations/{id}")
    public String destinationDetail(@PathVariable("id") Long id, Model model) {
        Destination dest = destinationRepository.findById(id).orElse(null);
        model.addAttribute("destination", dest);
        return "destination";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
}
