package com.example.dermaapplication.fragments.registration

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities

/**
 * Fragment umożliwiający użytkownikowi zalogowanie się do aplikacji, przejścia do rejestracji,
 * jeżeli nie posiada konta. Również umożliwia zresetowania hasła użytkownika.
 */
class LoginFragment : Fragment() {
    private lateinit var loginButton: AppCompatButton
    private lateinit var loginButtonDoc: AppCompatButton
    private lateinit var loginEmailField: EditText
    private lateinit var loginPasswordField: EditText
    private lateinit var loginResetPassword: TextView
    private lateinit var loginRegister: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        loginEmailField = view.findViewById(R.id.loginEmailField)
        loginPasswordField = view.findViewById(R.id.loginPasswordField)
        loginButton = view.findViewById(R.id.loginButton)
        loginResetPassword = view.findViewById(R.id.login_passwordReset)
        loginRegister = view.findViewById(R.id.login_registerAccount)

        // Obsługa przycisku umożliwiającego zalogowanie użytkownika do aplikacji
        loginButton.setOnClickListener {
            if (loginEmailField.text.isNotEmpty() && loginPasswordField.text.isNotEmpty()) {
                val email = loginEmailField.text.toString()
                val password = loginPasswordField.text.toString()
                Utilities.user.loginUser(email, password, requireActivity()) { success ->
                    if (success) {
                        (activity as MainActivity).changeNavigationHeader()
                        Utilities.initializeUserStatus { isDoctor ->
                            if (isDoctor) {
                                Log.d("LoginStatus", "Zalogowano jako doktor")
                            } else {
                                Log.d("LoginStatus", "Zalogowano jako użytkownik")
                            }
                        }
                    }
                }
            }
        }


//        }
//            } else Toast.makeText(requireContext(),"Dane logowania są puste",Toast.LENGTH_SHORT).show()
//        }

        // Obsługa przycisku umożliwiającego przejście do fragmentu rejestracji RegistrationFragment
        loginRegister.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(RegistrationFragment())
        }

        // Obsługa przycisku umożliwiającego zresetowania hasła
        loginResetPassword.setOnClickListener {
            // TODO RESET PASSWORD
        }
        return view
    }
}