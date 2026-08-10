package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Mobile Robot 可处理的一组不可变载荷能力。 */
public final class LoadSet {
    private final String setName;
    private final String loadType;
    private final List<String> loadPositions;
    private final BoundingBoxReference boundingBoxReference;
    private final LoadDimensions loadDimensions;
    private final Double maximumWeight;
    private final Double minimumLoadhandlingHeight;
    private final Double maximumLoadhandlingHeight;
    private final Double minimumLoadhandlingDepth;
    private final Double maximumLoadhandlingDepth;
    private final Double minimumLoadhandlingTilt;
    private final Double maximumLoadhandlingTilt;
    private final Double maximumSpeed;
    private final Double maximumAcceleration;
    private final Double maximumDeceleration;
    private final Double pickTime;
    private final Double dropTime;
    private final String description;
    private final ExtensionFields extensionFields;

    private LoadSet(Builder builder) {
        this.setName = Objects.requireNonNull(builder.setName, "setName");
        this.loadType = Objects.requireNonNull(builder.loadType, "loadType");
        this.loadPositions = builder.loadPositions == null
            ? null
            : List.copyOf(builder.loadPositions);
        this.boundingBoxReference = builder.boundingBoxReference;
        this.loadDimensions = builder.loadDimensions;
        this.maximumWeight = builder.maximumWeight;
        this.minimumLoadhandlingHeight = builder.minimumLoadhandlingHeight;
        this.maximumLoadhandlingHeight = builder.maximumLoadhandlingHeight;
        this.minimumLoadhandlingDepth = builder.minimumLoadhandlingDepth;
        this.maximumLoadhandlingDepth = builder.maximumLoadhandlingDepth;
        this.minimumLoadhandlingTilt = builder.minimumLoadhandlingTilt;
        this.maximumLoadhandlingTilt = builder.maximumLoadhandlingTilt;
        this.maximumSpeed = builder.maximumSpeed;
        this.maximumAcceleration = builder.maximumAcceleration;
        this.maximumDeceleration = builder.maximumDeceleration;
        this.pickTime = builder.pickTime;
        this.dropTime = builder.dropTime;
        this.description = builder.description;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String setName() {
        return setName;
    }

    public String loadType() {
        return loadType;
    }

    /** @return 可选的不可变适用位置；缺失或空列表均表示适用于全部设备 */
    public List<String> loadPositions() {
        return loadPositions;
    }

    public BoundingBoxReference boundingBoxReference() {
        return boundingBoxReference;
    }

    public LoadDimensions loadDimensions() {
        return loadDimensions;
    }

    /** @return 可选的最大载荷质量，单位 kg */
    public Double maximumWeight() {
        return maximumWeight;
    }

    /** @return 可选的最小载荷处理高度，单位 m */
    public Double minimumLoadhandlingHeight() {
        return minimumLoadhandlingHeight;
    }

    /** @return 可选的最大载荷处理高度，单位 m */
    public Double maximumLoadhandlingHeight() {
        return maximumLoadhandlingHeight;
    }

    /** @return 可选的最小载荷处理深度，单位 m */
    public Double minimumLoadhandlingDepth() {
        return minimumLoadhandlingDepth;
    }

    /** @return 可选的最大载荷处理深度，单位 m */
    public Double maximumLoadhandlingDepth() {
        return maximumLoadhandlingDepth;
    }

    /** @return 可选的最小载荷处理倾角，单位 rad */
    public Double minimumLoadhandlingTilt() {
        return minimumLoadhandlingTilt;
    }

    /** @return 可选的最大载荷处理倾角，单位 rad */
    public Double maximumLoadhandlingTilt() {
        return maximumLoadhandlingTilt;
    }

    /** @return 可选的最大速度，单位 m/s */
    public Double maximumSpeed() {
        return maximumSpeed;
    }

    /** @return 可选的最大加速度，单位 m/s² */
    public Double maximumAcceleration() {
        return maximumAcceleration;
    }

    /** @return 可选的最大减速度，单位 m/s² */
    public Double maximumDeceleration() {
        return maximumDeceleration;
    }

    /** @return 可选的近似取载时间，单位 s */
    public Double pickTime() {
        return pickTime;
    }

    /** @return 可选的近似卸载时间，单位 s */
    public Double dropTime() {
        return dropTime;
    }

    public String description() {
        return description;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof LoadSet that
                && setName.equals(that.setName)
                && loadType.equals(that.loadType)
                && Objects.equals(loadPositions, that.loadPositions)
                && Objects.equals(
                    boundingBoxReference,
                    that.boundingBoxReference
                )
                && Objects.equals(loadDimensions, that.loadDimensions)
                && Objects.equals(maximumWeight, that.maximumWeight)
                && Objects.equals(
                    minimumLoadhandlingHeight,
                    that.minimumLoadhandlingHeight
                )
                && Objects.equals(
                    maximumLoadhandlingHeight,
                    that.maximumLoadhandlingHeight
                )
                && Objects.equals(
                    minimumLoadhandlingDepth,
                    that.minimumLoadhandlingDepth
                )
                && Objects.equals(
                    maximumLoadhandlingDepth,
                    that.maximumLoadhandlingDepth
                )
                && Objects.equals(
                    minimumLoadhandlingTilt,
                    that.minimumLoadhandlingTilt
                )
                && Objects.equals(
                    maximumLoadhandlingTilt,
                    that.maximumLoadhandlingTilt
                )
                && Objects.equals(maximumSpeed, that.maximumSpeed)
                && Objects.equals(
                    maximumAcceleration,
                    that.maximumAcceleration
                )
                && Objects.equals(
                    maximumDeceleration,
                    that.maximumDeceleration
                )
                && Objects.equals(pickTime, that.pickTime)
                && Objects.equals(dropTime, that.dropTime)
                && Objects.equals(description, that.description)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            setName,
            loadType,
            loadPositions,
            boundingBoxReference,
            loadDimensions,
            maximumWeight,
            minimumLoadhandlingHeight,
            maximumLoadhandlingHeight,
            minimumLoadhandlingDepth,
            maximumLoadhandlingDepth,
            minimumLoadhandlingTilt,
            maximumLoadhandlingTilt,
            maximumSpeed,
            maximumAcceleration,
            maximumDeceleration,
            pickTime,
            dropTime,
            description,
            extensionFields
        );
    }

