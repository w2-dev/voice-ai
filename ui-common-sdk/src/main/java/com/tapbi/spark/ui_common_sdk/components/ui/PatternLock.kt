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
    rows: Int = 3,
    cols: Int = 3,
    isError: Boolean = false,
    clearTrigger: Any? = null,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    errorColor: Color = MaterialTheme.colorScheme.error,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    dotImage: ImageBitmap? = null,
    dotImageSelected: ImageBitmap? = null,
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
        val cellWidth = canvasWidth / cols
        val cellHeight = canvasHeight / rows
        val dotRadius = minOf(cellWidth, cellHeight) / 8

        val totalNodes = rows * cols

        // Tính tọa độ trung tâm của các điểm trong lưới rows x cols
        val nodeCenters = (0 until totalNodes).map { index ->
            val row = index / cols
            val col = index % cols
            Offset(
                x = col * cellWidth + cellWidth / 2,
                y = row * cellHeight + cellHeight / 2
            )
        }

        // Logic bắt điểm khi lướt qua
        currentDragPos?.let { pos ->
            if (selectedNodes.isEmpty()) {
                for (i in 0 until totalNodes) {
                    val distance = (pos - nodeCenters[i]).getDistance()
                    if (distance < dotRadius * 3) {
                        selectedNodes = listOf(i)
                        break
                    }
                }
            }

            for (i in 0 until totalNodes) {
                if (!selectedNodes.contains(i)) {
                    val center = nodeCenters[i]
                    val distance = (pos - center).getDistance()
                    if (distance < dotRadius * 2) {
                        selectedNodes = addNodeWithIntermediate(selectedNodes, i, cols)
                    }
                }
            }
        }

        val isCurrentlyDragging = currentDragPos != null
        val currentLineColor = if (isError && !isCurrentlyDragging) errorColor else lineColor

        // Vẽ các đường thẳng nối giữa các điểm đã chọn
        if (selectedNodes.isNotEmpty()) {
            val path = Path()
            path.moveTo(nodeCenters[selectedNodes[0]].x, nodeCenters[selectedNodes[0]].y)
            for (i in 1 until selectedNodes.size) {
                val center = nodeCenters[selectedNodes[i]]
                path.lineTo(center.x, center.y)
            }
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

        // Vẽ các điểm chấm tròn
        for (i in 0 until totalNodes) {
            val center = nodeCenters[i]
            val isSelected = selectedNodes.contains(i)

            val currentImage = if (isSelected) dotImageSelected else dotImage

            if (currentImage != null) {
                val imageWidth = (dotRadius * if (isSelected) 3f else 2f).toInt()
                val dstSize = IntSize(imageWidth, imageWidth)
                val dstOffset = IntOffset(
                    x = (center.x - imageWidth / 2f).toInt(),
                    y = (center.y - imageWidth / 2f).toInt()
                )

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
                drawCircle(
                    color = if (isSelected) currentLineColor else inactiveColor,
                    radius = if (isSelected) dotRadius else dotRadius * 0.4f,
                    center = center
                )
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

private fun addNodeWithIntermediate(currentList: List<Int>, newNode: Int, cols: Int): List<Int> {
    if (currentList.contains(newNode)) return currentList
    if (currentList.isEmpty()) return listOf(newNode)

    val lastNode = currentList.last()
    val intermediate = getIntermediateNode(lastNode, newNode, cols)

    return if (intermediate != null && !currentList.contains(intermediate)) {
        currentList + intermediate + newNode
    } else {
        currentList + newNode
    }
}

private fun getIntermediateNode(indexA: Int, indexB: Int, cols: Int): Int? {
    val rowA = indexA / cols
    val colA = indexA % cols
    val rowB = indexB / cols
    val colB = indexB % cols

    val dRow = rowB - rowA
    val dCol = colB - colA

    if (dRow % 2 == 0 && dCol % 2 == 0) {
        val midRow = rowA + dRow / 2
        val midCol = colA + dCol / 2
        return midRow * cols + midCol
    }
    return null
}
