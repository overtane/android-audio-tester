package org.github.overtane.soundbrowser.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SoundViewModel : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val validSampleRates = listOf(8000.0, 16000.0, 24000.0, 32000.0, 44100.0, 48000.0)

    // The Pager object calls the load() method from the PagingSource object,
    // providing it with the LoadParams object and receiving the LoadResult object in return.
    val pageFlow = Pager(
        config = PagingConfig(pageSize = 15),
        pagingSourceFactory = { SoundRepository.soundPagingSource() }
    ).flow
        .cachedIn(viewModelScope)
        .map { pagingData ->
            pagingData
                .filter { item -> item.samplerate in validSampleRates }
                .map { item -> SoundListItem(item) }
        }

    init {
        viewModelScope.launch {
            SoundRepository.message.collectLatest { _message.postValue(it) }
        }
    }

}