package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Сервис проверки "устройства", создается каждый раз новый для каждой отдельной проверки с указанием
// идентификатора "устройства" - в данном упрощении IP адрес. Может проводиться только одна проверка
// для определенного "устройства" в момент времени.
public class PacketMeasurer implements Callable<Device> {

    // Шаблон для парсинга ответа на ICMP запрос - 0 или 1 в group(1):
    // Пакетов: отправлено = 1, получено = 1, потеряно = 0
    Pattern packetSentRegex = Pattern.compile("\\s+получено\\s=\\s(\\w),");
    private final String deviceToCheck;

    public PacketMeasurer(String deviceToCheck) {
        this.deviceToCheck = deviceToCheck;
    }

    @Override
    public Device call() throws Exception {
        // Синхронизация на "ID устройства" (айпи адресе) - только одна проверка в момент времени
        // для одного устройства
        synchronized (deviceToCheck) {
            int maxPacketSize = 1600;
            int minPacketSize = 0;
            int tempPacketSize = maxPacketSize;
            boolean isPassed;
            // Бинарный поиск максимального размера ICMP пакета - считаем, что 1600 байт желаемый размер
            while (maxPacketSize - minPacketSize > 1) {
                System.out.println("Temp: " + tempPacketSize + " Min: " + minPacketSize + ", Max: " + maxPacketSize);
                isPassed = sendPacketToDevice(tempPacketSize);
                if (isPassed) {
                    minPacketSize = tempPacketSize;
                } else {
                    maxPacketSize = tempPacketSize;
                }
                tempPacketSize = (maxPacketSize + minPacketSize) / 2;
            }
            Device device = new Device(deviceToCheck, Integer.toString(minPacketSize));
            System.out.println(Thread.currentThread().getName()+": "+device);
            return device;
        }
    }


    private boolean sendPacketToDevice(int packetSize) throws IOException {
        long startTime = System.currentTimeMillis();
        // Список, содержащий ответы от трех запросов пинг
        List<String> results = new ArrayList<>(3);
        // отправляем последовательно три пакета и проверяем, есть ли хотя бы один ответ из трех ICMP запросов
        for (int i = 0; i <= 2; i++) {
            String[] command = {"CMD", "/c", String.format("ping -n 1 -w 1 -l %s ", packetSize) + deviceToCheck};
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            // MS-DOS кодировка в командной строке локальной машины - поэтому Cp866
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            String line;
            // Читаем и записываем данные, пока не истекли 3 секунды
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                Matcher matcher = packetSentRegex.matcher(line);
                if (matcher.find()) {
                    String result = matcher.group(1);
                    System.out.println("Result: " + result);
                    results.add(result);
                }
                if (System.currentTimeMillis() - startTime > 3000) {
                    return false;
                }
            }
        }
        // Истинно, если хотя бы один пинг из трех вернулся
        return results.contains("1");
    }
}
