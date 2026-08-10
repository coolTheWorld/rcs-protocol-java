package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Factsheet 中移动机器人几何定义的不可变根对象。 */
public final class MobileRobotGeometry {
    private final List<WheelDefinition> wheelDefinitions;
    private final List<Envelope2d> envelopes2d;
    private final List<Envelope3d> envelopes3d;
    private final ExtensionFields extensionFields;

    private MobileRobotGeometry(Builder builder) {
        this.wheelDefinitions = copyOptional(builder.wheelDefinitions);
        this.envelopes2d = copyOptional(builder.envelopes2d);
        this.envelopes3d = copyOptional(builder.envelopes3d);
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<WheelDefinition> wheelDefinitions() {
        return wheelDefinitions;
    }

    public List<Envelope2d> envelopes2d() {
        return envelopes2d;
    }

    public List<Envelope3d> envelopes3d() {
        return envelopes3d;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof MobileRobotGeometry that
                && Objects.equals(wheelDefinitions, that.wheelDefinitions)
                && Objects.equals(envelopes2d, that.envelopes2d)
                && Objects.equals(envelopes3d, that.envelopes3d)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            wheelDefinitions,
            envelopes2d,
            envelopes3d,
            extensionFields
        );
    }

    private static <T> List<T> copyOptional(List<T> values) {
        return values == null ? null : List.copyOf(values);
    }

    public static final class Builder {
        private List<WheelDefinition> wheelDefinitions;
        private List<Envelope2d> envelopes2d;
        private List<Envelope3d> envelopes3d;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder wheelDefinitions(List<WheelDefinition> wheelDefinitions) {
            this.wheelDefinitions = wheelDefinitions;
            return this;
        }

        public Builder envelopes2d(List<Envelope2d> envelopes2d) {
            this.envelopes2d = envelopes2d;
            return this;
        }

        public Builder envelopes3d(List<Envelope3d> envelopes3d) {
            this.envelopes3d = envelopes3d;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public MobileRobotGeometry build() {
            return new MobileRobotGeometry(this);
        }
    }
}
