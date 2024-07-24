package com.example.testcomposeui.utils

import android.util.Log

/**
 * debuggable 이 true 로 설정한 상태에서 로그 출력하지 않거나, false 로 설정한 상태에서 로그를 출력해야 하는 상황을 고려하여
 * 로그 출력 여부를 gradle 에서 설정할 수 있도록 source tree 를 분리
 */
object MyLog {
    private const val TAG = "__SKT_MP__"

    internal enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    /**
     * Log.d
     *
     * @param log : 출력할 String
     */
    fun d(log: String? = null) {
        showLog(LogLevel.DEBUG, log)
    }

    /**
     * Log.d
     *
     * @param tag : 출력 태그
     * @param log : 출력할 String
     */
    fun d(tag: String?, log: String? = null) {
        showLog(LogLevel.DEBUG, log, tag ?: TAG)
    }

    /**
     * Log.d
     *
     * @param t : 출력할 Throwable 객체
     */
    fun d(t: Throwable) {
        showLog( LogLevel.DEBUG, t.toString())
    }

    /**
     * Log.d
     * @param tag : 출력 태그
     * @param t : 출력할 Throwable 객체
     */
    fun d(tag: String?, t: Throwable) {
        showLog( LogLevel.DEBUG, t.toString(), tag ?: TAG)
    }

    /**
     * Log.i
     *
     * @param t : 출력할 Throwable 객체
     */
    fun i(t: Throwable) {
        showLog( LogLevel.INFO, t.toString())
    }

    /**
     * Log.i
     * @param tag : 출력 태그
     * @param t : 출력할 Throwable 객체
     */
    fun i(tag: String?, t: String) {
        showLog( LogLevel.INFO, t.toString(), tag ?: TAG)
    }

    /**
     * Log.e
     *
     * @param log : 출력할 String
     */
    fun e(log: String? = null) {
        showLog(LogLevel.ERROR, log)
    }

    /**
     * Log.e
     *
     * @param log : 출력할 String
     */
    fun e(tag: String?, log: String? = null) {
        showLog(LogLevel.ERROR, log, tag ?: TAG)
    }

    /**
     * Log.e
     *
     * @param t : 출력할 Throwable 객체
     */
    fun e(t: Throwable) {
        showLog(LogLevel.ERROR, t.toString())
    }

    /**
     * Log.e
     *
     * @param t : 출력할 Throwable 객체
     */
    fun e(tag: String?, t: Throwable) {
        showLog(LogLevel.ERROR, t.toString(), tag ?: TAG)
    }

    /**
     * call stack 정보를 구성하여 로그를 출력하는 메소드
     */
    private val prettyInfo: String
         get() {
            var prettyInfoStr = ""

            try {
                val stack = Throwable().fillInStackTrace()
                val trace = stack.stackTrace

                for (index in trace.indices) {
                    // DLog 를 호출한 클래스 & 메소드 명을 찾아서 출력
                    if (!MyLog.javaClass.name.equals(trace[index]?.className, true)) {
                        prettyInfoStr = trace[index].className + "::(" + trace[index].fileName + ":" + trace[index].lineNumber + ")::"
                        break
                    }
                }
            } catch (e: Exception) {
                // nothing to do
            }

            return prettyInfoStr
         }

    /**
     * 로그의 길이가 너무 길면 짤리는 현상이 있어서, 길이가 긴 로그를 나눠서 출력
     *
     * @param msg       원본 메시지
     * @param level     로그 출력 레벨
     */
    private fun showLog(level: LogLevel, msg: String?, tag: String = TAG) {
        try {
            val message = msg ?: "__TRACE__"
            val maxLogSize = 1000
            for (pos in 0..message.length / maxLogSize) {
                val start = pos * maxLogSize
                var end = (pos + 1) * maxLogSize
                end = if (end > message.length) message.length else end
                val printLog = if (pos == 0) {
                    prettyInfo + message.substring(start, end)
                } else {
                    message.substring(start, end)
                }
                when (level) {
                    LogLevel.VERBOSE -> Log.v(tag, printLog)
                    LogLevel.DEBUG -> Log.d(tag, printLog)
                    LogLevel.INFO -> Log.i(tag, printLog)
                    LogLevel.WARN -> Log.w(tag, printLog)
                    LogLevel.ERROR -> Log.e(tag, printLog)
                }
            }
        } catch (e: Exception) {
            // nothing to do
        }
    }
}