package com.example.benchmark

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.example.benchmark.ui.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * RecyclerView benchmark - 滑动一个RecyclerView，测试每个Item花费的时间
 * 你可以通过这种方式来对RecyclerView的性能进行基准测试。以下是一些注意事项：
 *  - Benchmark一个ItemView。如果你有如Sction头，或者其他类型的Item变体，建议使用伪造的适配器数据，一次只有一种
 *  item类型
 *  - 如果你想基准TextView的性能，使用随机文本。在Item之间重用单词（如在这个简单的测试中）将会比实际使用表现更好，
 *  这是由于不现实的布局缓存命中率造成的。
 *  - 在这个简单的例子中，你不会看见RecyclerView Prefetching（在下一帧来之前，处理完数据，将得到的itemholder缓存，
 *  等真正要使用的时候直接从缓存中获取），或者Async Text布局的效果。我们将会添加更复杂的RecyclerView例子。
 *  参考：http://mikeejy.github.io/2019/08/16/Android%E5%BC%80%E5%8F%91%E4%B8%ADRecyclerView%E7%9A%84
 *  %E4%B8%80%E4%BA%9B%E4%BC%98%E5%8C%96%E6%8A%80%E6%9C%AF/
 *
 * 这个基准测量展示一个Item多个昂贵阶段的成本总和：
 * - 将ItemView附加到RecyclerView
 * - 从RecyclerView分离ItemView（滑出窗口）
 * - onBindViewHolder
 * - Item layout
 *
 * 它不测量任何下面的工作：
 * - onCreateViewHolder
 * - RenderThread 和 GPU Render工作
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class RecyclerViewBenchmark {

    class LazyComputedList<T>(
        override val size: Int = Int.MAX_VALUE,
        private inline val compute: (Int) -> T
    ) : AbstractList<T>() {
        override fun get(index: Int): T = compute(index)
    }

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        activityRule.runOnUiThread {
            val activity = activityRule.activity

            //设置RecyclerView只有1像素高度。
            //这样确保一次只能显示一项。
            activity.recyclerView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, 1)

            //使用Mock数据初始化Adapter
            //（为了简单起见，首先提交null，这样两者是同步的）
            //第1个ViewHolder将会inflated，并在下一个onActivity回调中显示
            activity.adapter.submitList(null)
            activity.adapter.submitList(LazyComputedList { buildRandomParagraph() })
        }
    }

    @Test
    fun buildParagraph() {
        benchmarkRule.measureRepeated {
            //测量生成字符的成本 - 这是primary scroll()测量中的开销，但是它只占整个工作中的一小部分。
            buildRandomParagraph()
        }
    }

    @UiThreadTest
    @Test
    fun scroll() {
        val recyclerView = activityRule.activity.recyclerView
        assertTrue("RecyclerView expected to have children", recyclerView.childCount > 0)
        assertEquals("RecyclerView must have height = 1", 1, recyclerView.height)

        //RecyclerView有children，它的item attached，bound，并且完成layout。
        benchmarkRule.measureRepeated {
            //RecyclerView滑动一个item
            //这将会同步执行:attach / detach(old item) / bind /layout
            recyclerView.scrollBy(0, recyclerView.getLastChild().height)
        }
    }
}

private fun ViewGroup.getLastChild(): View = getChildAt(childCount - 1)
