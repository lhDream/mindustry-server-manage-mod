package io.github.lhdream

import arc.Events
import arc.util.CommandHandler
import mindustry.Vars.maps
import mindustry.content.Blocks
import mindustry.game.EventType.BuildSelectEvent
import mindustry.game.EventType.CoreChangeEvent
import mindustry.game.Gamemode
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.mod.Plugin


class ManagePlugin: Plugin() {

    override fun init() {
        Events.on(BuildSelectEvent::class.java){
            if(!it.breaking && it.builder?.buildPlan()?.block == Blocks.thoriumReactor && it.builder.isPlayer){
                Call.sendMessage("${it.builder.player.name} 在[${it.tile.x},${it.tile.y}]位置建造了反应堆")
            }
        }

        Events.on(CoreChangeEvent::class.java){

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
        handler.register<Player>("nextMap","<mapName>","设置下一张地图"){ args,player ->
            if(!player.admin){
                Call.sendMessage("非管理员用户，不可切换地图","系统消息",player)
                return@register
            }
            val res = maps.byName(args[0])
            if(res != null){
                maps.shuffleMode.next(Gamemode.survival,res)
                Call.sendMessage("设置完成","系统消息",player)
            }else{
                Call.sendMessage("地图${args[0]}无法找到","系统消息",player)
            }

        }

    }



}