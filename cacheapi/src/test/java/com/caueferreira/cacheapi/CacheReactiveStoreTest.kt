package com.caueferreira.cacheapi

import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.mockito.MockitoAnnotations
import io.reactivex.functions.Function
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.mockito.Mock
import java.util.concurrent.TimeUnit

class CacheReactiveStoreTest {

    private lateinit var cacheReactiveStore: CacheReactiveStore<String, TestObject>

    @Mock
    private lateinit var timeUtils: TimeUtils
    private val lifespanSeconds = 5L

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

        var cache = Cache<String, TestObject>(
            Function { it.id },
            lifespan = TimeUnit.SECONDS.toMillis(lifespanSeconds),
            timeUtils = timeUtils,
            scheduler = Schedulers.trampoline()
        )

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
            .assertValueCount(2)
            .values()
            .containsAll(ab)
    }

    @Test
    fun `add array of values`() {
        CacheReactiveStoreBuilder()
            .withValues(zxy)
            .thenAll()
            .assertValueCount(3)
            .values()
            .containsAll(zxy)
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
            .assertValueCount(2)
            .values()
            .containsAll(ab)
    }

    @Test
    fun `add many values than replace all of then`() {
        CacheReactiveStoreBuilder()
            .withValues(ab)
            .replacingWith(zxy)
            .thenAll()
            .assertValueCount(3)
            .values()
            .containsAll(zxy)
    }

    @Test
    fun `add a value and search all when it is expired`() {
        CacheReactiveStoreBuilder()
            .withValueAt(x, 1, TimeUnit.SECONDS)
            .thenAllAt(lifespanSeconds + 2, TimeUnit.SECONDS)
            .assertNoValues()
        cacheReactiveStore.get(x.id).test().assertNoValues()
    }

    @Test
    fun `add a value and search it when it is expired`() {
        CacheReactiveStoreBuilder()
            .withValueAt(x, 1, TimeUnit.SECONDS)
            .thenSingleAt(x.id, lifespanSeconds + 2, TimeUnit.SECONDS)
            .assertNoValues()
    }

    @Test
    fun `add a value and search it when it is not expired`() {
        CacheReactiveStoreBuilder()
            .withValueAt(x, 1, TimeUnit.SECONDS)
            .thenSingleAt(x.id, 2, TimeUnit.SECONDS)
            .assertValue(x)
    }

    @Test
    fun `add many values and search all when two are expired`() {
        CacheReactiveStoreBuilder()
            .withValueAt(x, 1, TimeUnit.SECONDS)
            .withValueAt(z, 20, TimeUnit.SECONDS)
            .withValueAt(y, 10, TimeUnit.SECONDS)
            .thenAllAt(lifespanSeconds + 11, TimeUnit.SECONDS)
            .assertValueCount(1)
            .assertValue(z)
    }

    @Test
    fun `add array of values and search when all are expired`() {
        CacheReactiveStoreBuilder()
            .withValues(zxy)
            .thenAllAt(lifespanSeconds + 1, TimeUnit.SECONDS)
            .assertNoValues()
    }

    @Test
    fun `add array of values and replace, then search when all are expired`() {
        CacheReactiveStoreBuilder()
            .withValues(zxy)
            .replacingWith(ab)
            .thenAllAt(lifespanSeconds + 1, TimeUnit.SECONDS)
            .assertNoValues()
    }

    private data class TestObject(val id: String)

    private inner class CacheReactiveStoreBuilder {

        private fun advanceTimeBy(value: Long, timeUnit: TimeUnit) {
            whenever(timeUtils.milliseconds()).thenReturn(timeUnit.toMillis(value))
        }

        fun withValue(testObject: TestObject): CacheReactiveStoreBuilder {
            cacheReactiveStore.store(testObject)
            return this
        }

        fun withValues(testObjects: List<TestObject>): CacheReactiveStoreBuilder {
            cacheReactiveStore.storeAll(testObjects)
            return this
        }

        fun replacingWith(testObjects: List<TestObject>): CacheReactiveStoreBuilder {
            cacheReactiveStore.replace(testObjects)
            return this
        }

        fun thenAll(): TestObserver<TestObject> {
            advanceTimeBy(lifespanSeconds, TimeUnit.SECONDS)
            return cacheReactiveStore.all().test()
        }

        fun thenSingle(id: String): TestObserver<TestObject> {
            advanceTimeBy(lifespanSeconds, TimeUnit.SECONDS)
            return cacheReactiveStore.get(id).test()
        }

        fun withValueAt(testObject: TestObject, value: Long, timeUnit: TimeUnit): CacheReactiveStoreBuilder {
            advanceTimeBy(value, timeUnit)
            cacheReactiveStore.store(testObject)
            return this
        }

        fun thenAllAt(value: Long, timeUnit: TimeUnit): TestObserver<TestObject> {
            advanceTimeBy(value, timeUnit)
            return cacheReactiveStore.all().test()
        }

        fun thenSingleAt(id: String, value: Long, timeUnit: TimeUnit): TestObserver<TestObject> {
            advanceTimeBy(value, timeUnit)
            return cacheReactiveStore.get(id).test()
        }
    }
}