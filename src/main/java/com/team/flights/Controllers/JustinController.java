package com.team.flights.Controllers;

import com.team.flights.Beans.Flight;
import com.team.flights.Beans.Trip;
import com.team.flights.Beans.User;
import com.team.flights.CustomUserDetails;
import com.team.flights.Helper.TravelHelper;
import com.team.flights.Repositories.*;
import com.team.flights.SSUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;

import static com.team.flights.Controllers.JosephController.setNameData;

@Controller
public class JustinController {

    @Autowired
    SSUserDetailsService userDetailsService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FlightRepository flightRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    RoleRepository roleRepository;

    //------------------------------------------------------------------------------------------------------------------
    @PostMapping("/processpayment")
    public String processPaymentForm(@RequestParam(value = "name",required=false) String name,
                                     @RequestParam(value = "creditcardnumber",required=false) String creditcardnumber,
                                     @RequestParam(value = "cvv",required=false) String cvv,
                                     @RequestParam(value = "expirationdate",required=false) String expirationdate,
                                     @RequestParam(name = "id",required=false) long id,
                                     Model model, Principal principal){
        User user = ((CustomUserDetails)((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser();
        Trip trip = tripRepository.findById(id);
        model.addAttribute("toLoc",flightRepository.findById(trip.getFlightToId()).getToLocation());
        model.addAttribute("fromLoc",flightRepository.findById(trip.getFlightFromId()).getFromLocation());
        String data = name+", "+creditcardnumber+", "+cvv+", "+expirationdate;
        model.addAttribute("trips", trip);
        trip.setCreditCard(data);
        model.addAttribute("users",user);
        model.addAttribute("id", trip.getId());
        tripRepository.save(trip);
        return "finalize";
    }
    //------------------------------------------------------------------------------------------------------------------
    @RequestMapping("/flight")
    public String loadFlightPage(Model model) {
        model.addAttribute("flights",flightRepository.findAll());
        Trip trip = new Trip();
        tripRepository.save(trip);
        model.addAttribute("tripId",trip.getId());
        model.addAttribute("on","departure");
        return "flight";
    }

    @PostMapping("/flightprocess")
    public String processFlight(@RequestParam(name = "destFrom", required=false) String destFrom,
                                @RequestParam(name = "destTo", required=false) String destTo,
                                @RequestParam(name = "toDate", required=false) String toDate,
                                @RequestParam(name = "id",required=false) long id,
                                @RequestParam(name = "tripId",required=false) long tripId,
                                @RequestParam(name = "on",required=false) String on, Model model, Principal principal) {
        Trip trip = tripRepository.findById(tripId);

        if (on.equals("Departure")) {
            trip.setFlightFromId(id);
            tripRepository.save(trip);

            ArrayList<Flight> flights = new ArrayList<>();
            for (Flight flight : flightRepository.findAll()) {
                if (flight.getAvailableSeats() >= trip.getPassengers() &&
                        TravelHelper.isAvailable(trip.getType(), flight.getFlightClass()) &&
                        flight.getDate().contains(toDate) &&
                        flight.getFromLocation().contains(destTo) &&
                        flight.getToLocation().contains(destFrom)) {
                    flights.add(flight);
                }
            }

            model.addAttribute("flights", flights);
            model.addAttribute("tripId", trip.getId());
            model.addAttribute("on", "Return");
            return "flight";
        } else {
            trip.setFlightToId(id);
            tripRepository.save(trip);
            model.addAttribute("id", trip.getId());

            return setNameData(model, trip, tripRepository);
        }
    }
    //------------------------------------------------------------------------------------------------------------------
    @RequestMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("flight",new Flight());
        return "admin";
    }

    @PostMapping("/adminprocess")
    public String adminProcessPage(@Valid Flight flight, BindingResult result) {
        if(result.hasErrors())
        {
            return "admin";
        }
        flightRepository.save(flight);
        return "redirect:/";
    }
    //------------------------------------------------------------------------------------------------------------------
}
