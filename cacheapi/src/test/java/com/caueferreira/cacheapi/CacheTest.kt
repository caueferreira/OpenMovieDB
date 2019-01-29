package com.caueferreira.cacheapi

import io.reactivex.functions.Function
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.mockito.Mock
import java.util.concurrent.TimeUnit
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class CacheTest {

    private lateinit var cache: Cache<String, TestObject>
    private val oneMillisecond = 1L

    private val dummyArray = arrayListOf(
        TestObject("x"),
        TestObject("y"),
        TestObject("z")
    )

    @Mock
    private lateinit var timeUtils: TimeUtils
    private val lifespan = TimeUnit.MINUTES.toMillis(oneMillisecond)

    companion object {
        @ClassRule
        @JvmField
        val schedulers = RxImmediateSchedulerRule()
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        cache = Cache(Function { it.id }, lifespan = lifespan, timeUtils = timeUtils)
    }

    @Test
    fun singleNotFound() {
        cache.get(dummyArray[0].id).test().assertNoValues()
    }

    @Test
    fun noneFound() {
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
    fun getAllWhenIsEmpty() {
        cache.getAll().test().assertNoValues()
    }

    @Test
    fun putSingleObject() {
        CacheBuilder()
            .add(dummyArray[0])

        cache.get(dummyArray[0].id).test().assertValue(dummyArray[0])
    }

    @Test
    fun putManyObject() {
        CacheBuilder()
            .add(dummyArray[0])
            .add(dummyArray[1])
            .add(dummyArray[2])

        cache.getAll().test().assertValue(dummyArray)
        cache.getAll().test().assertValue { it.size == dummyArray.size }
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

        cache.get(dummyArray[1].id).test().assertValue(dummyArray[1])
    }

    @Test
    fun searchingWrongObject() {
        CacheBuilder()
            .add(dummyArray[1])

        cache.get(dummyArray[0].id).test().assertNoValues()
    }

    @Test
    fun retrieveWhenSingleValueExpired() {
        CacheBuilder()
            .addAtTime(dummyArray[0], oneMillisecond)
            .nextCallTime(oneMillisecond * 10 + lifespan)

        cache.getAll().test().assertNoValues()
        cache.get(dummyArray[0].id).test().assertNoValues()
    }

    @Test
    fun retrieveWhenSomeValuesAreExpired() {
        CacheBuilder()
            .addAtTime(dummyArray[0], oneMillisecond)
            .addAtTime(dummyArray[1], oneMillisecond * 10)
            .addAtTime(dummyArray[2], oneMillisecond * 20)
            .nextCallTime(oneMillisecond * 10 + lifespan)

        cache.getAll().test().assertValue(arrayListOf(dummyArray[2]))
        cache.getAll().test().assertValue { it.size == dummyArray.size - 2 }
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
            `when`(timeUtils.milliseconds()).thenReturn(time)
            return this
        }
    }
}