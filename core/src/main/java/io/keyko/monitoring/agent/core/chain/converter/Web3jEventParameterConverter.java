package io.keyko.monitoring.agent.core.chain.converter;

import io.keyko.monitoring.agent.core.settings.EventeumSettings;
import io.keyko.monitoring.agent.core.dto.event.parameter.ArrayParameter;
import io.keyko.monitoring.agent.core.dto.event.parameter.EventParameter;
import io.keyko.monitoring.agent.core.dto.event.parameter.NumberParameter;
import io.keyko.monitoring.agent.core.dto.event.parameter.StringParameter;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Web3j Type objects into Eventeum EventParameter objects.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component("web3jEventParameterConverter")
public class Web3jEventParameterConverter implements EventParameterConverter<Type> {

    private Map<String, EventParameterConverter<Type>> typeConverters = new HashMap<String, EventParameterConverter<Type>>();

    private EventeumSettings settings;

    public Web3jEventParameterConverter(EventeumSettings settings) {
        typeConverters.put("address",
                (type) -> new StringParameter(type.getTypeAsString(), Keys.toChecksumAddress(type.toString()), ""));

        registerNumberConverters("uint", 8, 256);
        registerNumberConverters("int", 8, 256);
        registerBytesConverters("bytes", 1, 32);

        typeConverters.put("byte", (type) -> convertBytesType(type));
        typeConverters.put("bool", (type) -> new NumberParameter(type.getTypeAsString(),
                (Boolean) type.getValue() ? BigInteger.ONE : BigInteger.ZERO, ""));
        typeConverters.put("string",
                (type) -> new StringParameter(type.getTypeAsString(),
                        trim((String) type.getValue()), ""));

        this.settings = settings;
    }

    @Override
    public EventParameter convert(Type toConvert) {
        final EventParameterConverter<Type> typeConverter = typeConverters.get(toConvert.getTypeAsString().toLowerCase());

        if (typeConverter == null) {
            //Type might be an array, in which case the type will be the array type class
            if (toConvert instanceof DynamicArray) {
                final DynamicArray<?> theArray = (DynamicArray<?>) toConvert;
                return convertDynamicArray(theArray);
            }

            throw new TypeConversionException("Unsupported type: " + toConvert.getTypeAsString());
        }

        return typeConverter.convert(toConvert);
    }

    private void registerNumberConverters(String prefix, int increment, int max) {
        for (int i = increment; i <= max; i = i + increment) {
            typeConverters.put(prefix + i,
                    (type) -> new NumberParameter(type.getTypeAsString(), (BigInteger) type.getValue(), ""));
        }
    }

    private void registerBytesConverters(String prefix, int increment, int max) {
        for (int i = increment; i <= max; i = i + increment) {
            typeConverters.put(prefix + i,
                    (type) -> convertBytesType(type));
        }
    }

    private EventParameter<?> convertDynamicArray(DynamicArray<?> toConvert) {
        final ArrayList<EventParameter<?>> convertedArray = new ArrayList<>();

        toConvert.getValue().forEach(arrayEntry -> convertedArray.add(convert(arrayEntry)));

        return new ArrayParameter(toConvert.getValue().get(0).getTypeAsString().toLowerCase(),
                toConvert.getComponentType(), convertedArray, "");
    }

    private EventParameter convertBytesType(Type bytesType) {
        if (settings.isBytesToAscii()) {
            return new StringParameter(
                    bytesType.getTypeAsString(), trim(new String((byte[]) bytesType.getValue())), "");
        }

        return new StringParameter(
                bytesType.getTypeAsString(), trim(Numeric.toHexString((byte[]) bytesType.getValue())), "");
    }

    private String trim(String toTrim) {
        return toTrim
                .trim()
                .replace("\\u0000", "");
    }
}