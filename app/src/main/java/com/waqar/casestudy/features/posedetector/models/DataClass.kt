package com.waqar.casestudy.features.posedetector.models

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import com.waqar.casestudy.posedetector.processor.VisionImageProcessor

sealed class DataClass
class GraphOverlayImageSourceData(val width: Int, val height: Int, val isImageFlipped: Boolean) : DataClass()
class PoseDetectorProcessorData(val imageProxy: ImageProxy, val poseDetectorProcessor: VisionImageProcessor) : DataClass()
class CameraProviderData(val cameraProvider: ProcessCameraProvider, val cameraSelector: CameraSelector, val analysisUseCase: ImageAnalysis) : DataClass()
