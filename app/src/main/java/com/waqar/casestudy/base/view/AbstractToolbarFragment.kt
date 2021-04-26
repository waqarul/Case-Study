package com.waqar.casestudy.base.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.waqar.casestudy.R
import com.waqar.casestudy.base.viewmodel.BaseViewModel

abstract class AbstractToolbarFragment<T : BaseViewModel> : BaseFragment<T>() {

    @BindView(R.id.toolbar_title_tv)
    lateinit var toolbarTitle: TextView

    @BindView(R.id.toolbar_back_btn)
    lateinit var toolbarBackButton: ImageView

    @BindView(R.id.toolbar_close_btn)
    lateinit var toolbarCloseButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(getToolbarTitle())
    }

    override fun setupBindings() {
        super.setupBindings()
    }

    protected open fun onCloseButtonClicked() {
        onClose()
    }

    protected open fun onBackButtonClicked() {
        onClose()
    }

    protected open fun onClose() {
        viewModel.handleBackButton()
    }

    protected fun setToolbarTitle(title: String) {
        toolbarTitle.text = title
        showToolbarTitle(true)
    }

    protected fun showToolbarTitle(shouldShowToolbarTitle: Boolean) {
        toolbarTitle.visibility = if (shouldShowToolbarTitle) View.VISIBLE else View.GONE
    }

    protected fun showBackButton(isShow: Boolean) {
        toolbarBackButton.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    protected fun showCloseButton(isShow: Boolean) {
        toolbarCloseButton.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    @OnClick(R.id.toolbar_close_btn)
    fun onCloseClicked() {
        onCloseButtonClicked()
    }

    @OnClick(R.id.toolbar_back_btn)
    fun onBackClicked() {
        onBackButtonClicked()
    }

    protected abstract fun getToolbarTitle(): String
}