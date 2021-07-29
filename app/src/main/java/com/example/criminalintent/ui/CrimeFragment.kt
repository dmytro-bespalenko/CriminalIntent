package com.example.criminalintent.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.criminalintent.Crime
import com.example.criminalintent.R
import com.example.criminalintent.getScaledBitmap
import kotlinx.android.synthetic.main.fragment_crime.*
import kotlinx.android.synthetic.main.zoom_layout.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_NUMBER = 3
private const val REQUEST_PHOTO = 4
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private val crimeDetailViewModel: CrimeDetailViewModel by viewModel()
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var treeObserver: ViewTreeObserver
    private var viewWidth = 0
    private var viewHeight = 0

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(hour: Int, minute: Int) {
        crime.date.hours = hour
        crime.date.minutes = minute
        crime_time.apply {
            val dateFormat = SimpleDateFormat("HH:mm")
            text = dateFormat.format(Date(crime.date.time))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID

        crimeDetailViewModel.loadCrime(crimeId)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner,
            { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.example.criminalintent.fileprovider",
                        photoFile
                    )
                    updateUI()
                }
            }
        )


    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            crime_photo.setImageBitmap(bitmap)
        } else {
            crime_photo.setImageDrawable(null)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateUI() {
        crime_title.setText(crime.title)
        crime_time.apply {
            val dateFormat = SimpleDateFormat("HH:mm")
            text = dateFormat.format(Date(crime.date.time))
        }
        crime_date.text = crime.date.toString()
        crime_solved.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            crime_suspect.text = crime.suspect
        }

        updatePhotoView()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }

                    it.moveToFirst()

                    val suspect = it.getString(0)

                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    crime_suspect.text = "VVV"

                }
            }

            requestCode == REQUEST_NUMBER && data != null -> {
                val contentResolver: ContentResolver = requireActivity().contentResolver
                val queryFields = arrayOf(Phone.NUMBER)
                if (checkPermission()) {
                    val cursor =
                        contentResolver.query(
                            Phone.CONTENT_URI,
                            queryFields,
                            ContactsContract.Contacts.HAS_PHONE_NUMBER + ">0 AND LENGTH(" + Phone.NUMBER + ")>0",
                            null,
                            "display_name ASC"
                        )
                    if (cursor != null && cursor.count > 0) while (cursor.moveToNext()) {

                        val mobileNumber =
                            cursor.getString(cursor.getColumnIndex(Phone.NUMBER))
                        callPhone(mobileNumber)

                    }
                    cursor?.close()
                }

            }

            requestCode == REQUEST_PHOTO -> {

                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
    }

    private fun getCrimeReport(): String {

        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspect
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_crime, container, false)
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        crime_title.addTextChangedListener(titleWatcher)

        crime_solved.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
        crime_date.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }

        crime_time.setOnClickListener {
            TimePickerFragment.newInstance(crime.date.time).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
            }

        }
        crime_report.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject)
                )
            }.also { intent ->
                val choseIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(choseIntent)
            }
        }

        crime_suspect.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(
                    pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        call_suspect.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, Phone.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_NUMBER)
            }

        }

        crime_camera.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolveActivity: ResolveInfo? =
                packageManager.resolveActivity(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

            if (resolveActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }


        }

        crime_photo.setOnClickListener {

            val newInstance = ImageDialogFragment.newInstance(crime.photoFileName)
            fragmentManager?.let { it1 -> newInstance.show(it1, null) }
        }

//        crime_photo.viewTreeObserver.addOnGlobalLayoutListener {
//
//            val newInstance = ImageDialogFragment.newInstance(crime.photoFileName)
//            fragmentManager?.let { it1 -> newInstance.show(it1, null) }
//        }

    }

    private fun callPhone(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        startActivity(intent)
    }

    private fun checkPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                Toast.makeText(
                    activity,
                    "Contact read permission needed. Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireActivity().packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivityForResult(intent, 789)
                false
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 123)
                false
            }
        } else true
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }

        }


    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(
            photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }


}