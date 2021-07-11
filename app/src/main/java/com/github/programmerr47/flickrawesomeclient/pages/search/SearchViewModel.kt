package com.github.programmerr47.flickrawesomeclient.pages.search

import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.models.PhotoList

class SearchViewModel : ViewModel() {
    var searchText: String = ""
    var searchResult: PagedList<Photo>? = null
}