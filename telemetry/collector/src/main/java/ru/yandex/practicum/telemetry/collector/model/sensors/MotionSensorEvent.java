package ru.yandex.practicum.telemetry.collector.model.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends BaseSensorEvent{
    @NotNull
    Integer linkQuality;
    @NotNull
    Boolean motion;
    @NotNull
    Integer voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}
