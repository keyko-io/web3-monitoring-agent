package io.keyko.monitoring.agent.core.chain.util;

import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventSpecification;
import io.keyko.monitoring.agent.core.dto.event.filter.ParameterDefinition;
import io.keyko.monitoring.agent.core.dto.event.filter.ParameterType;
import io.keyko.monitoring.agent.core.service.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes1;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Useful Web3j related utility methods
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
public class Web3jUtil {

    private static Map<ParameterType, TypeMapping> typeMappings = new HashMap<ParameterType, TypeMapping>();

    private static final String ADDRESS = "ADDRESS";
    private static final String BOOL = "BOOL";
    private static final String STRING = "STRING";
    private static final String BYTE = "BYTE";

    static {
        addUintMappings(8, 256);
        addUintArrayMappings(8, 256);
        addIntMappings(8, 256);
        addIntArrayMappings(8, 256);
        addBytesMappings(1, 32);
        addBytesArrayMappings(1, 32);
        typeMappings.put(ParameterType.build(BYTE), new TypeMapping(new TypeReference<Bytes1>() {
        }, Bytes1.class));
        typeMappings.put(ParameterType.build(BYTE + "[]"), new TypeMapping(
                new TypeReference<DynamicArray<Bytes1>>() {
                }, DynamicArray.class));
        typeMappings.put(ParameterType.build(ADDRESS), new TypeMapping(new TypeReference<Address>() {
        }, Address.class));
        typeMappings.put(ParameterType.build(ADDRESS + "[]"), new TypeMapping(
                new TypeReference<DynamicArray<Address>>() {
                }, DynamicArray.class));
        typeMappings.put(ParameterType.build(BOOL), new TypeMapping(new TypeReference<Bool>() {
        }, Bool.class));
        typeMappings.put(ParameterType.build(BOOL + "[]"), new TypeMapping(
                new TypeReference<DynamicArray<Bool>>() {
                }, DynamicArray.class));
        typeMappings.put(ParameterType.build(STRING), new TypeMapping(new TypeReference<Utf8String>() {
        }, Utf8String.class));
        typeMappings.put(ParameterType.build(STRING + "[]"),
                new TypeMapping(new TypeReference<DynamicArray<Utf8String>>() {
                }, DynamicArray.class));
    }

    public static List<TypeReference<?>> getTypeReferencesFromParameterDefinitions(
            List<ParameterDefinition> parameterDefinitions) {
        if (parameterDefinitions == null || parameterDefinitions.isEmpty()) {
            return Collections.emptyList();
        }

        return parameterDefinitions
                .stream()
                .map(parameterDefinition -> getTypeReferenceFromParameterType(parameterDefinition.getType()))
                .collect(Collectors.toList());
    }

    public static TypeReference<?> getTypeReferenceFromParameterType(ParameterType parameterType) {
        if (!typeMappings.containsKey(parameterType)) {
            throw new ValidationException(String.format("Type %s not supported", parameterType.getType()));
        }

        return typeMappings.get(parameterType).getTypeReference();
    }

    public static Class<? extends Type> getClassForParameterType(ParameterType parameterType) {
        return typeMappings.get(parameterType).getClazz();
    }

    public static String getSignature(ContractEventSpecification spec) {

        final List<ParameterDefinition> allParameterDefinitions = new ArrayList<>();
        addAllDefinitions(allParameterDefinitions, spec.getIndexedParameterDefinitions());
        addAllDefinitions(allParameterDefinitions, spec.getNonIndexedParameterDefinitions());
        Collections.sort(allParameterDefinitions);

        final Event event = new Event(spec.getEventName(),
                getTypeReferencesFromParameterDefinitions(allParameterDefinitions));

        return EventEncoder.encode(event);
    }

    private static void addUintMappings(int interval, int max) {
        addMappings(interval, max, "org.web3j.abi.datatypes.generated.Uint", "UINT");
    }

    private static void addUintArrayMappings(int interval, int max) {
        addArrayMappings(interval, max, "UINT");
    }

    private static void addIntMappings(int interval, int max) {
        addMappings(interval, max, "org.web3j.abi.datatypes.generated.Int", "INT");
    }

    private static void addIntArrayMappings(int interval, int max) {
        addArrayMappings(interval, max, "INT");
    }

    private static void addBytesMappings(int interval, int max) {
        addMappings(interval, max, "org.web3j.abi.datatypes.generated.Bytes", "BYTES");
    }

    private static void addBytesArrayMappings(int interval, int max) {
        addArrayMappings(interval, max, "BYTES");
    }

    @SuppressWarnings("unchecked")
    private static void addMappings(int interval, int max, String classPrefix, String parameterTypePrefix) {
        try {
            for (int i = interval; i <= max; i = i + interval) {
                final ParameterType type = ParameterType.build(parameterTypePrefix + i);
                final String className = classPrefix + i;
                final Class<? extends Type> clazz = (Class<? extends Type>) Class.forName(className);

                typeMappings.put(type, new TypeMapping(TypeReference.create(clazz), clazz));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addArrayMappings(int interval, int max, String parameterType) {
        try {
            for (int i = interval; i <= max; i = i + interval) {
                final ParameterType type = ParameterType.build(parameterType + i + "[]");

                typeMappings.put(type, new TypeMapping(
                        TypeReference.makeTypeReference(parameterType.toLowerCase() + i + "[]"), DynamicArray.class));
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Type> TypeReference<T> createTypeReference(Class<T> clazz) {
        return new TypeReference<T>() {
        };
    }

    @Data
    @AllArgsConstructor
    private static class TypeMapping {
        private TypeReference<?> typeReference;

        private Class<? extends Type> clazz;
    }

    private static void addAllDefinitions(List<ParameterDefinition> fullList, List<ParameterDefinition> toAdd) {
        if (toAdd != null) {
            fullList.addAll(toAdd);
        }
    }
}
