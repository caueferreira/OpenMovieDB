package com.caueferreira.cacheapi

import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class CacheTest {

    private lateinit var cache: Cache<String, TestObject>
    private val oneMillisecond = 1L

    private val dummyArray = arrayListOf(
        TestObject("z"),
        TestObject("x"),
        TestObject("y")
    )

    private val z = dummyArray[0]
    private val x = dummyArray[1]
    private val y = dummyArray[2]

    @Mock
    private lateinit var timeUtils: TimeUtils
    private val lifespan = TimeUnit.MINUTES.toMillis(oneMillisecond)

    @Before
    fun `before each test`() {
        MockitoAnnotations.initMocks(this)
        cache = Cache(
            Function { it.id },
            lifespan = lifespan,
            timeUtils = timeUtils,
            scheduler = Schedulers.trampoline()
        )
    }

    @Test
    fun `search single value when none is present`() {
        cache.get(x.id).test().assertNoValues()
    }

    @Test
    fun `search all values when none is present`() {
        cache.getAll().test().assertNoValues()
    }

    @Test
    fun `add values then remove all values`() {
        CacheBuilder()
            .addAll(dummyArray)
            .clear()

        cache.getAll().test().assertNoValues()
    }

    @Test
    fun `add single value`() {
        CacheBuilder()
            .add(x)

        cache.get(x.id).test().assertValue(x)
    }

    @Test
    fun `add many values`() {
        CacheBuilder()
            .add(y)
            .add(x)
            .add(z)

        cache.getAll().test().assertValue { it.containsAll(arrayListOf(y, x, z)) }
        cache.getAll().test().assertValue { it.size == 3 }
    }

    @Test
    fun `add array of values`() {
        CacheBuilder()
            .addAll(dummyArray)

        cache.getAll().test().assertValue { it.containsAll(dummyArray) }
        cache.getAll().test().assertValue { it.size == dummyArray.size }
    }

    @Test
    fun `add array of values and search single`() {
        CacheBuilder()
            .addAll(dummyArray)

        cache.get(y.id).test().assertValue(y)
    }

    @Test
    fun `add a value and search for another that is not present`() {
        CacheBuilder()
            .add(y)

        cache.get(x.id).test().assertNoValues()
    }

    @Test
    fun `add a value and search when it is expired`() {
        CacheBuilder()
            .addAtTime(x, oneMillisecond)
            .nextCallTime(oneMillisecond + lifespan)

        cache.getAll().test().assertNoValues()
        cache.get(x.id).test().assertNoValues()
    }

    @Test
    fun `add many values and search all when two are expired`() {
        CacheBuilder()
            .addAtTime(x, oneMillisecond)
            .addAtTime(z, oneMillisecond * 20)
            .addAtTime(y, oneMillisecond * 10)
            .nextCallTime(oneMillisecond * 10 + lifespan)

        cache.getAll().test().assertValue(arrayListOf(z))
        cache.getAll().test().assertValue { it.size == 1 }
    }

    @Test
    fun `add array of values and search when all are expired`() {
        CacheBuilder()
            .addAll(dummyArray)
            .nextCallTime(System.currentTimeMillis() + lifespan)

        cache.getAll().test().assertNoValues()
    }

    private data class TestObject(val id: String)

    private inner class CacheBuilder {

        fun clear(): CacheBuilder {
            cache.clear()
            return this
        }

        fun addAll(testObjects: List<TestObject>): CacheBuilder {
            cache.putAll(testObjects)
            return this
        }

        fun add(testObject: TestObject): CacheBuilder {
            cache.put(testObject)
            return this
        }

        fun addAtTime(testObject: TestObject, time: Long): CacheBuilder {
            nextCallTime(time)
            cache.put(testObject)
            return this
        }

        fun nextCallTime(time: Long): CacheBuilder {
            whenever(timeUtils.milliseconds()).thenReturn(time)
            return this
        }
    }
}