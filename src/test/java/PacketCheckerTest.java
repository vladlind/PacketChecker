import app.Device;
import app.DeviceRepository;
import app.PacketCheckerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = PacketCheckerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PacketCheckerTest {
    Logger log  = Logger.getLogger(PacketCheckerTest.class.getName());

    private final TestRestTemplate testRestTemplate;

    private final DeviceRepository deviceRepository;

    @Autowired
    public PacketCheckerTest(TestRestTemplate testRestTemplate, DeviceRepository deviceRepository) {
        this.testRestTemplate = testRestTemplate;
        this.deviceRepository = deviceRepository;
    }

    @Test
    public void testSingleManualPacketCheck() {
        ResponseEntity<String> response =
                testRestTemplate.getForEntity("/packetchecker/127.0.0.1", String.class);
        log.info(response.getBody());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("127.0.0.1"));
        List<Device> devices = deviceRepository.findDeviceByIpAddress("127.0.0.1");
        log.info(devices.get(0).toString());
        assertEquals("1600", devices.get(0).getMaxPacketSize());
    }

    @Test
    public void testTwoSimultaneousManualPacketChecksWithDifferentDevices()  {
        ResponseEntity<String> response =
                testRestTemplate.getForEntity("/packetchecker/8.8.8.8,1.2.3.4", String.class);
        log.info(response.getBody());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("8.8.8.8,1.2.3.4"));
        List<Device> devices1  = deviceRepository.findDeviceByIpAddress("8.8.8.8");
        List<Device> devices2  = deviceRepository.findDeviceByIpAddress("1.2.3.4");
        Device device1 = devices1.get(0);
        Device device2 = devices2.get(0);
        log.info(device1.toString());
        log.info(device2.toString());
        assertEquals("1472", device1.getMaxPacketSize());
        assertEquals("0", device2.getMaxPacketSize());
        assertEquals(device1.getStartTime(), device2.getStartTime());
    }

    @Test
    public void testTwoSimultaneousManualPacketChecksWithTheSameDevice()  {
        ResponseEntity<String> response =
                testRestTemplate.getForEntity("/packetchecker/1.1.1.1,1.1.1.1", String.class);
        log.info(response.getBody());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("1.1.1.1,1.1.1.1"));
        List<Device> devices  = deviceRepository.findDeviceByIpAddress("1.1.1.1");
        Device device1 = devices.get(0);
        Device device2 = devices.get(1);
        log.info(device1.toString());
        log.info(device2.toString());
        assertEquals("1472", device1.getMaxPacketSize());
        assertEquals("1472", device2.getMaxPacketSize());
        assertEquals(device1.getStartTime(), device2.getStartTime());
        assertEquals(device1.getEndTime(), device2.getEndTime());
    }
}
