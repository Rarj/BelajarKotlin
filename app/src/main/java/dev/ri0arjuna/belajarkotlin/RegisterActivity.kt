package dev.ri0arjuna.belajarkotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import ru.whalemare.sheetmenu.SheetMenu
import java.util.logging.Logger

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var progressDialog: ProgressDialog? = null
    private var databaseReference: DatabaseReference? = FirebaseDatabase.getInstance().getReference().root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(toolbar)

        user = FirebaseAuth.getInstance().currentUser
        mAuth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener {
            if (user != null) {
                Toast.makeText(this, "User udah pernah login!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Logger.getLogger(RegisterActivity::class.java.name).warning("User belum pernah login")
            }
        }

        progressDialog = ProgressDialog(this)

        regis.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val id = v!!.id

        if (id == R.id.regis) {
            createAccount(emaill.text.toString(), passwordd.text.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater

        inflater.inflate(R.menu.menu_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId){
        R.id.login_activity -> {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.forgot_activity -> {
            Toast.makeText(this, "Forgot Activity clicked!", Toast.LENGTH_LONG).show()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    fun String.isValidEmail(): Boolean = this.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun createAccount(email: String, password: String) {

        if (email.isEmpty()) {
            emaill.setError("Required")
            emaill.requestFocus()
            return
        }
        if (password.length < 6) {
            passwordd.setError("Minimum 6 characters")
            passwordd.requestFocus()
            return
        }
        if (!email.isValidEmail()) {
            emaill.setError("Email not valid")
            emaill.requestFocus()
            return
        }
        if (password.isEmpty()) {
            passwordd.setError("Required")
            passwordd.requestFocus()
            return
        }

        progressDialog!!.show()
        progressDialog!!.setMessage("Loading...")

        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.w("FB", "createUserWithEmail:success", task.exception)

                        val splitEmail = email.substring(0, email.indexOf("@"))
                        var currentUser: String? = null

                        user = FirebaseAuth.getInstance().currentUser
                        if (user != null){
                            currentUser = user!!.uid
                            Toast.makeText(this, "UID : "+currentUser, Toast.LENGTH_LONG).show()
                        }

                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.toString())

                        try {
                            val data = HashMap<String, String>()
                            data.put("username", splitEmail)
                            data.put("email", email)

                            databaseReference!!.setValue(data)
                        } catch (e: Exception){
                            e.printStackTrace()
                        }

                        Toast.makeText(this, "Registered Succes!.", Toast.LENGTH_SHORT).show();
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w("FB", "createUserWithEmail:failure", task.exception)
                        if (task.getException() is FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "User with this email already exist.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressDialog!!.dismiss()
                }
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(this.authStateListener!!)
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null){
            mAuth!!.removeAuthStateListener(authStateListener!!)
        }
    }
}
