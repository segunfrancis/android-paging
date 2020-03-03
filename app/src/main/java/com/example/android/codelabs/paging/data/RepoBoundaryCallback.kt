package com.example.android.codelabs.paging.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.api.searchRepos
import com.example.android.codelabs.paging.db.GithubLocalCache
import com.example.android.codelabs.paging.model.Repo

/**
 * Created by SegunFrancis
 */
class RepoBoundaryCallback(
        private val query: String,
        private val service: GithubService,
        private val cache: GithubLocalCache
) : PagedList.BoundaryCallback<Repo>() {

    // avoid triggering multiple requests in the same time
    private var isRequestInProgress = false
    // keep the last requested page. When the request is successful, increment the page number.
    private var lastRequestedPage = 1

    // LiveData of network errors.
    private val _networkErrors = MutableLiveData<String>()

    val networkErrors: LiveData<String>
        get() = _networkErrors

    private fun requestAndSaveData(query: String) {
        if (isRequestInProgress) return

        isRequestInProgress = true
        searchRepos(service, query, lastRequestedPage, NETWORK_PAGE_SIZE, { repos ->
            cache.insert(repos) {
                lastRequestedPage++
                isRequestInProgress = false
            }
        }, { error ->
            _networkErrors.postValue(error)
            isRequestInProgress = false
        })
    }

    override fun onZeroItemsLoaded() {
        requestAndSaveData(query)
    }

    override fun onItemAtEndLoaded(itemAtEnd: Repo) {
        requestAndSaveData(query)
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 50
    }
}