package com.begemot.tales.player

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player

class PlaybackForwardingPlayer(
    player: Player,
    private val onNext: () -> Unit,
    private val onPrevious: () -> Unit
) : ForwardingPlayer(player) {

    override fun getAvailableCommands(): Player.Commands {
        return super.getAvailableCommands().buildUpon()
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .build()
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return when (command) {
            Player.COMMAND_SEEK_TO_NEXT,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> true
            else -> super.isCommandAvailable(command)
        }
    }

    override fun seekToNext() {
        onNext()
    }

    override fun seekToNextMediaItem() {
        onNext()
    }

    override fun seekToPrevious() {
        onPrevious()
    }

    override fun seekToPreviousMediaItem() {
        onPrevious()
    }

    override fun hasNextMediaItem(): Boolean = true
    override fun hasPreviousMediaItem(): Boolean = true
}
