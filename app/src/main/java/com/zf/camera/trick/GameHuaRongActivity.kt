package com.zf.camera.trick

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.zf.camera.trick.base.BaseActivity
import kotlin.random.Random

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

            setListener(appCompatTextView, index)
        }
    }

    private fun initData() {
        numData.clear()
        dataSet.clear()
        for (i in 0..8) {
            val itemNumber = getRandom()
            if (0 == itemNumber) {
                emptyViewLocation = i
            }
            numData.add(Data(i, "${itemNumber}"))
        }
    }

    private fun getRandom(): Int {
        val random = Random(System.currentTimeMillis())
        val itemNumber = random.nextInt(9)
        if (dataSet.contains(itemNumber)) {
            return getRandom()
        }
        dataSet.add(itemNumber)

        return itemNumber
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            viewHeight = mNumView1.measuredHeight.toFloat()
            viewWidth = mNumView1.measuredWidth.toFloat()
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

data class Data(val position: Int, var text: String)