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

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * MaterialNames.XMaterial_matchString             avgt    3  2853.204 ± 36451.124  ns/op
 * SoundNames.XRegistry_matchString                avgt    3    57.905 ±   266.262  ns/op
 * XMaterial_matchBukkit                           avgt    3    16.383 ±     8.532  ns/op
 * XRegistry_matchBukkit                           avgt    3     6.464 ±     0.041  ns/op
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Threads(3)
@Fork(0)
public class RegistryBenchmark {
    @State(Scope.Benchmark)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 3)
    @Measurement(iterations = 3)
    @Threads(3)
    @Fork(0)
    public static class SoundNames {
        private String soundName;
        private static final String[] VALUES =
                {"AMBIENT_CAVE", "VALID_DOESNT_EXIST", "randOm #4n Name", "block.note_block.bell", "", "ACACIA_BOAT", "minecraft:block.anvil.hit"};

        @Setup(Level.Iteration)
        public void setupName() {
            soundName = RandomUtil.random(VALUES);
        }

        @Benchmark
        public XSound XRegistry_matchString() {
            return XSound.of(soundName).orElse(null);
        }
    }

    @State(Scope.Benchmark)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 3)
    @Measurement(iterations = 3)
    @Threads(3)
    @Fork(0)
    public static class MaterialNames {
        public String materialName;

        private static final String[] VALUES =
                {"ACACIA_BOAT", "VALID_DOESNT_EXIST", "randOm #4n Name", "bamboo.raft", "", "AMBIENT_CAVE", "minecraft:air"};

        @Setup(Level.Iteration)
        public void setupName() {
            materialName = RandomUtil.random(VALUES);
        }

        @Benchmark
        public XMaterial XMaterial_matchString() {
            return XMaterial.matchXMaterial(materialName).orElse(null);
        }
    }

    @Benchmark
    public XSound XRegistry_matchBukkit() {
        return XSound.of(Sound.AMBIENT_CAVE);
    }

    @Benchmark
    public XMaterial XMaterial_matchBukkit() {
        return XMaterial.matchXMaterial(Material.AIR);
    }
}
