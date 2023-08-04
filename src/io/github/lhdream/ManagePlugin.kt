package io.github.lhdream

import arc.Core
import mindustry.mod.Plugin

class ManagePlugin: Plugin() {

    override fun init() {
        super.init()
        Core.settings;
    }



}