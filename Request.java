public class Request {
    private String customerName;
    private int month;
    private int day;
    private String time;

    public Request(String customerName, int month, int day, String time) {
        this.customerName = customerName;
        this.month = month;
        this.day = day;
        this.time = time;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
        return "Request{" +
                "customerName='" + customerName + '\'' +
                ", month=" + month +
                ", day=" + day +
                ", time='" + time + '\'' +
                '}';
    }

    public String toStringForFile() {
        return customerName + "," + month + "/" + day + "," + time;
    }
}
