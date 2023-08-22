package com.xilidou.service

import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.channels.Selector
import java.nio.channels.spi.SelectorProvider
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy


abstract class SingleThreadEventExecutor() :Executor{

    private val DEFAULT_MAX_PENDING_TASKS = Int.MAX_VALUE

    @Volatile
    private var start = false

    private var taskQueue: Queue<Runnable>

    private var rejectedExecutionHandler: RejectedExecutionHandler


    private var provider: SelectorProvider

    private var selector: Selector

    private var thread: Thread? = null


    init {
        provider = SelectorProvider.provider()
        taskQueue = newTaskQueue(DEFAULT_MAX_PENDING_TASKS)
        rejectedExecutionHandler = AbortPolicy()
        selector = openSelector()
    }

    private fun newTaskQueue(maxPendingTasks: Int): Queue<Runnable> {
        return LinkedBlockingQueue(maxPendingTasks)
    }

    private fun openSelector(): Selector {
        return try {
            selector = provider.openSelector()
            selector
        } catch (e: IOException) {
            throw RuntimeException("failed to open a new selector", e)
        }
    }

    override fun execute(task: Runnable) {
        addTask(task)
        startThread()
    }

    fun addTask(task:Runnable){

        if(!offerTask(task)){
            reject(task)
        }
    }

    private fun startThread() {
        if (start) {
            return
        }
        start = true
        Thread { //这里是得到了新创建的线程
            thread = Thread.currentThread()
            //执行run方法，在run方法中，就是对io事件的处理
            this@SingleThreadEventExecutor.run()
        }.start()
        logger.info("新线程创建了！")
    }

    protected final fun reject(task:Runnable){
//        rejectedExecutionHandler.rejectedExecution(task,this)
    }


    fun offerTask(task: Runnable):Boolean{
        return taskQueue.offer(task);
    }

    fun hasTasks():Boolean{
        logger.info("我没有任务了")
        return !taskQueue.isEmpty()
    }

    fun runAllTasks(){
        runAllTaskFrom(taskQueue)
    }

    fun runAllTaskFrom(taskQueue: Queue<Runnable>){
        var task: Runnable? = pollTaskFrom(taskQueue) ?: return

        while (true) {

            //执行任务队列中的任务
            if (task != null) {
                safeExecute(task)
            }else{
                return
            }
            //执行完毕之后，拉取下一个任务，如果为null就直接返回
            task = pollTaskFrom(taskQueue)

        }

    }

    fun safeExecute(task: Runnable) {
        task.run()
    }

    fun pollTaskFrom(taskQueue: Queue<Runnable>):Runnable?{
        return taskQueue.poll()
    }

    fun inEventLoop(thread: Thread): Boolean {
        return thread == this.thread
    }


    abstract fun run()

    companion object{
        val logger = LoggerFactory.getLogger(SingleThreadEventExecutor::class.java)
    }
}