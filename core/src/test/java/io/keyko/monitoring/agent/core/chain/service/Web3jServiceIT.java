package io.keyko.monitoring.agent.core.chain.service;

import io.keyko.monitoring.agent.core.chain.converter.Web3Converter;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.*;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class Web3jServiceIT {

    private static Web3jService web3jService;
    private static Web3j web3j;
    private static Web3jService service;
    private static HttpService httpService;
    private static final String ETHEREUM_URL= "http://192.168.0.210:8545";
//    private static final String ETHEREUM_URL= "https://baklava-forno.celo-testnet.org";

    private static final String CONTRACT_ADDRESS= "0x158a8eaf9253b6d52ec172d6e3a4e0bdfb546d9d";

    @BeforeClass
    public static void setUp() {
        log.info("Configuring Web3j client");
        httpService= new HttpService(ETHEREUM_URL);
        web3j= Web3j.build(httpService);
        service = new Web3jService("default", web3j, null, null, null, null);
        log.info("Client Version: " + service.getClientVersion());
    }

    @Test
    public void stableToken_balanceOfMethod() throws UnsupportedEncodingException {
        final String methodName= "balanceOf";
        log.info("Executing " + methodName);

        final String contractAddress= "0x7c08fEc4dA47EbeCe57DE73204bd632DDAC91027";
        final Address address = new Address(contractAddress);

        Type addressType = Web3Converter.getEncodeAbiType("address", contractAddress);
        TypeReference<Uint> funcOutput = new TypeReference<Uint>() {};
        Function function = new Function(methodName,
                Arrays.<Type>asList(addressType),
                Arrays.<TypeReference<?>>asList(funcOutput));

        List<Type> result = service.executeReadCall(CONTRACT_ADDRESS, function);

        assertTrue(result.size()>0);
        BigInteger balance= (BigInteger) result.get(0).getValue();
        log.info("Returned value: " + balance.toString());

        assertEquals(1, balance.compareTo(BigInteger.TEN));
    }

    @Test
    public void exchange_getBuyAndSellBuckets() throws UnsupportedEncodingException {
        final String methodName= "getBuyAndSellBuckets";
        log.info("Executing " + methodName);

        final String contractAddress= "0xC8FD77490A12F46709BffbCC0FCe35740Da8D860";

        Type inputType = Web3Converter.getEncodeAbiType("bool", "true");
        TypeReference<Uint> funcOutput = new TypeReference<Uint>() {};
        Function function = new Function(methodName,
                Arrays.<Type>asList(inputType),
                Arrays.<TypeReference<?>>asList(funcOutput, funcOutput));

        List<Type> result = service.executeReadCall(contractAddress, function);

        assertTrue(result.size()>0);

        BigInteger currentStableBucket= (BigInteger) result.get(0).getValue();
        log.info("Returned value: currentStableBucket=" + currentStableBucket.toString());
        assertEquals(1, currentStableBucket.compareTo(BigInteger.TEN));

        BigInteger currentGoldBucket= (BigInteger) result.get(1).getValue();
        log.info("Returned value: currentGoldBucket  =" + currentGoldBucket.toString());
        assertEquals(1, currentGoldBucket.compareTo(BigInteger.TEN));
    }

    @Test
    public void reserve_getReserveRatio() throws UnsupportedEncodingException {
        final String methodName= "getReserveRatio";
        log.info("Executing " + methodName);

        final String contractAddress= "0x1726428A6D575FdC9C7C3B7bac9f2247a5649Bf2";

        TypeReference<Uint> funcOutput = new TypeReference<Uint>() {};
        Function function = new Function(methodName,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(funcOutput));

        List<Type> result = service.executeReadCall(contractAddress, function);

        assertTrue(result.size()>0);
        BigInteger ratio= (BigInteger) result.get(0).getValue();
        log.info("Returned value: " + ratio.toString());

        assertEquals(1, ratio.compareTo(BigInteger.TEN));
    }


    @Test
    public void goldToken_totalSupply() throws UnsupportedEncodingException {
        final String methodName= "totalSupply";
        log.info("Executing " + methodName);

        final String contractAddress= "0x14D449EF428e679da48B3e8CfFa9036fF404B28A";

        TypeReference<Uint> funcOutput = new TypeReference<Uint>() {};
        Function function = new Function(methodName,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(funcOutput));

        List<Type> result = service.executeReadCall(contractAddress, function);

        assertTrue(result.size()>0);
        BigInteger ratio= (BigInteger) result.get(0).getValue();
        log.info("Returned value: " + ratio.toString());

        assertEquals(1, ratio.compareTo(BigInteger.TEN));
    }

    @Test
    public void epochReward_getTargetGoldTotalSupply() throws UnsupportedEncodingException {
        final String methodName= "getTargetGoldTotalSupply";
        log.info("Executing " + methodName);

        final String contractAddress= "0x188A97403db355DB1968d039aE9C1Db2c33663F7";

        TypeReference<Uint> funcOutput = new TypeReference<Uint>() {};
        Function function = new Function(methodName,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(funcOutput));

        List<Type> result = service.executeReadCall(contractAddress, function);

        assertTrue(result.size()>0);
        BigInteger ratio= (BigInteger) result.get(0).getValue();
        log.info("Returned value: " + ratio.toString());

        assertEquals(1, ratio.compareTo(BigInteger.TEN));
    }

    @Test
    public void reserve_getReserveGoldBalance() throws UnsupportedEncodingException {
        final String methodName= "getReserveGoldBalance";
        log.info("Executing " + methodName);

        final String contractAddress= "0x1726428A6D575FdC9C7C3B7bac9f2247a5649Bf2";

        TypeReference<Uint> funcOutput = new TypeReference<Uint>() {};
        Function function = new Function(methodName,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(funcOutput));

        List<Type> result = service.executeReadCall(contractAddress, function);

        assertTrue(result.size()>0);
        BigInteger ratio= (BigInteger) result.get(0).getValue();
        log.info("Returned value: " + ratio.toString());

        assertEquals(1, ratio.compareTo(BigInteger.TEN));
    }

}