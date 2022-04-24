package com.example.demo

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay


suspend fun main() {
	try {
		println("1: start main method.")
		var retValue = coroutineLearnSample()
		println("7: retValue")
		println(retValue)
	} catch(e: Exception) {
		println("throw exception.")
	} finally {
	    println("8: finally call on main method.")
	}
}

// NOTE: 参考 https://kotlinlang.org/docs/composing-suspending-functions.html#structured-concurrency-with-async
suspend fun coroutineLearnSample(): Int = coroutineScope {
	val one = async {
		try {
			delay(3000L) // Emulates very long computation
			10
		} finally {
			println("5. call 'one', after 'two' method.")
		}
	}
	val two = async {
		println("3: call 'two' method.")
		20
	}
	println("2: before calling coroutine await.")
	// NOTE: one.await() したからといって後続の two.await() がシーケンシャルに実行されるわけじゃないのが罠
	// ( JavaScript の async/await 的な挙動イメージだと勘違いしてしまう ）
	return@coroutineScope one.await().let {
		var twoRet = two.await()
		println("4: completed 'two' method.")
		var ret = twoRet + it
		println("6: return sum of each coroutine return.")
		ret
	}
}
