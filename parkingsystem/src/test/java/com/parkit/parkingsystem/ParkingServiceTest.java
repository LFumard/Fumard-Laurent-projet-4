package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import junit.framework.Assert;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock //
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock //
    private static TicketDAO ticketDAO;

    @Test
    private void processExitingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            when(ticketDAO.getNbTicket(anyString())).thenReturn(1); // Simule 1 précédent passage
            parkingService.processExitingVehicle();
            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle (){
      try {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

          //ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            
          /*when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
          when(inputReaderUtil.readSelection()).thenReturn(1);
          when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
          when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
          when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
          when(ticketDAO.getTicket(anyString())).thenReturn(any(Ticket.class));
          when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1); //    getId(any(ParkingSpot.class))).thenReturn(1);
          when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(false);
          parkingService.processIncomingVehicle();
          verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));*/
        } catch (Exception e) {
           e.printStackTrace();
           throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
    
    @Test
    public void processExitingVehicleTestUnableUpdate () {
      when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(IllegalArgumentException.class);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processExitingVehicle();

        verify(ticketDAO, Mockito.times(0)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(0)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailable() {
      try {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpotAvailable = new ParkingSpot(1, ParkingType.CAR, true);
        
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        
        verify(inputReaderUtil).readSelection();
        verify(parkingSpotDAO).getNextAvailableSlot(any(ParkingType.class));
        System.out.println("ICI parkingSpotAvailable: " + parkingSpotAvailable.getId());
        System.out.println("ICI parkingSpot: " + parkingSpot.getId());
        
        Assert.assertEquals(parkingSpotAvailable.getId(), parkingSpot.getId());
        
        } catch (Exception e) {
           e.printStackTrace();
           throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //ParkingSpot parkingSpotAvailable = new ParkingSpot(1, ParkingType.CAR, true);
        
        //ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        //assertThrows(IllegalAccessException.class, () -> parkingService.getNextParkingNumberIfAvailable());
        // assertThrows(MyException.class, myStackObject::doStackAction, "custom message if assertion fails..."); 
        assertNull(parkingService.getNextParkingNumberIfAvailable());
        //Assert.assertEquals(-1,parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        try {
          when(inputReaderUtil.readSelection()).thenReturn(3);
          parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

          ParkingSpot actualParkingSpot = parkingService.getNextParkingNumberIfAvailable();

          //assertThrows(IllegalArgumentException.class, () -> parkingService.getVehichleType());
          verify(inputReaderUtil).readSelection();
          assertNull(actualParkingSpot);

        } catch (Exception e) {
           e.printStackTrace();
           throw  new RuntimeException("Failed to set up test mock objects");
        }
    }   
}