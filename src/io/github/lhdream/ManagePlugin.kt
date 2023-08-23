package io.github.lhdream

import arc.Core
import arc.Core.bundle
import arc.Events
import arc.struct.ArrayMap
import arc.struct.Seq
import arc.util.Align
import arc.util.CommandHandler
import arc.util.Log.*
import io.github.lhdream.config.UserConfig
import mindustry.Vars.*
import mindustry.content.Blocks
import mindustry.game.EventType.*
import mindustry.game.Gamemode
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.maps.Map
import mindustry.mod.Plugin
import mindustry.net.Net
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class ManagePlugin: Plugin() {

    private val userConfigs: HashMap<String, UserConfig> = HashMap()

    override fun init() {
        Events.on(BlockBuildEndEvent::class.java){
            Call.sendMessage("${it.unit.player} 在[${it.tile.x},${it.tile.y}]位置建造了杜反应堆")
        }
//        Events.on(BuildSelectEvent::class.java){
//            if(!it.breaking && it.builder?.buildPlan()?.block == Blocks.thoriumReactor && it.builder.isPlayer){
//                Call.sendMessage("${it.builder.player.name} 在[${it.tile.x},${it.tile.y}]位置建造了反应堆")
//            }
//        }
        Events.on(PlayerJoin::class.java){
            it.player
            Call.infoPopup(it.player.con,"",2.0f,Align.center,0,0,0,0)
            Call.menu(it.player.con,0,"","", arrayOf(arrayOf()))
        }


        thread{
            while(true){
                sleep(1000)
                Core.app.post {
                    Groups.player.forEach {
                        val userConfig = userConfigs[it.uuid()]
                        if(userConfig == null || userConfig.broad){
                            val msg = """
                            [magenta]欢迎[goldenrod]${it.name}[magenta]来到服务器[red]
                            [violet]当前地图为: [yellow][${state.map.name()}][orange]  宽 : ${state.map.width},高 : ${state.map.height}
                            [royal]输入 /broad 可以开关该显示
                        """.trimIndent()
                            Call.infoPopup(it.con,msg,2.013f,Align.topLeft,200,0,0,0)
                        }
                    }
                }
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
        handler.register<Player>("broad","开关消息面板"){ args, player ->
            var userConfig = userConfigs[player.uuid()]
            if(userConfig == null){
                userConfig = UserConfig()
                userConfigs[player.uuid()] = userConfig
            }
            userConfig.broad = !userConfig.broad
        }

        // 查看地图列表
        handler.register<Player>("maps","[all/custom/default]","显示可用的地图。 默认情况下仅显示自定义地图。"){args,player ->
            val custom = args.isEmpty() || args[0].equals("custom") || args[0].equals("all")
            val def = args.isNotEmpty() && (args[0].equals("default") || args[0].equals("all"))

            if (!maps.all().isEmpty) {
                val all = Seq<Map>()
                if (custom) all.addAll(maps.customMaps())
                if (def) all.addAll(maps.defaultMaps())
                if (all.isEmpty) {
                    val msg = formatColors("未加载自定义地图. 显示默认地图, 请使用 \"@\" 参数.",false,"all")
                    Call.sendMessage(msg,"系统消息",player)
                } else {
                    info("Maps:")
                    val msg = StringBuilder("[magenta]Maps\n")
                    for (map in all) {
                        val mapName: String = map.name().replace(' ', '_')
                        if (map.custom) {
                            val str = formatColors("[violet] @ (@): &fiCustom / @x@\n",false, mapName, map.file.name(), map.width, map.height)
                            msg.append(str)
                        } else {
                            val str = formatColors("[violet] @: &fiDefault / @x@\n",false, mapName, map.width, map.height)
                            msg.append(str)
                        }
                    }
                    Call.sendMessage(msg.toString(),"",player)
                }
            } else {
                Call.sendMessage("[yellow]没有发现任何地图","",player)
            }
        }
        // 设置下一张地图
        handler.register<Player>("nextMap","<mapName>","设置下一张地图"){ args,player ->
            if(!player.admin){
                Call.sendMessage("非管理员用户，不可切换地图","",player)
                return@register
            }
            val res = maps.byName(args[0])
            if(res != null){
                maps.shuffleMode.next(Gamemode.survival,res)
                Call.sendMessage("设置完成","",player)
            }else{
                Call.sendMessage("[yellow]地图[magenta]${args[0]}[yellow]无法找到","",player)
            }
        }
        // 本局游戏结束
        handler.register<Player>("gg","结束当前游戏"){ args,player->
            if(state.isMenu){
                err("Not playing a map.");
                return@register
            }
            Call.updateGameOver(state.rules.waveTeam)
            Events.fire(GameOverEvent(state.rules.waveTeam))
            Core.app
        }

    }

}