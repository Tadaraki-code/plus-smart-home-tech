package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    @NotBlank
    String sensorId;
    @NotNull
    ConditionType type;
    @NotNull
    ConditionOperation operation;
    Integer value;
}
