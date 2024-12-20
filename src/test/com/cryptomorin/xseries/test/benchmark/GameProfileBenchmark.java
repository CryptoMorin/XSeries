/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cryptomorin.xseries.test.benchmark;

import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import com.mojang.authlib.GameProfile;
import org.openjdk.jmh.annotations.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark     Mode          Score              Units
 * textureURL    avgt         59.507              us/op
 * base64        avgt          7.787              us/op
 * textureHash   avgt          7.289              us/op
 * uuid          avgt          6.395              us/op
 * username      avgt          4.190              us/op
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Threads(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class GameProfileBenchmark {
    private static final UUID id = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");

    @State(Scope.Benchmark)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public static class DetectTest {
        private static final String[] VALUES = {"SomeRandomName", "Invalid String 4825uh89Y(&$#Y#Q(&$T(", ""};
        String detectValues;

        @Setup(Level.Iteration)
        public void setDetectValue() {
            detectValues = RandomUtil.random(VALUES);
        }

        @Benchmark
        @Measurement(iterations = 1, batchSize = 10)
        public GameProfile detect() {
            return Profileable.detect(detectValues).getProfile();
        }
    }

    @Benchmark
    @Warmup(iterations = 0)
    @Measurement(iterations = 1, batchSize = 10)
    public GameProfile username() {
        return Profileable.username("Notch").getProfile();
    }

    @Benchmark
    @Warmup(iterations = 0)
    @Measurement(iterations = 1, batchSize = 10)
    public GameProfile uuid() {
        return Profileable.of(id).getProfile();
    }

    @Benchmark
    public GameProfile base64() {
        return Profileable.of(ProfileInputType.BASE64, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI0ZTY3ZGNlN2E0NDE4ZjdkYmE3MTE3MDQxODAzMDQ1MDVhMDM3YzEyZjE1NWE3MDYwM2UxOWYxMzIwMzRiMSJ9fX0=").getProfile();
    }

    @Benchmark
    public GameProfile textureURL() {
        return Profileable.of(ProfileInputType.TEXTURE_URL, "https://textures.minecraft.net/texture/f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990").getProfile();
    }

    @Benchmark
    public GameProfile textureHash() {
        return Profileable.of(ProfileInputType.TEXTURE_HASH, "f9f28fe3a81d67e67472b7b91caad063722477dfc37f0d729a19be49c2ec2990").getProfile();
    }
}
