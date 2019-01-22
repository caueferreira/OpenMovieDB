//package com.caueferreira.cacheapi
//
//import org.junit.Before
//import org.junit.ClassRule
//import org.junit.Test
//import polanski.option.Option.none
//import polanski.option.Option.ofObj
//
//class LocalDataSourceTest {
//
//    private lateinit var cache: Cache<String>
//    private lateinit var dataSource: DataSource<String>
//
//    private var dummyArray = arrayListOf("z", "x", "y")
//
//    companion object {
//        @ClassRule
//        @JvmField
//        var schedulers = RxImmediateSchedulerRule()
//    }
//
//    @Before
//    fun setUp() {
//        cache = Cache(null)
//        dataSource = DataSource(cache)
//    }
//
//
//    @Test
//    fun noneIsEmittedWhenCacheIsEmpty() {
//        dataSource.all().test().assertValue(none())
//    }
//
//    @Test
//    fun shouldEmitSameAmountOfStoredAdded() {
//        dataSource.storeAll(dummyArray)
//
//        dataSource.all().test()
//            .assertNoErrors()
//            .assertValue { it == ofObj(dummyArray) }
//    }
//
//    @Test
//    fun shouldEmitSameAmountOfReplacedAdded() {
//        dataSource.storeAll(dummyArray)
//        dataSource.storeAll(dummyArray)
//        dataSource.replaceAll(dummyArray)
//
//        dataSource.all().test()
//            .assertNoErrors()
//            .assertValue { it == ofObj(dummyArray) }
//    }
//}