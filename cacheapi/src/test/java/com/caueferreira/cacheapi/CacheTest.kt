package com.caueferreira.cacheapi

import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.functions.Function
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class CacheTest {

    private lateinit var cache: Cache<String, TestObject>
    private val lifespanSeconds = 5L

    private val zxy = arrayListOf(
        TestObject("z"),
        TestObject("x"),
        TestObject("y")
    )

    private val z = zxy[0]
    private val x = zxy[1]
    private val y = zxy[2]

    @Mock
    private lateinit var timeUtils: TimeUtils

    @Before
    fun `before each test`() {
        MockitoAnnotations.initMocks(this)
        cache = Cache(
            Function { it.id },
            lifespan = TimeUnit.SECONDS.toMillis(lifespanSeconds),
            timeUtils = timeUtils,
            scheduler = Schedulers.trampoline()
        )
    }

    @Test
    fun `search single value when none is present`() {
        CacheBuilder()
            .thenSingle(x.id)
            .assertNoValues()
    }

    @Test
    fun `search all values when none is present`() {
        CacheBuilder()
            .thenAll()
            .assertNoValues()
    }

    @Test
    fun `add values then remove all values`() {
        CacheBuilder()
            .withValues(zxy)
            .withoutValues()
            .thenAll()
            .assertNoValues()
    }

    @Test
    fun `add single value`() {
        CacheBuilder()
            .withValue(x)
            .thenSingle(x.id)
            .assertValueCount(1)
            .assertValue(x)
    }

    @Test
    fun `add many values`() {
        CacheBuilder()
            .withValue(y)
            .withValue(x)
            .withValue(z)
            .thenAll()
            .assertValueCount(1)
            .values()
            .contains(zxy)
    }

    @Test
    fun `add array of values`() {
        CacheBuilder()
            .withValues(zxy)
            .thenAll()
            .assertValueCount(1)
            .values()
            .contains(zxy)
    }

    @Test
    fun `add array of values and search single`() {
        CacheBuilder()
            .withValues(zxy)
            .thenSingleAt(y.id, lifespanSeconds, TimeUnit.SECONDS)
            .assertValueCount(1)
            .assertValue(y)
    }

    @Test
    fun `add a value and search for another that is not present`() {
        CacheBuilder()
            .withValue(y)
            .thenSingle(x.id)
            .assertNoValues()
    }

    @Test
    fun `add a value and search when it is expired`() {
        CacheBuilder()
            .withValueAt(x, 1, TimeUnit.SECONDS)
            .thenAllAt(lifespanSeconds + 2, TimeUnit.SECONDS)
            .assertNoValues()
    }

    @Test
    fun `add many values and search all when two are expired`() {
        CacheBuilder()
            .withValueAt(x, 1, TimeUnit.SECONDS)
            .withValueAt(z, 20, TimeUnit.SECONDS)
            .withValueAt(y, 10, TimeUnit.SECONDS)
            .thenAllAt(lifespanSeconds + 11, TimeUnit.SECONDS)
            .assertValueCount(1)
            .assertValue(arrayListOf(z))
    }

    @Test
    fun `add array of values and search when all are expired`() {
        CacheBuilder()
            .withValues(zxy)
            .thenAllAt(lifespanSeconds + 1, TimeUnit.SECONDS)
            .assertNoValues()
    }

    private data class TestObject(val id: String)

    private inner class CacheBuilder {

        private fun advanceTimeBy(value: Long, timeUnit: TimeUnit) {
            whenever(timeUtils.milliseconds()).thenReturn(timeUnit.toMillis(value))
        }

        fun withoutValues(): CacheBuilder {
            cache.clear()
            return this
        }

        fun withValues(testObjects: List<TestObject>): CacheBuilder {
            cache.putAll(testObjects)
            return this
        }

        fun withValue(testObject: TestObject): CacheBuilder {
            cache.put(testObject)
            return this
        }

        fun thenAll(): TestObserver<List<TestObject>> {
            advanceTimeBy(lifespanSeconds, TimeUnit.SECONDS)
            return cache.getAll().test()
        }

        fun thenSingle(id: String): TestObserver<TestObject> {
            advanceTimeBy(lifespanSeconds, TimeUnit.SECONDS)
            return cache.get(id).test()
        }

        fun withValueAt(testObject: TestObject, value: Long, timeUnit: TimeUnit): CacheBuilder {
            advanceTimeBy(value, timeUnit)
            cache.put(testObject)
            return this
        }

        fun thenAllAt(value: Long, timeUnit: TimeUnit): TestObserver<List<TestObject>> {
            advanceTimeBy(value, timeUnit)
            return cache.getAll().test()
        }

        fun thenSingleAt(id: String, value: Long, timeUnit: TimeUnit): TestObserver<TestObject> {
            advanceTimeBy(value, timeUnit)
            return cache.get(id).test()
        }
    }
}