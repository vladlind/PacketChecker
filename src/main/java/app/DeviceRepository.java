package app;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Integer> {

    List<Device> findDeviceByIpAddress(String ipAddress);
}
