import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Request {
    private String customerName;
    private LocalDateTime dateTime;
    private int eventDuration;


    // Constructor
    public Request(String customerName, LocalDateTime dateTime, int eventDuration) {
        this.customerName = customerName;
        this.dateTime = dateTime;
        this.eventDuration = eventDuration;
    }

    // Getter methods
    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public int getEventDuration() {
        return eventDuration;
    }

    public String toStringForFile() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        return customerName + "," + dateTime.format(formatter);
    }
}
