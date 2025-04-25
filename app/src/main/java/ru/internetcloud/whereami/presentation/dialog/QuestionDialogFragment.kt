package ru.internetcloud.whereami.presentation.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ru.internetcloud.whereami.R

class QuestionDialogFragment : DialogFragment() {

    private var question: String = ""
    private var requestKey = ""
    private var answerArgName = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let { arg ->
            question = arg.getString(QUESTION_ARG, "")
            requestKey = arg.getString(PARENT_REQUEST_KEY, "")
            answerArgName = arg.getString(PARENT_ANSWER_ARG_NAME, "")
        } ?: run { throw RuntimeException("There are not arguments in QuestionDialogFragment") }

        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage(question)
        alertDialogBuilder.setPositiveButton(R.string.yes_button) { dialog, which ->
            sendResultToFragment(true)
        }

        alertDialogBuilder.setNegativeButton(R.string.no_button) { dialog, which ->
            sendResultToFragment(false)
        }

        return alertDialogBuilder.create()
    }

    private fun sendResultToFragment(result: Boolean) {
        val bundle = Bundle().apply { putBoolean(answerArgName, result) }
        setFragmentResult(requestKey, bundle)
    }

    companion object {

        private const val QUESTION_ARG = "question_arg"
        private const val PARENT_REQUEST_KEY = "parent_request_key"
        private const val PARENT_ANSWER_ARG_NAME = "parent_answer_arg_name"

        fun newInstance(
            question: String,
            parentRequestKey: String,
            parentAnswerArgName: String,
        ): QuestionDialogFragment {
            val args =
                Bundle().apply {
                    putString(QUESTION_ARG, question)
                    putString(PARENT_REQUEST_KEY, parentRequestKey)
                    putString(PARENT_ANSWER_ARG_NAME, parentAnswerArgName)
                }
            return QuestionDialogFragment().apply { arguments = args }
        }
    }
}
