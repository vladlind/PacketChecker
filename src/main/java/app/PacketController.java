package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.*;

@Controller
@RequestMapping("/devices")
public class PacketController {

    private final DeviceRepository deviceRepository;

    //Пул потоков для ручных (инициированных пользователем) проверок
    private final ExecutorService userPool = Executors.newFixedThreadPool(10);

    //Пул потоков для проверок по расписанию
    private final ExecutorService scheduledPool = Executors.newFixedThreadPool(3);

    // Спринг читает список "устройств" из application.properties для регулярных проверок
    @Value("${devices}")
    private final String[] devices;

    @Autowired
    public PacketController(DeviceRepository deviceRepository, String[] devices) {
        this.deviceRepository = deviceRepository;
        this.devices = devices;
    }

    /*Запуск ручной проверки - через http://localhost:8080/devices/{device_to_check}   */
    @GetMapping("/{device}")
    public ResponseEntity<String> checkPacketSizePerRequest(@PathVariable("device") String ipAddress) {
        measurePackets(ipAddress, userPool);
        return new ResponseEntity<>(
                String.format("Запрос на проверку %s отправлен", ipAddress),
                HttpStatus.OK);
    }

    /*    Запускаем задачу каждые 4 часа - проверяем в цикле полученный из application.properties
        список "устройств" в потоках из выделенного пула потоков, чтобы не занимать пользовательский пул*/

    @Scheduled(initialDelay = 10000, fixedRate = 4 * 60 * 60 * 1000)
    public void checkPacketSizeBulkScheduled() {
        for (String device : devices) {
            measurePackets(device, scheduledPool);
        }
    }

    private void measurePackets(String ipAddress, ExecutorService executorService) {
        Future<Device> future = executorService.submit(new PacketMeasurer(ipAddress));
        Device device;
        try {
            device = future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            future.cancel(true);
            // Если в течение 30 секунд не вернулся результат проверки -
            // создаем дефолтный объект "устройства" с maxPacketSize = 0
            device = new Device(ipAddress, "0");
        }
        // Сохраняем результат в inmemory БД
        deviceRepository.save(device);
    }
}
