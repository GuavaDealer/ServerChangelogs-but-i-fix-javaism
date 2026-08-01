package com.codingcat.changelogs.platformapi.command;

import com.codingcat.changelogs.platformapi.audience.IBasePermissionHolderAudienceWrapper;
import com.codingcat.changelogs.platformapi.player.IPlayer;
import org.jetbrains.annotations.Nullable;

public interface ICommandSource extends IBasePermissionHolderAudienceWrapper {
    @Nullable IPlayer getExecutingPlayer();
}
