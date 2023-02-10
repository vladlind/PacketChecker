package app;



public class DeviceDTO {

    private String ipAddress;
    private Integer maxPacketSize;

    public DeviceDTO() {
    }

    public DeviceDTO(String ipAddress) {
        this.ipAddress = ipAddress;
        maxPacketSize = 0;
    }

    public Integer getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(Integer maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }
}
