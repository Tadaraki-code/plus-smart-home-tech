package ru.yandex.practicum.telemetry.collector.model.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TemperatureSensorEvent extends BaseSensorEvent{
    @NotNull
    Integer temperatureC;
    @NotNull
    Integer temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}
