SELECT stop_time, lat, lon, speed_diff, max_deceleration, vehicle_type
FROM vehicle_position
MATCH_RECOGNIZE (
    PARTITION by vehicle_id
    ORDER BY event_time
    MEASURES
        MATCH_ROWTIME() as stop_time,
        stopped.lat as lat,
        stopped.lon as lon,
        cruising.speed - stopped.speed AS speed_diff,
        MIN(braking.acceleration) as max_deceleration,
        stopped.vehicle_type as vehicle_type
    ONE ROW PER MATCH
    AFTER MATCH SKIP PAST LAST ROW
    PATTERN (cruising braking+ stopped) WITHIN INTERVAL '10' SECOND
    DEFINE
        cruising AS cruising.speed >= 10,
        braking AS braking.acceleration < 0,
        stopped AS stopped.speed < 1
)
