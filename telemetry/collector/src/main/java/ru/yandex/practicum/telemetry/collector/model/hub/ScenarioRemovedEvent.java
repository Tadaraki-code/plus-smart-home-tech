package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioRemovedEvent extends BaseDeviceEvent{
    @NotNull
    @Size(min = 3)
    String name;

    @Override
    public DeviceEventType getType() {
        return DeviceEventType.SCENARIO_REMOVED;
    }
}
