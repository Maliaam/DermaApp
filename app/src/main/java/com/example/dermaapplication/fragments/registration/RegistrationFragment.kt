package com.example.dermaapplication.fragments.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R

/**
 * Fragment RegistrationFragment
 * Rejestracja użytkownika
 * Przypomnienie hasła
 */
class RegistrationFragment : Fragment() {

    private lateinit var registrationEmailField: EditText
    private lateinit var registrationPasswordField: EditText
    private lateinit var registrationForwardButton: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        registrationEmailField = view.findViewById(R.id.editTextTextEmail)
        registrationPasswordField = view.findViewById(R.id.editTextTextPassword)
        registrationForwardButton = view.findViewById(R.id.registrationButton)

        registrationForwardButton.setOnClickListener {
            val email = registrationEmailField.text.toString()
            val password = registrationPasswordField.text.toString()
            val bundle = Bundle()
            bundle.putString("email", email)
            bundle.putString("password", password)
            val registrationAdditionalFragment = RegistrationAdditionalFragment()
            registrationAdditionalFragment.arguments = bundle
            (activity as? MainActivity)?.replaceFragment(registrationAdditionalFragment)
        }
        return view
    }
}