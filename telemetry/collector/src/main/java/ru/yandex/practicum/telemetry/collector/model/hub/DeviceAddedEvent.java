package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceAddedEvent extends BaseDeviceEvent{
    @NotBlank
    String id;
    @NotNull
    DeviceType deviceType;

    @Override
    public DeviceEventType getType() {
     return DeviceEventType.DEVICE_ADDED;
    }
}
