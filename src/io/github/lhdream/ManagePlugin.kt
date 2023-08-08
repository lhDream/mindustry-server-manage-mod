package io.github.lhdream

import arc.Core
import arc.Events
import arc.util.CommandHandler
import mindustry.content.Blocks
import mindustry.game.EventType.BuildSelectEvent
import mindustry.gen.Call
import mindustry.mod.Plugin

class ManagePlugin: Plugin() {

    override fun init() {

        Events.on(BuildSelectEvent::class.java){
            if(!it.breaking && it.builder?.buildPlan()?.block == Blocks.thoriumReactor && it.builder.isPlayer){
                Call.sendMessage("scarlet]ALERT![] ${it.builder.player.name} 在[${it.tile.x},${it.tile.y}]位置建造了反应堆")
            }
        }

    }

    /**
     * 服务器命令
     */
    override fun registerServerCommands(handler: CommandHandler) {

    }

    /**
     * 客户端命令
     */
    override fun registerClientCommands(handler: CommandHandler) {

    }



}