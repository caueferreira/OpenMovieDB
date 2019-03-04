package com.caueferreira.cacheapi

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.mockito.MockitoAnnotations
import io.reactivex.functions.Function
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.mockito.Mock

class CacheReactiveStoreTest {

    private lateinit var cacheReactiveStore: CacheReactiveStore<String, TestObject>

    @Mock
    private lateinit var cache: Cache<String, TestObject>

    private val zxy = arrayListOf(
        TestObject("z"),
        TestObject("x"),
        TestObject("y")
    )

    private val ab = arrayListOf(
        TestObject("a"),
        TestObject("b")
    )

    private val z = zxy[0]
    private val x = zxy[1]
    private val y = zxy[2]

    private val a = ab[0]
    private val b = ab[1]

    @Before
    fun `before each test`() {
        MockitoAnnotations.initMocks(this)

        cacheReactiveStore = CacheReactiveStore(
            Function { it.id },
            cache,
            Schedulers.trampoline()
        )
    }

    @Test
    fun `search single value when none is present`() {
        CacheReactiveStoreBuilder()
            .thenSingle(a.id)
            .assertNoValues()
    }

    @Test
    fun `search all values when none is present`() {
        CacheReactiveStoreBuilder()
            .thenAll()
            .assertNoValues()
    }

    @Test
    fun `add single value`() {
        CacheReactiveStoreBuilder()
            .withValue(x)
            .thenSingle(x.id)
            .assertValue(x)
    }

    @Test
    fun `add many values`() {
        CacheReactiveStoreBuilder()
            .withValue(a)
            .withValue(b)
            .thenAll()
            .assertValueCount(1)
            .values()
            .contains(ab)
    }

    @Test
    fun `add array of values`() {
        CacheReactiveStoreBuilder()
            .withValues(zxy)
            .thenAll()
            .assertValueCount(1)
            .values()
            .contains(zxy)
    }

    @Test
    fun `add array of values and search single`() {
        CacheReactiveStoreBuilder()
            .withValues(zxy)
            .thenSingle(y.id)
            .assertValueCount(1)
            .assertValue(y)
    }

    @Test
    fun `add a value and search for another that is not present`() {
        CacheReactiveStoreBuilder()
            .withValue(y)
            .thenSingle(a.id)
            .assertNoValues()
    }

    @Test
    fun `add a value than replace it`() {
        CacheReactiveStoreBuilder()
            .withValue(y)
            .replacingWith(ab)
            .thenAll()
            .assertValueCount(1)
            .values()
            .contains(ab)
    }

    @Test
    fun `add many values than replace all of then`() {
        CacheReactiveStoreBuilder()
            .withValues(ab)
            .replacingWith(zxy)
            .thenAll()
            .assertValueCount(1)
            .values()
            .contains(zxy)
    }

    @Test
    fun `add a value and search it`() {
        CacheReactiveStoreBuilder()
            .withValue(x)
            .thenSingle(x.id)
            .assertValue(x)
    }

    @Test
    fun `add many values and search all`() {
        CacheReactiveStoreBuilder()
            .withValue(x)
            .withValue(z)
            .withValue(y)
            .thenAll()
            .assertValueCount(1)
            .values()
            .contains(zxy)
    }

    private data class TestObject(val id: String)

    private inner class CacheReactiveStoreBuilder {

        private fun buildWhenever(testObject: TestObject) {
            whenever(cache.get(testObject.id)).thenReturn(Maybe.just(testObject))
            whenever(cache.getAll()).thenReturn(Maybe.just(arrayListOf(testObject)))
        }

        private fun buildWhenever(testObjects: List<TestObject>) {
            testObjects.forEach {
                buildWhenever(it)
            }
            whenever(cache.getAll()).thenReturn(Maybe.just(testObjects))
        }

        fun withValue(testObject: TestObject): CacheReactiveStoreBuilder {
            buildWhenever(testObject)

            cacheReactiveStore.store(testObject)
            verify(cache, atLeastOnce()).put(any())
            return this
        }

        fun withValues(testObjects: List<TestObject>): CacheReactiveStoreBuilder {
            buildWhenever(testObjects)

            cacheReactiveStore.storeAll(testObjects)
            verify(cache, atLeastOnce()).putAll(any())
            return this
        }

        fun replacingWith(testObjects: List<TestObject>): CacheReactiveStoreBuilder {
            buildWhenever(testObjects)

            cacheReactiveStore.replace(testObjects)
            verify(cache, atLeastOnce()).clear()
            verify(cache, atLeastOnce()).putAll(any())
            return this
        }

        fun thenAll(): TestObserver<List<TestObject>> = cacheReactiveStore.all().test()

        fun thenSingle(id: String): TestObserver<TestObject> = cacheReactiveStore.get(id).test()
    }
}