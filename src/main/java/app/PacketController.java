package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Controller
@RequestMapping("/packetchecker")
public class PacketController {

    Logger log  = Logger.getLogger(PacketController.class.getName());

    private final DeviceRepository deviceRepository;

    //Пул потоков для ручных (инициированных пользователем) проверок
    private final ExecutorService userPool = Executors.newFixedThreadPool(3);

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

    /*Запуск ручной проверки - через http://localhost:8080/packetchecker/{devices_to_check_comma_separated}
    * Пример: http://localhost:8080/packetchecker/1.1.1.1,2.2.2.2,4.3.2.1   */
    @GetMapping("/{devices}")
    public ResponseEntity<String> checkPacketSizePerRequest(@PathVariable("devices") String ipAddresses) {
        String[] ipArray = Arrays.stream(ipAddresses.split(",")).toArray(String[] ::new);
        measurePackets(ipArray, userPool);
        return new ResponseEntity<>(
                String.format("Запрос на проверку %s отправлен", ipAddresses),
                HttpStatus.OK);
    }

    /*    Запускаем задачу каждые 4 часа - проверяем полученный из application.properties
        список "устройств" в потоках из выделенного пула потоков, чтобы не занимать пользовательский пул*/
    @Scheduled(initialDelay = 10000, fixedRate = 4 * 60 * 60 * 1000)
    public void checkPacketSizeBulkScheduled() {
        measurePackets(devices, scheduledPool);
    }

    private void measurePackets(String[] devices, ExecutorService executorService) {
        List<Future<Device>> listFutureDevice = new ArrayList<>();
        for (String device : devices) {
            listFutureDevice.add(executorService.submit(new PacketMeasurer(device)));
        }
        for (Future<Device> future : listFutureDevice) {
            Device deviceResult;
            try {
                deviceResult = future.get(30, TimeUnit.SECONDS);
                // Сохраняем результат в inmemory БД
                deviceRepository.save(deviceResult);
            } catch (Exception e) {
                e.printStackTrace();
                // Отменяем задачу, если что-то пошло не так
                future.cancel(true);
            }
        }
    }
}
