package com.example.introduce_yourself.Activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.introduce_yourself.Models.UserLinksModel
import com.example.introduce_yourself.R
import com.example.introduce_yourself.database.*
import com.example.introduce_yourself.utils.byteArrayToBitmap
import com.example.introduce_yourself.utils.currentUser
import com.example.introduce_yourself.utils.saveImageByteArray
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.recyclerviewapp.UserLinksAdapter
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.upperCase
import java.io.IOException

class EditProfileActivity : AppCompatActivity(), View.OnClickListener {
    private var userLinksList = ArrayList<UserLinksModel>()
    private var backgroundByteArray: ByteArray = ByteArray(1)
    private var profilePictureByteArray: ByteArray = ByteArray(1)
    private var remove: Boolean = false

    companion object {
        const val GALLERY_P_CODE = 1
        const val GALLERY_BG_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        setSupportActionBar(toolbar_edit_profile)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar_edit_profile.setNavigationOnClickListener {
            finish()
        }
        refreshCurrentUser()
        if (currentUser != null) {
            edit_profile_user_picture.setImageBitmap(byteArrayToBitmap(currentUser!!.profile_picture.bytes))

            edit_profile_user_name_tv.text = currentUser!!.name
            edit_profile_user_name_et.setText(currentUser!!.name)

            edit_profile_user_surname_tv.text = currentUser!!.surname
            edit_profile_user_surname_et.setText(currentUser!!.surname)

            edit_profile_user_description_tv.text = currentUser!!.description
            edit_profile_user_description_et.setText(currentUser!!.description)

            edit_profile_user_email_tv.text = currentUser!!.email
            edit_profile_user_email_et.setText(currentUser!!.email)

            if (currentUser!!.background_picutre != null) {
                edit_profile_bg_picture.setImageBitmap(byteArrayToBitmap(currentUser!!.background_picutre!!.bytes))
            }
        }

        readUserLinks()
        userLinksList.add(
            UserLinksModel("link1", "www.facebook.com")
        )
        userLinksList.add(
            UserLinksModel("link2", "www.youtube.com")
        )

        if (userLinksList.size > 0) {
            linksRecyclerView(userLinksList)
        }

        user_name_edit_btn.setOnClickListener(this)
        user_name_edit_save_btn.setOnClickListener(this)
        user_surname_edit_btn.setOnClickListener(this)
        user_surname_edit_save_btn.setOnClickListener(this)
        user_email_edit_btn.setOnClickListener(this)
        user_email_edit_save_btn.setOnClickListener(this)
        user_description_edit_btn.setOnClickListener(this)
        user_description_edit_save_btn.setOnClickListener(this)
        user_picture_edit_btn.setOnClickListener(this)
        user_bg_picture_edit_btn.setOnClickListener(this)
        user_name_edit_abort_btn.setOnClickListener(this)
        user_surname_edit_abort_btn.setOnClickListener(this)
        user_email_edit_abort_btn.setOnClickListener(this)
        user_description_edit_abort_btn.setOnClickListener(this)
        edit_profile_add_link_btn.setOnClickListener(this)
        edit_profile_remove_link_btn.setOnClickListener(this)
        edit_profile_remove_link_abort_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.user_name_edit_btn -> {
                edit_profile_user_name_tv.visibility = View.GONE
                edit_profile_user_name_et.visibility = View.VISIBLE
                user_name_edit_btn.visibility = View.GONE
                user_name_edit_save_btn.visibility = View.VISIBLE

                user_name_edit_abort_btn.visibility = View.VISIBLE
            }
            R.id.user_name_edit_save_btn -> {
                if (edit_profile_user_name_tv.text.toString() != edit_profile_user_name_et.text.toString()) {
                    val name = edit_profile_user_name_et.text.toString()
                    when {
                        name.isNullOrEmpty() -> {
                            Toast.makeText(
                                this,
                                "Podaj imię!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        name.length > 30 || name.length < 2 -> {
                            Toast.makeText(
                                this,
                                "Imię powinno zawierać od 2 do 30 znaków!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        !validateName(name) -> {
                            Toast.makeText(
                                this,
                                "Imię powinno mieć format [A-Z][a-z]+",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            updateUserName(name)
                            edit_profile_user_name_tv.text = name

                            edit_profile_user_name_et.visibility = View.GONE
                            edit_profile_user_name_tv.visibility = View.VISIBLE
                            user_name_edit_save_btn.visibility = View.GONE
                            user_name_edit_btn.visibility = View.VISIBLE
                            user_name_edit_abort_btn.visibility = View.GONE
                        }
                    }
                }
            }
            R.id.user_surname_edit_btn -> {
                edit_profile_user_surname_tv.visibility = View.GONE
                edit_profile_user_surname_et.visibility = View.VISIBLE
                user_surname_edit_btn.visibility = View.GONE
                user_surname_edit_save_btn.visibility = View.VISIBLE

                user_surname_edit_abort_btn.visibility = View.VISIBLE
            }
            R.id.user_surname_edit_save_btn -> {

                if (edit_profile_user_surname_tv.text.toString() != edit_profile_user_surname_et.text.toString()) {
                    val surname = edit_profile_user_surname_et.text.toString()
                    when {
                        surname.isNullOrEmpty() -> {
                            Toast.makeText(
                                this,
                                "Podaj nazwisko!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        surname.length > 30 || surname.length < 2 -> {
                            Toast.makeText(
                                this,
                                "Nazwisko powinno zawierać od 2 do 30 znaków!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        !validateSurname(surname) -> {
                            Toast.makeText(
                                this,
                                "Nazwisko powinno mieć format [A-Z][a-z]+([-][A-Z][a-z]+)?",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            updateUserSurname(surname)
                            edit_profile_user_surname_tv.text = surname

                            edit_profile_user_surname_et.visibility = View.GONE
                            edit_profile_user_surname_tv.visibility = View.VISIBLE
                            user_surname_edit_save_btn.visibility = View.GONE
                            user_surname_edit_btn.visibility = View.VISIBLE
                            user_surname_edit_abort_btn.visibility = View.GONE
                        }
                    }
                }
            }
            R.id.user_email_edit_btn -> {
                edit_profile_user_email_tv.visibility = View.GONE
                edit_profile_user_email_et.visibility = View.VISIBLE
                user_email_edit_btn.visibility = View.GONE
                user_email_edit_save_btn.visibility = View.VISIBLE

                user_email_edit_abort_btn.visibility = View.VISIBLE
            }
            R.id.user_email_edit_save_btn -> {

                if (edit_profile_user_email_tv.text.toString() != edit_profile_user_email_et.text.toString()) {
                    val email = edit_profile_user_email_et.text.toString()
                    when {
                        email.isNullOrEmpty() -> {
                            Toast.makeText(
                                this,
                                "Podaj e-mail!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        email.length > 50 || email.length < 5 -> {
                            Toast.makeText(
                                this,
                                "E-mail powinien zawierać od 5 do 50 znaków!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        !validateEmail(email) -> {
                            Toast.makeText(
                                this,
                                "Nieprawidłowy format e-mail!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        checkIfUserExists(email) -> {
                            Toast.makeText(
                                this,
                                "Użytkownik o podanym e-mailu istnieje!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            updateUserEmail(email)
                            edit_profile_user_email_tv.text = email

                            edit_profile_user_email_et.visibility = View.GONE
                            edit_profile_user_email_tv.visibility = View.VISIBLE
                            user_email_edit_save_btn.visibility = View.GONE
                            user_email_edit_btn.visibility = View.VISIBLE
                            user_email_edit_abort_btn.visibility = View.GONE
                        }
                    }
                }
            }
            R.id.user_description_edit_btn -> {
                edit_profile_user_description_tv.visibility = View.GONE
                edit_profile_user_description_et.visibility = View.VISIBLE
                user_description_edit_btn.visibility = View.GONE
                user_description_edit_save_btn.visibility = View.VISIBLE

                user_description_edit_abort_btn.visibility = View.VISIBLE
            }
            R.id.user_description_edit_save_btn -> {

                if (edit_profile_user_description_tv.text.toString() != edit_profile_user_description_et.text.toString()) {
                    val description = edit_profile_user_description_et.text.toString()
                    when {
                        description.length > 1000 -> {
                            Toast.makeText(
                                this,
                                "Opis może zawierać do 1000 znaków",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            updateUserDescription(description)
                            edit_profile_user_description_tv.text = description

                            edit_profile_user_description_et.visibility = View.GONE
                            edit_profile_user_description_tv.visibility = View.VISIBLE
                            user_description_edit_save_btn.visibility = View.GONE
                            user_description_edit_btn.visibility = View.VISIBLE
                            user_description_edit_abort_btn.visibility = View.GONE
                        }
                    }
                }
            }
            R.id.user_picture_edit_btn -> {
                chooseFromGallery(GALLERY_P_CODE)
                if (!profilePictureByteArray.contentEquals(ByteArray(1))) {
                    updateUserProfilePicture(profilePictureByteArray)
                }
            }
            R.id.user_bg_picture_edit_btn -> {
                chooseFromGallery(GALLERY_BG_CODE)
                if (!backgroundByteArray.contentEquals(ByteArray(1))) {
                    updateUserBackgroundPicture(backgroundByteArray)
                }
            }
            R.id.user_name_edit_abort_btn -> {
                edit_profile_user_name_tv.visibility = View.VISIBLE
                edit_profile_user_name_et.visibility = View.GONE
                user_name_edit_abort_btn.visibility = View.GONE
                user_name_edit_save_btn.visibility = View.GONE
                user_name_edit_btn.visibility = View.VISIBLE
            }
            R.id.user_surname_edit_abort_btn -> {
                edit_profile_user_surname_tv.visibility = View.VISIBLE
                edit_profile_user_surname_et.visibility = View.GONE
                user_surname_edit_abort_btn.visibility = View.GONE
                user_surname_edit_save_btn.visibility = View.GONE
                user_surname_edit_btn.visibility = View.VISIBLE
            }
            R.id.user_email_edit_abort_btn -> {
                edit_profile_user_email_tv.visibility = View.VISIBLE
                edit_profile_user_email_et.visibility = View.GONE
                user_email_edit_abort_btn.visibility = View.GONE
                user_email_edit_save_btn.visibility = View.GONE
                user_email_edit_btn.visibility = View.VISIBLE
            }
            R.id.user_description_edit_abort_btn -> {
                edit_profile_user_description_tv.visibility = View.VISIBLE
                edit_profile_user_description_et.visibility = View.GONE
                user_description_edit_abort_btn.visibility = View.GONE
                user_description_edit_save_btn.visibility = View.GONE
                user_description_edit_btn.visibility = View.VISIBLE
            }
            R.id.edit_profile_add_link_btn -> {
                when {
                    edit_profile_add_link_title.text.toString().isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "Tytuł linku nie może być pusty!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    edit_profile_add_link_title.text.toString().length > 20 ||
                            edit_profile_add_link_title.text.toString().length < 2 -> {
                        Toast.makeText(
                            this,
                            "Długość tytułu linku powinna mieć od 2 do 20 znaków!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    edit_profile_add_link_url.text.toString().isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "Link nie może być pusty!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    edit_profile_add_link_url.text.toString().length > 100 ||
                            edit_profile_add_link_title.text.toString().length < 5 -> {
                        Toast.makeText(
                            this,
                            "Link może zawierać od 2 do 100 znaków!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } //TODO: Mateusz regex url validation
                    else -> {
                        val ulm = UserLinksModel(
                            edit_profile_add_link_title.text.toString(),
                            edit_profile_add_link_url.text.toString())
                        if(checkIfLabLinkOg(ulm)){
                            Toast.makeText(
                                this,
                                "Wprowadzony link już istnieje!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else {
                            addUserLink(ulm)
                            userLinksList.clear()
                            edit_profile_add_link_title.text.clear()
                            edit_profile_add_link_url.text.clear()

                            readUserLinks()
                            linksRecyclerView(userLinksList)
                        }
                    }
                }
            }
            R.id.edit_profile_remove_link_btn -> {
                remove = true
                edit_profile_remove_link_btn.visibility = View.GONE
                edit_profile_remove_link_abort_btn.visibility = View.VISIBLE
            }
            R.id.edit_profile_remove_link_abort_btn -> {
                remove = false
                edit_profile_remove_link_btn.visibility = View.VISIBLE
                edit_profile_remove_link_abort_btn.visibility = View.GONE
            }
        }
    }

    private fun linksRecyclerView(userLinks: ArrayList<UserLinksModel>) {
        edit_profile_links_recycler_view.layoutManager = LinearLayoutManager(this)
        edit_profile_links_recycler_view.setHasFixedSize(true)
        val userLinks = UserLinksAdapter(this, userLinks)
        edit_profile_links_recycler_view.adapter = userLinks

        userLinks.setOnClickListener(object : UserLinksAdapter.OnClickListener {
            override fun onClick(position: Int, model: UserLinksModel) {
                if(remove){
                    removeLink(model)
                }
                else {
                    val alert = AlertDialog.Builder(this@EditProfileActivity)
                    alert.setTitle("Czy chcesz otworzyć ${model.link}?")
                    val items = arrayOf(
                        "Tak",
                        "Nie"
                    )
                    alert.setItems(items) { _, n ->
                        when (n) {
                            0 -> {
                                var link = model.link
                                if (!link.startsWith("http://") && !link.startsWith("https://"))
                                    link = "http://$link"

                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(link)
                                )
                                startActivity(intent)
                            }
                            1 -> {}
                        }
                    }
                    alert.show()
                }
            }
        })
    }

    //validation
    private fun validateName(s: String): Boolean {
        val regex = ("[A-Z][a-z]+").toRegex()
        return regex.matches(s)
    }

    private fun validateSurname(s: String): Boolean {
        val regex = ("[A-Z][a-z]+([-][A-Z][a-z]+)?").toRegex()
        return regex.matches(s)
    }

    private fun validateEmail(s: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()
    }

    private fun checkIfUserExists(email: String): Boolean {
        return runBlocking {
            val result = newSuspendedTransaction(Dispatchers.IO) {
                User.find { Users.email.upperCase() eq email.uppercase() }.toList()
            }
            return@runBlocking result.isNotEmpty()
        }
    }

    private fun readUserLinks() = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            val stalked_user_links = UserLink.find { UserLinks.user eq currentUser!!.id }
                .orderBy(UserLinks.position to SortOrder.ASC).toList()
            if (stalked_user_links.isNotEmpty())
                for (i in stalked_user_links)
                    userLinksList.add(UserLinksModel(title = i.label.name, link = i.link))
        }
    }

    private fun refreshCurrentUser() = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            currentUser = User.findById(currentUser!!.id)
        }
    }

    private fun updateUserName(s: String) = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            User.findById(currentUser!!.id)!!.name = s
        }
    }

    private fun updateUserSurname(s: String) = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            User.findById(currentUser!!.id)!!.surname = s
        }
    }

    private fun updateUserEmail(s: String) = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            User.findById(currentUser!!.id)!!.email = s
        }
    }

    private fun updateUserDescription(s: String) = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            User.findById(currentUser!!.id)!!.description = s
        }
    }

    private fun updateUserProfilePicture(ba: ByteArray) = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            User.findById(currentUser!!.id)!!.profile_picture = ExposedBlob(ba)
        }
    }

