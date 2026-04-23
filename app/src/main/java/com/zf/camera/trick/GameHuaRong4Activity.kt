package com.zf.camera.trick

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.zf.camera.trick.base.BaseActivity
import java.util.Locale
import kotlin.math.pow

open class GameHuaRong4Activity : BaseActivity() {
    private val TAG = "GameHuaRongActivity"

    companion object {
        const val EMPTY_LOCATION_TEXT = ""
        const val EMPTY_LOCATION_NUMBER = 0
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, GameHuaRong4Activity::class.java))
        }
    }

    private lateinit var mNumView0: AppCompatTextView;
    private lateinit var mNumView1: AppCompatTextView;
    private lateinit var mNumView2: AppCompatTextView;
    private lateinit var mNumView3: AppCompatTextView;

    private lateinit var mNumView4: AppCompatTextView;
    private lateinit var mNumView5: AppCompatTextView;
    private lateinit var mNumView6: AppCompatTextView;
    private lateinit var mNumView7: AppCompatTextView;

    private lateinit var mNumView8: AppCompatTextView;
    private lateinit var mNumView9: AppCompatTextView;
    private lateinit var mNumView10: AppCompatTextView;
    private lateinit var mNumView11: AppCompatTextView;

    private lateinit var mNumView12: AppCompatTextView;
    private lateinit var mNumView13: AppCompatTextView;
    private lateinit var mNumView14: AppCompatTextView;
    private lateinit var mNumView15: AppCompatTextView;

    private lateinit var resetButton: AppCompatTextView
    private lateinit var stepTextView: AppCompatTextView

    private lateinit var content: View
    private lateinit var line1: View
    private lateinit var line2: View
    private lateinit var line3: View
    private lateinit var line4: View

    private lateinit var debugLayout: View
    private lateinit var debugInfoTv: AppCompatTextView

    private var numViewArray = listOf<AppCompatTextView>()
    private var numData = mutableListOf<Data>()
    private var dataSet = mutableSetOf<Int>()
    private var reversePairsNumber = 0
    private var emptyLineNumber = -1

    private var viewHeight = 0f
    private var viewWidth = 0f

    private var emptyViewLocation = -1
    private lateinit var emptyView: AppCompatTextView
    private lateinit var emptyViewBg: ColorDrawable
    private var isInAnimation = false
    private var stepCount = 0;


    override var isDarkFont: Boolean
        get() = false
        set(value) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_huarong4_layout)

        initData()
        initView()
        setListener()
    }

    private fun setListener() {
        numViewArray.forEachIndexed { index, appCompatTextView ->
            val data = numData[index]

            appCompatTextView.apply {
                text = numData[index].text
                setBackgroundColor(Color.parseColor(data.bgColor))
                setListener(this, index)
            }
        }
    }

    /**
     * 数字转 16 进制字符串
     * @param uppercase 是否输出大写，默认 true
     * @param padStart 最小长度，不足自动补 0，默认 0（不补）
     */
    private fun Number.toHex(
        uppercase: Boolean = true,
        padStart: Int = 0
    ): String {
        val hex = when (this) {
            is Int -> this.toString(16)
            is Long -> this.toString(16)
            else -> this.toLong().toString(16)
        }.padStart(padStart, '0') // 补前导零

        return if (uppercase) hex.uppercase() else hex
    }

    /**
     * 注意：3×3 无解判定还需结合空位位置！
     * 若空位在右下角（目标位置），逆序数奇数 → 无解；
     * 最后一个数是0才统计（代表右下角为空，此时若逆序数为奇数则无解）
     * 判断方法如下：
     * 1. 遍历数组，统计逆序数(剔除最后一个数0)；
     * 2. 若逆序数是奇数，则无解。
     */
    private fun initData() {
        numData.clear()
        dataSet.clear()

        val tmpArray = mutableListOf<Data>()
        val lineCount = 4
        val powerTop = (lineCount.toFloat().pow(2)).toInt()
        val numberTop: Int = powerTop - 1

        val colorStep = 256 / powerTop
        for (i in 0..numberTop) {
            val opaque = (i * colorStep - 1).toHex(true, 2)
            tmpArray.add(Data(i, i, "$i", "#${opaque}67C8FF"))
        }

        while (true) {
            tmpArray.shuffle()
            Log.d(TAG, "initData: tmpArray = ${tmpArray.joinToString(",")}")
            if (isEmptyItemNumber(tmpArray[numberTop].itemNumber)) {
                //判断是否无解
                val (reversePairsNumber, _) = getReversePairsNumber(lineCount, numberTop, tmpArray)
                if (reversePairsNumber % 2 == 1) {
                    Log.e(
                        TAG,
                        "initData: 当前数据无解，重新生成数据, sum = ${reversePairsNumber}, tmpArray = ${
                            tmpArray.joinToString(",")
                        }"
                    )
                } else {
                    break
                }
            } else {
                break
            }
        }
        if (!BuildConfig.isRelease) {
            val pair = getReversePairsNumber(lineCount, numberTop, tmpArray)
            reversePairsNumber = pair.first
            emptyLineNumber = pair.second
        }

        tmpArray.forEachIndexed { index, data ->
            if (EMPTY_LOCATION_NUMBER == data.itemNumber) {
                emptyViewLocation = index
                numData.add(data.copy(text = EMPTY_LOCATION_TEXT, bgColor = "#00000000"))
            } else {
                numData.add(data.copy(position = index))
            }
        }
    }

    private fun isEmptyItemNumber(itemNumber: Int): Boolean {
        return EMPTY_LOCATION_NUMBER == itemNumber
    }

    private fun getReversePairsNumber(
        lineCount: Int,
        numberTop: Int,
        tmpArray: List<Data>
    ): Pair<Int, Int> {
        var reversePairsNumber = 0;
        var emptyLineNumber = -1

        var firstNumber = -1
        for (i in 0..numberTop) {
            firstNumber = tmpArray[i].itemNumber
            for (j in (i + 1)..numberTop) {
                val itemNumber = tmpArray[j].itemNumber
                //统计逆序数
                if (!isEmptyItemNumber(itemNumber) && firstNumber > itemNumber) {
                    reversePairsNumber++
                }
            }
            if (isEmptyItemNumber(firstNumber)) {
                //从最后一行为1开始往上数
                emptyLineNumber = lineCount - (i / lineCount)
            }
        }

        return Pair(reversePairsNumber, emptyLineNumber)
    }

    /**
     * 获取屏幕物理宽度（px）
     * @param context 上下文，建议使用 Activity 避免内存泄漏
     */
    private fun getScreenPhysicalWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        // 获取屏幕显示信息
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {

            viewHeight = getScreenPhysicalWidth(this) / 4f
            viewWidth = mNumView1.measuredWidth.toFloat()
            val LP1 = line1.layoutParams
            LP1.height = viewHeight.toInt()
            line1.layoutParams = LP1

            val LP2 = line2.layoutParams
            LP2.height = viewHeight.toInt()
            line2.layoutParams = LP2

            val LP3 = line3.layoutParams
            LP3.height = viewHeight.toInt()
            line3.layoutParams = LP3
            Log.d(TAG, "onWindowFocusChanged: viewHeight = $viewHeight, viewWidth = $viewWidth")
        }
    }

    private fun initView() {
        mNumView0 = findViewById(R.id.number_0)
        mNumView1 = findViewById(R.id.number_1)
        mNumView2 = findViewById(R.id.number_2)
        mNumView3 = findViewById(R.id.number_3)

        mNumView4 = findViewById(R.id.number_4)
        mNumView5 = findViewById(R.id.number_5)
        mNumView6 = findViewById(R.id.number_6)
        mNumView7 = findViewById(R.id.number_7)

        mNumView8 = findViewById(R.id.number_8)
        mNumView9 = findViewById(R.id.number_9)
        mNumView10 = findViewById(R.id.number_10)
        mNumView11 = findViewById(R.id.number_11)

        mNumView12 = findViewById(R.id.number_12)
        mNumView13 = findViewById(R.id.number_13)
        mNumView14 = findViewById(R.id.number_14)
        mNumView15 = findViewById(R.id.number_15)

        content = findViewById(R.id.content)
        line1 = findViewById(R.id.line1)
        line2 = findViewById(R.id.line2)
        line3 = findViewById(R.id.line3)
        line4 = findViewById(R.id.line4)

        debugLayout = findViewById(R.id.debug_info_layout)
        debugInfoTv = findViewById(R.id.debug_info_tv)

        stepTextView = findViewById(R.id.game_step)
        resetButton = findViewById(R.id.reset)

        numViewArray = listOf(
            mNumView0, mNumView1, mNumView2, mNumView3,
            mNumView4, mNumView5, mNumView6, mNumView7,
            mNumView8, mNumView9, mNumView10, mNumView11,
            mNumView12, mNumView13, mNumView14, mNumView15,
        )
        emptyView = numViewArray[emptyViewLocation]

        emptyViewBg = emptyView.background as ColorDrawable
        emptyView.setBackgroundDrawable(ColorDrawable())
        setStepCountInfo(0)

        actionBar?.title = "华容道"
        if (BuildConfig.isRelease) {
            debugLayout.visibility = View.GONE
        } else {
            debugLayout.visibility = View.VISIBLE
            debugInfoTv.text = String.format(
                Locale.ENGLISH,
                getString(R.string.reverse_pairs_number),
                reversePairsNumber,
                emptyLineNumber,
                reversePairsNumber + emptyLineNumber
            )
        }
    }

    private fun setListener(numberView: AppCompatTextView, viewIndex: Int) {
        numberView.setOnClickListener {
            val data = numData[viewIndex]
            Log.d(TAG, "setListener: viewIndex = $viewIndex, data = $data")
            val pair = getTranslation(data.position)
            if (0f == pair.first && 0f == pair.second) {
                return@setOnClickListener
            }
            animateView(numberView, pair.first, pair.second, viewIndex)
        }

        resetButton.setOnClickListener {
            emptyView.background = emptyViewBg
            stepCount = 0

            initData()
            initView()
            setListener()
        }
    }

    private fun getTranslation(viewIndex: Int): Pair<Float, Float> {
        when (emptyViewLocation) {
            viewIndex -> {
                return Pair(0f, 0f)
            }

            viewIndex - 4 -> {
                return Pair(0f, -viewHeight)
            }

            viewIndex + 4 -> {
                return Pair(0f, viewHeight)
            }

            viewIndex - 1 -> {
                return Pair(-viewWidth, 0f)
            }

            viewIndex + 1 -> {
                return Pair(viewWidth, 0f)
            }

            else -> {
                return Pair(0f, 0f)
            }
        }
    }


    private fun animateView(numberView: AppCompatTextView, tsX: Float, tsY: Float, viewIndex: Int) {
        if (isInAnimation) {
            return
        }
        numberView.animate().translationX(tsX).translationY(tsY).setDuration(300)
            .withStartAction {
                isInAnimation = true
            }
            .withEndAction {

                /**
                 * 交换两个位置(emptyViewLocation, viewIndex)的数据，
                 * 但是position保持不变（因为移动位置时依赖这个position来判断，所以需要保持不变）
                 */
                val emptyViewData = numData[emptyViewLocation].copy(position = viewIndex)
                val numberViewData = numData[viewIndex].copy(position = emptyViewLocation)

                //保存emptyView的UI视觉效果【背景及文本】
                val emptyBackground = emptyView.background
                val emptyText = emptyViewData.text

                //emptyView的UI视觉效果设置为numberView的UI视觉效果
                emptyView.text = numberViewData.text
                emptyView.setBackgroundDrawable(numberView.background)

                //numberView的UI视觉效果设置为emptyView的UI视觉效果
                numberView.text = emptyText
                numberView.background = emptyBackground
                //numberView坐标归位
                numberView.translationX -= tsX
                numberView.translationY -= tsY

                //【先交换数据再交换记录emptyViewLocation，不能反过来】
                numData[emptyViewLocation] = numberViewData
                numData[viewIndex] = emptyViewData

                emptyView = numberView
                emptyViewLocation = viewIndex

                setStepCountInfo(++stepCount)
                checkSuccess()

                isInAnimation = false
            }.start()
    }

    private fun setStepCountInfo(stepCount: Int) {
        stepTextView.text = String.format(Locale.ENGLISH, getString(R.string.step_count), stepCount)
    }

    private fun checkSuccess() {
        var targetIndex = -1
        for (index in 0 until numData.size) {
            val data = numData[index]
            if ((index + 1) != data.itemNumber) {
                break;
            }
            targetIndex = index
        }

        if ((targetIndex + 2) == numData.size) {
            Toast.makeText(this, "你成功了", Toast.LENGTH_LONG).show()
        }
    }
}