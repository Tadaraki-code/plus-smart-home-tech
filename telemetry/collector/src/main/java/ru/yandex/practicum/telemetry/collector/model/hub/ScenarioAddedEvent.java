package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ScenarioAddedEvent extends BaseDeviceEvent{
    @NotNull
    @Size(min = 3)
    private String name;
    @NotNull
    @Size(min = 1)
    private List<ScenarioCondition> conditions;
    @NotNull
    @Size(min = 1)
    private List<DeviceAction> actions;

    @Override
    public DeviceEventType getType() {
        return DeviceEventType.SCENARIO_ADDED;
    }
}
