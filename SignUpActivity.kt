package com.threedev.appconvertor.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.threedev.appconvertor.MainActivity
import com.threedev.appconvertor.R
import com.threedev.appconvertor.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isEmailFocused = false
    private var isPasswordFocused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.btnSignup.isEnabled = false
        binding.btnSignup.setBackgroundColor(Color.parseColor("#B0B0B0"))

        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            isEmailFocused = hasFocus
        }

        binding.tvLogin.setOnClickListener {
            val intent=Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            isPasswordFocused = hasFocus
        }
        binding.cvSingUpGoogle.setOnClickListener {
            signInWithGoogle()
        }
        // TextWatcher to handle dynamic validations and button state
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                // Handle email validation
                if (isEmailFocused) {
                    if (email.isEmpty()) {
                        binding.etEmail.error = "Required"
                    } else if (!isEmailValid(email)) {
                        binding.etEmail.error = "Invalid email address"
                    } else {
                        binding.etEmail.error = null // Clear error when valid
                    }
                }

                // Handle password validation
                if (isPasswordFocused) {
                    if (password.isEmpty()) {
                        binding.etPassword.error = "Required"
                    } else if (!isPasswordValid(password)) {
                        binding.etPassword.error = "Password must be 6-12 characters"
                    } else {
                        binding.etPassword.error = null
                    }
                }

                // Enable or disable the sign-up button
                binding.btnSignup.isEnabled = isEmailValid(email) && isPasswordValid(password)

                if (binding.btnSignup.isEnabled) {
                    binding.btnSignup.setBackgroundColor(Color.parseColor("#518EF8"))
                } else {
                    binding.btnSignup.setBackgroundColor(Color.parseColor("#B0B0B0"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }


        // Attach the TextWatcher to email and password fields
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)

        // Handle Sign-Up Button click
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Ensure no errors before signing up
            if (binding.etEmail.error != null || binding.etPassword.error != null) {
                Toast.makeText(this, "Please fix errors before signing up", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create new user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, FormActivity::class.java).apply {
                            putExtra("USER_NAME","")
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SignUpActivity.RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SignUpActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(SignUpActivity.TAG, "Google Sign-In failed", e)
                Toast.makeText(this, "Sign-In Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..12
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser

                    val username= user?.displayName?:"N/A"
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FormActivity::class.java).apply {
                        putExtra("USER_NAME",username)
                    }
                    startActivity(intent)
                    finish() // Close LoginActivity
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        const val TAG = "SignUpActivity"
        private const val RC_SIGN_IN = 9001
    }
}