    public static final class Builder {
        private String setName;
        private String loadType;
        private List<String> loadPositions;
        private BoundingBoxReference boundingBoxReference;
        private LoadDimensions loadDimensions;
        private Double maximumWeight;
        private Double minimumLoadhandlingHeight;
        private Double maximumLoadhandlingHeight;
        private Double minimumLoadhandlingDepth;
        private Double maximumLoadhandlingDepth;
        private Double minimumLoadhandlingTilt;
        private Double maximumLoadhandlingTilt;
        private Double maximumSpeed;
        private Double maximumAcceleration;
        private Double maximumDeceleration;
        private Double pickTime;
        private Double dropTime;
        private String description;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder setName(String setName) {
            this.setName = setName;
            return this;
        }

        public Builder loadType(String loadType) {
            this.loadType = loadType;
            return this;
        }

        public Builder loadPositions(List<String> loadPositions) {
            this.loadPositions = loadPositions;
            return this;
        }

        public Builder boundingBoxReference(
            BoundingBoxReference boundingBoxReference
        ) {
            this.boundingBoxReference = boundingBoxReference;
            return this;
        }

        public Builder loadDimensions(LoadDimensions loadDimensions) {
            this.loadDimensions = loadDimensions;
            return this;
        }

        public Builder maximumWeight(Double maximumWeight) {
            this.maximumWeight = maximumWeight;
            return this;
        }

        public Builder minimumLoadhandlingHeight(
            Double minimumLoadhandlingHeight
        ) {
            this.minimumLoadhandlingHeight = minimumLoadhandlingHeight;
            return this;
        }

        public Builder maximumLoadhandlingHeight(
            Double maximumLoadhandlingHeight
        ) {
            this.maximumLoadhandlingHeight = maximumLoadhandlingHeight;
            return this;
        }

        public Builder minimumLoadhandlingDepth(
            Double minimumLoadhandlingDepth
        ) {
            this.minimumLoadhandlingDepth = minimumLoadhandlingDepth;
            return this;
        }

        public Builder maximumLoadhandlingDepth(
            Double maximumLoadhandlingDepth
        ) {
            this.maximumLoadhandlingDepth = maximumLoadhandlingDepth;
            return this;
        }

        public Builder minimumLoadhandlingTilt(
            Double minimumLoadhandlingTilt
        ) {
            this.minimumLoadhandlingTilt = minimumLoadhandlingTilt;
            return this;
        }

        public Builder maximumLoadhandlingTilt(
            Double maximumLoadhandlingTilt
        ) {
            this.maximumLoadhandlingTilt = maximumLoadhandlingTilt;
            return this;
        }

        public Builder maximumSpeed(Double maximumSpeed) {
            this.maximumSpeed = maximumSpeed;
            return this;
        }

        public Builder maximumAcceleration(Double maximumAcceleration) {
            this.maximumAcceleration = maximumAcceleration;
            return this;
        }

        public Builder maximumDeceleration(Double maximumDeceleration) {
            this.maximumDeceleration = maximumDeceleration;
            return this;
        }

        public Builder pickTime(Double pickTime) {
            this.pickTime = pickTime;
            return this;
        }

        public Builder dropTime(Double dropTime) {
            this.dropTime = dropTime;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public LoadSet build() {
            return new LoadSet(this);
        }
    }
}
