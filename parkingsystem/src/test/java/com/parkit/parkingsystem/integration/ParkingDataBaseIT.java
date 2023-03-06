package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.constants.Fare;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        assertNotNull(ticketDAO.getTicket("ABCDEF"));        
        assertNotEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));        
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database

        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        ticket.setOutTime(null);
        ticket.setPrice(0);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setId(1);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticketDAO.saveTicket(ticket);
        parkingSpotDAO.updateParking(ticket.getParkingSpot());

        parkingService.processExitingVehicle();
        
        assertNotEquals(null, ticketDAO.getTicket("ABCDEF").getOutTime());
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticketDAO.getTicket("ABCDEF").getPrice());
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        // Premier passage
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000)); // 1 heure
        ticket.setOutTime(null);
        ticket.setPrice(0);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setId(1);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticketDAO.saveTicket(ticket);
        parkingSpotDAO.updateParking(ticket.getParkingSpot());
        parkingService.processExitingVehicle();

        // Second passage pour ce même véhicule
        ParkingService parkingService2 = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService2.processIncomingVehicle();
        Ticket ticket2 = new Ticket();
        ticket2.setInTime(new Date(System.currentTimeMillis() - 600 * 60 * 1000)); // 10 heures
        ticket2.setOutTime(null);
        ticket2.setPrice(0);
        ticket2.setVehicleRegNumber("ABCDEF");
        ticket2.setId(2);
        ticket2.setParkingSpot(new ParkingSpot(2, ParkingType.CAR, false));
        ticketDAO.saveTicket(ticket2);
        parkingSpotDAO.updateParking(ticket2.getParkingSpot());
        parkingService2.processExitingVehicle();

        assertNotEquals(null, ticketDAO.getTicket("ABCDEF").getOutTime());
        assertEquals(10.0 * 0.95 * Fare.CAR_RATE_PER_HOUR , ticketDAO.getTicket("ABCDEF").getPrice());
    }
}
