package com.brsv.itlink_gallery.data.local

import com.brsv.itlink_gallery.domain.models.CacheState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit-тесты для FakeFileCacheManager.
 * Тестирует поведение фейковой реализации FileCacheManager.
 */
class FileCacheManagerTest {

    private lateinit var fakeCacheManager: FakeFileCacheManager

    @Before
    fun setUp() {
        fakeCacheManager = FakeFileCacheManager()
    }

    @Test
    fun `initial state should be Empty`() = runTest {
        val state = fakeCacheManager.cacheState.first()

        assertEquals(CacheState.Empty, state)
    }

    @Test
    fun `getFileContent should return success when result is set`() = runTest {
        val testContent = "https://example.com/image1.jpg\nhttps://example.com/image2.jpg"
        fakeCacheManager.setResult(testContent)

        val result = fakeCacheManager.getFileContent()

        assertTrue(result.isSuccess)
        assertEquals(testContent, result.getOrNull())
    }

    @Test
    fun `getFileContent should return failure when error is set`() = runTest {
        val exception = RuntimeException("Network error")
        fakeCacheManager.setError(exception)

        val result = fakeCacheManager.getFileContent()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `cacheState should be Ready after successful getFileContent`() = runTest {
        val testContent = "https://example.com/image.jpg"
        fakeCacheManager.setResult(testContent)

        fakeCacheManager.getFileContent()
        val state = fakeCacheManager.cacheState.first()

        assertTrue(state is CacheState.Ready)
        assertEquals(testContent, (state as CacheState.Ready).content)
    }

    @Test
    fun `cacheState should be Error after failed getFileContent`() = runTest {
        val exception = RuntimeException("HTTP 500")
        fakeCacheManager.setError(exception)

        fakeCacheManager.getFileContent()
        val state = fakeCacheManager.cacheState.first()

        assertTrue(state is CacheState.Error)
    }

    @Test
    fun `callCount should increment on each getFileContent call`() = runTest {
        fakeCacheManager.setResult("content1")
        fakeCacheManager.setResult("content2")
        fakeCacheManager.setResult("content3")

        fakeCacheManager.getFileContent()
        fakeCacheManager.getFileContent()
        fakeCacheManager.getFileContent()

        assertEquals(3, fakeCacheManager.getCallCount())
    }

    @Test
    fun `storedContent should return last successful content`() = runTest {
        fakeCacheManager.setResult("first content")
        fakeCacheManager.setResult("second content")

        fakeCacheManager.getFileContent()
        fakeCacheManager.getFileContent()

        // storedContent обновляется при каждом успешном вызове
        assertEquals("second content", fakeCacheManager.getStoredContent())
    }

    @Test
    fun `reset should clear all state`() = runTest {
        fakeCacheManager.setResult("test content")
        fakeCacheManager.getFileContent()

        fakeCacheManager.reset()

        assertEquals(0, fakeCacheManager.getCallCount())
        assertNull(fakeCacheManager.getStoredContent())
        assertEquals(CacheState.Empty, fakeCacheManager.cacheState.first())
    }

    @Test
    fun `setCacheState should update state manually`() = runTest {
        val loadingState = CacheState.Loading
        fakeCacheManager.setCacheState(loadingState)

        val state = fakeCacheManager.cacheState.first()

        assertEquals(loadingState, state)
    }

    @Test
    fun `getFileContent should work with forceRefresh parameter`() = runTest {
        val testContent = "content"
        fakeCacheManager.setResult(testContent)

        val result = fakeCacheManager.getFileContent(forceRefresh = true)

        assertTrue(result.isSuccess)
        assertEquals(testContent, result.getOrNull())
    }

    @Test
    fun `multiple results should be returned in order`() = runTest {
        fakeCacheManager.setResult("content1")
        fakeCacheManager.setResult("content2")
        fakeCacheManager.setResult("content3")

        val result1 = fakeCacheManager.getFileContent()
        val result2 = fakeCacheManager.getFileContent()
        val result3 = fakeCacheManager.getFileContent()

        assertEquals("content1", result1.getOrNull())
        assertEquals("content2", result2.getOrNull())
        assertEquals("content3", result3.getOrNull())
    }

    @Test
    fun `getFileContent should fail when no result is configured`() = runTest {
        val result = fakeCacheManager.getFileContent()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}