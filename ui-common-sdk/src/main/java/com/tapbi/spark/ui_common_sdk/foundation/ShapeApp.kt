/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tapbi.spark.ui_common_sdk.foundation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val round4 = RoundedCornerShape(grid_x0_5)
val round8 = RoundedCornerShape(grid_x1)
val round10 = RoundedCornerShape(grid_x1_25)
val round12 = RoundedCornerShape(grid_x1_5)
val round14 = RoundedCornerShape(grid_x1_75)
val round16 = RoundedCornerShape(grid_x2)
val round20 = RoundedCornerShape(grid_x2_5)
val round25 = RoundedCornerShape(grid_x3_125)
val round30 = RoundedCornerShape(grid_x3_75)

object ShapeApp {
    val small = RoundedCornerShape(grid_x0)
    val medium = RoundedCornerShape(grid_x2_5)
    val large = RoundedCornerShape(percent = 50)

    val materialShapes =
        Shapes(
            small = small,
            medium = medium,
            large = large,
        )
}