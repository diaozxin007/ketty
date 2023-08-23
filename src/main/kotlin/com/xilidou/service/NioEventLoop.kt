package com.xilidou.service

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.channels.spi.SelectorProvider

class NioEventLoop :SingleThreadEventLoop(){

    private var selector:Selector


    private var provider:SelectorProvider = SelectorProvider.provider()

    init {
        this.selector = openSelector()
    }

    private fun openSelector():Selector{
        return provider.openSelector()
    }

    fun selector():Selector{
        return selector;
    }

    fun select(){
        val selector = this.selector
        while (true){
            val selectedKeys = selector.select(3000)
            if(selectedKeys != 0 || hasTasks()){
                break;
            }
        }
    }

    fun processSelectedKeys(selectedKeys:MutableSet<SelectionKey>){
        if(selectedKeys.isEmpty()){
            return
        }

        val i = selectedKeys.iterator()

        while (i.hasNext()){

            val k = i.next()
            i.remove()

            processSelectedKey(k)


        }
    }

    fun processSelectedKey(k:SelectionKey) {

        if(k.isReadable){
            val channel = k.channel() as SocketChannel
            val byteBuffer = ByteBuffer.allocate(1024)
            val len = channel.read(byteBuffer)

            if(len == -1){
                logger.info("客户端通道要关闭")
                channel.close()
                return
            }

            val bytes = ByteArray(len)
            byteBuffer.flip()
            byteBuffer.get(bytes)
            logger.info("新线程收到客户端发送的数据：{}",String(bytes))
        }
    }

    override fun run(){
        while (true){
            try {
                select()
                processSelectedKeys(selector.selectedKeys())
            }finally {
                runAllTasks()
            }
        }
    }


}