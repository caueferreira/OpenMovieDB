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
        TestObject("x"),
        TestObject("y"),
        TestObject("z")
    )

    private val x = dummyArray[0]
    private val y = dummyArray[1]
    private val z = dummyArray[2]

    @Mock
    private lateinit var timeUtils: TimeUtils
    private val lifespan = TimeUnit.MINUTES.toMillis(oneMillisecond)

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        cache = Cache(
            Function { it.id },
            lifespan = lifespan,
            timeUtils = timeUtils,
            scheduler = Schedulers.trampoline()
        )
    }

    @Test
    fun singleNotFound() {
        cache.get(x.id).test().assertNoValues()
    }

    @Test
    fun getAllWhenIsEmpty() {
        cache.getAll().test().assertNoValues()
    }

    @Test
    fun clearRemovesAll() {
        CacheBuilder()
            .addAll(dummyArray)
            .clear()

        cache.getAll().test().assertNoValues()
    }

    @Test
    fun putSingleObject() {
        CacheBuilder()
            .add(x)

        cache.get(x.id).test().assertValue(x)
    }

    @Test
    fun putManyObject() {
        CacheBuilder()
            .add(x)
            .add(y)
            .add(z)

        cache.getAll().test().assertValue(arrayListOf(x, y, z))
        cache.getAll().test().assertValue { it.size == 3 }
    }

    @Test
    fun putAllObjects() {
        CacheBuilder()
            .addAll(dummyArray)

        cache.getAll().test().assertValue(dummyArray)
        cache.getAll().test().assertValue { it.size == dummyArray.size }
    }

    @Test
    fun findSingleObject() {
        CacheBuilder()
            .addAll(dummyArray)

        cache.get(y.id).test().assertValue(y)
    }

    @Test
    fun searchingWrongObject() {
        CacheBuilder()
            .add(y)

        cache.get(x.id).test().assertNoValues()
    }

    @Test
    fun retrieveWhenSingleValueExpired() {
        CacheBuilder()
            .addAtTime(x, oneMillisecond)
            .nextCallTime(oneMillisecond + lifespan)

        cache.getAll().test().assertNoValues()
        cache.get(x.id).test().assertNoValues()
    }

    @Test
    fun retrieveWhenSomeValuesAreExpired() {
        CacheBuilder()
            .addAtTime(x, oneMillisecond)
            .addAtTime(y, oneMillisecond * 10)
            .addAtTime(z, oneMillisecond * 20)
            .nextCallTime(oneMillisecond * 10 + lifespan)

        cache.getAll().test().assertValue(arrayListOf(z))
        cache.getAll().test().assertValue { it.size == 1 }
    }

    @Test
    fun retrieveWhenAllValuesAreExpired() {
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