package com.zero.client.animation.data;

import com.zero.Zero;
import com.zero.client.animation.json.BedrockAnimationFile;
import com.zero.server.file.FileList;
import com.zero.server.file.FileType;

public class AnimationDataUtil {
    public static final BedrockAnimationFile gunDefault = Zero.server.loadAnimation(new FileList("gunDefault","gunDefault", FileType.GUN),"default_gun");

}
