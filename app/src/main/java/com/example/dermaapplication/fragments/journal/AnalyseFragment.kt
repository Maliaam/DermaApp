package com.example.dermaapplication.fragments.journal

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.example.dermaapplication.R
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.abs
import android.Manifest
import android.net.Uri
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.Utilities
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AnalyseFragment : Fragment() {
    private lateinit var imageCapture: ImageButton
    private lateinit var turnOnFlash: ImageButton
    private lateinit var flipCamera: ImageButton
    private lateinit var goBack: ImageButton
    private lateinit var previewView: PreviewView
    private var cameraFacing = CameraSelector.LENS_FACING_BACK

    private val activityResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                startCamera(cameraFacing)
            }
        }


    private fun initializeViews(view: View) {
        imageCapture = view.findViewById(R.id.captureImage)
        turnOnFlash = view.findViewById(R.id.turnOnFlash)
        flipCamera = view.findViewById(R.id.flipCamera)
        goBack = view.findViewById(R.id.go_back)
        previewView = view.findViewById(R.id.cameraPreview)
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = maxOf(width, height).toDouble() / minOf(width, height)
        return if (abs(previewRatio - 4.0 / 3.0) <= abs(previewRatio - 16.0 / 9.0)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
    }

    private fun setFlashIcon(camera: Camera) {
        if (camera.cameraInfo.hasFlashUnit()) {
            val torchState = camera.cameraInfo.torchState.value
            if (torchState == TorchState.OFF) {
                camera.cameraControl.enableTorch(true)
                turnOnFlash.setImageResource(R.drawable.baseline_flash_off_24)
            } else {
                camera.cameraControl.enableTorch(false)
                turnOnFlash.setImageResource(R.drawable.baseline_flash_on_24)
            }
        } else {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Flash nie jest dostępny", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun takePicture(imageCapture: ImageCapture) {
        val file =
            File(requireContext().getExternalFilesDir(null), "${System.currentTimeMillis()}.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputFileOptions,
            Executors.newCachedThreadPool(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Zdjęcie zapisane", Toast.LENGTH_SHORT)
                            .show()
                    }
                    val storage = FirebaseStorage.getInstance()
                    val storageRef: StorageReference = storage.reference
                    val photoRef: StorageReference = storageRef.child(
                        "users/${Utilities.getCurrentUserUid()}/" +
                                "${Utilities.currentJournalRecord?.recordTitle}/${file.name}"
                    )

                    photoRef.putFile(Uri.fromFile(file)).addOnSuccessListener { task ->
                        photoRef.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            saveImageUrlToFirestore(imageUrl)
                        }
                    }.addOnFailureListener { exception ->
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Nie udało się zapisać BD",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        exception.printStackTrace()
                    }
                    startCamera(cameraFacing)
                }

                override fun onError(exception: ImageCaptureException) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Zdjęcie nie zostało zapisane",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val recordRef = Utilities.currentJournalRecord?.documentId?.let {
            Utilities.firestore.collection("journals").document(it)
        }

        recordRef?.get()?.addOnSuccessListener { documentSnapshot ->
            val existingImageUrls = documentSnapshot.get("imageUrls") as? MutableList<String> ?: mutableListOf()
            existingImageUrls.add(imageUrl)

            recordRef.update("imageUrls", existingImageUrls)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "URL zdjęcia zapisany w Firestore", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    Toast.makeText(requireContext(), "Błąd przy zapisie URL", Toast.LENGTH_SHORT).show()
                }
        }?.addOnFailureListener { exception ->
            exception.printStackTrace()
            Toast.makeText(requireContext(), "Błąd przy pobieraniu danych", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startCamera(cameraFacing: Int) {
        val aspectRatio = aspectRatio(previewView.width, previewView.height)
        val listenableFuture = ProcessCameraProvider.getInstance(requireContext())

        listenableFuture.addListener({
            try {
                val cameraProvider = listenableFuture.get()
                val preview = Preview.Builder()
                    .setTargetAspectRatio(aspectRatio)
                    .build()
                val imageCaptrure = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(requireActivity().windowManager.defaultDisplay.rotation)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraFacing)
                    .build()
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCaptrure
                )
                imageCapture.setOnClickListener {
                    if (checkSelfPermission(
                            requireContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    takePicture(imageCaptrure)
                }
                turnOnFlash.setOnClickListener {
                    setFlashIcon(camera)
                }
                preview.surfaceProvider = previewView.surfaceProvider
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_analyse, container, false)
        (activity as MainActivity).hideBottomNav()
        initializeViews(view)
        goBack.setOnClickListener {
            fragmentManager?.popBackStack()
            (activity as MainActivity).showBottomNav()
        }
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activityResultLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera(cameraFacing)
        }

        flipCamera.setOnClickListener {
            cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera(cameraFacing)
        }
        return view
    }
}
