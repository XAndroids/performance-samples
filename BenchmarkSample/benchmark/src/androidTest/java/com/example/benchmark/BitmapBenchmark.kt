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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val JETPACK = "images/jetpack.png"

@LargeTest
@RunWith(AndroidJUnit4::class)
class BitmapBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var bitmap: Bitmap

    @Before
    fun setUp() {
        //从asserts获取图片
        val inputStream = context.assets.open(JETPACK)
        bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
    }

    /**
     * 测试许多相对便宜的JNI调用的成本，这些调用获取一行像素，一次一个像素
     */
    @Test
    fun bitmapGetPixelBenchmark() {
        val pixels = IntArray(100) { it }
        benchmarkRule.measureRepeated {
            pixels.map { bitmap.getPixel(it, 0) }
        }
    }

    /**
     * 测试获取一行100像素的单个昂贵的JNI调用
     */
    @Test
    fun bitmapGetPixelsBenchmark() {
        val pixels = IntArray(100) { it }
        benchmarkRule.measureRepeated {
            bitmap.getPixels(pixels, 0, 100, 0, 0, 100, 1)
        }
    }
}
