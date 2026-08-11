package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

import java.util.List;
import java.util.Objects;

/**
 * 动作参数在线路上的强类型值。
 *
 * <p>该类型保留协议允许的六类 JSON 值形态。数值范围、有限性和参数定义约束由后续独立校验阶段处理。</p>
 */
public sealed interface ActionParameterValue permits
        ActionParameterValue.BooleanValue,
        ActionParameterValue.NumberValue,
        ActionParameterValue.IntegerValue,
        ActionParameterValue.StringValue,
        ActionParameterValue.ObjectValue,
        ActionParameterValue.ArrayValue {

    /**
     * 返回该线路值对应的参数数据类型。
     *
     * @return 参数数据类型
     */
    ActionValueDataType valueDataType();

    /** 布尔线路值。 */
    record BooleanValue(Boolean value) implements ActionParameterValue {
        public BooleanValue {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public ActionValueDataType valueDataType() {
            return ActionValueDataType.BOOL;
        }
    }

    /** 浮点数线路值。 */
    record NumberValue(Double value) implements ActionParameterValue {
        public NumberValue {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public ActionValueDataType valueDataType() {
            return ActionValueDataType.NUMBER;
        }
    }

    /** 整数线路值。 */
    record IntegerValue(Long value) implements ActionParameterValue {
        public IntegerValue {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public ActionValueDataType valueDataType() {
            return ActionValueDataType.INTEGER;
        }
    }

    /** 字符串线路值。 */
    record StringValue(String value) implements ActionParameterValue {
        public StringValue {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public ActionValueDataType valueDataType() {
            return ActionValueDataType.STRING;
        }
    }

    /** 对象线路值中的一个有序成员。 */
    record ObjectMember(String name, ActionParameterValue value) {
        public ObjectMember {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * 对象线路值。
     *
     * <p>成员列表保留输入顺序，并允许调用方在独立校验阶段识别重复名称。</p>
     */
    record ObjectValue(List<ObjectMember> members) implements ActionParameterValue {
        public ObjectValue {
            members = List.copyOf(Objects.requireNonNull(members, "members"));
        }

        @Override
        public ActionValueDataType valueDataType() {
            return ActionValueDataType.OBJECT;
        }
    }

    /** 数组线路值。 */
    record ArrayValue(List<ActionParameterValue> values) implements ActionParameterValue {
        public ArrayValue {
            values = List.copyOf(Objects.requireNonNull(values, "values"));
        }

        @Override
        public ActionValueDataType valueDataType() {
            return ActionValueDataType.ARRAY;
        }
    }
}
