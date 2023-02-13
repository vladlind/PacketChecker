package app;

import jakarta.persistence.*;

// Репрезентация результата проверки
@Entity
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String ipAddress;
    private String maxPacketSize;

    public Device() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device(String ipAddress, String maxPacketSize) {
        this.ipAddress = ipAddress;
        this.maxPacketSize = maxPacketSize;
    }

    public Device(String ipAddress) {
        this.ipAddress = ipAddress;
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

    @Override
    public String toString() {
        return "DeviceDTO{" +
                "ipAddress='" + ipAddress + '\'' +
                ", maxPacketSize=" + maxPacketSize +
                '}';
    }
}
