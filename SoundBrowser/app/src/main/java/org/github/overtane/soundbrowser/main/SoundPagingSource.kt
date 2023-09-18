package org.github.overtane.soundbrowser.main

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.github.overtane.soundbrowser.freesound.FreeSoundHttpClient
import org.github.overtane.soundbrowser.freesound.FreeSoundSearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

import java.nio.channels.UnresolvedAddressException

class SoundPagingSource(
    private val backend: FreeSoundHttpClient,
    private val query: MutableStateFlow<String>,
    private val message: MutableStateFlow<String>
) : PagingSource<Int, FreeSoundSearchResult>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, FreeSoundSearchResult> = try {
        val page = params.key ?: 1
        val response = backend.search(query.value, page)
        message.update { "Found ${response.count} sounds matching '${query.value}'"}
        LoadResult.Page(
            data = response.results,
            prevKey = if (response.previous == null) null else page - 1,
            nextKey = if (response.next == null) null else page + 1
        )
    } catch (e: UnresolvedAddressException) {
        message.update { "Network error" }
        LoadResult.Error(e)
    } catch (e: Exception) {
        message.update { "Problem loading sounds" }
        LoadResult.Error(e)
    }

    // Always start new query from the first page
    override fun getRefreshKey(state: PagingState<Int, FreeSoundSearchResult>): Int? = null
}