    private fun updateUserBackgroundPicture(ba: ByteArray) = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            User.findById(currentUser!!.id)!!.background_picutre = ExposedBlob(ba)
        }
    }

    private fun checkIfLabLinkOg(x: UserLinksModel): Boolean //TODO: WITOLD split
            = userLinksList.any { i -> i.link.lowercase() == x.link.lowercase()
            && i.title.lowercase() == x.title.lowercase() }


    private fun addUserLink(x: UserLinksModel) = runBlocking {
        newSuspendedTransaction(Dispatchers.IO) {
            val label_check = LinkLabel.find { LinkLabels.name eq x.title }.toList()
            if (label_check.isEmpty()) {
                UserLink.new {
                    link = x.link
                    position = userLinksList.size
                    label = LinkLabel.new { name = x.title }
                    user = currentUser!!
                }
            } else {
                UserLink.new {
                    link = x.link
                    position = userLinksList.size
                    label = label_check[0]
                    user = currentUser!!
                }
            }
            userLinksList.add(UserLinksModel(x.title, x.link))
        }
    }

    //choosing image from gallery
    private fun chooseFromGallery(code: Int) {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(
                    report: MultiplePermissionsReport?
                ) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent =
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                        startActivityForResult(
                            galleryIntent,
                            code
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    permissionDeniedDialog()
                }
            }).onSameThread().check()
    }

    private fun permissionDeniedDialog() {
        AlertDialog.Builder(this).setMessage("Brak uprawnień")
            .setPositiveButton("Przejdz do USTAWIEŃ")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts(
                        "package",
                        packageName,
                        null
                    )
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("ANULUJ")
            { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_P_CODE) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImage =
                            MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                contentURI
                            )
                        profilePictureByteArray = saveImageByteArray(selectedImage)
                        edit_profile_user_picture.setImageBitmap(selectedImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Błąd!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            if (requestCode == GALLERY_BG_CODE) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImage =
                            MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                contentURI
                            )
                        backgroundByteArray = saveImageByteArray(selectedImage)
                        edit_profile_bg_picture.setImageBitmap(selectedImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Błąd!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    private fun removeLink(model: UserLinksModel) { //TODO: WITOLD REMOVE LINK
        Log.e("model: ", model.toString())
    }
}