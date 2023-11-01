package org.stevi.balancer.server;

import lombok.extern.slf4j.Slf4j;
import org.stevi.balancer.http.model.RegisteredService;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceRegistry {

    private static final Map<String, Set<RegisteredService>> pathToRegisterServicesMap = new ConcurrentHashMap<>();

    public static void registerService(String requestPath, int port, String address) {
        String path = requestPath.split("/register")[1];

        Set<RegisteredService> registeredServices = pathToRegisterServicesMap.get(path);
        if (registeredServices == null) {
            var newService = new RegisteredService(1, port, address);

            registeredServices = new HashSet<>();
            registeredServices.add(newService);

            pathToRegisterServicesMap.put(path, registeredServices);

            log.info("Registered new service with path {} and port {}", path, port);
        } else if (isNotExistingService(port, address, registeredServices)) {
            var newService = new RegisteredService(1, port, address);
            registeredServices.add(newService);
        }
    }

    private static boolean isNotExistingService(int port, String address, Set<RegisteredService> registeredServices) {
        return registeredServices.stream()
                .noneMatch(service -> service.getPort() == port && service.getAddress().equals(address));
    }

    public static void unregisterService(String requestPath, int port, String address) {
        String path = requestPath.split("/register")[1];

        Set<RegisteredService> registeredServices = pathToRegisterServicesMap.get(path);
        if (registeredServices != null) {
            boolean removed = registeredServices.removeIf(service -> service.getPort() == port && service.getAddress().equals(address));
            if (removed) {
                log.info("Removing service with path {} and port {}", path, port);
            }
        }
    }

    public static Optional<RegisteredService> findAvailableService(String requestPath) {
        Set<RegisteredService> registeredServices = pathToRegisterServicesMap.get(requestPath);
        if (registeredServices == null) {
            return Optional.empty();
        }
        return registeredServices
                .stream()
                .min(Comparator.comparingInt(RegisteredService::getActiveConnection));
    }
}
