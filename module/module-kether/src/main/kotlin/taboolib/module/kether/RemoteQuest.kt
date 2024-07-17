package taboolib.module.kether

import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.common.OpenContainer
import taboolib.common.platform.function.pluginId
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.Quest
import java.util.*

/**
 * TabooLib
 * taboolib.module.kether.RemoteQuest
 *
 * @author sky
 * @since 2021/8/11 11:33 上午
 */
class RemoteQuest(val remote: OpenContainer, val source: Any) : Quest {

    override fun getId(): String {
        return source.invokeMethod("getId", remap = false)!!
    }

    override fun getBlock(label: String): Optional<Quest.Block> {
        val getBlock = source.invokeMethod<Optional<Any>>("getBlock", label, remap = false)!!
        return if (getBlock.isPresent) {
            Optional.of(RemoteBlock(remote, getBlock.get()))
        } else {
            Optional.empty()
        }
    }

    override fun getBlocks(): Map<String, Quest.Block> {
        return source.invokeMethod<Map<String, Any>>("getBlocks", remap = false)!!.map { it.key to RemoteBlock(remote, it.value) }.toMap()
    }

    override fun blockOf(action: ParsedAction<*>): Optional<Quest.Block> {
        // 远程创建 ParsedAction
        val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, action.action, action.properties))
        // 远程执行方法
        val blockOf = source.invokeMethod<Optional<Any>>("blockOf", remoteAction.value, remap = false)!!
        return if (blockOf.isPresent) {
            Optional.of(RemoteBlock(remote, blockOf.get()))
        } else {
            Optional.empty()
        }
    }

    class RemoteBlock(val remote: OpenContainer, val source: Any) : Quest.Block {

        override fun getLabel(): String {
            return source.invokeMethod("getLabel", remap = false)!!
        }

        override fun getActions(): MutableList<ParsedAction<*>> {
            return source.invokeMethod<List<Any>>("getActions", remap = false)!!
                .map { ParsedAction(RemoteQuestAction<Any>(remote, it.getProperty<Any>("action", remap = false)!!), it.getProperty<Map<String, Any>>("properties", remap = false)!!) }
                .toMutableList()
        }

        override fun indexOf(action: ParsedAction<*>): Int {
            // 远程创建 ParsedAction
            val remoteAction = remote.call(StandardChannel.REMOTE_CREATE_PARSED_ACTION, arrayOf(pluginId, action.action, action.properties))
            // 远程执行方法
            return source.invokeMethod("indexOf", remoteAction.value, remap = false)!!
        }

        override fun get(i: Int): Optional<ParsedAction<*>> {
            val action = source.invokeMethod<Optional<Any>>("get", i, remap = false)!!
            return if (action.isPresent) {
                val remoteAction = RemoteQuestAction<Any>(remote, action.get().getProperty<Any>("action", remap = false)!!)
                Optional.of(ParsedAction(remoteAction, action.get().getProperty<Map<String, Any>>("properties", remap = false)!!))
            } else {
                Optional.empty()
            }
        }
    }
}