package ru.radiationx.anilibria.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_container.*
import kotlinx.android.synthetic.main.activity_main.*
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.Screens
import ru.radiationx.anilibria.ui.activities.main.MainActivity
import ru.radiationx.anilibria.ui.common.RouterProvider
import ru.radiationx.anilibria.ui.fragments.auth.AuthFragment
import ru.radiationx.anilibria.ui.fragments.auth.AuthSocialFragment
import ru.radiationx.anilibria.utils.DimensionHelper
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportAppNavigator


/**
 * Created by radiationx on 30.12.17.
 */
class AuthActivity : AppCompatActivity(), RouterProvider {

    override fun getRouter(): Router = App.navigation.root.router
    override fun getNavigator(): Navigator = navigatorNew
    private val navigationHolder = App.navigation.root.holder

    private val dimensionsProvider = App.injections.dimensionsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomShadow.visibility = View.GONE
        tabsRecycler.visibility = View.GONE

        DimensionHelper(measure_view, measure_root_content, object : DimensionHelper.DimensionsListener {
            override fun onDimensionsChange(dimensions: DimensionHelper.Dimensions) {
                root_container.post {
                    root_container.setPadding(
                            root_container.paddingLeft,
                            root_container.paddingTop,
                            root_container.paddingRight,
                            dimensions.keyboardHeight
                    )
                }
                dimensionsProvider.update(dimensions)
            }
        })
        getRouter().newRootScreen(Screens.AUTH)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigationHolder.setNavigator(navigatorNew)
    }

    override fun onPause() {
        super.onPause()
        navigationHolder.removeNavigator()
    }

    private val navigatorNew by lazy {
        object : SupportAppNavigator(this, R.id.root_container) {
            override fun createActivityIntent(screenKey: String?, data: Any?): Intent? {
                return when (screenKey) {
                    Screens.MAIN -> Intent(this@AuthActivity, MainActivity::class.java)
                    else -> null
                }
            }

            override fun createFragment(screenKey: String?, data: Any?): Fragment? {
                return when (screenKey) {
                    Screens.AUTH -> AuthFragment()
                    Screens.AUTH_SOCIAL -> AuthSocialFragment().apply {
                        arguments = Bundle().apply {
                            putString(AuthSocialFragment.ARG_SOCIAL_URL, data as String)
                        }
                    }
                    else -> null
                }
            }
        }
    }
}
