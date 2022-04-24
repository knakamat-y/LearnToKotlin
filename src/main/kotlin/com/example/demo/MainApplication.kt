package com.example.demo

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay


suspend fun main() {
	try {
		println(cnt() + "start main method.")
		var retValue = coroutineLearnSample()
		println(cnt() + "retValue $retValue")
	} finally {
	    println(cnt() + "finally call on main method.")
	}
}

// NOTE: 参考 https://kotlinlang.org/docs/composing-suspending-functions.html#structured-concurrency-with-async
suspend fun coroutineLearnSample(): Int = coroutineScope {

	val delayedFunc = async {
		try {
			delay(1000L) // Emulates very long computation
			10
		} finally {
			println(cnt() + "Call 'delayedFunc' method.")
		}
	}

	val normalFunc = async {
		// NOTE: 2回目の two.await では println が実行されないので、スキップ？されている
		println(cnt() + "Call 'normalFunc' method.")
		20
	}

	val normalFuncTwo = async {
		println(cnt() + "Call 'normalFuncTwo' method.")
		50
	}

	// NOTE: 先頭にawaitを書いたからといって、最初に処理が始まることが保証されるわけではない
	println(cnt() + "Completed 'normalFunc' method. " + normalFunc.await())

	println(cnt() + "Before calling coroutine await.")
	// NOTE: delayedFunc.await() したからといって後続の two.await() が常にシーケンシャルに実行されるわけじゃないのが罠
	// 1. delayedFunc.await() する前に normalFuncTwo.await() の実行は開始される
	// 2. delayedFunc の 結果に依存する  dependOnDelayedFunc は delayedFunc.await() が完了してから開始される
	//  -> つまり明示的な依存関係がない限り実行順序は保証されない・・・ってコト?? ( JavaScript の async/await の挙動とはだいぶ違う ）
	return@coroutineScope delayedFunc.await().let {

		println(cnt() + "Completed 'delayedFunc' method.")

		// NOTE: coroutineScope な関数が２回呼ばれても Exception が Throw されないのでJavaのStreamとは挙動が違う
		var secondCallOfTwoRet = normalFunc.await()
		println(cnt() + "Completed '2nd normalFunc' method. " + secondCallOfTwoRet)

		// NOTE: it（別のawaitの結果）を使う場合は実行順が保証されているっぽい
		// ※ awaitが解決される前に dependOnDelayedFunc が実行されることはない
		dependOnDelayedFunc(it)
		println(cnt() + "Completed 'dependOnDelayedFunc' method.")

		var ret = secondCallOfTwoRet + it
		println(cnt() + "Return sum of each coroutine return.")

		normalFuncTwo.await()
		ret
	}
}

suspend fun dependOnDelayedFunc(num: Int) : Int = coroutineScope {
	println(cnt() + "Call 'dependOnDelayedFunc' method.")
	var sum = async {
		40 + num
	}
	return@coroutineScope sum.await()
}


// NOTE: (謎・・・・・) 実行タイミングによって順番が変わるので、コンソール上の実行順とカウントが一致しない場合がある
// デバッガとかで一部の関数にブレークポイントを貼るとわかりやすい
var count = 0;
fun cnt() :String {
	count++
	return "$count :"
}
