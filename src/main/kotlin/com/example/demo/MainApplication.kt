package com.example.demo

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay


suspend fun main(args: Array<String>) {
	System.out.println("test0")
	try {
		System.out.println("test1")
		failedConcurrentSum()
		System.out.println("test2")
	} catch(e: ArithmeticException) {
		println("test7. Computation failed with ArithmeticException")
		System.out.println("test3")
	}
	System.out.println("test4")
}

suspend fun failedConcurrentSum(): Int = coroutineScope {
	val one = async<Int> {
		try {
			delay(Long.MAX_VALUE) // Emulates very long computation
			42
		} finally {
			println("test5. First child was cancelled")
		}
	}
	val two = async<Int> {
		println("test6. Second child throws an exception")
		throw ArithmeticException()
	}
	one.await() + two.await()
}
