package com.parkit.parkingsystem.service;

//import java.text.DecimalFormat;
import java.lang.Math;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }
        
        //int inHour = ticket.getInTime().getHours(); Correction prendre en compte les durées > 24h
        long inHour = ticket.getInTime().getTime();
        //int outHour = ticket.getOutTime().getHours(); Correction prendre en compte les durées > 24h
        long outHour = ticket.getOutTime().getTime();
        double duration = (double) (outHour - inHour) / 60.0 / 60.0 / 1000.0; // Convertir en heure
        
        // La durée de stationnement n'est pas retenue si <= 30 mn (gratuit)
        if(duration <= 0.5) 
          duration = 0.0;
        
        // Remise de 5% pour les utilisateurs récurrents
        if(discount == true) 
          duration *= 0.95;
          
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                duration = duration * Fare.CAR_RATE_PER_HOUR;
                // Arrondir 2 chiffres après la virgule
                //duration = (double) (((int)((duration + 0.005) * 100.0)) / 100.0);
                duration = ((int) Math.round(duration  * 100.0))/ 100.0;
                ticket.setPrice(duration);
                break;
            }
            case BIKE: {
                duration = duration * Fare.BIKE_RATE_PER_HOUR;
                duration = ((int) Math.round(duration  * 100.0))/ 100.0;
                ticket.setPrice(duration);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }

        
    }
    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}