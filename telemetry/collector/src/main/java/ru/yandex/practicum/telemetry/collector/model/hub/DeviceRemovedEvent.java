package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceRemovedEvent extends BaseDeviceEvent{
    @NotBlank
    String id;

    @Override
    public DeviceEventType getType() {
        return DeviceEventType.DEVICE_REMOVED;
    }
}
