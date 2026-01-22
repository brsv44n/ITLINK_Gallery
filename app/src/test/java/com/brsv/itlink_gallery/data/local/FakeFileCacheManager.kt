package com.brsv.itlink_gallery.data.local

import com.brsv.itlink_gallery.domain.FileCacheManager
import com.brsv.itlink_gallery.domain.models.CacheError
import com.brsv.itlink_gallery.domain.models.CacheState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

/**
 * Фейковая реализация FileCacheManager для тестирования.
 * Позволяет управлять поведением менеджера кеша в тестах.
 */
class FakeFileCacheManager : FileCacheManager {

    private val _cacheState = MutableStateFlow<CacheState>(CacheState.Empty)
    override val cacheState: StateFlow<CacheState> = _cacheState.asStateFlow()

    private val results = mutableListOf<Result<String>>()
    private var callCount = 0
    private var shouldFail = false
    private var failException: Throwable? = null
    private var storedContent: String? = null

    /**
     * Устанавливает контент для возврата при следующем вызове getFileContent
     */
    fun setResult(content: String) {
        results.add(Result.success(content))
        storedContent = content
    }

    /**
     * Настраивает фейковый объект на возврат ошибки
     */
    fun setError(exception: Throwable) {
        shouldFail = true
        failException = exception
        results.add(Result.failure(exception))
    }

    /**
     * Сбрасывает состояние фейкового объекта
     */
    fun reset() {
        _cacheState.value = CacheState.Empty
        results.clear()
        callCount = 0
        shouldFail = false
        failException = null
        storedContent = null
    }

    /**
     * Устанавливает состояние кеша вручную
     */
    fun setCacheState(state: CacheState) {
        _cacheState.value = state
    }

    /**
     * Возвращает количество вызовов getFileContent
     */
    fun getCallCount(): Int = callCount

    /**
     * Возвращает последний сохранённый контент
     */
    fun getStoredContent(): String? = storedContent

    private fun Throwable.toCacheError(): CacheError =
        when (this) {
            is IOException -> CacheError.Io(this)
            else -> CacheError.Unknown(this)
        }

    override suspend fun getFileContent(forceRefresh: Boolean): Result<String> {
        callCount++

        return when {
            shouldFail && failException != null -> {
                _cacheState.value = CacheState.Error(failException!!.toCacheError())
                Result.failure(failException!!)
            }
            results.isNotEmpty() -> {
                val result = results.removeAt(0)
                if (result.isSuccess) {
                    storedContent = result.getOrNull()
                    _cacheState.value = CacheState.Ready(
                        content = result.getOrNull() ?: "",
                        timestamp = System.currentTimeMillis()
                    )
                }
                result
            }
            else -> {
                Result.failure(IllegalStateException("No result configured"))
            }
        }
    }
}