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
    private static final String CONTRACT_ADDRESS= "0x158a8eaf9253b6d52ec172d6e3a4e0bdfb546d9d";

    @BeforeClass
    public static void setUp() {
        log.info("Configuring Web3j client");
        httpService= new HttpService(ETHEREUM_URL);
        web3j= Web3j.build(httpService);
        service = new Web3jService("default", web3j, null, null, null, null);
    }

    @Test
    public void executeReadCall() throws UnsupportedEncodingException {
        log.info("executeReadCall - clientVersion: " + service.getClientVersion());
        final String methodName= "balanceOf";
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

}