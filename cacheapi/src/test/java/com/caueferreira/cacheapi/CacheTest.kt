package com.caueferreira.cacheapi

import io.reactivex.functions.Function
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

class CacheTest {

    private lateinit var cache: Cache<String, TestObject>
    private var dummyArray = arrayListOf(
        TestObject("x"),
        TestObject("y"),
        TestObject("z")
    )

    companion object {
        @ClassRule
        @JvmField
        var schedulers = RxImmediateSchedulerRule()

        data class TestObject(var id: String)
    }

    @Before
    fun setup() {
        cache = Cache(Function { it.id })
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
        cache.putAll(dummyArray)
        cache.clear()
        cache.getAll().test().assertNoValues()
    }


    @Test
    fun getAllWhenIsEmpty() {
        cache.getAll().test().assertNoValues()
    }


    @Test
    fun putSingleObject() {
        cache.put(dummyArray[0])
        cache.get(dummyArray[0].id).test().assertValue(dummyArray[0])
    }

    @Test
    fun putManyObject() {
        cache.put(dummyArray[0])
        cache.put(dummyArray[1])
        cache.put(dummyArray[2])

        cache.getAll().test().assertValue(dummyArray)
        cache.getAll().test().assertValue { it.size == dummyArray.size }
    }


    @Test
    fun putAllObjects() {
        cache.putAll(dummyArray)

        cache.getAll().test().assertValue(dummyArray)
        cache.getAll().test().assertValue { it.size == dummyArray.size }
    }

    @Test
    fun findSingleObject() {
        cache.putAll(dummyArray)

        cache.get(dummyArray[1].id).test().assertValue(dummyArray[1])
    }
}