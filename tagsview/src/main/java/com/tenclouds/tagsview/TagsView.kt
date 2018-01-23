package com.tenclouds.tagsview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import org.apmem.tools.layouts.FlowLayout
import java.util.*

class TagsView : LinearLayout {

    private lateinit var flowLayout: FlowLayout
    private lateinit var textInput: AutoCompleteTextView

    private var isEditable: Boolean = false
    private var onTagSelectedListener: OnTagSelectedListener? = null

    val tags = ArrayList<String>()

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
        textInput.setOnEditorActionListener { _, actionId, _ ->
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
        textInput.setOnItemClickListener { _, _, position, _ ->
            val item = textInput.adapter.getItem(position)
            if (item is String) {
                addTagView(item)
                textInput.setText("")
                textInput.hint = ""
            }
        }
        textInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                textInput.setText("")
            }
        }
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line)
        if (!isInEditMode) {
            populateAdapter(adapter)
        }
        textInput.setAdapter(adapter)
    }

    private fun populateAdapter(adapter: ArrayAdapter<String>) {

    }

    fun setEditable(editable: Boolean) {
        this.isEditable = editable
        if (editable) {
            textInput.visibility = View.VISIBLE
        } else {
            textInput.visibility = View.GONE
        }
    }

    fun addTags(tags: List<String>) {
        for (tag in tags) {
            addTagView(tag)
        }
    }

    fun addTags(tags: Array<String>) {
        for (tag in tags) {
            addTagView(tag)
        }
    }

    fun removeTags() {
        flowLayout.removeAllViews()
        tags.clear()
    }

    fun addTagView(tag: String?) {
        if (tag == null) return
        for (s in tags) {
            if (tag.equals(s, ignoreCase = true)) return
        }

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view: View
        if (isEditable) {
            view = inflater.inflate(R.layout.view_item_tag_icon, flowLayout, false)
        } else {
            view = inflater.inflate(R.layout.view_item_tag, flowLayout, false)
        }

        val tagText = view.findViewById<TextView>(R.id.category_name)
        tagText.text = tag

        val r = context.resources
        val minHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ITEM_HEIGHT_DIPS.toFloat(), r.displayMetrics).toInt()
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ITEM_MARGIN_DIPS.toFloat(), r.displayMetrics).toInt()

        val layoutParams = FlowLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, margin, margin, margin)

        view.layoutParams = layoutParams
        view.minimumHeight = minHeight
        view.tag = tag

        if (isEditable) {
            flowLayout.addView(view, flowLayout.childCount - 1)

            view.setOnClickListener { v ->
                flowLayout.removeView(view)
                tags.remove(tag)

                if (onTagSelectedListener != null) {
                    onTagSelectedListener!!.onTagSelected(tag)
                }
            }
        } else {
            flowLayout.addView(view)

            view.setOnClickListener { v ->
                if (onTagSelectedListener != null) {
                    onTagSelectedListener!!.onTagSelected(tag)
                }
            }
        }

        tags.add(tag)
    }

    fun setOnTagSelectedListener(listener: OnTagSelectedListener) {
        onTagSelectedListener = listener
    }

    interface OnTagSelectedListener {
        fun onTagSelected(tag: String?)
    }

    companion object {

        private val ITEM_MARGIN_DIPS = 4
        private val ITEM_HEIGHT_DIPS = 36
    }
}
