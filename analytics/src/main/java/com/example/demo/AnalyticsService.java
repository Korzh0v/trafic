package com.example.demo;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalyticsService {

    private static final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private InfluxDBClient influxClient;

    private final Map<Integer, BusAnalytics> analyticsMap = new ConcurrentHashMap<>();

    public void saveToInlufx (BusAnalytics a) {
        WriteApiBlocking apiBlocking = influxClient.getWriteApiBlocking();
        Point point = Point.measurement("bus_analytics")
                .addTag("bus_number", String.valueOf(a.busNumber))

                // Пасажири
                .addField("passengers_current",  a.passengers.count)
                .addField("passengers_avg",      a.passengers.average())
                .addField("passengers_peak",     a.passengers.peak)

                // Швидкість
                .addField("speed_current_kmh",   a.lastSpeedKmh)
                .addField("speed_avg_kmh",
                        a.speedSamples == 0 ? 0 : a.totalSpeedSum / a.speedSamples)

                // Маршрут
                .addField("progress_pct",        a.lastProgress * 100)
                .addField("distance_to_stop_m",  a.lastDistanceToNextStop)
                .addField("lat",                 a.prevLat)
                .addField("lng",                 a.prevLng)

                // Зупинки
                .addField("total_stops",         a.totalStops)
                .addField("total_dwell_ms",      a.totalDwellMs)

                // Затримки
                .addField("traffic_count",       a.trafficCount)
                .addField("accident_count",      a.accidentCount)
                .addField("total_delay_seconds", a.totalDelaySeconds)

                .time(Instant.now(), WritePrecision.MS);
                apiBlocking.writePoint("transport", "org", point);

    }


    public void process(String key, String value) {
        try {
            JsonNode node = mapper.readTree(value);

            if (node.has("eventType")) {
                BusEventDto event = mapper.treeToValue(node, BusEventDto.class);
                processEvent(event);
            } else {
                BusDTO bus = mapper.treeToValue(node, BusDTO.class);
                processTelemetry(bus);
            }

        } catch (Exception e) {
            System.err.println("Failed to deserialize. Key: " + key
                    + " | Error: " + e.getMessage());
        }
    }

    // ─── Telemetry (BusDTO) ───────────────────────────────────────

    private void processTelemetry(BusDTO bus) {
        BusAnalytics a = get(bus.getBusNumber());

        // Пасажири
        a.passengers.addSample(bus.getPassengers());

        // Швидкість через відстань між двома позиціями
        if (a.prevLat != 0 && a.prevLng != 0 && a.prevTimestamp != 0) {
            double distMeters = haversine(
                    a.prevLat, a.prevLng,
                    bus.getLat(), bus.getLng()
            );
            long deltaMs = System.currentTimeMillis() - a.prevTimestamp;

            if (deltaMs > 0) {
                double speedKmh = (distMeters / (deltaMs / 1000.0)) * 3.6;
                a.lastSpeedKmh   = speedKmh;
                a.totalSpeedSum += speedKmh;
                a.speedSamples++;
            }
        }

        a.prevLat       = bus.getLat();
        a.prevLng       = bus.getLng();
        a.prevTimestamp = System.currentTimeMillis();

        // Прогрес маршруту
        if (bus.getProgress() != null) {
            a.lastProgress = bus.getProgress();
        }

        // Відстань до наступної зупинки
        if (bus.getDistanceToNextStop() != null) {
            a.lastDistanceToNextStop = bus.getDistanceToNextStop();
        }

        printTelemetry(a, bus);
        saveToInlufx(a);
    }

    // ─── Events (BusEventDto) ─────────────────────────────────────

    private void processEvent(BusEventDto event) {
        BusAnalytics a = get(event.busNumber());

        switch (event.eventType()) {
            case "MOVING"   -> handleMoving(a, event);
            case "AT_STOP"  -> handleAtStop(a, event);
            case "ARRIVED"  -> handleArrived(a, event);
            case "DEPARTED" -> handleDeparted(a, event);
            case "TRAFFIC"  -> handleTraffic(a, event);
            case "ACCIDENT" -> handleAccident(a, event);
        }

        printEvent(a, event);
        saveToInlufx(a);
    }

    private void handleMoving(BusAnalytics a, BusEventDto e) {
        a.passengers.addSample(e.passengers());
    }

    private void handleAtStop(BusAnalytics a, BusEventDto e) {
        a.passengers.addSample(e.passengers());
    }

    private void handleArrived(BusAnalytics a, BusEventDto e) {
        a.arrivedAt = e.timestamp();
    }

    private void handleDeparted(BusAnalytics a, BusEventDto e) {
        if (a.arrivedAt != null) {
            long dwellMs    = e.timestamp() - a.arrivedAt;
            a.totalDwellMs += dwellMs;
            a.totalStops++;
            a.arrivedAt = null;
        }
    }

    private void handleTraffic(BusAnalytics a, BusEventDto e) {
        // Рахуємо тільки початок нового затору
        if (e.durationTicks() > a.lastTrafficTicks) {
            a.trafficCount++;
            a.totalDelaySeconds += (long) e.durationTicks() * 5;
        }
        a.lastTrafficTicks = e.durationTicks();
    }

    private void handleAccident(BusAnalytics a, BusEventDto e) {
        if (e.durationTicks() > a.lastAccidentTicks) {
            a.accidentCount++;
            a.totalDelaySeconds += (long) e.durationTicks() * 5;
        }
        a.lastAccidentTicks = e.durationTicks();
    }

    // ─── Snapshot ─────────────────────────────────────────────────

    public BusAnalyticsSnapshot snapshot(int busNumber) {
        BusAnalytics a = analyticsMap.get(busNumber);
        if (a == null) return null;
        return toSnapshot(a);
    }

    public Collection<BusAnalyticsSnapshot> snapshotAll() {
        return analyticsMap.values()
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    private BusAnalyticsSnapshot toSnapshot(BusAnalytics a) {
        return new BusAnalyticsSnapshot(
                a.busNumber,
                a.passengers.average(),
                a.passengers.peak,
                a.lastSpeedKmh,
                a.speedSamples == 0 ? 0 : a.totalSpeedSum / a.speedSamples,
                a.totalStops,
                a.totalDwellMs,
                a.trafficCount,
                a.accidentCount,
                a.totalDelaySeconds,
                a.lastProgress,
                a.lastDistanceToNextStop
        );
    }

    // ─── Print ────────────────────────────────────────────────────

    private void printTelemetry(BusAnalytics a, BusDTO bus) {
        System.out.printf(
                "Telemetry | Bus: %d | Lat: %.5f | Lng: %.5f | " +
                        "Passengers: %d (avg: %.1f peak: %d) | " +
                        "Speed: %.1f km/h | Progress: %.1f%% | Dist to stop: %.0f m%n",
                bus.getBusNumber(),
                bus.getLat(), bus.getLng(),
                bus.getPassengers(),
                a.passengers.average(), a.passengers.peak,
                a.lastSpeedKmh,
                a.lastProgress * 100,
                a.lastDistanceToNextStop
        );
    }

    private void printEvent(BusAnalytics a, BusEventDto e) {
        System.out.printf(
                "Event | Bus: %d | Type: %-10s | Passengers: %d | " +
                        "Stops: %d | Traffic: %d | Accidents: %d | Delay: %ds%n",
                e.busNumber(),
                e.eventType(),
                e.passengers(),
                a.totalStops,
                a.trafficCount,
                a.accidentCount,
                a.totalDelaySeconds
        );
    }

    // ─── Haversine ────────────────────────────────────────────────

    private double haversine(double lat1, double lon1,
                             double lat2, double lon2) {
        final int R = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a    = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private BusAnalytics get(int busNumber) {
        return analyticsMap.computeIfAbsent(busNumber, BusAnalytics::new);
    }

    // ─── Inner state ──────────────────────────────────────────────
    private static class BusAnalytics {
        final int busNumber;

        PassengerStats passengers = new PassengerStats();

        double lastSpeedKmh  = 0;
        double totalSpeedSum = 0;
        int    speedSamples  = 0;

        int    totalStops    = 0;
        long   totalDwellMs  = 0;
        Long   arrivedAt     = null;

        int  trafficCount    = 0;
        int  accidentCount   = 0;
        long totalDelaySeconds = 0;
        int  lastTrafficTicks  = 0;
        int  lastAccidentTicks = 0;

        double prevLat       = 0;
        double prevLng       = 0;
        long   prevTimestamp = 0;

        double lastProgress          = 0;
        double lastDistanceToNextStop = 0;

        BusAnalytics(int busNumber) {
            this.busNumber = busNumber;
        }
    }
}