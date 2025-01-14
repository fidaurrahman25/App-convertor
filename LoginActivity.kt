package com.threedev.appconvertor.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.threedev.appconvertor.MainActivity
import com.threedev.appconvertor.R
import com.threedev.appconvertor.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
        private var isEmailFocused = false
        private var isPasswordFocused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {

            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Disable the sign-up button initially
        binding.btnLogin.isEnabled = false
        binding.btnLogin.setBackgroundColor(Color.parseColor("#B0B0B0"))


        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            isEmailFocused = hasFocus
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            isPasswordFocused = hasFocus
        }


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
                        binding.etEmail.error = null
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
                binding.btnLogin.isEnabled = isEmailValid(email) && isPasswordValid(password)

                // Change button background color based on its state
                if (binding.btnLogin.isEnabled) {
                    binding.btnLogin.setBackgroundColor(Color.parseColor("#000000"))
                } else {
                    binding.btnLogin.setBackgroundColor(Color.parseColor("#B0B0B0"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)

        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)


        binding.tvSignIn.setOnClickListener {

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btnGoogleSignin.setOnClickListener {
            signInWithGoogle()
        }
        binding.cvSingUpGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Handle Login Button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Validate email and password
            if (email.isEmpty()) {
                showError("Please enter a valid email address.")
                return@setOnClickListener
            }

            if (!isEmailValid(email)) {
                showError("Please enter a correct email address.")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showError("Please enter a password.")
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                showError("Password must be between 4 to 8 characters.")
                return@setOnClickListener
            }

            // If everything is valid, proceed with Firebase login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login successful, navigate to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Optional, to finish the login activity so the user can't go back to it
                    } else {
                        // If login fails, show an error message
                        showError("Authentication failed. Please check your credentials.")
                    }
                }
        }
        binding.tvForgotPassword.setOnClickListener {
                // Create an AlertDialog
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.reset_title)

                // Add an EditText to the dialog for email input
                val input = EditText(this)
                input.hint = getString(R.string.enter_email)
                input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                builder.setView(input)

                // Add "Send Reset Link" button
                builder.setPositiveButton(R.string.snd_reset_link) { dialog, _ ->
                    val email = input.text.toString().trim()

                    // Validate email
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    } else if (!isEmailValid(email)) {
                        Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                    } else {
                        // Send reset email
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Password reset email sent. Check your inbox.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to send reset email: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }

                // Add "Cancel" button
                builder.setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }

                // Show the dialog
                builder.show()
            }

        }

    // Function to check if the email is in valid format
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to show error messages to the user
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    // Function to check if the password is between 4 and 8 characters
    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..8
    }



    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In failed", e)
                Toast.makeText(this, "Sign-In Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
    }
}
