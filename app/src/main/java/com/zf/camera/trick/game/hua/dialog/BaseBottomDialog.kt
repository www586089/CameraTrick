import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.StyleRes
import com.zf.camera.trick.R

class BaseBottomDialog(private val mContext: Context) {
    private val dialog: Dialog
    private lateinit var contentView: View

    private var dimAmount = 0.5f
    private var maxHeightRatio = 0.85f

    @StyleRes
    private var animStyle: Int = R.style.BottomDialogAnimation

    init {
        dialog = Dialog(mContext)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }

    fun setLayout(layoutRes: Int): BaseBottomDialog {
        contentView = LayoutInflater.from(mContext).inflate(layoutRes, null)
        dialog.setContentView(contentView)
        return this
    }

    fun setDim(alpha: Float): BaseBottomDialog {
        dimAmount = alpha
        return this
    }

    // 设置最大高度比例（0~1）
    fun setMaxHeight(ratio: Float): BaseBottomDialog {
        maxHeightRatio = ratio
        return this
    }

    fun setOutsideCancel(enable: Boolean): BaseBottomDialog {
        dialog.setCanceledOnTouchOutside(enable)
        return this
    }

    fun setBackCancel(enable: Boolean): BaseBottomDialog {
        dialog.setCancelable(enable)
        return this
    }

    fun setAnim(@StyleRes style: Int): BaseBottomDialog {
        animStyle = style
        return this
    }

    // ====================== ✅ 核心修复：限制最大高度，而不是设置固定高度 ======================
    fun show() {
        val window = dialog.window ?: return

        // 底部、宽度铺满
        window.setGravity(Gravity.BOTTOM)
        window.attributes = window.attributes.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            dimAmount = this@BaseBottomDialog.dimAmount
        }

        if (animStyle != 0) {
            window.setWindowAnimations(animStyle)
        }

        // 限制最大高度（关键：用 View.post 确保能拿到测量高度）
        contentView.post {
            val screenHeight = mContext.resources.displayMetrics.heightPixels
            val maxHeight = (screenHeight * maxHeightRatio).toInt()

            if (contentView.height > maxHeight) {
                contentView.layoutParams.height = maxHeight
            } else {
                contentView.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            contentView.requestLayout()
        }

        dialog.show()
    }

    fun dismiss() {
        if (dialog.isShowing) dialog.dismiss()
    }

    fun getRootView(): View = contentView
    fun isShowing(): Boolean = dialog.isShowing
}