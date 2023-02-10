package app;

import org.apache.tomcat.util.file.Matcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.regex.Pattern;

// Сервис проверки "устройства", создается каждый раз новый для каждой отдельной проверки с указанием
// идентификатора "устройства" - в данном упрощении IP адрес.
public class PacketMeasurer implements Callable<DeviceDTO> {

    Pattern packetSentRegex = Pattern.compile("получено\\s=\\s(\\w),");

    private final Logger log = Logger.getLogger(PacketController.class.getName());

    private final String deviceToCheck;

    public PacketMeasurer(String ipAddress) {
        this.deviceToCheck = ipAddress;
    }

    @Override
    public DeviceDTO call() throws Exception {
        sendPacketToDevice(1500);
        log.info("device check executed for "+ deviceToCheck);
        return new DeviceDTO();
    }


    private boolean sendPacketToDevice(Integer packetSize) throws IOException {
        boolean isPassed = false;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i <= 2; i++) {
            String[] command = {"CMD", "/c", String.format("ping -n 1 -w 1 -l %s ", packetSize) + deviceToCheck};
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            // MS-DOS кодировка в командной строке локальной машины - поэтому Cp866
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
            String line;
            // Читаем и записываем данные, пока не истекли 3 секунды
            while ((line = br.readLine()) != null && System.currentTimeMillis() - startTime > 3) {
                System.out.println(line);
                if (packetSentRegex.matcher(line).matches()) {
                    log.info("Packet sent: "+packetSentRegex.matcher(line).group(1));
                }
            }
        }
        return isPassed;
    }
}
