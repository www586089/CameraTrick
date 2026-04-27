package com.zf.camera.trick.game.hua

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.zf.camera.trick.BuildConfig
import com.zf.camera.trick.R
import com.zf.camera.trick.base.BaseActivity
import com.zf.camera.trick.game.hua.settings.SettingsActivity
import java.util.Locale
import kotlin.math.pow

class GameNewHuaRongActivity : BaseActivity() {
    private val TAG = "GameNewHuaRongActivity"

    companion object {
        const val EMPTY_LOCATION_TEXT = ""
        const val EMPTY_LOCATION_NUMBER = 0

        const val MENU_GROUP_ACTION = 100
        const val MENU_ITEM_UNDO = 101      //撤销
        const val MENU_ITEM_REDO = 102      //重做
        const val MENU_ITEM_SETTINGS = 103  //设置

        var key = 0
        val GAME_COUNT_MAP = mutableMapOf(
            Pair(key++, 2), Pair(key++, 3), Pair(key++, 4), Pair(key++, 5), Pair(key++, 6),
            Pair(key++, 7), Pair(key++, 8), Pair(key++, 9), Pair(key++, 10)
        )

        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, GameNewHuaRongActivity::class.java))
        }
    }


    private lateinit var gameLayoutContent: LinearLayout
    private lateinit var resetButton: AppCompatTextView
    private lateinit var stepTextView: AppCompatTextView

    //调试信息
    private lateinit var debugLayout: View
    private lateinit var debugInfoTv: AppCompatTextView

    private var numLayoutArray = mutableListOf<LinearLayout>()
    private var numViewArray = mutableListOf<AppCompatTextView>()
    private var numData = mutableListOf<Data>()
    private var dataSet = mutableSetOf<Int>()

    private val unDoRedoSize = 25

    //命令撤销队列
    private val cmdUndoList = mutableListOf<Command>()

    //命令重做队列
    private val cmdRedoList = mutableListOf<Command>()
    private var isRedo = false

    private var reversePairsNumber = 0
    private var emptyLineNumber = -1
    private var lineCount = 3
    private var powerTop = -1

    private var viewHeight = 0f
    private var viewWidth = 0f

    private var emptyViewLocation = -1
    private lateinit var emptyView: AppCompatTextView
    private lateinit var emptyViewBg: ColorDrawable
    private var isInAnimation = false
    private var stepCount = 0;

    private var clickDebugInfoCount = 0
    private var isShowDebugInfo = !BuildConfig.isRelease

    private lateinit var vibrator: Vibrator

    private lateinit var sp: SharedPreferences
    private var isVibrateEnable = true
    private var isAnimEnable = true

    override var isDarkFont: Boolean
        get() = false
        set(value) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_new_huarong_layout)

        sp = getSharedPreferences("app_config", MODE_PRIVATE)
        initVibrator()
        initGame()
    }

    private fun addOnGoingMenu(
        menu: Menu,
        group: Int,
        menuId: Int,
        menuIconId: Int,
        title: String
    ) {
        // 主按钮：右上角常驻图标
        val undoMenuItem = menu.add(group, menuId, menuId, title)
        // 关键：常驻显示
        undoMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        // 原生齿轮设置图标
        // ✅ 修复图标类型错误（关键代码）
        ContextCompat.getDrawable(baseContext, menuIconId)?.let {
            undoMenuItem.icon = it
        }
    }

    /**
     * invalidateOptionsMenu()
     *         ↓
     * onCreateOptionsMenu()  →  重建菜单（加载布局）
     *         ↓
     * onPrepareOptionsMenu() → 准备菜单（做动态修改）
     *         ↓
     * onOptionsItemSelected() → 处理菜单点击事件
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {


            addOnGoingMenu(this, MENU_GROUP_ACTION, MENU_ITEM_UNDO, R.drawable.ic_undo, "撤销")
            addOnGoingMenu(this, MENU_GROUP_ACTION, MENU_ITEM_REDO, R.drawable.ic_redo, "重做")
            addOnGoingMenu(
                this,
                MENU_GROUP_ACTION,
                MENU_ITEM_SETTINGS,
                R.drawable.ic_settings,
                "设置"
            )

            GAME_COUNT_MAP.forEach { (key, value) ->
                add(0, key, key, "${value}阶华容道")
            }
        }
        super.onCreateOptionsMenu(menu)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            Log.d(TAG, "onPrepareOptionsMenu: ")
            //刷新撤销按钮图标
            val undoEnable = cmdUndoList.isNotEmpty()
            val undoDrawableId = if (undoEnable) R.drawable.ic_undo else R.drawable.ic_undo_disable
            updateMenuItem(this, MENU_ITEM_UNDO, undoEnable, undoDrawableId)

            //刷新重做按钮图标
            val redoEnable = cmdRedoList.isNotEmpty()
            val redoDrawableId = if (redoEnable) R.drawable.ic_redo else R.drawable.ic_redo_disable
            updateMenuItem(this, MENU_ITEM_REDO, redoEnable, redoDrawableId)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun updateMenuItem(
        menu: Menu,
        menuItemId: Int,
        isMenuItemEnable: Boolean,
        drawableId: Int
    ) {
        menu.findItem(menuItemId).apply {
            isEnabled = isMenuItemEnable
            ContextCompat.getDrawable(baseContext, drawableId)?.let {
                icon = it
            }
        }
    }


    override fun onResume() {
        super.onResume()
        isVibrateEnable = SettingsActivity.isVibrateEnable(sp)
        isAnimEnable = SettingsActivity.isAnimEnable(sp)
    }

    private fun invalidateMenuItem() {
        invalidateOptionsMenu()
        Log.d(TAG, "invalidateMenuItem")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item.itemId}")
        val keys = GAME_COUNT_MAP.keys
        if (item.itemId == MENU_ITEM_UNDO) {
            if (cmdUndoList.isNotEmpty()) {
                val cmd = cmdUndoList.removeLastOrNull()
                if (cmd != null) {
                    //撤销的动作需要加入redo队列
                    isRedo = true
                    numViewArray[cmd.position].performClick()
                    if (cmdUndoList.isEmpty()) {
                        invalidateMenuItem()
                    }
                }
            } else {
                Toast.makeText(this, "没有可撤销的命令", Toast.LENGTH_SHORT).show()
            }
        } else if (item.itemId == MENU_ITEM_REDO) {
            if (cmdRedoList.isNotEmpty()) {
                val cmd = cmdRedoList.removeLastOrNull()
                if (cmd != null) {
                    //重做的动作默认在undo队列
                    numViewArray[cmd.position].performClick()
                    if (cmdRedoList.isEmpty()) {
                        invalidateMenuItem()
                    }
                }
            } else {
                Toast.makeText(this, "没有可重做的命令", Toast.LENGTH_SHORT).show()
            }
        } else if (item.itemId == MENU_ITEM_SETTINGS) {
            SettingsActivity.startActivity(this)
        } else if (keys.contains(item.itemId)) {
            lineCount = GAME_COUNT_MAP[item.itemId]!!
            initGame()
            return true
        }
        return super.onOptionsItemSelected(item)
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
        stepCount = 0;
        numData.clear()
        dataSet.clear()

        val tmpArray = mutableListOf<Data>()
        powerTop = (lineCount.toFloat().pow(2)).toInt()
        val numberTop: Int = powerTop - 1

        val colorStep = 256 / powerTop
        for (i in 0..numberTop) {
            val opaque = (((i + 1) * colorStep) - 1).toHex(true, 2)
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
            getDebugInfo(numberTop, tmpArray)
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

    private fun getDebugInfo(numberTop: Int, tmpArray: List<Data>) {
        val pair = getReversePairsNumber(lineCount, numberTop, tmpArray)
        reversePairsNumber = pair.first
        emptyLineNumber = pair.second
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


            Log.d(TAG, "onWindowFocusChanged: viewHeight = $viewHeight, viewWidth = $viewWidth")
        }
    }


    private fun getLinearLayout(viewHeight: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                viewHeight
            )
            clipChildren = false
        }
    }

    private fun getTextView(data: Data): AppCompatTextView {
        return AppCompatTextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                viewWidth.toInt(),
                viewHeight.toInt()
            ).apply {
                weight = 1f
            }

            id = data.position
            setTextColor(Color.WHITE)
            background = ColorDrawable(Color.parseColor(data.bgColor))
            ellipsize = TextUtils.TruncateAt.END
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 1. 定义需要获取的主题属性数组
                val attrs = intArrayOf(android.R.attr.selectableItemBackground)
                // 2. 解析主题属性
                val typedArray = context.obtainStyledAttributes(attrs)
                // 3. 获取对应的Drawable
                val selectableDrawable = typedArray.getDrawable(0)
                // 4. 设置前景
                foreground = selectableDrawable
                // 5. 回收TypedArray，避免内存泄漏
                typedArray.recycle()
            }

            gravity = Gravity.CENTER
            maxLines = 1

            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                this,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                24,
                50,
                1,
                TypedValue.COMPLEX_UNIT_DIP
            )
        }
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun initView() {
        viewHeight = (getScreenPhysicalWidth(this) / lineCount.toFloat())
        viewWidth = viewHeight

        gameLayoutContent = findViewById(R.id.game_layout)
        gameLayoutContent.removeAllViews()

        numLayoutArray.clear()

        for (i in 0..(lineCount - 1)) {
            numLayoutArray.add(getLinearLayout(viewHeight.toInt()))
        }

        //初始化调试区域
        debugLayout = findViewById(R.id.debug_info_layout)
        debugInfoTv = findViewById(R.id.debug_info_tv)

        stepTextView = findViewById(R.id.game_step)
        resetButton = findViewById(R.id.reset)

        numViewArray.clear()
        //创建具体子View
        for (i in 0..(powerTop - 1)) {
            numViewArray.add(getTextView(numData[i]))
        }
        //构建华容道布局
        var index = 0
        for (layout in numLayoutArray) {
            //先构建每一行的父布局
            gameLayoutContent.addView(layout)
            //在构建当前行的子View
            for (i in 0..(lineCount - 1)) {
                layout.addView(numViewArray[index++])
            }
        }

        gameLayoutContent.post {
            for (layout in numLayoutArray) {
                val LP = layout.layoutParams
                LP.height = viewHeight.toInt()
                layout.layoutParams = LP
            }
        }

        emptyView = numViewArray[emptyViewLocation]

        emptyViewBg = emptyView.background as ColorDrawable
        emptyView.setBackgroundDrawable(ColorDrawable())
        setStepCountInfo(0)

        setTitle()
        if (BuildConfig.isRelease) {
            debugInfoTv.visibility = View.GONE
        } else {
            debugInfoTv.visibility = View.VISIBLE
            debugInfoTv.text = String.format(
                Locale.ENGLISH,
                getString(R.string.reverse_pairs_number),
                reversePairsNumber,
                emptyLineNumber,
                reversePairsNumber + emptyLineNumber
            )
        }
    }

    private fun setTitle() {
        actionBar?.title = "华容道${lineCount}x${lineCount}"
    }

    private fun setListener(numberView: AppCompatTextView, viewIndex: Int) {
        numberView.setOnClickListener {
            val data = numData[viewIndex]
            Log.d(TAG, "setListener: viewIndex = $viewIndex, data = $data")
            val pair = getTranslation(data.position)
            if (0f == pair.first && 0f == pair.second) {
                return@setOnClickListener
            }
            if (isVibrateEnable) {
                if (vibrator.hasVibrator()) {
                    val vibrateTime = 20L
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createOneShot(
                            vibrateTime,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                        vibrator.vibrate(effect)
                    } else {
                        vibrator.vibrate(vibrateTime)
                    }
                }
            }

            animateView(numberView, pair.first, pair.second, viewIndex)
        }

        resetButton.setOnClickListener {
            initGame()
        }

        debugLayout.setOnClickListener {
            val clickMaxCount = 8
            Log.d(TAG, "setListener: isRelease = ${BuildConfig.isRelease}")
            if (BuildConfig.isRelease) {
                clickDebugInfoCount++
                Log.d(TAG, "setListener: clickDebugInfoCount = $clickDebugInfoCount")
                if (clickDebugInfoCount == clickMaxCount) {
                    isShowDebugInfo = true
                } else if (clickDebugInfoCount > clickMaxCount) {
                    clickDebugInfoCount = 0
                    isShowDebugInfo = false
                } else {
                    isShowDebugInfo = false
                }
                getDebugInfo(powerTop - 1, numData)
                updateDebugInfoLayout()
            }
        }
    }

    private fun updateDebugInfoLayout() {
        if (isShowDebugInfo) {
            debugInfoTv.visibility = View.VISIBLE
            debugInfoTv.text = String.format(
                Locale.ENGLISH,
                getString(R.string.reverse_pairs_number),
                reversePairsNumber,
                emptyLineNumber,
                reversePairsNumber + emptyLineNumber
            )
        } else {
            debugInfoTv.visibility = View.GONE
        }
    }

    private fun initGame() {
        initData()
        initView()
        setListener()
    }

    private fun getTranslation(viewIndex: Int): Pair<Float, Float> {
        when (emptyViewLocation) {
            viewIndex -> {
                return Pair(0f, 0f)
            }

            viewIndex - lineCount -> {//上移
                return Pair(0f, -viewHeight)
            }

            viewIndex + lineCount -> {//下移
                return Pair(0f, viewHeight)
            }

            viewIndex - 1 -> {//左移
                if ((viewIndex / lineCount) != emptyViewLocation / lineCount) {
                    return Pair(0f, 0f)
                }
                return Pair(-viewWidth, 0f)
            }

            viewIndex + 1 -> {//右移
                if ((viewIndex / lineCount) != emptyViewLocation / lineCount) {
                    return Pair(0f, 0f)
                }
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
        numberView.animate().translationX(tsX).translationY(tsY)
            .rotation(if (isAnimEnable) 360f else 0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
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
                numberView.rotation = 0f

                //【先交换数据再交换记录emptyViewLocation，不能反过来】
                numData[emptyViewLocation] = numberViewData
                numData[viewIndex] = emptyViewData
                if (isRedo) {
                    //撤销的动作需要加入重做队列【redo】
                    queueRedo(emptyViewLocation, numberViewData)
                    isRedo = false
                } else {
                    //重做或者新的命令动作需要加入撤销队列【undo】
                    queueUndo(emptyViewLocation, numberViewData)
                }


                emptyView = numberView
                emptyViewLocation = viewIndex

                setStepCountInfo(++stepCount)
                checkSuccess()

                isInAnimation = false
            }.start()
    }

    private fun queueRedo(emptyViewLocation: Int, emptyViewData: Data) {
        val invalidateMenu = cmdRedoList.isEmpty()
        if (cmdRedoList.size >= unDoRedoSize) {
            cmdRedoList.removeAt(0)
        }
        cmdRedoList.add(Command(emptyViewLocation, emptyViewData))

        if (invalidateMenu) {
            invalidateMenuItem()
        }
    }

    private fun queueUndo(viewIndex: Int, data: Data) {
        val invalidateMenu = cmdUndoList.isEmpty()
        if (cmdUndoList.size >= unDoRedoSize) {
            cmdUndoList.removeAt(0)
        }
        cmdUndoList.add(Command(viewIndex, data))

        if (invalidateMenu) {
            invalidateMenuItem()
        }
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

data class Data(val position: Int, val itemNumber: Int, var text: String, val bgColor: String) {

}

data class Command(val position: Int, val data: Data)