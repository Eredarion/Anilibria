package ru.radiationx.anilibria.ui.fragments.auth

import android.os.Bundle
import android.support.v7.content.res.AppCompatResources
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.fragment_main_base.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.presentation.auth.AuthPresenter
import ru.radiationx.anilibria.presentation.auth.AuthView
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.BaseFragment

/**
 * Created by radiationx on 30.12.17.
 */
class AuthFragment : BaseFragment(), AuthView {

    @InjectPresenter
    lateinit var presenter: AuthPresenter

    @ProvidePresenter
    fun provideAuthPresenter(): AuthPresenter {
        return AuthPresenter((activity as RouterProvider).router, App.injections.authRepository)
    }

    override fun getLayoutResource(): Int = R.layout.fragment_auth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authPatreon.setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(authPatreon.context, R.drawable.ic_logo_patreon), null, null, null)
        authVk.setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(authPatreon.context, R.drawable.ic_logo_vk), null, null, null)

        appbarLayout.visibility = View.GONE
        authSubmit.setOnClickListener {
            presenter.signIn(authLogin.text.toString(), authPassword.text.toString())
        }
        authSkip.setOnClickListener {
            presenter.skip()
        }

        //ImageLoader.getInstance().displayImage("drawable://" + R.drawable.alib_new_or_b, auth_logo)

        authPatreon.isEnabled = false
        authVk.isEnabled = false

        authPatreon.setOnClickListener {
            presenter.socialClick(AuthPresenter.SOCIAL_PATREON)
        }

        authVk.setOnClickListener {
            presenter.socialClick(AuthPresenter.SOCIAL_VK)
        }
    }

    override fun showSocial() {
        authPatreon.isEnabled = true
        authVk.isEnabled = true
    }

    override fun onBackPressed(): Boolean {
        presenter.onBackPressed()
        return false
    }

    override fun setRefreshing(refreshing: Boolean) {
        authSwitcher.displayedChild = if (refreshing) 1 else 0
    }
}
