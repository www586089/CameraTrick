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

open class GameHuaRongActivity: BaseActivity() {
    private val TAG = "GameHuaRongActivity"

    companion object {
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, GameHuaRongActivity::class.java))
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
    private lateinit var resetButton: AppCompatTextView
    private lateinit var stepTextView: AppCompatTextView

    private lateinit var content: View
    private lateinit var line1: View
    private lateinit var line2: View
    private lateinit var line3: View

    private var numViewArray = listOf<AppCompatTextView>()
    private var numData = mutableListOf<Data>()
    private var dataSet = mutableSetOf<Int>()

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
        setContentView(R.layout.activity_game_huarong_layout)

        initData()
        initView()
        setListener()
    }

    private fun setListener() {
        numViewArray.forEachIndexed { index, appCompatTextView ->
            val data = numData[index]
            if (emptyViewLocation != data.position) {
                appCompatTextView.text = numData[index].text
            } else {
                appCompatTextView.text = ""
            }
            appCompatTextView.setBackgroundColor(Color.parseColor(data.bgColor))

            setListener(appCompatTextView, index)
        }
    }

    /**
     * 数字转 16 进制字符串
     * @param uppercase 是否输出大写，默认 true
     * @param padStart 最小长度，不足自动补 0，默认 0（不补）
     */
    fun Number.toHex(
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
        val numberTop = 8
        val lineCount = 3
        for (i in 0..numberTop) {
            val hex = (i + 2).toHex()
            tmpArray.add(Data(i, i, "$i", "#$hex${hex}67c8ff"))
        }

//        tmpArray.clear()
//        tmpArray.addAll(listOf(3, 4, 2, 7, 6, 5, 1, 8, 0))//无解
        while (true) {
            tmpArray.shuffle()
            Log.d(TAG, "initData: tmpArray = ${tmpArray.joinToString(",")}")
            if (tmpArray[numberTop].itemNumber == 0) {
                //判断是否无解
                var sum = 0
                var firstNumber = -1
                for (i in 0..<numberTop) {
                    firstNumber = tmpArray[i].itemNumber
                    for (j in (i + 1)..< numberTop) {
                        //统计逆序数
                        if (firstNumber > tmpArray[j].itemNumber) {
                            sum++
                        }
                    }
                }
                if (sum % 2 == 1) {
                    Log.e(TAG, "initData: 当前数据无解，重新生成数据, sum = ${sum}, tmpArray = ${tmpArray.joinToString(",")}")
                } else {
                    break
                }
            } else {
                break
            }
        }

        tmpArray.forEachIndexed { index, data ->
            if (0 == data.itemNumber) {
                emptyViewLocation = index
                numData.add(Data(index, data.itemNumber, data.text, "#00000000"))
            } else {
                numData.add(Data(index, data.itemNumber, data.text, data.bgColor))
            }
        }
    }

    /**
     * 获取屏幕物理宽度（px）
     * @param context 上下文，建议使用 Activity 避免内存泄漏
     */
    fun getScreenPhysicalWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        // 获取屏幕显示信息
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {

            viewHeight = getScreenPhysicalWidth(this) / 3f
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

        content = findViewById(R.id.content)
        line1 = findViewById(R.id.line1)
        line2 = findViewById(R.id.line2)
        line3 = findViewById(R.id.line3)

        stepTextView = findViewById(R.id.game_step)
        resetButton = findViewById(R.id.reset)

        numViewArray = listOf(
            mNumView0, mNumView1, mNumView2,
            mNumView3, mNumView4, mNumView5,
            mNumView6, mNumView7, mNumView8
        )
        emptyView = numViewArray[emptyViewLocation]

        emptyViewBg = emptyView.background as ColorDrawable
        emptyView.setBackgroundDrawable(ColorDrawable())
        stepTextView.text = "步数：${stepCount}"

        actionBar?.title = "华容道"
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
            viewIndex - 3 -> {
                return Pair(0f, -viewHeight)
            }
            viewIndex + 3 -> {
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

//                numViewArray[emptyViewLocation].text = numData[viewIndex]

//                emptyView.alpha = 1f
                val emptyBackground = emptyView.background
                emptyView.text = numData[viewIndex].text
                emptyView.setBackgroundDrawable(numberView.background)

                numberView.text = ""
                numberView.background = emptyBackground
                numberView.translationX -= tsX
                numberView.translationY -= tsY


                emptyView = numberView

                val tmp = numData[emptyViewLocation]
                numData[emptyViewLocation].text = numData[viewIndex].text
                numData[viewIndex].text = ""

                var sb = "\n"
                numData.forEachIndexed { index, i ->
                    sb = sb.plus("$i ")
                    if ((index + 1) % 3 == 0) {
                        sb = sb.plus("\n")
                    }
                }

                Log.d(TAG, "animateView: ${sb}")

                emptyViewLocation = viewIndex
                isInAnimation = false

                stepTextView.text = "步数：${++stepCount}"
                checkSuccess()
        }.start()
    }

    private fun checkSuccess() {
        var targetIndex = -1
        for (index in 0 until numData.size) {
            val data = numData[index]
            if ("${index + 1}" != data.text) {
                break;
            }
            targetIndex = index
        }

        if ((targetIndex + 2) == numData.size) {
            Toast.makeText(this, "你成功了", Toast.LENGTH_LONG).show()
        }
    }
}

data class Data(val position: Int, val itemNumber: Int, var text: String, val bgColor: String)