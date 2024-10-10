package com.example.dermaapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.dermaapplication.R
import com.example.dermaapplication.user.User


class RegistrationFragment : Fragment() {

    private lateinit var registrationLoginField: EditText
    private lateinit var registrationEmailField: EditText
    private lateinit var registrationPasswordField: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_registration, container, false)

        registrationLoginField = root.findViewById(R.id.editTextTextLogin)
        registrationEmailField = root.findViewById(R.id.editTextTextEmail)
        registrationPasswordField = root.findViewById(R.id.editTextTextPassword)
        val registrationForwardButton: Button = root.findViewById(R.id.registrationButton)

        registrationForwardButton.setOnClickListener {
            val login = registrationLoginField.text.toString()
            val email = registrationEmailField.text.toString()
            val password = registrationPasswordField.text.toString()

            val user = User()
            activity?.let { it1 -> user.registerUser(login, email, password, it1) }
        }

        return root
    }
}