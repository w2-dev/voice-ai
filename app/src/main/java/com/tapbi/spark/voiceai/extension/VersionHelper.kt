package com.tapbi.spark.voiceai.extension

import android.os.Build

val isAtLeastApi23 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
val isAtLeastApi24 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
val isAtLeastApi25 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
val isAtLeastApi26 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val isAtLeastApi27 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
val isAtLeastApi28 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
val isAtLeastApi29 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
val isAtLeastApi30 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
val isAtLeastApi31 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
val isAtLeastApi32 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2
val isAtLeastApi33 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
val isAtLeastApi34 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
val isAtLeastApi35 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
