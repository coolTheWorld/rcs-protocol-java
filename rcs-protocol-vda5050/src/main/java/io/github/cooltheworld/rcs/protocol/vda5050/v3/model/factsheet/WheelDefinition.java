package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中单个车轮的不可变定义。 */
public final class WheelDefinition {
    private final WheelType type;
    private final Boolean isActiveDriven;
    private final Boolean isActiveSteered;
    private final WheelPosition position;
    private final Double diameter;
    private final Double width;
    private final Double centerDisplacement;
    private final String constraints;
    private final ExtensionFields extensionFields;

    private WheelDefinition(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "type");
        this.isActiveDriven = Objects.requireNonNull(
            builder.isActiveDriven,
            "isActiveDriven"
        );
        this.isActiveSteered = Objects.requireNonNull(
            builder.isActiveSteered,
            "isActiveSteered"
        );
        this.position = Objects.requireNonNull(builder.position, "position");
        this.diameter = Objects.requireNonNull(builder.diameter, "diameter");
        this.width = Objects.requireNonNull(builder.width, "width");
        this.centerDisplacement = builder.centerDisplacement;
        this.constraints = builder.constraints;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public WheelType type() {
        return type;
    }

    public Boolean isActiveDriven() {
        return isActiveDriven;
    }

    public Boolean isActiveSteered() {
        return isActiveSteered;
    }

    public WheelPosition position() {
        return position;
    }

    public Double diameter() {
        return diameter;
    }

    public Double width() {
        return width;
    }

    public Double centerDisplacement() {
        return centerDisplacement;
    }

    public String constraints() {
        return constraints;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof WheelDefinition that
                && type.equals(that.type)
                && isActiveDriven.equals(that.isActiveDriven)
                && isActiveSteered.equals(that.isActiveSteered)
                && position.equals(that.position)
                && diameter.equals(that.diameter)
                && width.equals(that.width)
                && Objects.equals(centerDisplacement, that.centerDisplacement)
                && Objects.equals(constraints, that.constraints)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            type,
            isActiveDriven,
            isActiveSteered,
            position,
            diameter,
            width,
            centerDisplacement,
            constraints,
            extensionFields
        );
    }

    public static final class Builder {
        private WheelType type;
        private Boolean isActiveDriven;
        private Boolean isActiveSteered;
        private WheelPosition position;
        private Double diameter;
        private Double width;
        private Double centerDisplacement;
        private String constraints;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder type(WheelType type) {
            this.type = type;
            return this;
        }

        public Builder isActiveDriven(Boolean isActiveDriven) {
            this.isActiveDriven = isActiveDriven;
            return this;
        }

        public Builder isActiveSteered(Boolean isActiveSteered) {
            this.isActiveSteered = isActiveSteered;
            return this;
        }

        public Builder position(WheelPosition position) {
            this.position = position;
            return this;
        }

        public Builder diameter(Double diameter) {
            this.diameter = diameter;
            return this;
        }

        public Builder width(Double width) {
            this.width = width;
            return this;
        }

        public Builder centerDisplacement(Double centerDisplacement) {
            this.centerDisplacement = centerDisplacement;
            return this;
        }

        public Builder constraints(String constraints) {
            this.constraints = constraints;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public WheelDefinition build() {
            return new WheelDefinition(this);
        }
    }
}
