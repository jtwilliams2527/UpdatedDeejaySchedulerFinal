import java.util.Arrays;
import java.util.List;
import java.util.*;

public class Booking {
    private String dj;
    private int month;
    private int day;
    private String time;
    private String customerName;
    private List<String> timeSlots;

    public Booking(String dj, int month, int day, String time, String customerName) {
        this.dj = dj;
        this.month = month;
        this.day = day;
        this.time = time;
        this.timeSlots = new ArrayList<>();
        this.timeSlots.add(time); // Add the initial time slot
        this.customerName = customerName;
    }

    public String getDj() {
        return dj;
    } //

    public void setDj(String dj) {
        this.dj = dj;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<Object> getDate(int month, int day){
        return Arrays.asList(month, day);
    }

    public String getDateStr(){
        return month + "/" + day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getTimeSlot() {
        return time;
    }

    @Override
    public String toString() {
        return "Bookings{" + "Customer Name='" + customerName +
                '\'' + ", DJ='" + dj + '\'' +
                ", Date= " + month +
                "/" + day +
                "/2023, Time='" + time + '\'' +
                '}';
    }
    public String toStringForFile() {
        return customerName + "," + dj + "," + month + "/" + day + "," + time;
    }
    public String getCustomerName() {
        return customerName;
    }
    public String getDate() {
        return month + "/" + day;
    }


    public int getIntTimeSlot() {
        return Integer.parseInt(time);
    }

    public boolean isOnSameDay(int month, int day) {
        return this.month == month && this.day == day;
    }

    public void addTimeSlot(String timeSlot) {
        this.timeSlots.add(timeSlot);
    }

    public List<String> getTimeSlots() {
        return timeSlots;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Booking booking = (Booking) obj;
        return customerName.equals(booking.customerName) && month == booking.month && day == booking.day && time.equals(booking.time);
    }
}
