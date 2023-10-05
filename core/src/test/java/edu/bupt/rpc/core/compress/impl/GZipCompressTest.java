package edu.bupt.rpc.core.compress.impl;

import edu.bupt.rpc.core.compress.Compress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GZipCompressTest {

    Compress compress = new GZipCompress();

    @BeforeEach
    void setUp() {
    }

    @Test
    public void compress_test() {
        byte[] b = "昨夜星辰昨夜风".getBytes();
        System.out.println(Arrays.toString(b));
        System.out.println(Arrays.toString(compress.compress(b)));
    }
}