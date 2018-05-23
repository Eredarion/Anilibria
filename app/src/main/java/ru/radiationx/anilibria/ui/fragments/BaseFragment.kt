package ru.radiationx.anilibria.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.LayoutRes
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatFragment
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_main_base.*
import org.json.JSONArray
import org.json.JSONObject
import ru.radiationx.anilibria.App
import ru.radiationx.anilibria.R
import ru.radiationx.anilibria.ui.activities.main.IntentActivity
import ru.radiationx.anilibria.ui.common.BackButtonListener
import ru.radiationx.anilibria.utils.DimensionHelper

/* Created by radiationx on 18.11.17. */

abstract class BaseFragment : MvpAppCompatFragment(), BackButtonListener {

    private val dimensionsProvider = App.injections.dimensionsProvider
    private var dimensionsDisposable: Disposable? = null

    protected open val needToolbarShadow = true

    @LayoutRes
    protected abstract fun getLayoutResource(): Int

    @LayoutRes
    protected open fun getBaseLayout(): Int = R.layout.fragment_main_base

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newView: View? = inflater.inflate(getBaseLayout(), container, false)
        if (getLayoutResource() != View.NO_ID) {
            inflater.inflate(getLayoutResource(), newView?.findViewById(R.id.fragment_content), true)
        }
        return newView
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && needToolbarShadow) {
            toolbar_shadow_prelp?.visibility = View.VISIBLE
        }

        backupInfo?.setOnClickListener {
            context?.let {
                val text = """В бекапе будет содержаться локальная история просмотра релизов и серий, а так-же временные метки просмотра серий.<br><br>Необходимо установить новую версию приложения: <a href="https://www.anilibria.tv/all/app/">на сайте</a> или <a href="https://play.google.com/store/apps/details?id=ru.radiationx.anilibria.app">Play Market</a>.<br><br><b>Эта версия программы больше не поддерживается и обновляться не будет.</b>"""
                AlertDialog.Builder(it)
                        .setMessage(Html.fromHtml(text))
                        .setPositiveButton("Ок, ясно", null)
                        .show()
            }
        }

        backupAction?.setOnClickListener {
            doBackup()
        }
    }

    private fun doBackup() {
        val schedulers = App.injections.schedulers
        Single
                .fromCallable { backupAsyncPart() }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe({ json ->
                    val intent = Intent(IntentActivity.ACTION_RESTORE, Uri.parse("app://anilibria.app")).apply {
                        putExtra(IntentActivity.KEY_RESTORE, json.toString())
                    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.instance.applicationContext.startActivity(Intent.createChooser(intent, "Восстановить бекап в").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }, {
                    it.printStackTrace()
                    Toast.makeText(App.instance.applicationContext, "Ошибка при бекапе: $it", Toast.LENGTH_SHORT).show()
                })

    }

    private fun backupAsyncPart(): JSONObject {
        val jsonReleases = JSONArray()
        App.injections.historyStorage.getReleases().forEach {
            jsonReleases.put(JSONObject().apply {
                put("id", it.id)
                put("idName", it.idName)
                put("title", it.title)
                put("originalTitle", it.originalTitle)
                put("torrentLink", it.torrentLink)
                put("link", it.link)
                put("image", it.image)
                put("episodesCount", it.episodesCount)
                put("description", it.description)
                put("seasons", JSONArray(it.seasons))
                put("voices", JSONArray(it.voices))
                put("genres", JSONArray(it.genres))
                put("types", JSONArray(it.types))
            })
        }

        val jsonEpisodes = JSONArray()
        App.injections.episodesCheckerStorage.getEpisodes().forEach {
            jsonEpisodes.put(JSONObject().apply {
                put("releaseId", it.releaseId)
                put("id", it.id)
                put("seek", it.seek)
                put("isViewed", it.isViewed)
                put("lastAccess", it.lastAccess)
            })
        }

        return JSONObject().apply {
            put("releases", jsonReleases.toString())
            put("episodes", jsonEpisodes.toString())
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dimensionsDisposable = dimensionsProvider.dimensions().subscribe {
            toolbar?.post {
                toolbar?.let { _ ->
                    updateDimens(it)
                }
            }
            updateDimens(it)
        }
    }

    open fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        toolbar?.layoutParams = (toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams).apply {
            topMargin = dimensions.statusBar
        }
        toolbar?.requestLayout()
    }

    fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return
        }
        baseStatusBar.setBackgroundColor(color)
    }

    fun setStatusBarVisibility(isVisible: Boolean) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            baseStatusBar.visibility = View.GONE
            return
        }
        baseStatusBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        dimensionsDisposable?.dispose()
    }
}
