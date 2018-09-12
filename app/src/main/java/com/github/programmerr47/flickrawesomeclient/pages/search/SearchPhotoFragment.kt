package com.github.programmerr47.flickrawesomeclient.pages.search

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.programmerr47.flickrawesomeclient.*
import com.github.programmerr47.flickrawesomeclient.FlickrApplication.Companion.appContext
import com.github.programmerr47.flickrawesomeclient.models.PhotoList
import com.github.programmerr47.flickrawesomeclient.pages.gallery.GalleryActivity
import com.github.programmerr47.flickrawesomeclient.services.FlickrSearcher
import com.github.programmerr47.flickrawesomeclient.services.RecentSearchSubject
import com.github.programmerr47.flickrawesomeclient.util.*
import com.github.programmerr47.flickrawesomeclient.util.sugar.textWatcher
import com.github.programmerr47.flickrawesomeclient.widgets.lists.GridSpacingItemDecoration
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.SupportFragmentInjector
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable

class SearchPhotoFragment : Fragment(), SupportFragmentInjector {
    override val injector: KodeinInjector = KodeinInjector()
    override fun provideOverridingModule() = Kodein.Module {
        bind<SearchViewModel>() with provider {
            ViewModelProviders.of(instance<AppCompatActivity>("activity"))[SearchViewModel::class.java]
        }
        bind<RecentSearchSubject>() with provider {
            val debounceMs: Long = instance<AppCompatActivity>("activity").resources
                    .getInteger(R.integer.recent_search_debounce_ms).toLong()

            RecentSearchSubject(instance(), instance("ioScheduler"), debounceMs)
        }
        bind<LoadMoreDetector>() with provider { LoadMoreDetector() }
    }

    private val flickrSearcher: FlickrSearcher by instance()
    private val searchViewModel: SearchViewModel by instance()
    private val recentSearchSubject: RecentSearchSubject by instance()
    private val loadMoreDetector: LoadMoreDetector by instance()

    private var searchView: AutoCompleteTextView? = null
    private var listAdapter: PhotoListAdapter? = null
    private var swipeProgressView: SwipeRefreshLayout? = null

    private var recentsDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initializeInjector()
        return inflater.inflate(R.layout.fragment_search_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.run {
            val toolbarShadowHeight = context.resources.getDimension(R.dimen.toolbar_shadow_height)

            findViewById<Toolbar>(R.id.toolbar).title = getString(R.string.page_search_title)
            findViewById<AppBarLayout>(R.id.appBarLayout)
                    .setStateListElevationAnimator(toolbarShadowHeight)

            findViewById<ImageView>(R.id.iv_search).setOnClickListener { searchByClick() }
            initList(findViewById(R.id.rv_list))

            searchView = initSearchView(findViewById(R.id.actv_search))
            swipeProgressView = findViewById<SwipeRefreshLayout>(R.id.srl_refresh).apply {
                setOnRefreshListener { refreshSearch() }
            }
        }

        loadMoreDetector.start { applySearch(FlickrSearcher::searchMorePhotos) }

        recentsDisposable = recentSearchSubject.recentsObservable
                .observeOn(mainThread())
                .subscribe(
                        { searchView?.run {
                            val adapter = ArrayAdapter<String>(context, R.layout.item_dropdown_simple, it)
                            setAdapter(adapter)
                            adapter.notifyDataSetChanged()
                        } },
                        { searchView?.setAdapter(null) }
                )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchView?.setText(searchViewModel.searchText)
        searchViewModel.searchResult?.let { listAdapter?.update(it.list) }

        if (searchViewModel.searchText.isNotEmpty() &&
                (listAdapter?.itemCount ?: 0) == 0) {
            applySearch()
        }
    }

    override fun onDestroyView() {
        swipeProgressView = null
        listAdapter = null
        searchView = null
        recentsDisposable?.dispose()
        loadMoreDetector.stop()
        destroyInjector()
        super.onDestroyView()
    }

    private fun initSearchView(editText: AutoCompleteTextView) = editText.apply {
        addTextChangedListeners(
                textWatcher().after { searchViewModel.searchText = it.toString() },
                textWatcher().after { recentSearchSubject.accept(it.toString()) }
        )
        setOnImeOptionsClickListener { searchByClick() }
        setOnItemClickListener { _, view, _, _ -> (view as? TextView)?.let {
            editText.setText(it.text)
            searchByClick()
        } }

        if (searchViewModel.searchText.isEmpty()) showKeyboard()
    }

    private fun initList(recyclerView: RecyclerView) = recyclerView.apply {
        val spanCount = context.resources.getInteger(R.integer.page_search_column_count)
        val gridPadding = context.resources.getDimensionPixelSize(R.dimen.padding_small)

        layoutManager = GridLayoutManager(context, spanCount)
        addItemDecoration(GridSpacingItemDecoration(spanCount, gridPadding))
        adapter = PhotoListAdapter({ context, pos ->
            GalleryActivity.open(context, searchViewModel.searchText, pos)
        }).also { listAdapter = it }

        addOnScrollListener(loadMoreDetector)
    }

    private fun searchByClick() {
        searchView?.setAdapter(null)
        recentSearchSubject.save(searchViewModel.searchText)
        applySearch()
    }

    private fun applySearch() = loadMoreDetector.search { applySearch(FlickrSearcher::searchPhotos) }
    private fun refreshSearch() = loadMoreDetector.search { applySearch(FlickrSearcher::searchForce) }

    private inline fun applySearch(searchFun: FlickrSearcher.(String) -> Single<PhotoList>) =
            applySearch(searchViewModel.searchText, searchFun)

    private inline fun applySearch(text: String, searchFun: FlickrSearcher.(String) -> Single<PhotoList>): Disposable {
        hideKeyboard()
        swipeProgressView?.isRefreshing = true
        return flickrSearcher.searchFun(text)
                .observeOn(mainThread())
                .doFinally { swipeProgressView?.isRefreshing = false }
                .subscribe(
                        {
                            searchViewModel.searchResult = it
                            listAdapter?.update(it.list)

                            if (it.list.isEmpty()) {
                                searchView?.let { showEmptyListSnackBar(it) }
                            }
                        },
                        {
                            if (!isNetworkAvailable(appContext)) {
                                showToast(R.string.error_no_connection)
                            } else {
                                showToast(it.localizedMessage)
                            }
                        }
                )
    }

    private fun showEmptyListSnackBar(searchView: EditText) {
        Snackbar.make(searchView, R.string.page_search_no_elements, LENGTH_INDEFINITE)
                .setAction(R.string.action_repeat) {
                    searchView.run {
                        setText("")
                        showKeyboard()
                    }
                }
                .show()
    }
}