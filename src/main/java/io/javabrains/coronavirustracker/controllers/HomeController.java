package io.javabrains.coronavirustracker.controllers;

import io.javabrains.coronavirustracker.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService coronaVirusDataService;

    @GetMapping("/")
    public String home(Model m){
        m.addAttribute("locationStats", coronaVirusDataService.getAllStats() );
        m.addAttribute("totalCases", coronaVirusDataService.getFinalCases());
        m.addAttribute("totalDeaths", coronaVirusDataService.getFinalDeaths());
        return "home";
    }
}
