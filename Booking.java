import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking {
    private String dj;
    private LocalDateTime dateTime;
    private String customerName;
    private int eventDuration;
  


    // Constructor
    public Booking(String dj, LocalDateTime dateTime, String customerName, int eventDuration) {
        this.dj = dj;
        this.dateTime = dateTime;
        this.customerName = customerName;
        this.eventDuration = eventDuration;
       
    }
  
    public int getEventDuration() {
        return eventDuration;
    }

    // Getter methods
    public String getDj() {
        return dj;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String toStringForFile() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        return customerName + "," + dj + "," + dateTime.format(formatter) + "," + eventDuration + "hrs";
    }
}
