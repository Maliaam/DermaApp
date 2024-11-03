package com.example.dermaapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities

/**
 * Fragment odpowiedzialny za pobranie dodatkowych danych, które uzupełniają proces rejestracji
 * użytkownika. Pozwala wybraać typ konta ( użytkownik lub lekarz) i zbiera odpowiednie informacje.
 */
class RegistrationAdditionalFragment : Fragment() {
    private lateinit var userRadioButton: RadioButton
    private lateinit var doctorRadioButton: RadioButton
    private lateinit var authenticationCode: EditText
    private lateinit var authenticationCodeContainer: LinearLayout
    private lateinit var authenticationCodeImg: ImageView
    private lateinit var nameText: EditText
    private lateinit var surnameText: EditText
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_registration_additional, container,
            false)

        // Inicjalizacja elementów UI
        userRadioButton = view.findViewById(R.id.chooseAccountUser)
        doctorRadioButton = view.findViewById(R.id.chooseAccountDoctor)
        authenticationCode = view.findViewById(R.id.additional_doctorAuthenticationCode)
        authenticationCodeContainer = view.findViewById(R.id.authenticationCodeContainer)
        authenticationCodeImg = view.findViewById(R.id.additional_doctorAuthImg)
        nameText = view.findViewById(R.id.additional_name)
        surnameText = view.findViewById(R.id.additional_surname)
        registerButton = view.findViewById(R.id.additional_Button)

        // Obsługa przycisku "Lekarz" oraz pola autoryzującego
        doctorRadioButton.setOnClickListener {
            if (doctorRadioButton.isChecked) {
                userRadioButton.isChecked = false
                authenticationCode.visibility = View.VISIBLE
                authenticationCodeImg.visibility = View.VISIBLE
            } else {
                authenticationCode.visibility = View.GONE
            }
        }

        // Obsługa przycisku "Użytkownik"
        userRadioButton.setOnClickListener {
            if (userRadioButton.isChecked) {
                doctorRadioButton.isChecked = false
                authenticationCodeImg.visibility = View.GONE
                authenticationCode.visibility = View.GONE
            }
        }
        // Pobranie danych przekazanych z fragmentu RegistrationFragment
        val email = arguments?.getString("email")!!
        val password = arguments?.getString("password")!!

        // Obsługa przycisku umożliwiającego rejestracje użytkownika
        registerButton.setOnClickListener {
            val name = nameText.text.toString()
            val surname = surnameText.text.toString()

            if (userRadioButton.isChecked) {
                activity?.let {
                    Utilities.user.registerUser(
                        email, password, name, surname, "users",
                        it
                    )
                }
            } else {
                // Weryfikacja kodu autoryzującego dla lekarza
                if (authenticationCode.text.toString() == "123123") {
                    // TODO: inne podejście do walidacji kodu
                    activity?.let {
                        Utilities.user.registerUser(
                            email, password, name, surname,
                            "doctors", it
                        )
                        (activity as? MainActivity)?.replaceFragment(HomeFragment())
                    }
                } else {
                    Toast.makeText(requireContext(), "Zły kod rejestracji", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        return view
    }
}