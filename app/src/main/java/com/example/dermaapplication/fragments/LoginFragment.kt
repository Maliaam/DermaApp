package com.example.dermaapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities


class LoginFragment : Fragment() {
    private lateinit var loginButton: AppCompatButton
    private lateinit var loginEmailField: EditText
    private lateinit var loginPasswordField: EditText
    private lateinit var loginResetPassword: TextView
    private lateinit var loginRegister: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        loginEmailField = view.findViewById(R.id.loginEmailField)
        loginPasswordField = view.findViewById(R.id.loginPasswordField)
        loginButton = view.findViewById(R.id.loginButton)
        loginResetPassword = view.findViewById(R.id.login_passwordReset)
        loginRegister = view.findViewById(R.id.login_registerAccount)

        loginButton.setOnClickListener {
            val email = loginEmailField.text.toString()
            val password = loginPasswordField.text.toString()
            Utilities.user.loginUser(email, password, requireActivity())
//            Toast.makeText(requireContext(),Utilities.getCurrentUserUid(),Toast.LENGTH_SHORT).show()
        }

        loginRegister.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(RegistrationFragment())
        }


        loginResetPassword.setOnClickListener {
            // TODO RESET PASSWORD
        }


        return view
    }

}