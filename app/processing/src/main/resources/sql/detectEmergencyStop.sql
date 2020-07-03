SELECT *
FROM vehicle_position
MATCH_RECOGNIZE (
    PARTITION by vehicle_id
    ORDER BY event_time
    MEASURES
        MATCH_ROWTIME() as stop_time,
        stopped.geocell as geocell,
        cruising.speed - stopped.speed AS speed_diff,
        MIN(braking.acceleration) as max_deceleration
    ONE ROW PER MATCH
    AFTER MATCH SKIP PAST LAST ROW
    PATTERN (cruising braking+ stopped) WITHIN INTERVAL '10' SECOND
    DEFINE
        cruising AS cruising.speed > 10,
        braking AS braking.acceleration < 0,
        stopped AS stopped.speed < 1
)
