package com.tapbi.spark.ui_common_sdk.components.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

@Composable
fun PatternLock(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    clearTrigger: Any? = null,
    lineColor: Color = MaterialTheme.colorScheme.primary, // Màu của đường kẻ
    errorColor: Color = MaterialTheme.colorScheme.error,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    dotImage: ImageBitmap? = null, // Ảnh dùng cho các chấm (chưa chọn)
    dotImageSelected: ImageBitmap? = null, // Ảnh dùng cho các chấm (đã chọn)
    onPatternComplete: (List<Int>) -> Unit
) {
    var selectedNodes by remember { mutableStateOf(emptyList<Int>()) }
    var currentDragPos by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(clearTrigger) {
        selectedNodes = emptyList()
        currentDragPos = null
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(clearTrigger) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Xóa các trạng thái cũ khi bắt đầu vẽ mới
                        selectedNodes = emptyList()
                        currentDragPos = offset
                    },
                    onDragEnd = {
                        currentDragPos = null
                        val completedNodes = selectedNodes
                        onPatternComplete(completedNodes)
                    },
                    onDragCancel = {
                        currentDragPos = null
                        selectedNodes = emptyList()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentDragPos = change.position
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val cellWidth = canvasWidth / 3
        val cellHeight = canvasHeight / 3
        val dotRadius = cellWidth / 8

        // Tính tọa độ trung tâm của 9 điểm
        val nodeCenters = (0..8).map { index ->
            val row = index / 3
            val col = index % 3
            Offset(
                x = col * cellWidth + cellWidth / 2,
                y = row * cellHeight + cellHeight / 2
            )
        }

        // Logic bắt điểm khi lướt qua
        currentDragPos?.let { pos ->
            // Khi vừa chạm vào (chưa drag qua điểm khác), cần kiểm tra xem có trúng điểm nào không
            if (selectedNodes.isEmpty()) {
                for (i in 0..8) {
                    val distance = (pos - nodeCenters[i]).getDistance()
                    if (distance < dotRadius * 3) {
                        selectedNodes = listOf(i)
                        break
                    }
                }
            }

            for (i in 0..8) {
                if (!selectedNodes.contains(i)) {
                    val center = nodeCenters[i]
                    val distance = (pos - center).getDistance()
                    if (distance < dotRadius * 2) {
                        selectedNodes = selectedNodes + i
                    }
                }
            }
        }

        val currentLineColor = if (isError) errorColor else lineColor

        // Vẽ các đường thẳng nối giữa các điểm đã chọn
        if (selectedNodes.isNotEmpty()) {
            val path = Path()
            path.moveTo(nodeCenters[selectedNodes[0]].x, nodeCenters[selectedNodes[0]].y)
            for (i in 1 until selectedNodes.size) {
                val center = nodeCenters[selectedNodes[i]]
                path.lineTo(center.x, center.y)
            }
            // Vẽ đường nối tới vị trí ngón tay hiện tại
            currentDragPos?.let {
                path.lineTo(it.x, it.y)
            }
            drawPath(
                path = path,
                color = currentLineColor,
                style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                alpha = 0.6f
            )
        }

        // Vẽ 9 điểm chấm tròn (hoặc Image)
        for (i in 0..8) {
            val center = nodeCenters[i]
            val isSelected = selectedNodes.contains(i)

            val currentImage = if (isSelected) dotImageSelected else dotImage

            if (currentImage != null) {
                // Nếu có truyền ảnh vào, ta sẽ vẽ ảnh
                // Tính toán kích thước để ảnh vừa vặn với kích thước ô (hoặc theo radius)
                val imageWidth = (dotRadius * if (isSelected) 3f else 2f).toInt()
                val dstSize = IntSize(imageWidth, imageWidth)
                val dstOffset = IntOffset(
                    x = (center.x - imageWidth / 2f).toInt(),
                    y = (center.y - imageWidth / 2f).toInt()
                )

                // Nếu đang lỗi mà điểm đó được chọn, vẽ một lớp tint đỏ đè lên ảnh
                val colorFilter = if (isSelected && isError) ColorFilter.tint(
                    errorColor,
                    BlendMode.Modulate
                ) else null

                drawImage(
                    image = currentImage,
                    dstOffset = dstOffset,
                    dstSize = dstSize,
                    colorFilter = colorFilter
                )
            } else {
                // Rơi về trạng thái vẽ chấm tròn mặc định nếu không có ảnh
                drawCircle(
                    color = if (isSelected) currentLineColor else inactiveColor,
                    radius = if (isSelected) dotRadius else dotRadius * 0.4f,
                    center = center
                )
                // Vẽ thêm 1 vòng sáng nhạt bên ngoài nếu điểm đó được chọn
                if (isSelected) {
                    drawCircle(
                        color = currentLineColor.copy(alpha = 0.2f),
                        radius = dotRadius * 1.5f,
                        center = center
                    )
                }
            }
        }
    }
}
