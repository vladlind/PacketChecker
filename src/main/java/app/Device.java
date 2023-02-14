package app;

import jakarta.persistence.*;

import java.util.Date;


// Репрезентация результата проверки
@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String ipAddress;
    private String maxPacketSize;

    @Temporal(TemporalType.TIME)
    private java.util.Date startTime;

    @Temporal(TemporalType.TIME)
    private java.util.Date endTime;

    public Device() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device(String ipAddress, String maxPacketSize, Date startTime, Date endTime) {
        this.ipAddress = ipAddress;
        this.maxPacketSize = maxPacketSize;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(String maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "DeviceDTO{" +
                "ipAddress='" + ipAddress + '\'' +
                ", maxPacketSize=" + maxPacketSize +
                '}';
    }
}
