package com.tenclouds.tagsview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import org.apmem.tools.layouts.FlowLayout
import java.util.*

class TagsView : LinearLayout {

    private lateinit var flowLayout: FlowLayout
    private lateinit var textInput: AutoCompleteTextView

    private var isEditable: Boolean = false

    var onTagSelectedListener: (String) -> Unit = {}

    private val tags = ArrayList<String>()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()

        applyAttributes(context, attrs)
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.TagsView,
                0, 0)

        try {
            setEditable(a.getBoolean(R.styleable.TagsView_tagsEditable, false))
        } finally {
            a.recycle()
        }
    }

    private fun init() {
        View.inflate(context, R.layout.view_tags_layout, this)

        initTextInput()

        flowLayout = findViewById(R.id.flowLayout)
        flowLayout.setOnClickListener {
            textInput.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(textInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun initTextInput() {
        textInput = findViewById(R.id.new_tag_input)
        textInput.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val input = textInput.text.toString()
                    if (input.trim { it <= ' ' } != "") {
                        addTagView(input.trim { it <= ' ' })
                        textInput.setText("")
                        textInput.hint = ""
                        return@setOnEditorActionListener true
                    }
                }
                false
            }

            setOnItemClickListener { _, _, position, _ ->
                val item = adapter.getItem(position)
                if (item is String) {
                    addTagView(item)
                    setText("")
                    hint = ""
                }
            }

            setOnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && textInput.text.isEmpty() && event.action == KeyEvent.ACTION_UP && tags.isNotEmpty()) {
                    removeTag(flowLayout.getChildAt(flowLayout.childCount - 2), tags.last())
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    setText("")
                }
            }

            val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line)
            setAdapter(adapter)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setEditable(editable: Boolean) {
        this.isEditable = editable
        if (editable) {
            textInput.visibility = View.VISIBLE
        } else {
            textInput.visibility = View.GONE
        }
    }

    @Suppress("unused")
    fun addTags(tags: List<String>) = tags.forEach { addTagView(it) }

    @Suppress("unused")
    fun addTags(tags: Array<String>) = tags.forEach { addTagView(it) }

    @Suppress("unused")
    fun removeAllTags() {
        flowLayout.removeAllViews()
        tags.clear()
    }

    private fun addTagView(tag: String?) {
        if (tag == null || tags.contains(tag)) return

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = if (isEditable) {
            inflater.inflate(R.layout.view_item_tag_icon, flowLayout, false)
        } else {
            inflater.inflate(R.layout.view_item_tag, flowLayout, false)
        }

        val tagText = view.findViewById<TextView>(R.id.category_name)
        tagText.text = tag

        val resources = context.resources
        val minHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ITEM_HEIGHT_DIPS.toFloat(), resources.displayMetrics).toInt()
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ITEM_MARGIN_DIPS.toFloat(), resources.displayMetrics).toInt()

        val layoutParams = FlowLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, margin, margin, margin)

        view.apply {
            this.layoutParams = layoutParams
            this.minimumHeight = minHeight
            this.tag = tag
        }

        val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)

        addTagAnimated(view, fadeInAnimation, tag)

        tags.add(tag)
    }

    private fun addTagAnimated(view: View, fadeInAnimation: Animation, tag: String) {
        flowLayout.addView(view, flowLayout.childCount - 1)
        view.apply {
            startAnimation(fadeInAnimation)
            setOnClickListener {
                if (isEditable) removeTag(view, tag)
                else onTagSelectedListener.invoke(tag)
            }
        }
    }

    private fun removeTag(tagView: View, tag: String) {
        val fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) = flowLayout.removeView(tagView)

            override fun onAnimationStart(animation: Animation?) {}
        })
        tagView.startAnimation(fadeOutAnimation)

        tags.remove(tag)

        onTagSelectedListener.invoke(tag)
    }

    companion object {
        private const val ITEM_MARGIN_DIPS = 4
        private const val ITEM_HEIGHT_DIPS = 36
    }
}
