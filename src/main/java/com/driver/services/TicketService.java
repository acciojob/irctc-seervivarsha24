package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        //check if station are valid or not;
        String []trainRoute = train.getRoute().split(",");
        int start=-1;
        int end=-1;
        for(int i = 0; i < trainRoute.length; i++){
            if(bookTicketEntryDto.getFromStation().toString().equals(trainRoute[i])){
                start=i;
                break;
            }
        }
        for(int i = 0; i < trainRoute.length; i++){
            if(bookTicketEntryDto.getToStation().toString().equals(trainRoute[i])){
                end = i;
                break;
            }
        }
        if(start==-1 || end ==-1 || end-start<0){
            throw new Exception("Invalid stations");
        }


        int bookedSeats = 0;
        List<Ticket> booked = train.getBookedTickets();
        for(Ticket ticket : booked){
            bookedSeats += ticket.getPassengersList().size();
        }
        if(train.getNoOfSeats()<bookTicketEntryDto.getNoOfSeats()+bookedSeats){
            throw new Exception("Less tickets are available");
        }

        //make ticket
        Ticket ticket = new Ticket();
        List<Passenger> passengerList = new ArrayList<>();
        for (Integer passengerId: bookTicketEntryDto.getPassengerIds()){
            passengerList.add(passengerRepository.findById(passengerId).get());
        }
        ticket.setPassengersList(passengerList);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        ticket.setTotalFare((end-start)*300*bookTicketEntryDto.getNoOfSeats());

        train.getBookedTickets().add(ticket);
        //# set no of set in train#;

        //Save ticket in passenger who booked the ticket;
        Passenger bookingPerson = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        bookingPerson.getBookedTickets().add(ticket);

        trainRepository.save(train);

        return ticketRepository.save(ticket).getTicketId();

    }
}
