package io.keyko.monitoring.agent.core.chain.converter;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Web3Converter {

    private static Map<String, TypeReference> typeMappings= new HashMap<>();
    private static Map<String, TypeReference> subTypeMappings= new HashMap<>();
    private static final String DEFAULT_TYPE_MAPPING = "string";
    static {
        typeMappings.put("bool", new TypeReference<Bool>(){});
        typeMappings.put("uint", new TypeReference<Uint>(){});
        typeMappings.put("address", new TypeReference<Address>(){});
        typeMappings.put("int", new TypeReference<Int>(){});
        typeMappings.put("string", new TypeReference<Utf8String>(){});
        typeMappings.put("bytes", new TypeReference<Bytes>(){});

        subTypeMappings.put("bool", new TypeReference<DynamicArray<Bool>>(){});
        subTypeMappings.put("uint", new TypeReference<DynamicArray<Uint>>(){});
        subTypeMappings.put("address", new TypeReference<DynamicArray<Address>>(){});
        subTypeMappings.put("int", new TypeReference<DynamicArray<Int>>(){});
        subTypeMappings.put("string", new TypeReference<DynamicArray<Utf8String>>(){});
        subTypeMappings.put("bytes", new TypeReference<DynamicArray<Bytes>>(){});
    }

    public static Type getEncodeAbiType(String type, Object value) throws UnsupportedEncodingException {
        type= type.toLowerCase();

        try {

            if (type.contains("[")) {
                String subType = type.replaceAll("\\[\\]", "");
                Object[] items = (String[]) value;
                Array result= new DynamicArray(Arrays.asList(items));
                return result;
            } else if (type.contains("bool"))
                return new Bool(Boolean.parseBoolean((String) value));
//            return new Bool((boolean) value);
            else if (type.contains("uint"))
                if(value instanceof Integer){
                    new Uint(BigInteger.valueOf((Integer) value));
                }
                else
                    new Uint((BigInteger) value);
            else if (type.contains("address"))
                return new Address((String) value);
            else if (type.contains("bytes"))
                return new DynamicBytes((byte[]) value);
            else if ("string".equals(type))
                return new Utf8String((String) value);

            return new Utf8String((String) value);
        } catch (Exception ex)  {
            throw new UnsupportedEncodingException("Error encoding " + type + ": " + ex.getMessage());
        }
    }

    public static TypeReference getTypeReference(String type) throws UnsupportedEncodingException {
        type= type.toLowerCase();
        String basicType= type.replaceAll("[^a-zA-Z]", "");

        if (type.contains("[")) {
            String subType = type.replaceAll("\\[\\]", "").toLowerCase();
            if (subTypeMappings.containsKey(subType))
                return subTypeMappings.get(subType);
            return subTypeMappings.get(DEFAULT_TYPE_MAPPING);
        } else if (typeMappings.containsKey(basicType))
            return typeMappings.get(basicType);
        else
            return typeMappings.get(DEFAULT_TYPE_MAPPING);
    }
}
