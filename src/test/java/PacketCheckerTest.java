import app.Device;
import app.DeviceRepository;
import app.PacketCheckerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

@SpringBootTest(classes = PacketCheckerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PacketCheckerTest {
    Logger log  = Logger.getLogger(PacketCheckerTest.class.getName());

    @LocalServerPort
    private Integer port;

    private final TestRestTemplate testRestTemplate;

    private final DeviceRepository deviceRepository;

    @Autowired
    public PacketCheckerTest(TestRestTemplate testRestTemplate, DeviceRepository deviceRepository) {
        this.testRestTemplate = testRestTemplate;
        this.deviceRepository = deviceRepository;
    }

    @Test
    public void testSingleManualPacketCheck() throws InterruptedException {
        ResponseEntity<String> response =
                testRestTemplate.getForEntity("/devices/4.3.2.1", String.class);
        log.info(response.getBody());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("4.3.2.1"));
        Thread.sleep(5000);
        List<Device> devices = deviceRepository.findDeviceByIpAddress("4.3.2.1");
        log.info(devices.get(0).toString());
        assertEquals("0", devices.get(0).getMaxPacketSize());
    }

    @Test
    public void testTwoSimultaneousManualPacketChecksWithTheSameDevice() throws InterruptedException {
        ResponseEntity<String> response1 =
                testRestTemplate.getForEntity("/devices/8.8.8.8", String.class);
        ResponseEntity<String> response2 =
                testRestTemplate.getForEntity("/devices/8.8.8.8", String.class);
        log.info(response1.getBody());
        log.info(response2.getBody());
        assertTrue(Objects.requireNonNull(response1.getBody()).contains("8.8.8.8"));
        assertTrue(Objects.requireNonNull(response2.getBody()).contains("8.8.8.8"));
        Thread.sleep(5000);
        List<Device> devices  = deviceRepository.findDeviceByIpAddress("8.8.8.8");
        log.info(devices.get(0).toString());
        log.info(devices.get(1).toString());
        assertEquals("1472", devices.get(0).getMaxPacketSize());
        assertEquals("1472", devices.get(0).getMaxPacketSize());
    }
}
