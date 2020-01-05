/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//benchmarks是一个标准的instrumentation测试
@LargeTest
@RunWith(AndroidJUnit4::class)
class AutoBoxingBenchmark {

    //使用BenchmarkRule
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    /**
     * 测试分配利用ART的缓存的装箱整型成本
     */
    @Test
    fun integerArtCacheAlloc() {
        var i = Integer(1000)
        benchmarkRule.measureRepeated {
            if (i < 100) {
                i = Integer(i.toInt() + 1)
            } else {
                i = Integer(0)
            }
        }
    }

    /**
     * 测试分配在ART缓存之外的装箱整型的成本
     */
    @Test
    fun integerAlloc() {
        var i = Integer(1000)
        benchmarkRule.measureRepeated {
            if (i < 1100) {
                i = Integer(i.toInt() + 1)
            } else {
                i = Integer(1000)
            }
        }
    }
}
